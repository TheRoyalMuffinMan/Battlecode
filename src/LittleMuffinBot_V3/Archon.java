package LittleMuffinBot_V3;

import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;

/**
    Archon's Attributes:
    - Builds robots and can repair nearby robots as long as they are in range.
    - Can transform to portable mode and can transform back to turret mode.
    - One of the Archons will spawn with a lead deposit in its range.
    - Will begin with 1-4 Archons every game.
    - Build radius: 2
    - Action radius: 20
    - Vision radius: 34
    - Bytecode limit: 20,000.
    Notes:
    - 0-9 in the shared array will communication space for the Archons.
    - There will be different states the Archon is currently in.
        - DEFEND: Spawn soldiers when under attack.
        - NORMAL: Build units at a normal rate and repair at a normal rate.
        - BUILD: Build units aggressively.
        - RELOCATE: Run back with the Archon to another location.
 */

public strictfp class Archon {
    // All the states that the Archon can currently have.
    private enum State {
        DEFEND, NORMAL, BUILD
    }

    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static State state;
    private static int health, ID, level, vision;

    // Archon's Info
    private static RobotInfo[] friendlies, enemies;
    private static int miners = 0, soldiers = 0, builders = 0, sages = 0;

    /**
      * Runs all the methods for the Archon.
      * @param rc, current Archon.
     */
    public static void run(RobotController rc) throws GameActionException {
        switch (RobotPlayer.turnCount) {
            case 1: state = State.BUILD; break;
            default: state = State.NORMAL;
        }
        initialize(rc);
        Communicator.append(rc, location, 0, 4);
        scan(rc);
        buildOrder(rc);
    }

    /**
      * Initializes all the attributes for this current Archon.
      * @param rc, current Archon
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
      * Scans for nearby enemies and friendlies currently within the Archon's range.
      * @param rc, current Archon
     */
    private static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(location, vision, RobotPlayer.otherTeam); // Bytecode: 100
        friendlies = rc.senseNearbyRobots(location, vision, RobotPlayer.team);
        if (enemies.length > 0) {
            state = State.DEFEND;
            for (RobotInfo robot : enemies) {
                Communicator.append(rc, robot.getLocation(),20, 30);
            }
        } else {
            state = State.NORMAL;
        }

    }

    /**
      * Current build order for the Archon given the current state of the Archon.
      * @param rc, current Archon
     */
    private static void buildOrder(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Selecting");
        switch (state) {
            case DEFEND:
                if (rc.getTeamGoldAmount(RobotPlayer.team) >= 50) {
                    build(rc, RobotType.SAGE);
                } else {
                    build(rc, RobotType.SOLDIER);
                }
                repair(rc); break;
            case NORMAL:
                if (miners < 6) {
                    build(rc, RobotType.MINER);
                } else if (sages < 3 && rc.getTeamGoldAmount(RobotPlayer.team) >= 50) {
                    build(rc, RobotType.SAGE);
                } else if (soldiers < 3 && rc.getTeamLeadAmount(RobotPlayer.team) >= 225) {
                    build(rc, RobotType.SOLDIER);
                } else if (builders < 3 && rc.getTeamLeadAmount(RobotPlayer.team) >= 120) {
                    build(rc, RobotType.BUILDER);
                } else if (rc.getTeamLeadAmount(RobotPlayer.team) > 500) {
                    miners = soldiers = builders = sages = 0;
                    buildOrder(rc);
                }
                repair(rc); break;
            case BUILD:
                if (miners < 5) {
                    build(rc, RobotType.MINER);
                } else if (sages < 3) {
                    build(rc, RobotType.SAGE);
                } else if (soldiers < 3) {
                    build(rc, RobotType.SOLDIER);
                } else if (builders < 3) {
                    build(rc, RobotType.BUILDER);
                } else if (rc.getTeamLeadAmount(RobotPlayer.team) > 1000) {
                    miners = soldiers = builders = sages = 0;
                    buildOrder(rc);
                }
        }

    }

    /**
      * Builds the current robot passed to the function if given enough resources.
      * @param rc, current Archon
      * @param type, type of the robot to build
     */
    private static void build(RobotController rc, RobotType type) throws GameActionException {
        rc.setIndicatorString("Building");
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, Comparator.comparingInt(a -> getRubble(rc, a)));
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(type, dir)) {
                rc.buildRobot(type, dir);
                switch(type) {
                    case MINER: miners++; break;
                    case SOLDIER: soldiers++; break;
                    case BUILDER: builders++; break;
                    case SAGE: sages++; break;
                }
            }
        }
    }

    /**
      * Repairs the robots that are within range of the Archon.
      * @param rc, current Archon
     */
    private static void repair(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Repairing");
        for (RobotInfo robot : friendlies) {
            if (rc.canRepair(robot.getLocation()) && robot.getHealth() < robot.getType().getMaxHealth(0)) {
                rc.repair(robot.getLocation());
            }
        }
    }

    /**
      * Checks the rubble count at the current adjacent position relative to the robot.
      * @param rc, current robot thats have its adjacent position checked for rubble.
      * @param dir, Direction to check for rubble count
     */
    public static int getRubble(RobotController rc, Direction dir) {
        rc.setIndicatorString("Checking nearby rubble");
        try {
            MapLocation adjacent = rc.getLocation().add(dir);
            if (rc.onTheMap(adjacent)) {
                return rc.senseRubble(adjacent);
            }
            return 10000; // Error when we find a location that is off the map.
        } catch (GameActionException e) {
            e.printStackTrace();
            return 0;
        }
    }
}