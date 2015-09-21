package it.tfd.neo4josm.routing.database;

import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * Created by ronald on 22/08/15.
 */
public class OsmReaderTest {

    private static final Logger log = LoggerFactory.getLogger(OsmReaderTest.class);

    private void logTags(Collection<Tag> tags) {
        if (tags.size() > 0) { log.info("  tags"); }
        for(Tag tag : tags) {
            log.info("    " + tag.getKey() + ": [" + tag.getValue() + "]");
        }
    }

    private void logNodes(List<WayNode> wayNodes) {
        if (wayNodes.size() > 0) { log.info("  nodes"); }
        for (WayNode node : wayNodes) {
            log.info("    " + Long.toString(node.getNodeId()));
        }
    }

    private void logMembers(List<RelationMember> members) {
        if (members.size() > 0) { log.info("  members"); }
        for (RelationMember member : members) {
            log.info("    " + Long.toString(member.getMemberId()) + " role: " +  member.getMemberRole() + " type:" + member.getMemberType().name());
        }
    }

    @Test
    public void testParse() throws Exception {
        final OsmReader reader = new OsmReader("./src/test/resources/monaco-latest.osm.pbf");
        reader.parse(new OsmReaderCallback() {
            @Override
            public void addNode(Node node) {
                log.info(node.toString());
                logTags(node.getTags());
            }

            @Override
            public void addWay(Way way) {
                log.info(way.toString());
                logTags(way.getTags());
                logNodes(way.getWayNodes());
            }

            @Override
            public void addRelation(Relation relation) {
                if (isRestriction(relation)) {
                    log.info(relation.toString());
                    logTags(relation.getTags());
                    logMembers(relation.getMembers());
                }
            }

            @Override
            public void complete() {

            }
        });
    }

    private boolean isRestriction(Relation relation) {
        for (Tag tag : relation.getTags()) {
            if (tag.getKey().compareTo("type") == 0 &&
                    tag.getValue().compareTo("restriction") == 0) {
                return true;
            }
        }
        return false;
    }
}