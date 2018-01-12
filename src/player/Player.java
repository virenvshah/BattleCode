import bc.*;

public class Player {
  public static void main(String[] args) {
    System.out.println("Starting");
    GameController gc = new GameController();
    PlanetMap earthPlanetMap = gc.startingMap(Planet.Earth);
    BattleMap earthBattleMap = new BattleMap(earthPlanetMap);
		
    while (true) {
      System.out.println("Current Round: " + gc.round());
      long startTime = System.nanoTime();
      earthBattleMap.shortestPath(new Tuple(0, 0), new Tuple(0, (int)earthPlanetMap.getHeight() - 1));
      long endTime = System.nanoTime();
      System.out.println((endTime-startTime)/1000000.0);
      gc.nextTurn();
    }
  }
}
