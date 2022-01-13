package LittleMuffinBot_V3;
import battlecode.common.*;

public strictfp class WatchTower {

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

    // Watchtower's info
    private static RobotInfo[] enemies;

    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        if (mode.equals(RobotMode.TURRET)) {
           turret(rc);
        }
        if (mode.equals(RobotMode.PORTABLE)) {
            if (enemies.length > 5 && rc.canTransform()) {
                rc.transform(); //switch to turret
                turret(rc);
            } else {
                portable(rc);
            }
        }

    }

    public static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, robot.getLocation(),10, 30);
        }
    }


    /**
     * Initializes all the attributes for this current watchtower.
     * @param rc, current watchtower
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

    private static void turret(RobotController rc) throws GameActionException {
        //could avoid killing all types? //do a priority queue
        for (int i = 0; i < enemies.length; i++) {
            if (enemies[i].type.equals(RobotType.ARCHON)) {
                attack(rc, enemies[i].location);
            }
        }

        //could avoid killing all?
        for (int i = 0; i < enemies.length; i++) {
            attack(rc, enemies[i].location);
        }
        if (rc.canTransform()) {
            rc.transform(); //switch to portable
            portable(rc);
        }
    }

    private static void attack(RobotController rc, MapLocation location) throws GameActionException {
        if (rc.canAttack(location)) {
            rc.attack(location);
        }
    }

    private static void portable(RobotController rc) throws GameActionException {
        Location loc = search(rc);
        if (loc.location == null || loc.index == -1) {
            rc.setIndicatorString("Exploring");
            Exploration.explore(rc, location, ID);
        } else if (enemies.length == 0) {
            if (Pathing.walkTowards(rc, loc.location)) {
                Communicator.remove(rc, loc.index);
            }
        } else {
            if (rc.canTransform()) {
                rc.transform(); //switch to portable
                turret(rc);
            }
        }
    }

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
