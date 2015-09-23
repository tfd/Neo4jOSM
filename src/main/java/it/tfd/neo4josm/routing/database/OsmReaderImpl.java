package it.tfd.neo4josm.routing.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.*;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a {@see OsmReader} that opens on OSM file and calls methods of {@see OsmReaderCallback} for all tags
 * found in the file.
 */
public class OsmReaderImpl implements OsmReader {

    private static final Logger log = LoggerFactory.getLogger(OsmReaderImpl.class);

    private final String filename;
    private final ExecutorService executor;

    public OsmReaderImpl(String filename, ExecutorService executor) {
        this.filename = filename;
        this.executor = executor;
    }

    public OsmReaderImpl(String filename) {
        this(filename, Executors.newSingleThreadExecutor());
    }

    @Override
    public void parse(final OsmReaderCallback cb) throws FileNotFoundException, InterruptedException, ExecutionException {
        File file = new File(filename); // the input file
        Sink sink = createSinkForOsmReaderCallback(cb);
        Future job = createReaderJob(file, sink);
        waitForJobToFinish(job);
    }

    @Override
    public String getFilename() {
        return filename;
    }

    private Sink createSinkForOsmReaderCallback(final OsmReaderCallback cb) {
        return new Sink() {
            @Override
            public void process(EntityContainer entityContainer) {
                Entity entity = entityContainer.getEntity();
                if (entity instanceof Bound) {
                    //do something with the bound
                    cb.setBound((Bound) entity);
                } else if (entity instanceof Node) {
                    //do something with the node
                    cb.addNode((Node) entity);
                } else if (entity instanceof Way) {
                    //do something with the way
                    cb.addWay((Way) entity);
                } else if (entity instanceof Relation) {
                    //do something with the relation
                    cb.addRelation((Relation) entity);
                }
            }

            @Override
            public void initialize(Map<String, Object> metaData) {
            }

            @Override
            public void release() {
            }

            @Override
            public void complete() {
                cb.complete();
            }
        };
    }

    private Future createReaderJob(File file, Sink sinkImplementation) throws FileNotFoundException {
        final RunnableSource reader = createOsmFileReader(file);
        reader.setSink(sinkImplementation);
        return executor.submit(reader);
    }

    private RunnableSource createOsmFileReader(File file) throws FileNotFoundException {
        return isCompressed(file) ? new XmlReader(file, false, getCompressionMethod(file)) : new crosby.binary.osmosis.OsmosisReader(new FileInputStream(file));
    }

    private void waitForJobToFinish(Future job) throws ExecutionException, InterruptedException {
        job.get();
    }

    private boolean isCompressed(File file) {
        return !file.getName().endsWith(".pbf");
    }

    private CompressionMethod getCompressionMethod(File file) {
        CompressionMethod compression;
        if (file.getName().endsWith(".gz")) {
            compression = CompressionMethod.GZip;
        } else if (file.getName().endsWith(".bz2")) {
            compression = CompressionMethod.BZip2;
        } else {
            compression = CompressionMethod.None;
        }
        return compression;
    }

    @Override
    public String toString() {
        return "OsmReaderImpl{" +
                "filename='" + filename + '\'' +
                '}';
    }
}
