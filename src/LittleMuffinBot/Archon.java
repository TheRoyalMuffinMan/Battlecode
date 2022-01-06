package LittleMuffinBot;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;
import static LittleMuffinBot.RobotPlayer.deposits;


public strictfp class Archon {
    static MapLocation location;

    // We first try to scan if it's the first turn, else we just try to build if it's possible.
    static void dispatcher(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        if (RobotPlayer.turnCount == 1) {
            scanMode(rc);
        }
        buildMode(rc);
    }

    // Scan for the nearby deposits to the Archon.
    static void scanMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        int vision = rc.getType().visionRadiusSquared;
        MapLocation[] nearby = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        // Bytecode: 500
        for (MapLocation position : nearby) {
            if (rc.senseLead(position) > 10) {
                deposits.put(position, rc.senseLead(position));
            }
        }
    }

    // The order we build in right now, due to change in the future.
    static void buildMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Building");
        if (rc.readSharedArray(0) < 20) {
            build(rc, RobotType.MINER);
        } else if (rc.readSharedArray(1) < 10) {
            build(rc, RobotType.SOLDIER);
        } else if (rc.readSharedArray(0) < rc.readSharedArray(1) / 2 && rc.getTeamLeadAmount(rc.getTeam()) < 5000) {
            build(rc, RobotType.MINER);
        } else {
            build(rc, RobotType.SOLDIER);
        }
    }

    // Build the robot at the position that has the least lead.
    static void build(RobotController rc, RobotType type) throws GameActionException {
        rc.setIndicatorString("Building");
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, Comparator.comparingInt(a -> getRubble(rc, a)));
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                switch(type) {
                    case MINER: rc.writeSharedArray(0, rc.readSharedArray(0) + 1); break;
                    case SOLDIER: rc.writeSharedArray(1, rc.readSharedArray(1) + 1); break;
                    default: break;
                }
            }
        }
    }

    // Get all the nearby rubble to the Archon using the following method
    static int getRubble(RobotController rc, Direction dir) {
        rc.setIndicatorString("Checking nearby rubble");
        try {
            MapLocation loc = rc.getLocation().add(dir);
            return rc.senseRubble(loc);
        } catch (GameActionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    static void repairMode(RobotController rc) {
        // NOTE: Needs to be implemented
    }

    static void walkMode(RobotController rc) {
        // NOTE: Needs to be implemented
    }

    static void turretMode(RobotController rc) {
        // NOTE: Needs to be implemented
    }
}
