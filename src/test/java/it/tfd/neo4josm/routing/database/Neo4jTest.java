package it.tfd.neo4josm.routing.database;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.test.TestGraphDatabaseFactory;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.*;

/**
 * Created by ronald on 20/08/15.
 */
public class Neo4jTest {

    private Neo4j graphDb;

    @Before
    public void setUp() throws Exception {
        graphDb = new Neo4j(new TestGraphDatabaseFactory().newImpermanentDatabase());
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test
    public void testStartUp() {
        Assert.assertTrue(graphDb.isReady());
    }
}