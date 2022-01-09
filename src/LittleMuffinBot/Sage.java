package LittleMuffinBot;

import battlecode.common.*;

public class Sage {
    static MapLocation[] surroundings;
    static void dispatcher(RobotController rc) throws GameActionException {
        surroundings = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
    }

    static void BFS(RobotController rc) {

    }

}
