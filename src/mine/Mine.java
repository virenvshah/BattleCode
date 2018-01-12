import bc.*;
import java.util.*;

/*
 * Small utility class for mining out an area.
 * @author vineet
*/
public class Mine{
  /*
   * mineArea causes the unit with id "idd" to mine out the cell it's standing
   * on along with all cells adjacent to it.
   * @requires: idd is the ID of a worker unit, gc and navi are valid
   * game controllers and maps.
   */
  public static void mineArea(int idd, GameController gc, Map navi){
      // gathers all karbonite from
      Unit worker = gc.unit(idd);
      Location unitLoc = worker.location();
      if(unitLoc.isInSpace()){
        return;
      } else {
        Direction[] dirs = Direction.value();
        MapLocation[] square =
          {
            unitLoc.mapLocation();
            unitLoc.mapLocation().add(East),
            unitLoc.mapLocation().add(North),
            unitLoc.mapLocation().add(Northeast),
            unitLoc.mapLocation().add(Northwest),
            unitLoc.mapLocation().add(South),
            unitLoc.mapLocation().add(Southeast),
            unitLoc.mapLocation().add(Southwest),
            unitLoc.mapLocation().add(West)
          };
        for (int i =0;i<9;i++){
          while (gc.karboniteAt(square[i])!=0){
            if(gc.canHarvest(idd,dirs[i])){
              gc.harvest(idd,dirs[i]);
            }
          }
        }
      }
  }

}
