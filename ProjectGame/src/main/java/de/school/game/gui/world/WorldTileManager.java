package de.school.game.gui.world;

import de.school.game.Game;
import de.school.game.util.FileUtil;
import de.school.game.util.rendering.RenderUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class WorldTileManager {
    public HashMap<Integer, TileTexture> tileLibrary;
    public HashMap<Integer, TileTexture> detailLibrary;
    public CollisionTileManager collisionTileManager;

    public int currentTileKey = 0;
    public int currentDetailKey = 0;
    public int[][] loadedMap;

    public int[][] loadedDetails;


    //Todo: Redondante Methoden wie loadMapTextures in eine Methode fassen(loadfromfile(arr,src))
    public WorldTileManager() {


        tileLibrary = new HashMap<>();
        detailLibrary = new HashMap<>();
        loadedMap = new int[Game.gameWindow().maxScreenCol][Game.gameWindow().maxScreenRows];
        loadedDetails = new int[Game.gameWindow().maxScreenCol][Game.gameWindow().maxScreenRows];
        collisionTileManager = new CollisionTileManager();
        addTileToLibrary(new TileTexture(RenderUtil.loadTexture(FileUtil.getFileByResource("textures/map/tiles/floor01.png"))));
        addTileToLibrary(new TileTexture(RenderUtil.loadTexture(FileUtil.getFileByResource("textures/map/tiles/grass01.png"))));
        addDetailToLibrary(new TileTexture(RenderUtil.loadTexture(FileUtil.getFileByResource("textures/map/details/air.png"))));
        addDetailToLibrary(new TileTexture(RenderUtil.loadTexture(FileUtil.getFileByResource("textures/map/details/tree01.png"))));
    }

    private void addTileToLibrary(TileTexture tileTexture) {
        tileLibrary.put(currentTileKey, tileTexture);
        currentTileKey++;
    }

    private void addDetailToLibrary(TileTexture tileTexture) {
        detailLibrary.put(currentDetailKey, tileTexture);
        currentDetailKey++;
    }

    public void loadmap(String resource) {
        loadMaptextures(resource + ".map");
        loadDetails(resource + ".detail");
        collisionTileManager.loadMapCollisions(resource + ".collision");

    }
    public void loadMapByDir(String resource) {
        File file = new File(resource);
        if (file.isDirectory()) {
            loadmap(resource);
        }
        String name = file.getName();
        loadmap(resource + File.separator + name);


    }

    public void loadMaptextures(String resource) {
        try {
            InputStream stream = WorldTileManager.class.getResourceAsStream(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            int row = 0;
            while (row < Game.gameWindow().maxScreenRows) {
                String currentLine = reader.readLine();
                if (currentLine == null) break;  // Falls weniger Zeilen vorhanden sind

                String[] tileIds = currentLine.split(" ");
                for (int col = 0; col < Game.gameWindow().maxScreenCol; col++) {
                    if (col < tileIds.length) { // Falls eine Zeile zu kurz ist
                        loadedMap[col][row] = Integer.parseInt(tileIds[col]);
                    } else {
                        loadedMap[col][row] = 0; // Standardwert setzen
                    }
                }
                row++;
            }
            reader.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadDetails(String resource) {
        try {
            InputStream stream = WorldTileManager.class.getResourceAsStream(resource);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            int row = 0;
            while (row < Game.gameWindow().maxScreenRows) {
                String currentLine = reader.readLine();
                if (currentLine == null) break;

                String[] tileIds = currentLine.split(" ");
                for (int col = 0; col < Game.gameWindow().maxScreenCol; col++) {
                    if (col < tileIds.length) {
                        loadedDetails[col][row] = Integer.parseInt(tileIds[col]);
                    } else {
                        loadedDetails[col][row] = 0;
                    }
                }
                row++;
            }
            reader.close();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void render(Graphics2D graphics2D) {
        render(graphics2D, loadedMap, tileLibrary);
        render(graphics2D, loadedDetails, detailLibrary);
    }

    private void render(Graphics2D graphics2D, int[][] map, HashMap<Integer, TileTexture> tiles) {
        int col = 0;
        int row = 0;
        int x = 0;
        int y = 0;

        while (col < Game.gameWindow().maxScreenCol && row < Game.gameWindow().maxScreenRows) {
            graphics2D.drawImage(tiles.get(map[col][row]).texture, x, y, Game.gameWindow().tileSize, Game.gameWindow().tileSize, null);
            col++;
            x += Game.gameWindow().tileSize;
            if (col == Game.gameWindow().maxScreenCol) {
                col = 0;
                x = 0;
                row++;
                y += Game.gameWindow().tileSize;
            }
        }
    }

    public class CollisionTileManager {
        public int[][] worldCollision;
        public int[] levelGoal;
        public int[] startposition;

        public CollisionTileManager() {
            worldCollision = new int[Game.gameWindow().maxScreenCol][Game.gameWindow().maxScreenRows];
            levelGoal = new int[2];
            //Startposition initialisieren
            startposition = new int[2];
            //0 ist keine Kollision
            //1 ist die Kollision mit einer Wand
            //2 ist das "sterben"/"neu beginnen" eines Levels
            //3 ist das Ziel eines Levels
            //4 ist die Startposition


        }

        public void loadMapCollisions(String resource) {
            try {
                InputStream stream = WorldTileManager.class.getResourceAsStream(resource);
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                int row = 0;
                while (row < Game.gameWindow().maxScreenRows) {
                    String currentLine = reader.readLine();
                    if (currentLine == null) break;

                    String[] tileIds = currentLine.split(" ");
                    for (int col = 0; col < Game.gameWindow().maxScreenCol; col++) {
                        if (col < tileIds.length) {
                            int coll = Integer.parseInt(tileIds[col]);
                            if (coll > 4) {
                                throw new IllegalArgumentException("diese Zahl/Kollision existiert nicht!");
                            }
                            worldCollision[col][row] = coll;
                            if (coll == 3) {
                                levelGoal[0] = col;
                                levelGoal[1] = row;
                            }
                            else if(coll == 4) {
                                startposition[0] = col;
                                startposition[1] = row;
                            }
                        } else {
                            worldCollision[col][row] = 0;
                        }
                    }
                    row++;
                }
                reader.close();
                stream.close();
                Game.player().x = startposition[0] * Game.gameWindow().tileSize;
                Game.player().y = startposition[1] * Game.gameWindow().tileSize;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public int[] getLevelGoal (){
            return levelGoal;
        }

    }
}
