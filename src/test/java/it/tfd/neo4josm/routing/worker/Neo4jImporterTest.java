package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4j;
import it.tfd.neo4josm.routing.database.Neo4jRouting;
import it.tfd.neo4josm.routing.database.OsmReader;
import it.tfd.neo4josm.routing.database.OsmReaderCallback;
import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by ronald on 22/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class Neo4jImporterTest {

    @Mock
    private Neo4jRouting neo4j;

    @Mock
    private GraphDatabaseService graphDb;

    @Mock
    private IndexManager indexManager;

    @Mock
    private Index<org.neo4j.graphdb.Node> index;

    @Mock
    private IndexHits<Node> hits;

    @Mock
    private OsmReader reader;

    @Mock
    private org.neo4j.graphdb.Node routingNode;

    @Mock
    private org.neo4j.graphdb.Node boundNode;

    @Mock
    private org.neo4j.graphdb.Node node;

    @Mock
    private Transaction transaction;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private OsmUser user = OsmUser.NONE;

    interface ParseCallback {
        void parse(OsmReaderCallback cb);
    }

    @Before
    public void initVariables() {
        calendar.set(2009, Calendar.MARCH, 11, 6, 30, 8);
    }

    @Test
    public void testCreateRoutingNode() throws FileNotFoundException, InterruptedException, ExecutionException {
        when(graphDb.index()).thenReturn(indexManager);
        when(indexManager.forNodes(anyString())).thenReturn(index);
        when(graphDb.beginTx()).thenReturn(transaction);
        when(graphDb.findNode(eq(Neo4jOsmLabels.OSM_ROUTING), eq("name"), eq("OsmRouting"))).thenReturn(null);
        when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_ROUTING))).thenReturn(routingNode);

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
            }
        });

        verify(graphDb, times(1)).index();
        verify(indexManager, times(2)).forNodes(anyString());
        verify(graphDb, times(1)).beginTx();
        verify(graphDb, times(1)).findNode(eq(Neo4jOsmLabels.OSM_ROUTING), eq("name"), eq("OsmRouting"));
        verify(graphDb, times(1)).createNode(eq(Neo4jOsmLabels.OSM_ROUTING));
        verify(routingNode, times(1)).setProperty(eq("name"), eq("OsmRouting"));
        verify(transaction, times(1)).close();

        verifyNoMoreInteractions(graphDb, transaction,routingNode);
    }

    @Test
    public void testSetBound() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();
        whenBound();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                setBound(cb);
            }
        });

        verifyDefaults();
        verifyBound();
        verifyNoMoreInteractions(graphDb, transaction, boundNode, routingNode);
    }

    @Test
    public void testTransactionSuccessCalledOnComplete() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                cb.complete();
            }
        });

        verifyDefaults();
        verify(transaction, times(1)).success();
        verifyNoMoreInteractions(graphDb, transaction, boundNode, routingNode);
    }

    @Test
    public void testAddWaterWay() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();
        whenBound();

        final Way way = createWaterWay();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                setBound(cb);
                cb.addWay(way);
            }
        });

        verifyBound();
        verifyDefaults();
        verifyNoMoreInteractions(graphDb, transaction, boundNode, routingNode);
    }

    @Test
    public void testAddRoad() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();
        whenBound();

        whenSimpleWay(new int[]{0, 0});
        final Way way = createSimpleWay();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                setBound(cb);
                cb.addWay(way);
            }
        });

        verifyDefaults();
        verifyBound();

        // Create OSM WAY node
        verify(graphDb, times(1)).createNode(eq(Neo4jOsmLabels.OSM_WAY));
        verify(node, times(1)).setProperty(eq("id"), eq(new Long(1)));
        verify(node, times(1)).setProperty(eq("highway"), eq("road"));

        // Bind way to bound
        verify(boundNode, times(1)).createRelationshipTo(eq(node), eq(Neo4jOsmRelationshipTypes.OSM_WAY));
    }

    @Test
    public void testAddRoadWithNewOsmWayNodes() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();
        whenBound();

        org.neo4j.graphdb.Node[] nodes = whenSimpleWay(new int[] { 0, 0 } );

        // Create way to add
        final Way way = createSimpleWay();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                setBound(cb);
                cb.addWay(way);
            }
        });

        // Create first OSM NODE node
        verifyNewWayNode(nodes, 0);
        verifyNewWayNode(nodes, 1);

        // Bind WAY to WAY NODE
        verify(node, times(1)).createRelationshipTo(eq(nodes[0]), eq(Neo4jOsmRelationshipTypes.OSM_FIRST));
        verify(nodes[0], times(1)).createRelationshipTo(eq(nodes[1]), eq(Neo4jOsmRelationshipTypes.OSM_NEXT));
    }

    @Test
    public void testAddRoadWithExistingWayNodes() throws FileNotFoundException, InterruptedException, ExecutionException {
        whenDefaults();
        whenBound();

        org.neo4j.graphdb.Node[] nodes = whenSimpleWay(new int[] { 1, 1 } );

        // Create way to add
        final Way way = createSimpleWay();

        startImport(new ParseCallback() {
            @Override
            public void parse(OsmReaderCallback cb) {
                setBound(cb);
                cb.addWay(way);
            }
        });

        // Create first OSM NODE node
        verifyExistingWayNode(nodes, 0);
        verifyExistingWayNode(nodes, 1);

        // Bind WAY to WAY NODE
        verify(node, times(1)).createRelationshipTo(eq(nodes[0]), eq(Neo4jOsmRelationshipTypes.OSM_FIRST));
        verify(nodes[0], times(1)).createRelationshipTo(eq(nodes[1]), eq(Neo4jOsmRelationshipTypes.OSM_NEXT));
    }

    private void whenDefaults() {
        when(graphDb.index()).thenReturn(indexManager);
        when(indexManager.forNodes(anyString())).thenReturn(index);
        when(graphDb.beginTx()).thenReturn(transaction);
        when(graphDb.findNode(eq(Neo4jOsmLabels.OSM_ROUTING), eq("name"), eq("OsmRouting"))).thenReturn(routingNode);
    }

    private void whenBound() {
        when(reader.getFilename()).thenReturn("file.osm");
        when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_BOUND))).thenReturn(boundNode);
    }

    private org.neo4j.graphdb.Node[] whenSimpleWay(final int[] hitSizes) {
        org.neo4j.graphdb.Node[] nodes = new org.neo4j.graphdb.Node[4];

        nodes[0] = mock(org.neo4j.graphdb.Node.class); // OSM_WAYNODE 1
        nodes[1] = mock(org.neo4j.graphdb.Node.class); // OSM_WAYNODE 2
        nodes[2] = mock(org.neo4j.graphdb.Node.class); // OSM_NODE 1
        nodes[3] = mock(org.neo4j.graphdb.Node.class); // OSM_NODE 2

        when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_WAY))).thenReturn(node);
        when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(nodes[0], nodes[1]);
        when(index.get(eq("node_id"), any(Integer.class))).thenReturn(hits);

        // Set hit sizes
        when(hits.size()).thenReturn(hitSizes[0], hitSizes[1]);
        if (hitSizes[0] == 0 && hitSizes[1] == 0) {
            // No nodes "found", both are created,
            when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_NODE))).thenReturn(nodes[2], nodes[3]);
        }
        else {
            if (hitSizes[0] == 0) {
                // First node not "found": create first node, get second node
                when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_NODE))).thenReturn(nodes[0]);
                when(hits.getSingle()).thenReturn(nodes[3]);
                when(nodes[3].hasProperty("junction")).thenReturn(false);
            }
            else if (hitSizes[1] == 0) {
                // Second node not "found": create second node, get first node
                when(graphDb.createNode(eq(Neo4jOsmLabels.OSM_NODE))).thenReturn(nodes[1]);
                when(hits.getSingle()).thenReturn(nodes[2]);
                when(nodes[2].hasProperty("junction")).thenReturn(false);
            }
            else {
                // Both nodes "found": get first and second node
                when(hits.getSingle()).thenReturn(nodes[2], nodes[3]);
                when(nodes[2].hasProperty("junction")).thenReturn(false);
                when(nodes[3].hasProperty("junction")).thenReturn(false);
            }
        }

        return nodes;
    }

    private Way createWaterWay() {
        final CommonEntityData commonEntityData = new CommonEntityData(1L, 1, calendar.getTime(), user, 0);
        final Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "waterway"));
        return way;
    }

    private Way createSimpleWay() {
        final CommonEntityData commonEntityData = new CommonEntityData(1L, 1, calendar.getTime(), user, 0);
        final Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(2));
        way.getWayNodes().add(new WayNode(3));
        return way;
    }

    private void startImport(final ParseCallback cb) throws FileNotFoundException, InterruptedException, ExecutionException {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                OsmReaderCallback osmReaderCallback = (OsmReaderCallback) invocation.getArguments()[0];
                cb.parse(osmReaderCallback);
                return null;
            }
        }).when(reader).parse(any(OsmReaderCallback.class));

        OsmFileImporter importer = new Neo4jImporter(neo4j);
        importer.importOsmFile(reader);
    }

    private void setBound(OsmReaderCallback cb) {
        Bound bound = new Bound(13.0, 8.0, 45.0, 40.0, "SEAT Italy Data");
        cb.setBound(bound);
    }

    private void verifyDefaults() {
        verify(graphDb, times(1)).index();
        verify(indexManager, times(2)).forNodes(anyString());
        verify(graphDb, times(1)).beginTx();
        verify(graphDb, times(1)).findNode(eq(Neo4jOsmLabels.OSM_ROUTING), eq("name"), eq("OsmRouting"));
        verify(transaction, times(1)).close();
    }

    private void verifyBound() {
        verify(graphDb, times(1)).createNode(eq(Neo4jOsmLabels.OSM_BOUND));

        verify(boundNode, times(1)).setProperty(eq("file"), eq("file.osm"));
        verify(boundNode, times(1)).setProperty(eq("date"), any(Date.class));
        verify(boundNode, times(1)).setProperty(eq("minlat"), eq(40.0));
        verify(boundNode, times(1)).setProperty(eq("maxlat"), eq(45.0));
        verify(boundNode, times(1)).setProperty(eq("minlon"), eq(8.0));
        verify(boundNode, times(1)).setProperty(eq("maxlon"), eq(13.0));
        verify(boundNode, times(1)).setProperty(eq("origin"), eq("SEAT Italy Data"));

        verify(routingNode, times(1)).createRelationshipTo(eq(boundNode), eq(Neo4jOsmRelationshipTypes.OSM_BOUND));
    }

    private void verifyNewWayNode(final org.neo4j.graphdb.Node[] nodes, int id) {
        Long wayNodeId = new Long(id);
        Long nodeId = new Long(id + 2);

        // Verify WAY_NODE
        verify(nodes[id], times(1)).setProperty(eq("wayId"), eq(new Long(1)));
        verify(nodes[id], times(1)).setProperty(eq("nodeId"), eq(nodeId));
        verify(nodes[id], times(1)).createRelationshipTo(eq(nodes[id + 2]), eq(Neo4jOsmRelationshipTypes.OSM_COORDINATES));

        // Verify NODE
        verify(nodes[id + 2], times(1)).setProperty(eq("id"), eq(nodeId));
        verify(boundNode, times(1)).createRelationshipTo(eq(nodes[id + 2]), eq(Neo4jOsmRelationshipTypes.OSM_NODE));
        verify(index, times(1)).add(eq(nodes[id + 2]), eq("node_id"), eq(nodeId));
    }

    private void verifyExistingWayNode(final org.neo4j.graphdb.Node[] nodes, int id) {
        Long wayNodeId = new Long(id);
        Long nodeId = new Long(id + 2);

        // Verify WAY_NODE
        verify(nodes[id], times(1)).setProperty(eq("wayId"), eq(new Long(1)));
        verify(nodes[id], times(1)).setProperty(eq("nodeId"), eq(nodeId));
        verify(nodes[id], times(1)).createRelationshipTo(eq(nodes[id + 2]), eq(Neo4jOsmRelationshipTypes.OSM_COORDINATES));

        // Verify no interactions with NODE as it's already created
        verify(nodes[id + 2], times(0)).setProperty(eq("id"), eq(nodeId));
        verify(boundNode, times(0)).createRelationshipTo(eq(nodes[id + 2]), eq(Neo4jOsmRelationshipTypes.OSM_NODE));
        verify(index, times(0)).add(eq(nodes[id + 2]), eq("node_id"), eq(nodeId));
    }
}