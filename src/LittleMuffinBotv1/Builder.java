package LittleMuffinBotv1;
import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;

//7,500 bytecode limit per turn
public strictfp class Builder {
    static MapLocation location;
    static int watchTowers = 0;
    static int laboratories = 0;
    static void run(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        scan(rc);
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        if(watchTowers < 2 || enemyRobots.length > 0){
            createBuilding(rc, RobotType.WATCHTOWER);
            watchTowers++;
        } else {
            createBuilding(rc, RobotType.LABORATORY);
            laboratories++;
        }
        Exploration.explore(rc, rc.getLocation());
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
            MapLocation buildSpot = location.add(dir);
            if (rc.canBuildRobot(robot, dir) && rc.onTheMap(buildSpot)) {
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
}
