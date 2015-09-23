package it.tfd.neo4josm.routing.model;

import org.neo4j.graphdb.RelationshipType;

/**
 * Created by ronald on 22/09/15.
 */
public enum Neo4jOsmRelationshipTypes implements RelationshipType {
    OSM_BOUND,
    OSM_WAY,
    OSM_FIRST,
    OSM_NEXT,
    OSM_COORDINATES,
    OSM_NODE
}
