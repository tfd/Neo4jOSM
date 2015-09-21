package it.tfd.neo4josm.routing.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ronald on 21/08/15.
 */
public class OsmReader {

    private static final Logger log = LoggerFactory.getLogger(OsmReader.class);

    private final String filename;

    public OsmReader(String filename) {
        this.filename = filename;
    }

    public void parse(final OsmReaderCallback cb) throws FileNotFoundException {
        File file = new File(filename); // the input file

        Sink sinkImplementation = new Sink() {
            @Override
            public void process(EntityContainer entityContainer) {
                Entity entity = entityContainer.getEntity();
                if (entity instanceof Node) {
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
            public void initialize(Map<String, Object> metaData) {}

            @Override
            public void release() {}

            @Override
            public void complete() { cb.complete(); }
        };

        boolean pbf = false;
        CompressionMethod compression = CompressionMethod.None;

        if (file.getName().endsWith(".pbf")) {
            pbf = true;
        } else if (file.getName().endsWith(".gz")) {
            compression = CompressionMethod.GZip;
        } else if (file.getName().endsWith(".bz2")) {
            compression = CompressionMethod.BZip2;
        }

        RunnableSource reader;

        if (pbf) {
            reader = new crosby.binary.osmosis.OsmosisReader(new FileInputStream(file));
        } else {
            reader = new XmlReader(file, false, compression);
        }

        reader.setSink(sinkImplementation);

        Thread readerThread = new Thread(reader);
        readerThread.start();

        while (readerThread.isAlive()) {
            try {
                readerThread.join();
            } catch (InterruptedException e) {
                /* do nothing */
            }
        }
    }

}
