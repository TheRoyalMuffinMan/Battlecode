package LittleMuffinBot_V4;
import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
    Builder's Attributes:
    - Is the building unit.
    - Has the abilites to build, repair, and mutate buildings (Archon, Watchtower, Laboratories)
    - Build radius: 2
    - Repair radius: 5
    - Vision radius: 20
    - Bytecode limit: 7,500
    - Cost: 50 Pb
    Notes:
    - States need to be implemented
 */
public strictfp class Builder extends Archon implements Attributes {
    // Robot's Info
    private static MapLocation location;
    private static RobotType type;
    private static int health, ID, level, vision;

    // Builder's Info
    private static RobotInfo[] enemies;
    private static int watchTowers = 0, laboratories = 0;

    /**
      * Runs all the methods for the builder.
      * @param rc, current builder.
     */
    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        Random rng = new Random(RobotPlayer.SEED * ID);
        switch (rng.nextInt(2)) {
            case 0: build(rc, RobotType.WATCHTOWER); watchTowers++; break;
            default: build(rc, RobotType.LABORATORY); laboratories++;
        }
        rc.setIndicatorString("Exploring");
        Exploration.explore(rc, location, ID);
    }

    /**
      * Initializes all the attributes for this current builder.
      * @param rc, current builder
     */
    private static void initialize(RobotController rc) {
        location = rc.getLocation();
        type = rc.getType();
        health = rc.getHealth();
        ID = rc.getID();
        level = rc.getLevel();
        vision = type.visionRadiusSquared;
    }

    /**
      * Scans for nearby buildings in prototype mode or buildings below half health and attempts to repair them.
      * @param rc, current builder
     */
    private static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        RobotInfo[] robots = rc.senseNearbyRobots(vision, RobotPlayer.team);
        for (int i = 0; i < robots.length; i++) {
            if (robots[i].getMode().equals(RobotMode.PROTOTYPE) || robots[i].health < (robots[i].getType().health / 2)) {
                Pathing.walkTowards(rc, robots[i].location);
                repair(rc, robots[i].location);
            }
        }

        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, robot.getLocation(),30, 40);
        }

    }

    /**
      * Builds the current building passed to the function if given enough resources.
      * @param rc, current builder
      * @param type, type of the robot to build
     */
    private static void build(RobotController rc, RobotType type) throws GameActionException {
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, Comparator.comparingInt(a -> getRubble(rc, a)));
        for (Direction dir : dirs) {
            MapLocation buildSpot = location.add(dir);
            if (rc.canBuildRobot(type, dir) && rc.onTheMap(buildSpot)) {
                rc.setIndicatorString("Building");
                rc.buildRobot(type, dir);
            }
        }
    }

    /**
      * Repairs at the location of the injured or unfinished building.
      * @param rc, current builder
      * @param loc, location of the building to repair
     */
    private static void repair(RobotController rc, MapLocation loc) throws GameActionException {
        if (rc.canRepair(loc)) {
            rc.setIndicatorString("Repairing");
            rc.repair(loc);
        }
    }

}
/*      Retreat code:
        rc.setIndicatorString("Retreating");
        Direction[] directions = RobotPlayer.directions;
        MapLocation farthest = null;
        int distance = Integer.MIN_VALUE;
        for (Direction dir : directions) {
            MapLocation newPosition = location.add(dir);
            int distanceTo = newPosition.distanceSquaredTo(closestEnemy);
            if (distanceTo > distance) {
                farthest = newPosition;
                distance = distanceTo;
            }
        }
 */