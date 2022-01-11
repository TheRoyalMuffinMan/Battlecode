package LittleMuffinBotv1;

import battlecode.common.*;
import java.util.Random;

public class Exploration {
    private static int changeDirThreshold;
    private static Direction dir;

    public static void explore(RobotController rc, MapLocation src) throws GameActionException {
        Random rng = new Random(RobotPlayer.SEED * rc.getID());
        if (dir == null) {
            int option = rng.nextInt(RobotPlayer.directions.length);
            dir = RobotPlayer.directions[option];
            changeDirThreshold = 0;
        }
        if (changeDirThreshold % 5 == 0) {
            switch(rng.nextInt(3)) {
                case 0: dir = dir.rotateRight(); break;
                case 1: dir = dir.rotateLeft(); break;
            }
        }
        changeDirThreshold++;
        if (!rc.onTheMap(src.add(dir))) {
            Direction diffDir;
            switch (rng.nextInt(4)) {
                case 0:
                    diffDir = dir.rotateLeft().rotateLeft();
                    if (!rc.onTheMap(src.add(diffDir))) {
                        diffDir = dir.rotateRight().rotateRight();
                    }
                    break;
                case 1:
                    diffDir = dir.rotateRight().rotateRight();
                    if (!rc.onTheMap(src.add(diffDir))) {
                        diffDir = dir.rotateLeft().rotateLeft();
                    }
                    break;
                case 2:
                    diffDir = dir.rotateLeft().rotateLeft().rotateLeft();
                    if (!rc.onTheMap(src.add(diffDir))) {
                        diffDir = dir.rotateRight().rotateRight().rotateRight();
                    }
                    break;
                case 3:
                    diffDir = dir.rotateRight().rotateRight();
                    if (!rc.onTheMap(src.add(diffDir))) {
                        diffDir = dir.rotateLeft().rotateLeft().rotateLeft();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + rng.nextInt(2));
            }
            dir = diffDir;
        }
        Pathing.walkTowards(rc, src.add(dir));
    }
}
