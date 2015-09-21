package it.tfd.neo4josm.routing.database;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

/**
 * Created by ronald on 21/09/15.
 */
public interface OsmReader {
    void parse(OsmReaderCallback cb) throws FileNotFoundException, InterruptedException, ExecutionException;
}
