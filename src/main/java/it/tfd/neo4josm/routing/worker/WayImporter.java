package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4jRouting;
import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ronald on 24/09/15.
 */
public class WayImporter {
    private final Neo4jRouting database;

    public WayImporter(Neo4jRouting database) {
        this.database = database;
    }

    public void load(Way way) {
        if (isHighway(way)) {
            org.neo4j.graphdb.Node node = database.createNode(Neo4jOsmLabels.OSM_WAY, way.getId());
            database.addWay(node);

            // Copy tags
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
                org.neo4j.graphdb.Node currentNode = database.createNode(Neo4jOsmLabels.OSM_WAY_NODE);
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

    private org.neo4j.graphdb.Node getCoordinates(long id) {
        org.neo4j.graphdb.Node node = database.getNode(id);
        if (node != null) {
            node.setProperty("junction", Boolean.TRUE);
        }
        else {
            node = database.createNode(Neo4jOsmLabels.OSM_NODE, id);
            database.addNode(node);
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
