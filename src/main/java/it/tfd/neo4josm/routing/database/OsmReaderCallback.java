package it.tfd.neo4josm.routing.database;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;


/**
 * Created by ronald on 22/08/15.
 */
public interface OsmReaderCallback {
    void addNode(Node node);
    void addWay(Way way);
    void addRelation(Relation relation);
    void complete();
}
