package it.tfd.neo4josm.routing.model;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Created by ronald on 27/09/15.
 */
public class OsmWayTest {

    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private OsmUser user = OsmUser.NONE;

    @Test
    public void testIsHighway() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "road"));

        assertEquals(true, new OsmWay(way).isHighway());
    }

    @Test
    public void testIsNotHighway() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);

        assertEquals(false, new OsmWay(way).isHighway());
    }

    @Test
    public void testIsNotRoad() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "bridleway"));

        assertEquals(false, new OsmWay(way).isHighway());
    }

    @Test
    public void testIsMotorWay() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway"));

        assertEquals(true, new OsmWay(way).isMotorWay());
    }

    @Test
    public void testIsMotorWayJunction() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway_junction"));

        assertEquals(true, new OsmWay(way).isMotorWay());
    }

    @Test
    public void testIsMotorWayLink() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "motorway_link"));

        assertEquals(true, new OsmWay(way).isMotorWay());
    }

    @Test
    public void testIsNotMotorWay() throws Exception {
        CommonEntityData commonEntityData = new CommonEntityData(358802885L, 1, calendar.getTime(), user, 0);
        Way way = new Way(commonEntityData);
        way.getTags().add(new Tag("highway", "trunk"));

        assertEquals(false, new OsmWay(way).isMotorWay());
    }

}