package LittleMuffinBot_V3;

import battlecode.common.*;

/**
    Soldier's Attributes:
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


public class Sage {
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

    // Sage's info
    private static RobotInfo[] enemies;

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
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, robot.getLocation(),10, 30);
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
            if (rc.canAttack(loc.location)){
                rc.setIndicatorString("Attacking");
                rc.attack(loc.location);
            } else if (rc.isActionReady()) {
                rc.setIndicatorString("Pathing To");
                if (Pathing.walkTowards(rc, loc.location)) {
                    Communicator.remove(rc, loc.index);
                }
            } else {
                // Retreat back to the Archon once an action can't be done.
                Pathing.walkTowards(rc, Communicator.get(rc, 0));
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
        MapLocation[] locations = Communicator.getCommunications(rc, 10, 30);
        MapLocation closest = null;
        int distance = Integer.MAX_VALUE, index = -1, offset = 10;
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
