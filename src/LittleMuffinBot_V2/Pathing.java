package LittleMuffinBot_V2;

import battlecode.common.*;

public strictfp class Pathing {

    private static final int ACCEPTABLE_RUBBLE = 50;
    // Null if we are not trying to go around a obstacle.
    private static Direction bugDirection = null;
    // Bug 0
    // WARNING: BUG 0 COULD FORM A INFINITE LOOP
    // Note: Might be better to switch to a BFS implementation but bytecode cost could be a issue.
    static void walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        // If cooldown is too high, nothing we can do.
        if (!rc.isMovementReady()) {
            return;
        }
        MapLocation currentLocation = rc.getLocation();
        // At our goal
        if (currentLocation.equals(target)) {
            return;
        }
        Direction dir = currentLocation.directionTo(target);
        if (Clock.getBytecodesLeft() > 50 && rc.canMove(dir) && !isObstacle(rc, dir)) {
            rc.move(dir);
            bugDirection = null;
        } else {
            // There is a obstacle in a way, try to move around it.
            if (bugDirection == null) {
                bugDirection = dir;
            }
            for (int i = 0; i < 8; i++) {
                if (Clock.getBytecodesLeft() > 50 && rc.canMove(bugDirection) && !isObstacle(rc, bugDirection)) {
                    rc.move(bugDirection);
                    // Turn back into the obstacle before ending
                    bugDirection = bugDirection.rotateLeft();
                    break;
                }
                // try to move along the obstacle
                bugDirection = bugDirection.rotateRight();
            }
        }
    }

    private static boolean isObstacle(RobotController rc, Direction dir) {
        MapLocation adjacentLocation = rc.getLocation().add(dir);
        return rc.canSenseRobotAtLocation(adjacentLocation);
    }
}
