package LittleMuffinBot;

import battlecode.common.*;

public strictfp class Sage {
    static void sageDispatcher(RobotController rc) throws GameActionException {
        int droid = 0;
        int turretBuilding = 0;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent()); //did action instead of vision
        for(int i = 0; i<robots.length; i++){
            if(robots[i].type.equals(RobotType.ARCHON)){
                attack(rc, robots[i], robots[i].location);
            } else if (robots[i].getType().equals(RobotType.SOLDIER) || robots[i].getType().equals(RobotType.BUILDER) || robots[i].getType().equals(RobotType.MINER)){ //could limit to just soldiers, or count miners if we have miners nearby to mine their lead
                droid++;
            } else if(robots[i].getMode().equals(RobotMode.TURRET)){
                turretBuilding++;
            }
        }
        if(droid > 100){
            anomaly(rc,AnomalyType.CHARGE);
        } else if (turretBuilding > 50){
            anomaly(rc,AnomalyType.FURY);
        }
    }

    static void attack(RobotController rc, RobotInfo robot, MapLocation location) throws GameActionException {
        while(robot.health > 0){
            if(rc.canAttack(location)){
                rc.attack(location);
            } else {
                break;
            }
        }
    }

    static void anomaly(RobotController rc, AnomalyType anom) throws GameActionException {
        if(rc.canEnvision(anom)){
            rc.envision(anom);
        }
    }


}
