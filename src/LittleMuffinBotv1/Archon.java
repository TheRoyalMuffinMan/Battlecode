package LittleMuffinBotv1;

import battlecode.common.*;
import java.util.Arrays;
import java.util.Comparator;

/*
    - Can build droids.
    - Can repair nearby droids.
    - Can move, but takes 10 turns to transform to 'Portable' mode, and takes 10 turns to transform back to 'Turret' mode.
    - At least one of the Archons will initially have a Lead deposit within its vision range.
    - Will begin with at least 1-4 Archons: rc.getArchonCount().
    - Bytecode limit: 20,000.
    Notes:
    - 0-9 in the shared array will communication space for the Archons.
    - There will be different states the Archon is currently in.
        - DEFEND: Spawn soldiers when under attack.
        - NORMAL: Build units at a normal rate and repair at a normal rate.
        - BUILD: Build units aggressively.
        - RELOCATE: Run back with the Archon to another location.
    - We will assume no Archons spawn at (0, 0) for now, location is stored in indexes 0-3.

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

    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        if (location.x == 0 && location.y == 0) {
            Communicator.append(rc, 0, 4, new MapLocation(1, 1));
        } else {
            Communicator.append(rc, 0, 4, location);
        }
        if (RobotPlayer.turnCount == 1) {
            state = State.BUILD;
        } else {
            state = State.NORMAL;
        }
        scan(rc);
        buildOrder(rc);
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

    // Scan for the nearby deposits to the Archon.
    private static void scan(RobotController rc) {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(location, vision, RobotPlayer.otherTeam); // Bytecode: 100
        friendlies = rc.senseNearbyRobots(location, vision, RobotPlayer.team);
        if (enemies.length > 0) {
            state = State.DEFEND;
        } else {
            state = State.NORMAL;
        }
    }

    // The order we build in right now, due to change in the future.
    private static void buildOrder(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Building");
        switch (state) {
            case DEFEND:
                build(rc, RobotType.SOLDIER); repair(rc); break;
            case NORMAL:
                if (miners < 6) {
                    build(rc, RobotType.MINER);
                } else if (soldiers < 3 && rc.getTeamLeadAmount(RobotPlayer.team) >= 225) {
                    build(rc, RobotType.SOLDIER);
                } else if (builders < 3 && rc.getTeamLeadAmount(RobotPlayer.team) >= 120) {
                    build(rc, RobotType.BUILDER);
                } else if (rc.getTeamLeadAmount(RobotPlayer.team) > 500) {
                    miners = soldiers = builders = 0;
                    buildOrder(rc);
                }
                repair(rc); break;
            case BUILD:
                if (miners < 5) {
                    build(rc, RobotType.MINER);
                } else if (soldiers < 3) {
                    build(rc, RobotType.SOLDIER);
                } else if (builders < 3) {
                    build(rc, RobotType.BUILDER);
                } else if (rc.getTeamLeadAmount(RobotPlayer.team) > 1000) {
                    miners = soldiers = builders = 0;
                    buildOrder(rc);
                }
        }

    }

    // Build the robot at the position that has the least rubble around it.
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

    private static void repair(RobotController rc) throws GameActionException {
        for (RobotInfo robot : friendlies) {
            if (rc.canRepair(robot.getLocation())) {
                rc.repair(robot.getLocation());
            }
        }
    }

    // Get all the nearby rubble to the Archon using the following method
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