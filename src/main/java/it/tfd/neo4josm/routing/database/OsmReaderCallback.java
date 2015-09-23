package it.tfd.neo4josm.routing.database;

import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * Callback for the {@see OsmReader}.
 */
public interface OsmReaderCallback {

    void setBound(Bound bound);
    void addNode(Node node);
    void addWay(Way way);
    void addRelation(Relation relation);

    /**
     * Called when the OSM file is completely read.
     */
    void complete();
}
