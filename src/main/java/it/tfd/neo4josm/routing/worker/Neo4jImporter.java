package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4j;
import it.tfd.neo4josm.routing.database.OsmReader;
import it.tfd.neo4josm.routing.database.OsmReaderCallback;
import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Import data into a Neo4j database.
 * <p/>
 * Implements the IsmFileImporter interface to import OSM files into Neo4j.
 */
public class Neo4jImporter implements OsmFileImporter, OsmReaderCallback {

    private static final Logger log = LoggerFactory.getLogger(Neo4jImporter.class);

    private final Neo4j database;
    private GraphDatabaseService graphDb;
    private String filename;
    private Index<org.neo4j.graphdb.Node> wayIdIndex;
    private Index<org.neo4j.graphdb.Node> nodeIdIndex;
    private org.neo4j.graphdb.Node routingNode;
    private org.neo4j.graphdb.Node rootNode;
    private boolean completed;

    public Neo4jImporter(Neo4j database) {
        this.database = database;
        graphDb = database.getGraphDb();
    }

    /* OsmFileImporter */

    @Override
    public boolean importOsmFile(OsmReader reader) {
        filename = reader.getFilename();
        completed = false;

        IndexManager indexManager = graphDb.index();
        wayIdIndex = indexManager.forNodes("OsmWayIds");
        nodeIdIndex = indexManager.forNodes("OsmNodeIds");

        try (Transaction tx = graphDb.beginTx()) {
            routingNode = graphDb.findNode(Neo4jOsmLabels.OSM_ROUTING, "name", "OsmRouting");
            if (routingNode == null) {
                routingNode = graphDb.createNode(Neo4jOsmLabels.OSM_ROUTING);
                routingNode.setProperty("name", "OsmRouting");
            }

            reader.parse(this);
            if (completed) {
                tx.success();
            }
            return true;
        } catch (FileNotFoundException e) {
            database.getGraphDb().beginTx();
            log.error("Parsing " + reader.toString(), e);
        } catch (InterruptedException e) {
            log.error("Parsing " + reader.toString(), e);
        } catch (ExecutionException e) {
            log.error("Parsing " + reader.toString(), e);
        }

        return false;
    }

    /* OsmReaderCallback */

    @Override
    public void setBound(Bound bound) {
        rootNode = graphDb.createNode(Neo4jOsmLabels.OSM_BOUND);
        rootNode.setProperty("file", filename);
        rootNode.setProperty("date", new Date());
        rootNode.setProperty("minlat", bound.getBottom());
        rootNode.setProperty("maxlat", bound.getTop());
        rootNode.setProperty("minlon", bound.getLeft());
        rootNode.setProperty("maxlon", bound.getRight());
        rootNode.setProperty("origin", bound.getOrigin());

        routingNode.createRelationshipTo(rootNode, Neo4jOsmRelationshipTypes.OSM_BOUND);
    }

    @Override
    public void addNode(Node node) {

    }

    @Override
    public void addWay(Way way) {
        if (isHighway(way)) {
            org.neo4j.graphdb.Node node = graphDb.createNode(Neo4jOsmLabels.OSM_WAY);
            rootNode.createRelationshipTo(node, Neo4jOsmRelationshipTypes.OSM_WAY);

            node.setProperty("id", way.getId());

            Iterator<Tag> tags = way.getTags().iterator();
            while (tags.hasNext()) {
                Tag tag = tags.next();
                node.setProperty(tag.getKey().toLowerCase(), tag.getValue().toLowerCase());
            }

            // Make sure that a "oneway" property is present
            if (!node.hasProperty("oneway")) {
                node.setProperty("oneway", getTagValue(way, "highway").equalsIgnoreCase("motorway") ? "yes" : "no");
            }

            org.neo4j.graphdb.Node prevNode = null;
            for (WayNode wayNode : way.getWayNodes()) {
                org.neo4j.graphdb.Node currentNode = graphDb.createNode(Neo4jOsmLabels.OSM_WAY_NODE);
                currentNode.setProperty("wayId", way.getId());
                currentNode.setProperty("nodeId", wayNode.getNodeId());

                org.neo4j.graphdb.Node coordinates = getCoordinates(wayNode.getNodeId());
                currentNode.createRelationshipTo(coordinates, Neo4jOsmRelationshipTypes.OSM_COORDINATES);

                if (prevNode == null) {
                    node.createRelationshipTo(currentNode, Neo4jOsmRelationshipTypes.OSM_FIRST);
                } else {
                    prevNode.createRelationshipTo(currentNode, Neo4jOsmRelationshipTypes.OSM_NEXT);
                }
                prevNode = currentNode;

                /*
                // Check for crossroad
                if (coordinates.hasProperty("junction") && coordinates.getProperty("junction").equalsIgnoreCase("yes") {

                }
                */
            }
        }
    }

    @Override
    public void addRelation(Relation relation) {

    }

    @Override
    public void complete() {
        completed = true;
    }

    private org.neo4j.graphdb.Node getCoordinates(long id) {
        org.neo4j.graphdb.Node node = null;
        IndexHits<org.neo4j.graphdb.Node> hits = nodeIdIndex.get("node_id", id);
        if (hits.size() > 0) {
            node = hits.getSingle();
            node.setProperty("junction", "yes");
        } else {
            node = graphDb.createNode(Neo4jOsmLabels.OSM_NODE);
            node.setProperty("id", id);
            node.setProperty("junction", "no");
            rootNode.createRelationshipTo(node, Neo4jOsmRelationshipTypes.OSM_NODE);
            nodeIdIndex.add(node, "node_id", id);
        }
        return node;
    }

    private List<String> permittedHighways = Arrays.asList(
            "motorway",
            "motorway_junction",
            "trunk",
            "primary",
            "secondary",
            "tertiary",
            "unclassified",
            "residential",
            "service",
            "road",
            "motorway_link",
            "trunk_link",
            "primary_link",
            "secondary_link",
            "tertiary_link",
            "living_street",
            "pedestrian",
            "track",
            "road",
            "footway",
            "steps",
            "path",
            "cycleway"
    );

    private boolean isHighway(Way way) {
        String highway = getTagValue(way, "highway");
        return highway != null && permittedHighways.contains(highway);
    }

    private String getTagValue(Entity entity, String key) {
        Iterator<Tag> tags = entity.getTags().iterator();
        String value = null;
        while (tags.hasNext()) {
            Tag tag = tags.next();
            if (tag.getKey().equalsIgnoreCase(key)) {
                value = tag.getValue();
            }
        }
        return value;
    }
}
