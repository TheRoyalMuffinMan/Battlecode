package LittleMuffinBot_V4;
import battlecode.common.*;


public strictfp class WatchTower extends Soldier implements Attributes {

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
            Communicator.append(rc, robot.getLocation(),20, 40);
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
        rc.setIndicatorString("Turret Mode");
        boolean canAttack = false;
        for (RobotInfo enemy : enemies) {
            if (rc.canAttack(enemy.location)) {
                rc.attack(enemy.location);
                canAttack = true;
            }
        }
        if (!canAttack && rc.canTransform()) {
            rc.transform();
            portable(rc);
        }
    }

    private static void portable(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Portable Mode");
        Location loc = search(rc);
        if (loc.location == null || loc.index == -1) {
            rc.setIndicatorString("Exploring");
            Exploration.explore(rc, location, ID);
        } else {
            if (location.distanceSquaredTo(loc.location) < 15) {
                rc.setIndicatorString("Attacking");
                if (rc.canTransform()) {
                    rc.transform();
                    turret(rc);
                }
            } else {
                rc.setIndicatorString("Pathing To");
                if (Pathing.walkTowards(rc, loc.location)) {
                    Communicator.remove(rc, loc.index);
                }
            }
        }

    }

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