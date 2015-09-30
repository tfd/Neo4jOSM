package it.tfd.neo4josm.routing.model;

import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ronald on 27/09/15.
 */
public class OsmWay {

    private Way way;
    private List<String> permittedHighways = Arrays.asList(
            "motorway",
            "motorway_junction",
            "trunk",
            "primary",
            "secondary",
            "tertiary",
            "unclassified",
            "residential",
            "service",
            "road",
            "motorway_link",
            "trunk_link",
            "primary_link",
            "secondary_link",
            "tertiary_link",
            "living_street",
            "pedestrian",
            "track",
            "road",
            "footway",
            "steps",
            "path",
            "cycleway"
    );

    public OsmWay(Way way) {
        this.way = way;
    }

    public boolean isHighway() {
        String highway = getTagValue("highway");
        return highway != null && permittedHighways.contains(highway);
    }

    public boolean isMotorWay() {
        return getTagValue("highway").toLowerCase().startsWith("motorway");
    }

    private String getTagValue(String key) {
        Iterator<Tag> tags = way.getTags().iterator();
        String value = null;
        while (tags.hasNext()) {
            Tag tag = tags.next();
            if (tag.getKey().equalsIgnoreCase(key)) {
                value = tag.getValue();
            }
        }
        return value;
    }
}
