package LittleMuffinBot_V4;

import battlecode.common.*;

/**
    Pathing:
    - This class consist of one function that will try to greedily path to a position.
    - It will go straight towards the target and move around the obstacle given in its path.
    - The greedy algorithm used here is known as Bug0 (WARNING: Infinite Loop is possible).
 */

public strictfp class Pathing {

    // Null if we are not trying to go around a obstacle.
    private static Direction bugDirection = null;

    /**
      * The robot will attempt to move to a position on the map if it's vacant.
      * @param rc, robot that will be moving
      * @param target, position the robot is trying to get to
     */
    public static boolean walkTowards(RobotController rc, MapLocation target) throws GameActionException {
        // If cooldown is too high, nothing we can do.
        if (!rc.isMovementReady()) {
            return true;
        }
        MapLocation currentLocation = rc.getLocation();
        // At our goal
        if (currentLocation.equals(target)) {
            return true;
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
        return false;
    }

    /**
      * The robot sense if there is a obstacle in the given direction.
      * @param rc, robot that will be sense if there is obstacle there.
      * @param dir, direction the robot is sensing in.
     */
    private static boolean isObstacle(RobotController rc, Direction dir) {
        MapLocation adjacentLocation = rc.getLocation().add(dir);
        return rc.canSenseRobotAtLocation(adjacentLocation);
    }
}
