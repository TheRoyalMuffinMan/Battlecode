package LittleMuffinBot_V4;

import battlecode.common.*;
import java.util.Random;

/**
    Exploration:
    - The robot will attempt to move in a random direction.
    - After a couple moves, the threshold will be reached and a new direction will be selected.
    - This will be selected once the threshold is mod of the random number given the seed.
 */

public class Exploration {
    private static int changeDirThreshold;
    private static Direction dir;

    /**
      * The robot will try to find a random direction to move in, once that direction
      * is found. It will path towards that direction.
      * @param rc, current robot choosing a direction to move
      * @param location, current robot's location
      * @param ID, robot's current ID
     */
    public static void explore(RobotController rc, MapLocation location, int ID) throws GameActionException {
        Random rng = new Random(RobotPlayer.SEED * ID);
        if (dir == null) {
            int option = rng.nextInt(RobotPlayer.directions.length);
            dir = RobotPlayer.directions[option];
            changeDirThreshold = 0;
        }
        // Choose a new direction to move in, Random number range: [2, 8]
        if (changeDirThreshold % (rng.nextInt((8 - 2 + 1)) + 2) == 0) {
            switch(rng.nextInt(3)) {
                case 0: dir = dir.rotateRight(); break;
                case 1: dir = dir.rotateLeft(); break;
            }
        }
        changeDirThreshold++;

        // Against the map boundary, move off it.
        if (!rc.onTheMap(location.add(dir))) {
            Direction diffDir;
            int choice = rng.nextInt(2);
            if (choice == 1) {
                diffDir = dir.rotateLeft().rotateLeft();
                if (!rc.onTheMap(location.add(diffDir))) {
                    diffDir = dir.rotateRight().rotateRight();
                }
            } else {
                diffDir = dir.rotateRight().rotateRight();
                if (!rc.onTheMap(location.add(diffDir))) {
                    diffDir = dir.rotateLeft().rotateLeft();
                }
            }
            dir = diffDir;
        }
        Pathing.walkTowards(rc, location.add(dir));
    }
}
