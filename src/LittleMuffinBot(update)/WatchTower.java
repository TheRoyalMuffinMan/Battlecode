package LittleMuffinBot;
import battlecode.common.*;
public strictfp class WatchTower {
    static Direction exploreDir = null;
    static void watchDispatcher(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        if(rc.getMode().equals(RobotMode.TURRET)) {
           turretMode(rc, robots);
        }
        if(rc.getMode().equals(RobotMode.PORTABLE)) {
            if (robots.length > 5 && rc.canTransform()) {
                rc.transform(); //switch to turret
                turretMode(rc, robots);
            } else {
                portableMode(rc);
            }
        }

    }
    static void turretMode(RobotController rc, RobotInfo [] robots) throws GameActionException {

        for(int i = 0; i< robots.length; i++){ //could avoid killing all types? //do a priority queue
            if(robots[i].type.equals(RobotType.ARCHON)){
               watchAttack(rc, robots[i], robots[i].location);
            }
        }
        robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        for(int i = 0; i< robots.length; i++){ //could avoid killing all?
            watchAttack(rc, robots[i], robots[i].location);

        }
        if (rc.canTransform()) {
            rc.transform(); //switch to portable
            portableMode(rc);
        }
    }

    static void watchAttack(RobotController rc, RobotInfo robot, MapLocation location) throws GameActionException {
        while(robot.health > 0){
            if(rc.canAttack(location)){
                rc.attack(location);
            } else {
                break;
            }
        }
    }

    static void portableMode(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        while(robots.length <= 5){
            exploreMode(rc);
            robots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        }
        if(robots.length > 5 && rc.canTransform()){
            rc.transform(); //switch to turret
            turretMode(rc, robots);
        }
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
