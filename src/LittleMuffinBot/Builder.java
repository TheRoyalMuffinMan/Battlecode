package LittleMuffinBot;
import battlecode.common.*;
//7,500 bytecode limit per turn
public strictfp class Builder {

    static Direction exploreDir = null;

    static void builderDispatcher(RobotController rc) throws GameActionException {

            createBuilding(rc, RobotType.WATCHTOWER, Direction.CENTER);

            createBuilding(rc, RobotType.LABORATORY, Direction.CENTER);
            //repairing nearby robots
            RobotInfo [] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam());
            for(int i = 0; i< robots.length; i++){
                if(robots[i].getHealth() < 100 && rc.canRepair(robots[i].location)){ //if full health is 130?
                    rc.move(rc.getLocation().directionTo(robots[i].location));
                    rc.repair(robots[i].location);
                }
            }

            exploreMode(rc);
        //exploration
    }
    static void createBuilding(RobotController rc, RobotType robot, Direction dir) throws GameActionException {
        if(rc.canBuildRobot(robot, dir) && rc.senseRubble(rc.getLocation()) < 50) {
            rc.buildRobot(robot, dir);
            repairBuilding(rc, rc.getLocation());
            mutateBuilding(rc, rc.getLocation());
        }
    }

    static void repairBuilding(RobotController rc, MapLocation loc) throws GameActionException {
            rc.repair(loc);
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
