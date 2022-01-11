package LittleMuffinBot_V2;

import battlecode.common.*;
import java.util.Random;


/**
    Communicator:
    - Will store MapLocation in the given "open" indexes within the shared array.
    - When the indexes that will be available to each robot will be given within a range.
    - Once we find that every position there is full, a random index will be picked and replaced
    - Bytecode cost (append): 100.
    - Bytecode Cost (get): 1.
 */

public class Communicator {
    public static final int length = 64;
    private static Random rng;
    private static final byte BIT_MASK = (byte)0xff; // lower 8 bits

    /**
      * The robot appends a MapLocation to an index in the array using bit manipulation.
      * @param rc, robot that will be writing to the array
      * @param location, location to be added in the shared array
      * @param start, start of the range (inclusive)
      * @param end, end of the range (exclusive)
     */
    public static void append(RobotController rc, MapLocation location, int start, int end) throws GameActionException {
        if (start >= length || start < 0 || end >= length || end < 0) {
            return;
        }
        location = (location.x == 0 && location.y == 0) ? new MapLocation(1,1) : location;
        int index = traverse(rc, start, end);
        int mask = (location.x << 8) | location.y;
        rc.writeSharedArray(index, mask);
    }

    /**
      * The robot will get a MapLocation from the array and return it for further use.
      * @param rc, robot that will be reading from the array
      * @param index, index in the array that robot will be reading from
      * @return MapLocation, location at that current index bit shifted out
     */
    public static MapLocation get(RobotController rc, int index) throws GameActionException {
        if (index >= length || index < 0) {
            return null;
        }
        int mask = rc.readSharedArray(index);
        //                                  x                              y
        return new MapLocation((byte)((mask >> 8) & BIT_MASK), (byte)(mask & BIT_MASK));
    }

    /**
      * The robot will remove the index passed to the function by resetting it to 0.
      * @param rc, the robot which will reset the given index
      * @param index, the index where the value will be reset to 0
     */
    public static void remove(RobotController rc, int index) throws GameActionException {
        if (index >= length || index < 0) {
            return;
        }
        rc.writeSharedArray(index, 0);
    }

    /**
      * The robot will attempt to find an open position to replace in the shared array, if it can't be
      * found, a position will be randomly selected from the shared array given the range.
      * @param rc, robot looking for an open position in the range
      * @param start, start of the range (inclusive)
      * @param end, end of the range (exclusive)
      * @return, open or index that will be replaced
     */
    public static int traverse(RobotController rc, int start, int end) throws GameActionException {
        for (int i = start; i < end; i++) {
            if (rc.readSharedArray(i) == 0) {
                return i;
            }
        }
        rng = new Random(RobotPlayer.SEED * rc.getID());
        // Random index in boundary [start, end]
        return rng.nextInt((end - start + 1)) + start;
    }

    /**
      * The robot will attempt to get all the communications within the given communication range.
      * @param rc, robot is getting all the locations given the range
      * @param start, start of the range (inclusive)
      * @param end, end of the range (exclusive)
      * @return MapLocation[], array of MapLocations
     */
    public static MapLocation[] getCommunications(RobotController rc, int start, int end) throws GameActionException {
        MapLocation[] locations = new MapLocation[end - start];
        for (int i = 0; i < end - start; i++) {
            locations[i] = get(rc, i + start);
        }
        return locations;
    }
}
