package LittleMuffinBot;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public strictfp class findEnemyArchon {
    static boolean foundAll = false;
    static void sense(RobotController rc) throws GameActionException {
        if(!foundAll) {
            RobotInfo[] enemyRobots = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());

            for (int i = 0; i < enemyRobots.length; i++) {
                if (enemyRobots[i].type.equals(RobotType.ARCHON)){
                    int index = 2;
                    while(index <= 1 + rc.getArchonCount()){
                        int readLoc = rc.readSharedArray(index);
                        int readX = readLoc & 0xFF;
                        int readY = readLoc >> 8;
                        int x = enemyRobots[i].location.x;
                        int y = enemyRobots[i].location.y;
                        if(Math.abs(readX - x) < 7 && Math.abs(readY - y) < 7){
                            break;
                        } else if(readLoc == 0){
                            y = y << 8;
                            int loc = x | y;
                            rc.writeSharedArray(index, loc);

                        }

                    }
                    if(index == rc.getArchonCount()){
                        foundAll = true;
                        break;
                    }
                }
            }

        }
    }
}
