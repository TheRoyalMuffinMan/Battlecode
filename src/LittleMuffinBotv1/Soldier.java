package LittleMuffinBotv1;

import battlecode.common.*;

/*
    - Attack Radius: 13
    - Bytecode limit: 10,000
    - Decent scout and foot soldiers
    - Bytecode limit: 10,000
    Notes:
    - 20-39 in the shared array will communication space for the soldiers.
    - There will be different states the Soldier is currently in.
        - ATTACK: Attack nearby troops.
        - SCOUT: Scout for nearby troops.
        - PURSUE: Purse retreating nearby troops
        - RETREAT: Retreat from nearby enemy troops
 */

public class Soldier {
    public enum State {
        ATTACK, SCOUT, PURSUE, RETREAT
    }

    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static State state;
    private static int health, ID, level, vision;

    private static RobotInfo[] friendlies, enemies;

    // Record information then dispatch to scanMode() and attackMode()
    static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        attack(rc);
    }

    private static void initialize(RobotController rc) {
        location = rc.getLocation();
        mode = rc.getMode();
        type = rc.getType();
        health = rc.getHealth();
        ID = rc.getID();
        level = rc.getLevel();
        vision = type.visionRadiusSquared;
    }

    static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        friendlies = rc.senseNearbyRobots(vision, RobotPlayer.team);
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, 10, 39, robot.getLocation());
        }
    }

    // Attack nearby troops while moving around randomly unless an Archon is spotted on the map,
    // then set preference target to that and go for it.
    static void attack(RobotController rc) throws GameActionException {
        Exploration.explore(rc, location);
    }

    // Exploring is done here randoming, we attempt to move in 2 different completely random directions.
    // We catch when we get stuck against the edges of the map.

    static void target(RobotController rc) throws GameActionException {
        // NOTE: Needs to be implemented
    }

}
//    static void attackMode(RobotController rc) throws GameActionException {
//        MapLocation nearbyArchon = null;
//        for (int i = 0; i <= 6; i += 2) {
//            if (rc.readSharedArray(i) != 0) {
//                nearbyArchon = new MapLocation(rc.readSharedArray(i), rc.readSharedArray(i + 1));
//            }
//        }
//        // We want to remove Archons that have been destroyed.
//        if (nearbyArchon != null && rc.canSenseLocation(nearbyArchon) && !rc.canSenseRobotAtLocation(nearbyArchon)) {
//            for (int i = 0; i <= 6; i += 2) {
//                if (rc.readSharedArray(i) == nearbyArchon.x && rc.readSharedArray(i + 1) == nearbyArchon.y) {
//                    rc.writeSharedArray(i, 0);
//                    rc.writeSharedArray(i + 1, 0);
//                    break;
//                }
//            }
//            nearbyArchon = null;
//        }
//        if (nearbyArchon != null) {
//            rc.setIndicatorString("Attacking Archon");
//            if (rc.canAttack(nearbyArchon)) {
//                rc.attack(nearbyArchon);
//            } else {
//                Pathing.walkTowards(rc, nearbyArchon);
////                Direction dir = location.directionTo(nearbyArchon);
////                if (rc.canMove(dir)) {
////                    rc.move(dir);
////                }
//            }
//        } else if (nearby.length != 0) {
//            rc.setIndicatorString("Attacking");
//            for (RobotInfo robot : nearby) {
//                if (rc.canAttack(robot.location)) {
//                    rc.attack(robot.location);
//                } else {
//                    Direction toMove = rc.getLocation().directionTo(robot.location);
//                    if (rc.canMove(toMove)) {
//                        rc.move(toMove);
//                    }
//                }
//            }
//        } else {
//            Exploration.explore(rc, location);
//        }
//    }
