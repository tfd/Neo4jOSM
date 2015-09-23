package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.OsmReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

/**
 * Interface for importing an OSM file.
 */
public interface OsmFileImporter {

    /**
     * Use the reader to read an OSM file and write it's contents to the Neo4j Database.
     * @param reader
     * @return
     */
    boolean importOsmFile(OsmReader reader);

}
