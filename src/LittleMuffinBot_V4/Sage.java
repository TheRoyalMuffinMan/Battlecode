package LittleMuffinBot_V4;

import battlecode.common.*;

/**
    Sage's Attributes:
    - Decent attacking force and has magic hands.
    - Very high cooldown for actions.
    - Action radius: 13
    - Vision radius: 20
    - Bytecode limit: 10,000
    = Cost: 50 Au
    Notes:
    - 10-30 in the shared array will communication space for the soldiers.
    - States need to be implemented
 */


public class Sage extends Soldier implements Attributes {

    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static int health, ID, level, vision;

    // Sage's info
    private static RobotInfo[] enemies;
    private static MapLocation closestEnemy;

    /**
     * Runs all the methods for the sage.
     * @param rc, current sage.
     */
    static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        attack(rc);
    }

    /**
     * Initializes all the attributes for this current sage.
     * @param rc, current sage
     */
    private static void initialize(RobotController rc) {
        location = rc.getLocation();
        mode = rc.getMode();
        type = rc.getType();
        health = rc.getHealth();
        ID = rc.getID();
        level = rc.getLevel();
        vision = type.visionRadiusSquared;
    }

    /**
      * Scans for nearby enemy troops then appends their location to the shared array.
      * @param rc, current sage
     */
    static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        int distance = Integer.MAX_VALUE;
        for (RobotInfo robot : enemies) {
            int distanceTo = location.distanceSquaredTo(robot.location);
            if (distanceTo < distance) {
                distance = distanceTo;
                closestEnemy = robot.location;
            }
            Communicator.append(rc, robot.getLocation(),20, 40);
        }
    }

    /**
      * Attempts to find the closest enemy than path to it looking to attack if possible
      * within its given turn.
      * @param rc, current sage
     */
    private static void attack(RobotController rc) throws GameActionException {
        Location loc = search(rc);
        if (loc.location == null || loc.index == -1) {
            rc.setIndicatorString("Exploring");
            Exploration.explore(rc, location, ID);
        } else {
            if (closestEnemy != null && location.distanceSquaredTo(closestEnemy) < 15){
                rc.setIndicatorString("In range");
                if (rc.canAttack(closestEnemy)) {
                    rc.attack(closestEnemy);
                }
                closestEnemy = null;
            } else if (closestEnemy != null) {
                rc.setIndicatorString("Pathing To Enemy");
                Pathing.walkTowards(rc, closestEnemy);
                closestEnemy = null;
            } else {
                rc.setIndicatorString("Pathing To");
                Pathing.walkTowards(rc, loc.location);
                Communicator.remove(rc, loc.index);
            }
        }
    }

    /**
      * Searches to find the closest enemy given the current location of the sage.
      * @param rc, current sage
      * @return Location, the closest location and its index stored in an object
     */
    private static Location search(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Searching");
        MapLocation[] locations = Communicator.getCommunications(rc, 20, 40);
        MapLocation closest = null;
        int distance = Integer.MAX_VALUE, index = -1, offset = 20;
        for (int i = 0; i < locations.length; i++) {
            MapLocation otherLocation = locations[i];
            int distanceTo = location.distanceSquaredTo(otherLocation);
            if (distanceTo < distance && !otherLocation.equals(new MapLocation(0, 0))) {
                closest = otherLocation;
                distance = distanceTo;
                index = i + offset;
            }
        }
        return new Location(closest, index);
    }

}
//            if (rc.canAttack(loc.location)){
//                    rc.setIndicatorString("Attacking");
//                    rc.attack(loc.location);
//                    } else if (rc.isActionReady()) {
//                    rc.setIndicatorString("Pathing To");
//                    if (Pathing.walkTowards(rc, loc.location)) {
//                    Communicator.remove(rc, loc.index);
//                    }
//                    } else {
//                    // Retreat back to the Archon once an action can't be done.
//                    Pathing.walkTowards(rc, Communicator.get(rc, 0));
//                    }