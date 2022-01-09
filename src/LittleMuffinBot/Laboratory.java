package LittleMuffinBot;

import battlecode.common.*;
import java.util.List;
import java.util.Map;

public class Laboratory {
    static MapLocation location;
    static int vision;
    static int nearbyFriendlies = 0, nearbyEnemies = 0;

    static void run(RobotController rc) {
        location = rc.getLocation();
        vision = rc.getType().visionRadiusSquared;
    }

    // Stay away from other bots
    static void scan(RobotController rc) {
        rc.setIndicatorString("Scanning");
        RobotInfo[] robots = rc.senseNearbyRobots(); // Bytecode: 100
        Team otherTeam = rc.getTeam().opponent();
        nearbyFriendlies = nearbyEnemies = 0;
        for (RobotInfo robot : robots) {
            if (robot.getTeam() == otherTeam) {
                nearbyEnemies++;
            } else {
                nearbyFriendlies++;
            }
        }
    }

    static void transmute(RobotController rc) {
        // Needs to be implemented
    }

    static void avoid(RobotController rc) {
        // Needs to be implemented
    }
}
