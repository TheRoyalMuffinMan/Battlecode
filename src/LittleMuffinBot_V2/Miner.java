package LittleMuffinBot_V2;

import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;

/**
    Miner's Attributes:
    - Is the resource gathering unit.
    - Acts a bit like a scout looking for nearby deposits.
    - Mine radius: 2
    - Vision radius: 20
    - Bytecode limit: 7,500
    Notes:
    - States need to be implemented
 */

public strictfp class Miner {
    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static int health, ID, level, vision;

    private static Map<MapLocation, Integer> deposits = new HashMap<>();

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
            Exploration.explore(rc, location);
        }
    }

    /**
      * Initializes all the attributes for this current miner.
      * @param rc, current miner
     */
    private static void initialize(RobotController rc) {
        location = rc.getLocation();
        mode = rc.getMode();
        type = rc.getType();
        health = rc.getHealth();
        ID = rc.getID();
        level = rc.getLevel();
        vision = type.visionRadiusSquared;
    }

    // Scan for nearby deposits in the static map we collected from all the robots this turn.

    /**
      * Performs a scan for nearby lead deposits and stores deposits (>6) in a map.
      * @param rc, scanning with current miner
     */
    private static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        MapLocation[] lead = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        for (MapLocation position : lead) {
            if (rc.senseLead(position) > 6) {
                deposits.put(position, rc.senseLead(position));
            }
        }
    }

    // This is where the mining is done once we are close to a mining deposit.

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
                while (rc.canMineGold(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    minerIsMining = true;
                    rc.mineGold(mineLocation);
                }
                while (rc.canMineLead(mineLocation) && rc.senseLead(mineLocation) > 1) {
                    minerIsMining = true;
                    rc.mineLead(mineLocation);
                }
            }
        }
        return minerIsMining;
    }

    // Searching is done here, we search for the highest deposit and start going toward it.

    /**
      * The miner will perform a search using the previous scanned area to see if there
      * are any lead deposits within range.
      * @param rc, current miner
      * @throws GameActionException
     */
    private static void search(RobotController rc) throws GameActionException {
        MapLocation biggestDeposit = null;
        int maxLead = Integer.MIN_VALUE;
        for (Map.Entry<MapLocation, Integer> deposit : deposits.entrySet()) {
            int lead = deposit.getValue();
            if (lead > maxLead) {
                maxLead = lead;
                biggestDeposit = deposit.getKey();
            }
        }
        if (biggestDeposit != null) {
            rc.setIndicatorString("Searching");
            Pathing.walkTowards(rc, biggestDeposit);
        }
    }
}