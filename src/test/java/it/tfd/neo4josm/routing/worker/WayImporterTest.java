package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.database.Neo4jRouting;
import it.tfd.neo4josm.routing.model.Neo4jOsmLabels;
import it.tfd.neo4josm.routing.model.Neo4jOsmRelationshipTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.neo4j.graphdb.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.Calendar;
import java.util.TimeZone;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by ronald on 27/09/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class WayImporterTest {

    @Mock
    private Neo4jRouting database;

    @Mock
    Node node;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private OsmUser user = OsmUser.NONE;

    @Test
    public void testInvalidRoadIsNotAdded() {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verifyNoMoreInteractions(database);
    }

    @Test
    public void testRoadIsAdded() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(false);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(database).addWay(eq(node));
    }

    @Test
    public void testOneWayIsSetToNoForRoad() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(false);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).setProperty("highway", "road");
        verify(node).setProperty("oneway", "no");
    }

    @Test
    public void testOneWayIsSetToYesForMotorway() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(false);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).setProperty("highway", "motorway");
        verify(node).setProperty("oneway", "yes");
    }

    @Test
    public void testOneWayIsSetToYesForMotorwayJunction() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(false);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway_junction"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).setProperty("highway", "motorway_junction");
        verify(node).setProperty("oneway", "yes");
    }

    @Test
    public void testOneWayIsSetToYesForMotorwayLink() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(false);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway_link"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).setProperty("highway", "motorway_link");
        verify(node).setProperty("oneway", "yes");
    }

    @Test
    public void testOneWayIsNotSetIfAlreadyPresent() {
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(node.hasProperty(eq("oneway"))).thenReturn(true);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway_link"));
        way.getTags().add(new Tag("oneway", "-1"));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).setProperty("highway", "motorway_link");
        verify(node).hasProperty("oneway");
        verify(node).setProperty("oneway", "-1");

        verifyNoMoreInteractions(node);
    }

    @Test
    public void testWayNodeIdsAreSet() {
        Node wayFirstNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(wayFirstNode).setProperty("wayId", 358802885L);
        verify(wayFirstNode).setProperty("nodeId", 453966480L);
    }

    @Test
    public void testNewNodeIsCreated() {
        Node wayFirstNode = Mockito.mock(Node.class);
        Node connectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode);
        when(database.getNode(eq(453966480L))).thenReturn(null);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_NODE), eq(453966480L))).thenReturn(connectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(database).addNode(eq(connectedNode));
    }

    @Test
    public void testExistingNodeIsUsed() {
        Node wayFirstNode = Mockito.mock(Node.class);
        Node connectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode);
        when(database.getNode(eq(453966480L))).thenReturn(connectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(database, times(0)).createNode(eq(Neo4jOsmLabels.OSM_NODE), eq(453966480L));
        verify(database, times(0)).addNode(eq(connectedNode));
    }

    @Test
    public void testNewNodeIsCreatedForSecondWayNode() {
        Node firstWayNode = Mockito.mock(Node.class);
        Node firstConnectedNode = Mockito.mock(Node.class);
        Node secondWayNode = Mockito.mock(Node.class);
        Node secondConnectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(firstWayNode, secondWayNode);
        when(database.getNode(eq(453966480L))).thenReturn(firstConnectedNode);
        when(database.getNode(eq(453966488L))).thenReturn(null);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_NODE), eq(453966488L))).thenReturn(secondConnectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));
        way.getWayNodes().add(new WayNode(453966488L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(database).addNode(eq(secondConnectedNode));
    }

    @Test
    public void testNodeIsNotCreatedForSecondTime() {
        Node firstWayNode = Mockito.mock(Node.class);
        Node secondWayNode = Mockito.mock(Node.class);
        Node connectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(firstWayNode, secondWayNode);
        when(database.getNode(eq(453966480L))).thenReturn(null, connectedNode);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_NODE), eq(453966480L))).thenReturn(connectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(database).addNode(eq(connectedNode));
        verify(database, times(1)).createNode(eq(Neo4jOsmLabels.OSM_NODE), eq(453966480L));
    }

    @Test
    public void testNodeIsConnectedToWayNode() {
        Node wayFirstNode = Mockito.mock(Node.class);
        Node connectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode);
        when(database.getNode(eq(453966480L))).thenReturn(connectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(wayFirstNode).createRelationshipTo(eq(connectedNode), eq(Neo4jOsmRelationshipTypes.OSM_COORDINATES));
    }

    @Test
    public void testWayNodeIsConnectedToWay() {
        Node wayFirstNode = Mockito.mock(Node.class);
        Node connectedNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode);
        when(database.getNode(eq(453966480L))).thenReturn(connectedNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(node).createRelationshipTo(eq(wayFirstNode), eq(Neo4jOsmRelationshipTypes.OSM_FIRST));
    }

    @Test
    public void testNextWayNodeIsConnectedToPreviousWayNode() {
        Node wayFirstNode = Mockito.mock(Node.class);
        Node connectedFirstNode = Mockito.mock(Node.class);
        Node waySecondNode = Mockito.mock(Node.class);
        Node connectedSecondNode = Mockito.mock(Node.class);

        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY), eq(358802885L))).thenReturn(node);
        when(database.createNode(eq(Neo4jOsmLabels.OSM_WAY_NODE))).thenReturn(wayFirstNode, waySecondNode);
        when(database.getNode(eq(453966480L))).thenReturn(connectedFirstNode);
        when(database.getNode(eq(453966488L))).thenReturn(connectedSecondNode);

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));
        way.getWayNodes().add(new WayNode(453966480L));
        way.getWayNodes().add(new WayNode(453966488L));

        WayImporter importer = new WayImporter(database);
        importer.load(way);

        verify(wayFirstNode).createRelationshipTo(eq(connectedFirstNode), eq(Neo4jOsmRelationshipTypes.OSM_COORDINATES));
        verify(wayFirstNode).createRelationshipTo(eq(waySecondNode), eq(Neo4jOsmRelationshipTypes.OSM_NEXT));
    }

}