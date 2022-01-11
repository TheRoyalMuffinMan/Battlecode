package LittleMuffinBot_V2;

import battlecode.common.*;

public strictfp class findEnemyArchon {
    static boolean foundAll = false;
    static void sense(RobotController rc) throws GameActionException {
        if(!foundAll) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());

            for (int i = 0; i < enemyRobots.length; i++) {
                if (enemyRobots[i].type.equals(RobotType.ARCHON)){
                    int index = 2;
                    while(index <=5){
                        int readLoc = rc.readSharedArray(index);
                        int readX = readLoc & 0xF;

                        if(readLoc == 0){
                            int x = enemyRobots[i].location.x;
                            int y = enemyRobots[i].location.y;
                            y = y << 8;
                            int loc = x | y;
                            rc.writeSharedArray(index, loc);
                        }

                    }
                }
            }
        }
    }
}
