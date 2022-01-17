package LittleMuffinBot_V4;

import battlecode.common.*;

/**
    Miner's Attributes:
    - Is the resource gathering unit.
    - Acts a bit like a scout looking for nearby deposits.
    - Mine radius: 2
    - Vision radius: 20
    - Bytecode limit: 7,500
    - Cost: 50 Pb
    Notes:
    - Indices 10-20 will be reserved for mining deposits
    - States need to be implemented
 */

public strictfp class Miner implements Attributes {
    // Robot's Info
    private static MapLocation location;
    private static RobotType type;
    private static int health, ID, level, vision, nearbyMiners;

    private static MapLocation[] lead, gold;
    private static RobotInfo[] enemies, friendlies;

    /**
      * Runs all the methods for the miner.
      * @param rc, current miner.
     */
    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        // If the miner is mining, it doesn't want to move.
        if (!mine(rc)) {
            search(rc);
            Exploration.explore(rc, location, ID);
        }
    }

    /**
      * Initializes all the attributes for this current miner.
      * @param rc, current miner
     */
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
        lead = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        for (MapLocation position : lead) {
            if (rc.senseLead(position) > 6) {
                Communicator.append(rc, position, 10, 20);
            }
        }

        gold = rc.senseNearbyLocationsWithGold(vision);
        for (MapLocation position : gold) {
            Communicator.append(rc, position, 10, 20);
        }

        friendlies = rc.senseNearbyRobots(vision, RobotPlayer.team);
        for (RobotInfo robot: friendlies) {
            if (robot.getType().equals(type)) {
                nearbyMiners++;
            }
        }
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        for (RobotInfo robot : enemies) {
            Communicator.append(rc, robot.getLocation(),30, 40);
        }

    }


    /**
      * The miner attempts to miner given its current surrounds, returns true
      * if its possible else false.
      * @param rc, current miner
      * @return boolean, true or false whether the miner can mine or not
     */
    private static boolean mine(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Mining");
        boolean minerIsMining = false;
        // Check all 4 directions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(location.x + dx, location.y + dy);
                while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
                    minerIsMining = true; rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    minerIsMining = true; rc.mineLead(mineLocation);
                }
            }
        }
        return minerIsMining;
    }

    /**
      * The miner will perform a search using the previous scanned area to see if there
      * are any lead deposits within range.
      * @param rc, current miner
     */
    private static void search(RobotController rc) throws GameActionException {
        MapLocation biggestDeposit = null;
        int maxDep = Integer.MIN_VALUE;
        for (MapLocation deposit : lead) {
            int dep = rc.senseLead(deposit);
            if (dep > maxDep && dep > 6) {
                maxDep = dep;
                biggestDeposit = deposit;
            }
        }
        if (biggestDeposit != null) {
            rc.setIndicatorString("Searching");
            Pathing.walkTowards(rc, biggestDeposit);
        }
    }

    // Method for getting lead count of lead deposits
    private static int getLeadCount(int rangeIndex) {
        switch (rangeIndex) {
            case 0:
            case 1: return 1;
            case 2: return 14;
            case 3: return 27;
            case 4: return 40;
            case 5: return 53;
            case 6: return 66;
            case 7: return 79;
            case 8: return 92;
            case 9: return 105;
            case 10: return 118;
            case 11: return 131;
            case 12: return 144;
            case 13: return 157;
            case 14: return 170;
            case 15: return 183;
            case 16: return 196;
        }
        return -1;
    }
}