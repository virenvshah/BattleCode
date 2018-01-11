import bc.*;
import java.util.Random;

public class Mine{
  /*
   * mineLoop commands the given worker to
   * mine until there is "kn" amount of Karbonite in the stash,
   * or indefinitely (kn=-1).
   */
  public static void mineLoop(Unit worker, long kn, GameController gc){
    while (kn!=gc.karbonite()){
      // so check the spaces around you
      // then gather karbonite if there's more than 3 around
      // then if you are finished getting karbonite (no significant
      // reserves around), move randomly.
      Location unitLoc = worker.location();
      if(unitLoc.isInSpace()){
        return;
      } else {
        MapLocation mapLoc = unitLoc.mapLocation();
        long[] karbonites = {gc.karboniteAt(mapLoc.translate(1,0));
                             gc.karboniteAt(mapLoc.translate(1,1));
                             gc.karboniteAt(mapLoc.translate(0,1));
                             gc.karboniteAt(mapLoc.translate(-1,1));
                             gc.karboniteAt(mapLoc.translate(-1,0));
                             gc.karboniteAt(mapLoc.translate(-1,-1));
                             gc.karboniteAt(mapLoc.translate(0,-1));
                             gc.karboniteAt(mapLoc.translate(1,-1))
                            };
        Direction[] dirs = Direction.value();
        int idd = worker.id();
        for (int i =0;i<8;i++){
          if (gc.canHarvest(idd,dirs[i+1]) && karbonites[i] > 0){
            gc.harvest(idd,dirs[i+1]);
          }
        }
        Random rand = new Random();

        if (gc.canMove(idd,dirs[rand.nextInt(8)+1])
          gc.moveRobot(idd,dirs[i+1]);

      }

    }
  }

}
