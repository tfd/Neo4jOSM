package it.tfd.neo4josm.routing.database;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

/**
 * Created by ronald on 21/09/15.
 */
public interface OsmReader {

    /**
     * Parse the OSM file.
     *
     * @param cb Callback called with the various entities found in the OSM file.
     * @throws FileNotFoundException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    void parse(OsmReaderCallback cb) throws FileNotFoundException, InterruptedException, ExecutionException;

    /**
     * @return Name of the original OSM file.
     */
    String getFilename();
}
