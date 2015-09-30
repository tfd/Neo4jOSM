package it.tfd.neo4josm.routing.worker;

import it.tfd.neo4josm.routing.model.AccessPermission;
import org.neo4j.graphdb.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.*;

/**
 * Created by ronald on 30/09/15.
 */
public class AccessPermissionsImporter {

    private interface ExamineKeyValue {
        void examine(final String value);
    };

    public AccessPermissionsImporter(Entity obj) {
        this.obj = obj;
    }

    public EnumSet<AccessPermission> getAccessPermissions() {
        final EnumSet<AccessPermission> accessPermissions = AccessPermission.ALL_TRANSPORT;

        examineKey("highway", new ExamineKeyValue() {
            @Override
            public void examine(String value) {
                if (value.equals("motorway") || value.equals("trunk")) {
                    accessPermissions.removeAll(AccessPermission.MOTORWAY_VEHICLES);
                } else if (value.equals("path") || value.equals("bridleway")) {
                    accessPermissions.removeAll(AccessPermission.VEHICLES);
                } else if (value.equals("bridleway")) {
                    accessPermissions.removeAll(vehicles);
                }
            }
        });

        examineKey("access", new ExamineKeyValue() {
            @Override
            public void examine(String value) {

            }
        });

        Iterator<Tag> tags = obj.getTags().iterator();
        while (tags.hasNext()) {
            Tag tag = tags.next();
            String key = tag.getKey().toLowerCase();
            String value = tag.getValue().toLowerCase();

            if (key.equals("highway")) {
            }
            else if (key.equals("access")) {
                if (value.equals("yes")) {
                    accessPermissions.addAll(AccessPermission.ALL_PERMISSIONS);
                }
                else if (value.equals("yes")) {

                }
            }
            else if (key.equals("bicycle")) {

            }
        }

        return accessPermissions;
    }

    private boolean examineKey(final String key, final ExamineKeyValue examinator) {
        Iterator<Tag> tags = obj.getTags().iterator();
        while (tags.hasNext()) {
            Tag tag = tags.next();
            if (tag.getKey().toLowerCase().equals(key)) {
                examinator.examine(tag.getValue().toLowerCase());
                return true;
            }
        }
        return false;
    }

    private Entity obj;
}
