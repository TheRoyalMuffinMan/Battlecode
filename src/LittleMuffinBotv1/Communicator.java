package LittleMuffinBotv1;

import battlecode.common.*;
import java.util.List;
import java.util.Random;

public class Communicator {
    public static final int length = 64;
    private static Random rng = new Random(RobotPlayer.SEED);

    public static void append(RobotController rc, int start, int end, MapLocation location) throws GameActionException {
        if (start >= length || start < 0 || end >= length || end < 0) {
            return;
        }
        int index = traverse(rc, start, end);
        int mask = (location.x << 8) | location.y;
        rc.writeSharedArray(index, mask);
    }

    public static MapLocation get(RobotController rc, int index) throws GameActionException {
        if (index >= length || index < 0) {
            return null;
        }
        int mask = rc.readSharedArray(index);
        byte BIT_MASK = (byte)0xff;   // low 8 bits
        //                                  x                              y
        return new MapLocation((byte)((mask >> 8) & BIT_MASK), (byte)(mask & BIT_MASK));
    }

    public static void remove(RobotController rc, int index) throws GameActionException {
        if (index >= length || index < 0) {
            return;
        }
        rc.writeSharedArray(0, 0);
    }

    public static int traverse(RobotController rc, int start, int end) throws GameActionException {
        for (int i = start; i < end; i++) {
            if (rc.readSharedArray(i) == 0) {
                return i;
            }
        }
        // Random index
        return rng.nextInt((end - start + 1)) + start;
    }

    public static List<MapLocation> getCommunications(RobotController rc) throws GameActionException {
        return null;
    }
}
