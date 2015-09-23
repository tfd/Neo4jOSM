package it.tfd.neo4josm.routing.database;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Created by ronald on 22/08/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class OsmReaderImplTest {

    private static final Logger log = LoggerFactory.getLogger(OsmReaderImplTest.class);

    @Mock
    private OsmReaderCallback cb;

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private OsmUser user = OsmUser.NONE;

    @Before
    public void initVariables() {
        calendar.set(2009, Calendar.MARCH, 11, 6, 30, 8);
    }

    @Test
    public void testSetBound() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/nodes.osm");
        reader.parse(cb);
        verify(cb, times(1)).setBound(any(Bound.class));
    }

    @Test
    public void testNumberOfNodes() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/nodes.osm");
        reader.parse(cb);
        verify(cb, times(6)).addNode(any(Node.class));
    }

    @Test
    public void testNumberOfWays() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/ways.osm");
        reader.parse(cb);
        verify(cb, times(1)).addWay(any(Way.class));
    }

    @Test
    public void testNumberOfRelations() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/relations.osm");
        reader.parse(cb);
        verify(cb, times(1)).addRelation(any(Relation.class));
    }

    class AssertCommonEntityDataAreEqual<T extends Entity> {

        protected T expected;
        protected T actual;

        public AssertCommonEntityDataAreEqual(T expected, T actual) {
            this.expected = expected;
            this.actual = actual;
        }

        public boolean areEqual() {
            String diff = getDifferenceAsString();
            if (diff.length() > 0) {
                fail(diff);
                return false;
            }
            return true;
        }

        protected String getDifferenceAsString() {
            if (actual.getId() != expected.getId()) {
                return String.format("Id: expected %1$d, actual %2$d", expected.getId(), actual.getId());
            }
            if (actual.getVersion() != expected.getVersion()) {
                return String.format("Version: expected %1$d, actual %2$d", expected.getVersion(), actual.getVersion());
            }
            if (actual.getUser().getName().compareTo(expected.getUser().getName()) != 0) {
                return String.format("Name: expected %1$s, actual %2$s", expected.getUser().getName(), actual.getUser().getName());
            }
            if (actual.getChangesetId() != expected.getChangesetId()) {
                return String.format("ChangeSetId: expected %1$d, actual %2$d", expected.getChangesetId(), actual.getChangesetId());
            }
            return "";
        }
    }

    class AssertNodesAreEqual extends AssertCommonEntityDataAreEqual<Node> {
        public AssertNodesAreEqual(Node expected, Node actual) {
            super(expected, actual);
        }

        @Override
        protected String getDifferenceAsString() {
            String diff = super.getDifferenceAsString();
            if (diff.length() == 0) {
                if (actual.getLatitude() != expected.getLatitude()) {
                    return String.format("Latitude: expected %1$f, actual %2$f", expected.getLatitude(), actual.getLatitude());
                }
                if (actual.getLongitude() != expected.getLongitude()) {
                    return String.format("Longitude: expected %1$f, actual %2$f", expected.getLongitude(), actual.getLongitude());
                }
            }
            return "";
        }
    }

    @Test
    public void testBound() throws FileNotFoundException, InterruptedException, ExecutionException {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/nodes.osm");

        reader.parse(cb);

        ArgumentCaptor<Bound> argument = ArgumentCaptor.forClass(Bound.class);

        verify(cb, times(1)).setBound(argument.capture());
        Bound bound = argument.getValue();

        assertEquals(34.0662408634219, bound.getBottom(), 0.00000000000001);
        assertEquals(34.0731374116421, bound.getTop(), 0.00000000000001);
        assertEquals(-118.736715316772, bound.getLeft(), 0.00000000000001);
        assertEquals(-118.73122215271, bound.getRight(), 0.00000000000001);
        assertEquals("OpenStreetMap server", bound.getOrigin());
    }

    @Test
    public void testNodes() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/nodes.osm");


        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Node node1 = new Node(commonEntityData, 34.0666735, -118.734254);

        commonEntityData = new CommonEntityData(453966480L, 1, calendar.getTime(), user, 0);
        Node node2 = new Node(commonEntityData, 34.07234, -118.7343501);

        commonEntityData = new CommonEntityData(453966482L, 1, calendar.getTime(), user, 0);
        Node node3 = new Node(commonEntityData, 34.0670965, -118.7322253);

        commonEntityData = new CommonEntityData(453966143L, 1, calendar.getTime(), user, 0);
        Node node4 = new Node(commonEntityData, 34.0724577, -118.7364799);

        commonEntityData = new CommonEntityData(453966130L, 1, calendar.getTime(), user, 0);
        Node node5 = new Node(commonEntityData, 34.0671122, -118.7364725);

        commonEntityData = new CommonEntityData(453966490L, 1, calendar.getTime(), user, 0);
        Node node6 = new Node(commonEntityData, 34.0722227, -118.7322321);

        reader.parse(cb);

        ArgumentCaptor<Node> argument = ArgumentCaptor.forClass(Node.class);

        verify(cb, times(6)).addNode(argument.capture());
        List<Node> nodes = argument.getAllValues();

        assertEquals(6, nodes.size());
        new AssertNodesAreEqual(node1, nodes.get(0)).areEqual();
        new AssertNodesAreEqual(node2, nodes.get(1)).areEqual();
        new AssertNodesAreEqual(node3, nodes.get(2)).areEqual();
        new AssertNodesAreEqual(node4, nodes.get(3)).areEqual();
        new AssertNodesAreEqual(node5, nodes.get(4)).areEqual();
        new AssertNodesAreEqual(node6, nodes.get(5)).areEqual();
    }

    class AssertWaysAreEqual extends AssertCommonEntityDataAreEqual<Way> {
        public AssertWaysAreEqual(Way expected, Way actual) {
            super(expected, actual);
        }

        @Override
        protected String getDifferenceAsString() {
            String diff = super.getDifferenceAsString();
            if (diff.length() == 0) {
                if (actual.isClosed() != expected.isClosed()) {
                    return String.format("isClosed: expected %1$b, actual %2$b", expected.isClosed(), actual.isClosed());
                }
            }
            return "";
        }
    }

    @Test
    public void testWays() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/ways.osm");

        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way expectedWay = new Way(commonEntityData);

        reader.parse(cb);

        ArgumentCaptor<Way> argument = ArgumentCaptor.forClass(Way.class);

        verify(cb, times(1)).addWay(argument.capture());
        Way actualWay = argument.getValue();

        new AssertWaysAreEqual(expectedWay, actualWay).areEqual();
    }

    @Test
    public void testNodeTags() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/nodes.osm");

        reader.parse(cb);

        ArgumentCaptor<Node> argument = ArgumentCaptor.forClass(Node.class);
        verify(cb, times(6)).addNode(argument.capture());

        List<Node> nodes = argument.getAllValues();
        TagCollection actualTags = (TagCollection) nodes.get(0).getTags();

        TagCollection expectedTags = new TagCollectionImpl();
        expectedTags.add(new Tag("gnis:created", "06/14/2000"));
        expectedTags.add(new Tag("gnis:county_id", "037"));
        expectedTags.add(new Tag("name", "Santa Monica Mountains National Recreation Area"));
        expectedTags.add(new Tag("leisure", "park"));
        expectedTags.add(new Tag("gnis:feature_id", "277263"));
        expectedTags.add(new Tag("gnis:state_id", "06"));
        expectedTags.add(new Tag("ele", "243"));

        checkTags(expectedTags, actualTags);
    }

    @Test
    public void testRelationTags() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/relations.osm");

        reader.parse(cb);

        ArgumentCaptor<Relation> argument = ArgumentCaptor.forClass(Relation.class);
        verify(cb, times(1)).addRelation(argument.capture());

        Relation relation = argument.getValue();
        TagCollection actualTags = (TagCollection) relation.getTags();

        TagCollection expectedTags = new TagCollectionImpl();
        expectedTags.add(new Tag("name", "Küstenbus Linie 123"));
        expectedTags.add(new Tag("network", "VVW"));
        expectedTags.add(new Tag("operator", "Regionalverkehr Küste"));
        expectedTags.add(new Tag("ref", "123"));
        expectedTags.add(new Tag("route", "bus"));
        expectedTags.add(new Tag("type", "route"));

        checkTags(expectedTags, actualTags);
    }

    @Test
    public void testWayTags() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/ways.osm");

        reader.parse(cb);

        ArgumentCaptor<Way> argument = ArgumentCaptor.forClass(Way.class);
        verify(cb, times(1)).addWay(argument.capture());

        Way way = argument.getValue();
        TagCollection actualTags = (TagCollection) way.getTags();

        TagCollection expectedTags = new TagCollectionImpl();
        expectedTags.add(new Tag("park:type", "state_park"));
        expectedTags.add(new Tag("csp:unitcode", "537"));
        expectedTags.add(new Tag("name", "Malibu Creek State Park"));
        expectedTags.add(new Tag("admin_level", "4"));
        expectedTags.add(new Tag("csp:globalid", "{4A422954-089E-407F-A5B3-1E808F830EAA}"));
        expectedTags.add(new Tag("leisure", "park"));
        expectedTags.add(new Tag("attribution", "CASIL CSP_Opbdys072008"));
        expectedTags.add(new Tag("note", "simplified with josm to reduce node #"));
        expectedTags.add(new Tag("boundary", "national_park"));

        checkTags(expectedTags, actualTags);
    }

    private void checkTags(TagCollection expectedTags, TagCollection actualTags) {
        assertEquals(expectedTags.size(), actualTags.size());

        for (Tag expectedTag : expectedTags) {
            boolean found = false;
            for (Tag actualTag: actualTags) {
                if (actualTag.compareTo(expectedTag) == 0) {
                    found = true;
                }
            }
            if (!found) {
                fail("Tag " + expectedTag.toString() + " is missing");
            }
        }
    }

    @Test
    public void testWayNodes() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/ways.osm");

        reader.parse(cb);

        ArgumentCaptor<Way> argument = ArgumentCaptor.forClass(Way.class);
        verify(cb, times(1)).addWay(argument.capture());

        Way way = argument.getValue();
        List<WayNode> nodes = way.getWayNodes();

        assertEquals(453966480L, nodes.get(0).getNodeId());
        assertEquals(453966490L, nodes.get(1).getNodeId());
        assertEquals(453966482L, nodes.get(2).getNodeId());
        assertEquals(453966130L, nodes.get(3).getNodeId());
        assertEquals(453966143L, nodes.get(4).getNodeId());
        assertEquals(453966480L, nodes.get(5).getNodeId());
    }

    @Test
    public void testRelationMembers() throws Exception {
        OsmReader reader = new OsmReaderImpl("./src/test/resources/relations.osm");

        reader.parse(cb);

        ArgumentCaptor<Relation> argument = ArgumentCaptor.forClass(Relation.class);
        verify(cb, times(1)).addRelation(argument.capture());

        Relation relation = argument.getValue();
        List<RelationMember> members = relation.getMembers();

        assertEquals(4, members.size());

        assertEquals(294942404L, members.get(0).getMemberId());
        assertEquals("start", members.get(0).getMemberRole());
        assertEquals(EntityType.Node, members.get(0).getMemberType());

        assertEquals(364933006L, members.get(1).getMemberId());
        assertEquals("stop", members.get(1).getMemberRole());
        assertEquals(EntityType.Node, members.get(1).getMemberType());

        assertEquals(4579143L, members.get(2).getMemberId());
        assertEquals("via", members.get(2).getMemberRole());
        assertEquals(EntityType.Way, members.get(2).getMemberType());

        assertEquals(249673494L, members.get(3).getMemberId());
        assertEquals("end", members.get(3).getMemberRole());
        assertEquals(EntityType.Node, members.get(3).getMemberType());
    }
}