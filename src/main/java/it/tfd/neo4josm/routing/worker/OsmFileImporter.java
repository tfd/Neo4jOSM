package it.tfd.neo4josm.routing.worker;

import java.io.File;

/**
 * Interface for importing an OSM file.
 */
public interface OsmFileImporter {

    /**
     * Read the given OSM file and writes it to the Neo4j Database.
     * @param file
     * @return
     */
    boolean importOsmFile(File file);

}
