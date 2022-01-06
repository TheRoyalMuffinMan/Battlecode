package LittleMuffinBot;

import battlecode.common.*;
import java.util.Map;
import static LittleMuffinBot.RobotPlayer.deposits;

public strictfp class Miner {
    static MapLocation location;
    static Direction exploreDir = null;

    // Every turn, we will first scan with the miner then attempt to mine.
    // If we can't mine, we search then explore.
    static void dispatcher(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        scanMode(rc);
        if (!mineMode(rc)) {
            searchMode(rc);
            exploreMode(rc);
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
            Direction toMove = location.directionTo(biggestDeposit);
            if (rc.canMove(toMove)) {
                rc.move(toMove);
            }
        }
    }

    // Exploring is done here randoming, we attempt to move in 2 different completely random directions.
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
            System.out.println("I moved!");
        }
    }
    // Scan for nearby deposits in the static map we collected from all the robots this turn.
    static void scanMode(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        int vision = rc.getType().visionRadiusSquared;
        MapLocation[] nearby = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        for (MapLocation position : nearby) {
            if (rc.senseLead(position) > 10) {
                deposits.put(position, rc.senseLead(position));
            }
        }
    }
}

// Extra Code
// Scan for nearby deposits
//        int vision = rc.getType().visionRadiusSquared;
//        MapLocation[] nearby = rc.getAllLocationsWithinRadiusSquared(me, vision);
//
//        // Find the shortest deposit at the given robot
//        MapLocation deposit = null;
//        int distanceToDeposit = Integer.MAX_VALUE;
//        for (MapLocation position : nearby) {
//            if (rc.senseLead(position) > 0 || rc.senseGold(position) > 0) {
//                int distance = me.distanceSquaredTo(position);
//                if (distance < distanceToDeposit) {
//                    distanceToDeposit = distance;
//                    deposit = position;
//                    break;
//                }
//            }
//        }
//        // If the miner is mining, we shouldn\t move him
//        if (!minerIsMining) {
//            // Might need to remove deposit once we are done mining it!!!!! Note: static variables don't persist
//            MapLocation deposit = searchForResources(deposits);
//            if (deposit != null) {
////              System.out.println("Miner moving!!");
//                Direction toMove = me.directionTo(deposit);
//                if (rc.canMove(toMove)) {
//                    rc.move(toMove);
//                }
//            }
//            explore(rc);
//        }