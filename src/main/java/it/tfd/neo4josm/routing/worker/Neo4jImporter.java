package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4jRouting;
import it.tfd.neo4josm.routing.database.OsmReader;
import it.tfd.neo4josm.routing.database.OsmReaderCallback;
import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexHits;
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

    private final Neo4jRouting database;
    private String filename;
    private boolean completed;

    public Neo4jImporter(Neo4jRouting database) {
        this.database = database;
    }

    /* OsmFileImporter */

    @Override
    public boolean importOsmFile(OsmReader reader) {
        filename = reader.getFilename();
        completed = false;

        try (Transaction tx = database.startTransaction()) {
            reader.parse(this);
            if (completed) {
                tx.success();
                return true;
            }
        } catch (FileNotFoundException e) {
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
        org.neo4j.graphdb.Node rootNode = database.createNode(Neo4jOsmLabels.OSM_BOUND);
        rootNode.setProperty("file", filename);
        rootNode.setProperty("date", new Date());
        rootNode.setProperty("minlat", bound.getBottom());
        rootNode.setProperty("maxlat", bound.getTop());
        rootNode.setProperty("minlon", bound.getLeft());
        rootNode.setProperty("maxlon", bound.getRight());
        rootNode.setProperty("origin", bound.getOrigin());

        database.addBound(rootNode);
    }

    @Override
    public void addNode(Node node) {

    }

    @Override
    public void addWay(Way way) {
        new WayImporter(database).load(way);
    }

    @Override
    public void addRelation(Relation relation) {

    }

    @Override
    public void complete() {
        completed = true;
    }
}
