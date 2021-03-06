package LittleMuffinBot;
import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

//7,500 bytecode limit per turn
public strictfp class Builder {

    static Direction exploreDir = null;
    static int watchTowers = 0;
    static int laboratories = 0;
    static void builderDispatcher(RobotController rc) throws GameActionException {
        scan(rc);
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        if(watchTowers < 2 || enemyRobots.length > 0){
            createBuilding(rc, RobotType.WATCHTOWER);
            watchTowers++;
        } else {
            createBuilding(rc, RobotType.LABORATORY);
            laboratories++;
        }
        Exploration.run(rc);
        enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());

    }

    static void scan(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
        for (int i = 0; i< robots.length; i++) {
            if ((robots[i].getMode().equals(RobotMode.PROTOTYPE) || robots[i].health <
               (robots[i].getType().health / 2))) {
                Pathing.walkTowards(rc, robots[i].location);
                repair(rc, robots[i].location);
            }
        }
    }
    static void createBuilding(RobotController rc, RobotType robot) throws GameActionException {
        Direction[] dirs = Arrays.copyOf(RobotPlayer.directions, RobotPlayer.directions.length);
        Arrays.sort(dirs, Comparator.comparingInt(a -> Archon.getRubble(rc, a)));
        for (Direction dir : dirs) {
            if (rc.canBuildRobot(robot, dir) && rc.senseRubble(rc.getLocation()) < 50) {
                rc.setIndicatorString("Building");
                rc.buildRobot(robot, dir);
            }
        }
    }

    static void repair(RobotController rc, MapLocation loc) throws GameActionException {
        if (rc.canRepair(loc)) {
            rc.repair(loc);
        }
    }

    static void mutateBuilding(RobotController rc, MapLocation loc) throws GameActionException {
        rc.mutate(loc);
    }

    static void exploreMode(RobotController rc) throws GameActionException {
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
}
