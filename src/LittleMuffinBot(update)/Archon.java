package LittleMuffinBot;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

public strictfp class Archon {
    static MapLocation location;
    // These values are saved between turns at one instance of Archon (so 1 Archon)
    static int miners = 0, soldiers = 0, builders = 20, sages = 0;
    static boolean underAttack = false;
    static int turn = 0;


    // We first try to scan if it's the first turn, else we just try to build if it's possible.
    static void run(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        scanMode(rc);
        buildMode(rc);
    }

    // Scan for the nearby deposits to the Archon.
    static void scanMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        int vision = rc.getType().visionRadiusSquared;
        RobotInfo[] nearby = rc.senseNearbyRobots(location, vision, rc.getTeam().opponent()); // Bytecode: 100
        if (nearby.length > 0) {
            underAttack = true;
        } else {
            underAttack = false;
        }
    }

    // The order we build in right now, due to change in the future.
    static void buildMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Building");
        if (underAttack) {
            build(rc, RobotType.SOLDIER);
        }
        if (miners < 5) {
            build(rc, RobotType.MINER);
        } else if (rc.readSharedArray(0) < rc.readSharedArray(1) / 2
                   && rc.readSharedArray(0) < 30) {
            build(rc, RobotType.MINER);
        } else if (rc.readSharedArray(1) < 30){
            build(rc, RobotType.BUILDER);
        }
    }

    // Build the robot at the position that has the least rubble around it.
    static void build(RobotController rc, RobotType type) throws GameActionException {
        rc.setIndicatorString("Building");
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, Comparator.comparingInt(a -> getRubble(rc, a)));
        int curr = 0;
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                switch(type) {
                    case MINER: miners++; rc.writeSharedArray(0, rc.readSharedArray(0) + 1); break;
                    case SOLDIER: soldiers++; break;
                    case BUILDER: builders++; rc.writeSharedArray(1, rc.readSharedArray(1) + 1); break;
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
//        } else if (miners < soldiers / 2 && rc.getTeamLeadAmount(rc.getTeam()) < 1000) {
//            build(rc, RobotType.MINER, 5);
//        } else {
//            if (builders % 4 == 0) {
//                build(rc, RobotType.BUILDER, 1);
//            } else {
//                build(rc, RobotType.SOLDIER, 2);
//                builders++;
//            }