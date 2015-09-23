package it.tfd.neo4josm.routing.model;

import org.neo4j.graphdb.Label;

/**
 * Created by ronald on 22/09/15.
 */
public enum Neo4jOsmLabels implements Label {
    OSM_ROUTING,
    OSM_BOUND,
    OSM_NODE,
    OSM_WAY,
    OSM_WAY_NODE,
    OSM_RELATION
}
