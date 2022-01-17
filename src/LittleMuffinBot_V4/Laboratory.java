package LittleMuffinBot_V4;

import battlecode.common.*;

public class Laboratory implements Attributes {
    private static MapLocation location;
    private static RobotType type;
    private static int health, ID, level, vision;

    private static RobotInfo[] enemies;

    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        transmute(rc);
    }

    private static void initialize(RobotController rc) {
        location = rc.getLocation();
        type = rc.getType();
        health = rc.getHealth();
        ID = rc.getID();
        level = rc.getLevel();
        vision = type.visionRadiusSquared;
    }


    /**
     * Performs a scan for nearby lead deposits and stores deposits (>6) in a map.
     * @param rc, scanning with current miner
     */
    private static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, robot.getLocation(),30, 40);
        }
    }

    private static void transmute(RobotController rc) throws GameActionException {
        if (rc.canTransmute() && rc.getTeamLeadAmount(RobotPlayer.team) > 500){
            rc.transmute();
        }
    }

}