package it.tfd.neo4josm.routing.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Enumerated all access types.
 * <p/>
 * TODO: check if it's more efficient to use BitSet instead of EnumSet.
 */
public enum AccessPermission {
    MOTORCAR, FOOT, BICYCLE, MOTORCYCLE, HGV, BUS, TAXI, EMERGENCY, AGRICULTURAL, HORSE, DELIVERY, NO_THRU_TRAFFIC;

    public static final EnumSet<AccessPermission> ALL_OPTS = EnumSet.allOf(AccessPermission.class);
    public static final EnumSet<AccessPermission> VEHICLES = EnumSet.of(
            AccessPermission.AGRICULTURAL,
            AccessPermission.BICYCLE,
            AccessPermission.BUS,
            AccessPermission.EMERGENCY,
            AccessPermission.HGV,
            AccessPermission.MOTORCAR,
            AccessPermission.MOTORCYCLE,
            AccessPermission.TAXI);
    public static final EnumSet<AccessPermission> MOTOR_VEHICLES = EnumSet.of(
            AccessPermission.AGRICULTURAL,
            AccessPermission.BUS,
            AccessPermission.EMERGENCY,
            AccessPermission.HGV,
            AccessPermission.MOTORCAR,
            AccessPermission.MOTORCYCLE,
            AccessPermission.TAXI);
    public static final EnumSet<AccessPermission> MOTORWAY_VEHICLES = EnumSet.of(
            AccessPermission.BUS,
            AccessPermission.EMERGENCY,
            AccessPermission.HGV,
            AccessPermission.MOTORCAR,
            AccessPermission.MOTORCYCLE,
            AccessPermission.TAXI);
    public static final EnumSet<AccessPermission> PEDESTRIAN = EnumSet.of(
            AccessPermission.BICYCLE,
            AccessPermission.FOOT,
            AccessPermission.HORSE);
    public static final EnumSet<AccessPermission> ALL_TRANSPORT = EnumSet.of(
            AccessPermission.AGRICULTURAL,
            AccessPermission.BICYCLE,
            AccessPermission.BUS,
            AccessPermission.EMERGENCY,
            AccessPermission.FOOT,
            AccessPermission.HGV,
            AccessPermission.HORSE,
            AccessPermission.MOTORCAR,
            AccessPermission.MOTORCYCLE,
            AccessPermission.TAXI);
}
