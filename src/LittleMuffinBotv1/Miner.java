package LittleMuffinBotv1;

import battlecode.common.*;
import java.util.HashMap;
import java.util.Map;

/*
    - Attack Radius: 13
    - Bytecode limit: 10,000
    - Decent scout and foot soldiers
    - Bytecode limit: 10,000
    Notes:
    - 10-19 in the shared array will communication space for the soldiers.
    - There will be different states the Soldier is currently in.
        - ATTACK: Attack nearby troops.
        - SCOUT: Scout for nearby troops.
        - PURSUE: Purse retreating nearby troops
        - RETREAT: Retreat from nearby enemy troops
 */

public strictfp class Miner {
    public enum State {
        MINE, EXPLORING, RETREATING
    }
    // Robot's Info
    private static MapLocation location;
    private static RobotMode mode;
    private static RobotType type;
    private static State state;
    private static int health, ID, level, vision;

    private static RobotInfo[] friendlies, enemies;
    private static Map<MapLocation, Integer> deposits = new HashMap<>();

    // Every turn, we will first scan with the miner then attempt to mine.
    // If we can't mine, we search then explore.
    public static void run(RobotController rc) throws GameActionException {
        initialize(rc);
        scan(rc);
        if (!mine(rc)) {
            search(rc);
            Exploration.explore(rc, location);
        }
    }

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
    private static void scan(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        MapLocation[] gold = rc.senseNearbyLocationsWithGold(vision);
        MapLocation[] lead = rc.senseNearbyLocationsWithLead(vision); // Bytecode: 100
        friendlies = rc.senseNearbyRobots(vision, RobotPlayer.team);
        enemies = rc.senseNearbyRobots(vision, RobotPlayer.otherTeam);
        for (MapLocation position : gold) {
            if (rc.senseGold(position) > 6) {
                deposits.put(position, rc.senseLead(position));
            }
        }
        for (MapLocation position : lead) {
            if (rc.senseLead(position) > 6) {
                deposits.put(position, rc.senseLead(position));
            }
        }
    }

    // This is where the mining is done once we are close to a mining deposit.
    private static boolean mine(RobotController rc) throws GameActionException {
        // Try to mine around us
        rc.setIndicatorString("Mining");
        boolean minerIsMining = false;
        // Check all 4 directions
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation mineLocation = new MapLocation(location.x + dx, location.y + dy);
                while (rc.canMineGold(mineLocation) && rc.senseGold(mineLocation) > 1) {
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
    private static void search(RobotController rc) throws GameActionException {
        MapLocation biggestDeposit = null;
        int maxDep = Integer.MIN_VALUE;
        for (Map.Entry<MapLocation, Integer> deposit : deposits.entrySet()) {
            int dep = deposit.getValue();
            if (dep > maxDep) {
                maxDep = dep;
                biggestDeposit = deposit.getKey();
            }
        }
        if (biggestDeposit != null) {
            rc.setIndicatorString("Searching");
            Pathing.walkTowards(rc, biggestDeposit);
        }
    }
}