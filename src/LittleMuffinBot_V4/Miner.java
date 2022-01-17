package LittleMuffinBot_V4;

import battlecode.common.*;

import java.util.Map;

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
        if(enemies.length > 0) {
            retreat(rc);
        }
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
        enemies = rc.senseNearbyRobots(location, vision, RobotPlayer.otherTeam);;
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

    private static void retreat(RobotController rc) throws GameActionException{
        System.out.print("retreating");
        rc.setIndicatorString("Retreating");
        int avgX = 0;
        int avgY = 0;
        String direction = "Random";
        int count = 0;

        for(int i = 0; i<enemies.length; i++){
            if(!(enemies[i].type.equals(RobotType.MINER) || enemies[i].type.equals(RobotType.BUILDER)));
            {
                avgX += enemies[i].location.x;
                avgY += enemies[i].location.y;
                count++;
            }
        }
        if(count > 0) {
            avgX = avgX / enemies.length;
            avgY = avgY / enemies.length;

            int x = avgX - location.x;
            int y = avgY - location.y;
            MapLocation loc;
            if (Math.abs(x) >= Math.abs((y * 2)) || y == 0) { //if x is substantially larger than y or if y = 0, only move left/right
                if (x > 0) {
                    direction = "west";
                    loc = new MapLocation(0, location.y); //move left (West)
                    Pathing.walkTowards(rc, loc);
                } else {
                    direction = "east";
                    loc = new MapLocation(rc.getMapWidth(), location.y); //move right (East)
                    Pathing.walkTowards(rc, loc);
                }
            } else if (Math.abs(y) >= Math.abs((x * 2)) || x == 0) { //if y is substanitally larger than x or if x = 0, only move up/down
                if (y > 0) {
                    direction = "south";
                    loc = new MapLocation(location.x, 0); //move down (south)
                    Pathing.walkTowards(rc, loc);
                } else {
                    direction = "north";
                    loc = new MapLocation(location.x, rc.getMapHeight()); //move up (north)
                    Pathing.walkTowards(rc, loc);
                }
            } else {
                if (y > 0 && x > 0) {
                    direction = "southwest";
                    loc = new MapLocation(0, 0); //move southwest (left and down)
                    Pathing.walkTowards(rc, loc);
                } else if (y > 0 && x < 0) {
                    direction = "southeast";
                    loc = new MapLocation(rc.getMapWidth(), 0); //move southeast (right and down)
                    Pathing.walkTowards(rc, loc);
                } else if (y < 0 && x > 0) {
                    direction = "northwest";
                    loc = new MapLocation(0, rc.getMapHeight()); //move northwest (left and up)
                    Pathing.walkTowards(rc, loc);
                } else if (y < 0 && x < 0) {
                    direction = "northeast";
                    loc = new MapLocation(rc.getMapWidth(), rc.getMapHeight()); //move northeast (right and up)
                    Pathing.walkTowards(rc, loc);
                } else {
                    direction = "away from closest archon";
                    int closest = Integer.MAX_VALUE; //difference between closest archon and miner. found by summing deltax and deltay of archon
                    MapLocation closestArchon = new MapLocation(closest, closest);  //the closest archon
                    for (int i = 0; i < rc.getArchonCount(); i++) {        //determining which archon is the closest to run away from it
                        int read = rc.readSharedArray(i);
                        int archonX = read >> 8;
                        int archonY = read & 0xFF;
                        int deltaX = Math.abs(archonX - rc.getLocation().x);
                        int deltaY = Math.abs(archonY - rc.getLocation().y);
                        if (deltaX + deltaY < closest) {
                            closest = deltaX + deltaY;
                            closestArchon = new MapLocation(-archonX, -archonY);
                        }
                    }
                    Pathing.walkTowards(rc, closestArchon);
                }

            }
        }
        System.out.println("retreat " + direction);

    }
}