package LittleMuffinBot;

import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;

public strictfp class Miner {
    static MapLocation location;
    static int vision;
    static Direction exploreDir = null;
    static Map<MapLocation, Integer> deposits = new HashMap<>();

    // Every turn, we will first scan with the miner then attempt to mine.
    // If we can't mine, we search then explore.
    static void run(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        vision = rc.getType().visionRadiusSquared;
        scanMode(rc);
        if (!mineMode(rc)) {
            searchMode(rc);
            exploreMode(rc);
        }
    }
    // Scan for nearby deposits in the static map we collected from all the robots this turn.
    static void scanMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        MapLocation[] nearby = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        for (MapLocation position : nearby) {
            if (rc.senseLead(position) > 1) {
                deposits.put(position, rc.senseLead(position));
            }
        }
    }

    // This is where the mining is done once we are close to a mining deposit.
    static boolean mineMode(RobotController rc) throws GameActionException {
        // Try to mine around us
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
    static void searchMode(RobotController rc) throws GameActionException {
        MapLocation biggestDeposit = null;
        int maxLead = Integer.MIN_VALUE;
        for (Map.Entry<MapLocation, Integer> deposit : deposits.entrySet()) {
            int lead = deposit.getValue();
            if (lead > maxLead) {
                maxLead = lead;
                biggestDeposit = deposit.getKey();
            }
        }
        // Might need to remove deposit once we are done mining it!!!!! Note: static variables don't persist
        if (biggestDeposit != null) {
            rc.setIndicatorString("Searching");
            Pathing.walkTowards(rc, biggestDeposit);
//            Direction toMove = location.directionTo(biggestDeposit);
//            if (rc.canMove(toMove)) {
//                rc.move(toMove);
//            }
        }
    }

    // Exploring is done here randomizing, we attempt to move in 2 different completely random directions.
    // We catch when we get stuck against the edges of the map.
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
