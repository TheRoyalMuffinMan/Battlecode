package LittleMuffinBot;

import battlecode.common.*;

public class Soldier {
    static MapLocation location;
    static Team otherTeam;
    static int vision;
    static Direction exploreDir = null;
    static RobotInfo[] nearby;

    // Record information then dispatch to scanMode() and attackMode()
    static void run(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        otherTeam = rc.getTeam().opponent();
        vision = rc.getType().visionRadiusSquared;
        scanMode(rc);
        attackMode(rc);
        Exploration.run();
        findEnemyArchon.sense(rc);
    }

    // Perform a scan for nearby Archons then save the result in the sharable array
    static void scanMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        nearby = rc.senseNearbyRobots(location, vision, otherTeam);
        for (RobotInfo robot : nearby) {
            MapLocation robotLocation = robot.getLocation();
            if (robot.getType() == RobotType.ARCHON) {
                // use the first 8 positions to save the enemy archons location
                for (int i = 0; i <= 6; i += 2) {
                    if (rc.readSharedArray(i) == 0) {
                        rc.writeSharedArray(i, robotLocation.x);
                        rc.writeSharedArray(i + 1, robotLocation.y);
                        break;
                    }
                }
            }
        }
    }
    // Attack nearby troops while moving around randomly unless an Archon is spotted on the map,
    // then set preference target to that and go for it.
    static void attackMode(RobotController rc) throws GameActionException {
        MapLocation nearbyArchon = null;
        for (int i = 0; i <= 6; i += 2) {
            if (rc.readSharedArray(i) != 0) {
                nearbyArchon = new MapLocation(rc.readSharedArray(i), rc.readSharedArray(i + 1));
            }
        }
        // We want to remove Archons that have been destroyed.
        if (nearbyArchon != null && rc.canSenseLocation(nearbyArchon) && !rc.canSenseRobotAtLocation(nearbyArchon)) {
            for (int i = 0; i <= 6; i += 2) {
                if (rc.readSharedArray(i) == nearbyArchon.x && rc.readSharedArray(i + 1) == nearbyArchon.y) {
                    rc.writeSharedArray(i, 0);
                    rc.writeSharedArray(i + 1, 0);
                    break;
                }
            }
            nearbyArchon = null;
        }
        if (nearbyArchon != null) {
            rc.setIndicatorString("Attacking Archon");
            if (rc.canAttack(nearbyArchon)) {
                rc.attack(nearbyArchon);
            } else {
                Pathing.walkTowards(rc, nearbyArchon);
//                Direction dir = location.directionTo(nearbyArchon);
//                if (rc.canMove(dir)) {
//                    rc.move(dir);
//                }
            }
        } else if (nearby.length != 0) {
            rc.setIndicatorString("Attacking");
            for (RobotInfo robot : nearby) {
                if (rc.canAttack(robot.location)) {
                    rc.attack(robot.location);
                } else {
                    Direction toMove = rc.getLocation().directionTo(robot.location);
                    if (rc.canMove(toMove)) {
                        rc.move(toMove);
                    }
                }
            }
        } else {
            exploreMode(rc);
        }
    }

    // Exploring is done here randoming, we attempt to move in 2 different completely random directions.
    // We catch when we get stuck against the edges of the map.
    static void exploreMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Exploring");
        if (exploreDir == null) {
            RobotPlayer.rng.setSeed(rc.getID());
            exploreDir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        }
        rc.setIndicatorString(exploreDir.toString());
        if (rc.canMove(exploreDir)) {
            rc.move(exploreDir);
        } else if (!rc.onTheMap(rc.getLocation().add(exploreDir))) {
            exploreDir = exploreDir.opposite();
        }
        int directionIndex = RobotPlayer.rng.nextInt(RobotPlayer.directions.length);
        Direction dir = RobotPlayer.directions[directionIndex];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    static void targetMode(RobotController rc) throws GameActionException {
        // NOTE: Needs to be implemented
    }

}
