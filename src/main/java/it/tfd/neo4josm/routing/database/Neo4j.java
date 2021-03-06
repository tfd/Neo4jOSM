package it.tfd.neo4josm.routing.database;

import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Created by ronald on 20/08/15.
 */
public class Neo4j {

    private static final String DB_PATH = "build/neo4j-osm-routing-db";
    protected GraphDatabaseService graphDb;

    public Neo4j() {
        this(new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH));
    }

    public Neo4j(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        registerShutdownHook(this.graphDb);
    }

    /**
     * Checks whether the database is opened correctly.
     *
     * @return true if the database is usable, false otherwise.
     */
    public boolean isReady() {
        return graphDb != null && graphDb.isAvailable(1);
    }

    /**
     * Clear the database. All nodes and relationships are removed.
     */
    public void clear() {
        try (Transaction tx = graphDb.beginTx()) {
            // let's remove the data
            graphDb.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r");

            tx.success();
        }
    }

    /**
     * Close the database.
     */
    public void shutdown() {
        graphDb.shutdown();
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it shuts down nicely when the VM exits (even if you
     * "Ctrl-C" the running application).
     *
     * @param graphDb
     */
    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }

    /**
     * Create a node with the given label.
     *
     * @param label
     * @return
     */
    public Node createNode(Label label) {
        return graphDb.createNode(label);
    }

    /**
     * Start a transaction.
     *
     * @return the transaction.
     */
    public Transaction startTransaction() {
        return graphDb.beginTx();
    }
}
