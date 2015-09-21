package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4j;

import java.io.File;

/**
 * Import data into a Neo4j database.
 *
 * Implements the IsmFileImporter interface to import OSM files into Neo4j.
 */
public class Neo4jImporter implements OsmFileImporter {

    private final Neo4j database;

    public Neo4jImporter(Neo4j database) {
        this.database = database;
    }

    @Override
    public boolean importOsmFile(File file) {
        return false;
    }
}
