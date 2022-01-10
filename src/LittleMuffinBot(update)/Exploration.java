package LittleMuffinBot;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public strictfp class Exploration {
    static MapLocation location;
    static int height = 0, width = 0;
    static List<MapLocation> locations = new ArrayList<>();
    static Set<MapLocation> visited = new HashSet<>();
    static int heightOffset = 0, widthOffset = 0;

    static void run(RobotController rc) throws GameActionException {
        if(rc.getType().equals(RobotType.MINER)) {
            location = rc.getLocation();
            height = rc.getMapHeight();
            width = rc.getMapWidth();
            heightOffset = height / 4;
            widthOffset = width / 4;
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    locations.add(new MapLocation(i * widthOffset, j * heightOffset));
                }
            }
            travel(rc);
        } else {
            int index = 2;
            int max = 1 + rc.getArchonCount();
            while(index < max){
                int readLoc = rc.readSharedArray(index);
                if(readLoc > 0){
                    int readX = readLoc & 0xFF;
                    int readY = readLoc >> 8;
                    locations.add(new MapLocation(readX, readY));
                }
            }
        }
    }
    static void travel(RobotController rc) throws GameActionException {
        MapLocation closest = null;
        if (visited.size() >= locations.size()) {
            visited.clear();
        }
        int distance = Integer.MAX_VALUE;
        for (MapLocation position : locations) {
            if (visited.contains(position)) {
                continue;
            }
            if (location.distanceSquaredTo(position) < distance) {
                closest = position;
                distance = location.distanceSquaredTo(position);
            }
        }
        Pathing.walkTowards(rc, closest);
    }
}
