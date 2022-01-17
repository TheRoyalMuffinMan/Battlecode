package LittleMuffinBot_V4;

import battlecode.common.*;

/**
    Soldier's Attributes:
    - Main attacking force.
    - Decent attack and scout range.
    - Action radius: 13
    - Vision radius: 20
    - Bytecode limit: 10,000.
    - Cost: 75 Pb
    Notes:
    - 10-30 in the shared array will communication space for the soldiers.
    - States need to be implemented
 */


public class Soldier implements Attributes {
    /**
        Location Attributes:
        - Is a location on the map with a given index correlating to the shared array.
    */
    public static class Location {
        MapLocation location;
        int index;
        public Location(MapLocation location, int index) {
            this.location = location;
            this.index = index;
        }
    }

    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static int health, ID, level, vision;

    // Soldier's info
    private static RobotInfo[] enemies;
    private static MapLocation closestEnemy = null;

    /**
      * Runs all the methods for the soldier.
      * @param rc, current soldier.
     */
    static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        move(rc);
    }

    /**
      * Initializes all the attributes for this current soldier.
      * @param rc, current soldier
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
      * @param rc, current soldier
     */
    private static void scan(RobotController rc) throws GameActionException {
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
      * @param rc, current soldier
     */
    private static void move(RobotController rc) throws GameActionException {
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
      * Searches to find the closest enemy given the current location of the soldier.
      * @param rc, current soldier
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
//        rc.setIndicatorString("Attacking");
//        rc.attack(loc.location);
//    } else {
//        rc.setIndicatorString("Pathing To");
//        if (Pathing.walkTowards(rc, loc.location)) {
//            Communicator.remove(rc, loc.index);
//        }
//    }