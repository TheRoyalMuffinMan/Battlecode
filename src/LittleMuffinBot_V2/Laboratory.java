package LittleMuffinBot_V2;

import battlecode.common.*;

public class Laboratory {
    static MapLocation location;
    static int vision;
    static int nearbyFriendlies = 0, nearbyEnemies = 0;

    static void run(RobotController rc) throws GameActionException {
        location = rc.getLocation();
        vision = rc.getType().visionRadiusSquared;
        while(true) {
            if (rc.senseNearbyRobots().length > 2) {
                avoid(rc);
            } else {
                transmute(rc);
            }
        }
    }

    // Stay away from other bots
    static void avoid(RobotController rc) throws GameActionException {
        rc.setIndicatorString("Scanning");
        int goUp = 0;
        int goDown = 0;
        int goLeft = 0;
        int goRight = 0;
        RobotInfo[] robots = rc.senseNearbyRobots();
        if (robots.length > 5) {
            for (int i = 0; i < robots.length; i++) {
                if (robots[i].getTeam().equals(rc.getTeam())) {
                    if (robots[i].getType().equals(RobotType.SOLDIER) || robots[i].getType().equals(RobotType.SAGE) || robots[i].getType().equals(RobotType.WATCHTOWER)) {
                        int dy = robots[i].location.y - rc.getLocation().y;
                        int dx = robots[i].location.x - rc.getLocation().x;
                        if (dy > 0) {
                            goDown += 2;
                        } else {
                            goUp += 2;
                        }
                        if (dx > 0) {
                            goLeft += 2;
                        } else {
                            goRight += 2;
                        }

                    }
                } else {
                    int dy = robots[i].location.y - rc.getLocation().y;
                    int dx = robots[i].location.x - rc.getLocation().x;
                    if (dy > 0) {
                        goDown += 2;
                    } else {
                        goUp += 2;
                    }
                    if (dx > 0) {
                        goLeft += 2;
                    } else {
                        goRight += 2;
                    }
                }
            }
            
            Direction dir = Direction.CENTER;
            if (goUp > goDown) {
                if (goLeft > goRight) {
                    if (goUp - goLeft > 2) {
                        dir = Direction.NORTH;
                    } else if (goLeft - goUp > 2) {
                        dir = Direction.WEST;
                    } else {
                        dir = Direction.NORTHWEST;
                    }
                } else if (goRight < goLeft) {
                    if (goUp - goRight > 2) {
                        dir = Direction.NORTH;
                    } else if (goRight - goUp > 2) {
                        dir = Direction.EAST;
                    } else {
                        dir = Direction.NORTHEAST;
                    }
                } else {
                    dir = Direction.NORTH;
                }
            } else {
                if (goLeft > goRight) {
                    if (goDown - goLeft > 2) {
                        dir = Direction.SOUTH;
                    } else if (goLeft - goDown > 2) {
                        dir = Direction.WEST;
                    } else {
                        dir = Direction.SOUTHWEST;
                    }
                } else if (goRight < goLeft) {
                    if (goDown - goRight > 2) {
                        dir = Direction.SOUTH;
                    } else if (goRight - goDown > 2) {
                        dir = Direction.EAST;
                    } else {
                        dir = Direction.SOUTHEAST;
                    }
                } else {
                    dir = Direction.SOUTH;
                }
            }
            Pathing.walkTowards(rc, location.add(dir));
        }
    }

    static void transmute(RobotController rc) throws GameActionException {
        if (rc.canTransmute()){
            rc.transmute();
        }
    }

}
