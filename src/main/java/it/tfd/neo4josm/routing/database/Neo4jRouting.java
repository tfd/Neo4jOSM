package it.tfd.neo4josm.routing.database;

import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

/**
 * Created by ronald on 24/09/15.
 */
public class Neo4jRouting extends Neo4j {

    private Index<Node> wayIdIndex;
    private Index<org.neo4j.graphdb.Node> nodeIdIndex;
    private org.neo4j.graphdb.Node routingNode;
    private org.neo4j.graphdb.Node rootNode;

    public Neo4jRouting() {
        super();
        initializeVariables();
    }

    public Neo4jRouting(GraphDatabaseService graphDb) {
        super(graphDb);
        initializeVariables();
    }

    private void initializeVariables() {
        IndexManager indexManager = graphDb.index();
        wayIdIndex = indexManager.forNodes("OsmWayIds");
        nodeIdIndex = indexManager.forNodes("OsmNodeIds");

        try (Transaction tx = graphDb.beginTx()) {
            routingNode = graphDb.findNode(Neo4jOsmLabels.OSM_ROUTING, "name", "OsmRouting");
            if (routingNode == null) {
                routingNode = graphDb.createNode(Neo4jOsmLabels.OSM_ROUTING);
                routingNode.setProperty("name", "OsmRouting");
            }

            tx.success();
        }
    }

    public Node createNode(Label label, long id) {
        Node node = createNode(label);
        node.setProperty("id", id);
        return node;
    }

    public void addBound(Node node) {
        rootNode = node;
        routingNode.createRelationshipTo(rootNode, Neo4jOsmRelationshipTypes.OSM_BOUND);
    }

    public void addWay(Node node) {
        wayIdIndex.add(node, "way_id", node.getProperty("id"));
        rootNode.createRelationshipTo(node, Neo4jOsmRelationshipTypes.OSM_WAY);
    }

    public void addNode(Node node) {
        nodeIdIndex.add(node, "node_id", node.getProperty("id"));
        rootNode.createRelationshipTo(node, Neo4jOsmRelationshipTypes.OSM_NODE);
    }

    public Node getNode(long id) {
        IndexHits<Node> hits = nodeIdIndex.get("node_id", id);
        if (hits.size() > 0) {
            return hits.getSingle();
        }
        return null;
    }
}
