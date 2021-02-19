//  Creates maps and stores map data
//  Maps were created using Google Sheets (linked below)
//  https://docs.google.com/spreadsheets/d/11SGpWf9tis6HM8a6GRM8gLvH7HZJ83TVDMFR9MKO0qk/edit?usp=sharing

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;

public class Map {
    
    //  A map image is created based on the text file given. This image is what the player sees
    //  Logic is all done using the list of Tile variables
    //  Also stores a map's associated images, music, etc
    private BufferedImage image;
    private Image background;
    private Image thumbnail;
    private Clip music;

    private String name;
    private int[][] arr;
    private LinkedList<Tile> tiles = new LinkedList<Tile>();
    private LinkedList<SpecialTile> special = new LinkedList<SpecialTile>();
    private int width;
    private int height;
    private int rows;
    private int cols;

    private int[] spawn = new int[2];

    private int tileSize = 32;
    private Graphics g;

    //  Constructor
    //  Parameters: Map name (String)
    public Map(String mapName) {
        loadMap(mapName);
        width = cols * tileSize;
        height = rows * tileSize;

        //  Grahpics setup
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        drawMap();
    }

    //  Reads map based on name given
    //  Return type: Changes global variables (void)
    //  Parameters: Map name (String)
    public void loadMap(String mapName) {
        try {
            name = mapName;

            BufferedReader inFile = new BufferedReader(new FileReader("maps/" + mapName + "/mapFile.txt"));

            //  Read map details
            spawn[0] = Integer.parseInt(inFile.readLine());
            spawn[1] = Integer.parseInt(inFile.readLine());
            cols = Integer.parseInt(inFile.readLine());
            rows = Integer.parseInt(inFile.readLine());

            //  Read and load background image
            background = Toolkit.getDefaultToolkit().getImage(inFile.readLine());

            //  Read and load thumbnail image
            thumbnail = Toolkit.getDefaultToolkit().getImage(inFile.readLine());

            //  Load music
            try {
                AudioInputStream stream = AudioSystem.getAudioInputStream(new File(inFile.readLine()));
                music = AudioSystem.getClip();
                music.open(stream);

                //  Adjust volume
                FloatControl gainControl = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(Driver.musicAdjust);
            }
            catch (UnsupportedAudioFileException e) {
                System.out.println("File not supported!");
            }
            catch (Exception e) {
                System.out.println(e);
            }

            //  Read map data
            arr = new int[rows][cols];
            String s = "";
            for (int r = 0; r < rows; r++) {
                s = inFile.readLine();
                String[] split = s.split("\t");
                for (int c = 0; c < split.length; c++)
                    arr[r][c] = Integer.parseInt(split[c]);
            }
            inFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            System.out.println("Reading error!");
        }
    }

    //  Create map image
    //  In map files, 0 = white, 1 = black, 2 = black with hitbox, 3 = hookable tile, 4 = climable tile,
    //  5 to 8 are ramps, 9+ are special tiles
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void drawMap() {
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);

        int[] x = new int[3];
        int[] y = new int[3];
        
        //  Creates tiles
        for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
                int value = arr[r][c];
				if (value == 1) {
                    g.setColor(Color.BLACK);
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                }
                //  Basic tiles
                else if (value == 2) {
                    g.setColor(Color.BLACK);
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                    tiles.add(new Tile(c * tileSize, r * tileSize, tileSize, 0, false, false));
                }
                //  Hookable tiles
                else if (value == 3) {
                    g.setColor(Color.BLACK);
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                    g.setColor(Color.WHITE);
                    g.fillRect(c * tileSize, r * tileSize + 24, tileSize, 8);
                    tiles.add(new Tile(c * tileSize, r * tileSize, tileSize, 0, true, false));
                }
                //  Climable tiles
                else if (value == 4) {
                    g.setColor(Color.BLACK);
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                    g.setColor(Color.WHITE);
                    g.fillRect(c * tileSize, r * tileSize + 8, 8, tileSize - 16);
                    g.fillRect(c * tileSize + 24, r * tileSize + 8, 8, tileSize - 16);
                    tiles.add(new Tile(c * tileSize, r * tileSize, tileSize, 0, false, true));
                }
                //  Ramp tiles
                else if (value >= 5 && value <= 8) {
                    if (value == 5) {
                        x = new int[]{c * tileSize, (c + 1) * tileSize, (c + 1) * tileSize};
                        y = new int[]{(r + 1) * tileSize, r * tileSize, (r + 1) * tileSize};
                    }
                    else if (value == 6) {
                        x = new int[]{c * tileSize, c * tileSize, (c + 1) * tileSize};
                        y = new int[]{r * tileSize, (r + 1) * tileSize, (r + 1) * tileSize};
                    }
                    else if (value == 7) {
                        x = new int[]{c * tileSize, (c + 1) * tileSize, (c + 1) * tileSize};
                        y = new int[]{r * tileSize, r * tileSize, (r + 1) * tileSize};
                    }
                    else if (value == 8) {
                        x = new int[]{c * tileSize, c * tileSize, (c + 1) * tileSize};
                        y = new int[]{r * tileSize, (r + 1) * tileSize, r * tileSize};
                    }
                    g.setColor(Color.BLACK);
                    g.fillPolygon(x, y, 3);
                    tiles.add(new Tile(c * tileSize, r * tileSize, tileSize, value - 4, false, false));
                }
                //  Special Tiles
                else if (value >= 9 && value <= 20) {
                    //  Finish flag
                    if (value == 9) {
                        special.add(new SpecialTile(c * tileSize, r * tileSize, tileSize, 0));
                    }
                    //  Speed boost left
                    else if (value == 10) {
                        special.add(new SpecialTile(c * tileSize, r * tileSize, tileSize, 1));
                    }
                    //  Speed boost right
                    else if (value == 11) {
                        special.add(new SpecialTile(c * tileSize, r * tileSize, tileSize, 2));
                    }
                    //  Jump pad
                    else if (value == 12) {
                        special.add(new SpecialTile(c * tileSize, r * tileSize, tileSize, 3));
                    }
                    g.drawImage(special.get(special.size() - 1).getModel(), c * tileSize, r * tileSize, tileSize, tileSize, null);
                }
			}
        }
        
        //  Assigns collision boundaries to tiles, as adjacent tiles will not need their connecting boundaries
        //  Loops through all tiles and checks for adjacent tiles or map border in all directions
        g.setColor(Color.BLACK);
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (arr[r][c] >= 2 && arr[r][c] <= 8) {
                    if (c - 1 >= 0 && arr[r][c - 1] >= 1 && arr[r][c - 1] <= 8 || c == 0) {
                        tiles.get(searchTile(r, c)).setLeft(false);
                        //  Cover climable surfaces pressed against other tiles with black
                        if (arr[r][c] == 4) {
                            g.fillRect(c * tileSize, r * tileSize, 8, tileSize);
                        }
                    }

                    if (c + 1 < cols && arr[r][c + 1] >= 1 && arr[r][c + 1] <= 8 || c == cols - 1) {
                        tiles.get(searchTile(r, c)).setRight(false);
                        //  Cover climable surfaces pressed against other tiles with black
                        if (arr[r][c] == 4)
                            g.fillRect(c * tileSize + 24, r * tileSize, 8, tileSize);
                    }

                    if (r - 1 >= 0 && arr[r - 1][c] >= 1 && arr[r - 1][c] <= 8 || r == 0)
                        tiles.get(searchTile(r, c)).setTop(false);

                    if (r + 1 < rows && arr[r + 1][c] >= 1 && arr[r + 1][c] <= 8 || r == rows - 1)
                        tiles.get(searchTile(r, c)).setBot(false);
                    
                }
            }
        }
    }

    //  Finds a tile in the list based on its row and column
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public int searchTile(int r, int c) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getX() / 32 == c && tiles.get(i).getY() / 32 == r)
                return i;
        }
        return -1;
    }

    //  Adds a time to the highscore file
    //  Return type: Updates files, no return (void)
    //  Parameters: Time to add (int)
    public void addHighscore(int time) {
        //  Load highscore file
        try {
            //  Adds new score to file
            BufferedWriter outFile = new BufferedWriter(new FileWriter("maps/" + name + "/highscores.txt", true));
            outFile.write(time + "\n");
            outFile.close();

            //  Sorts scores from lowest time to highest time by adding them to an ArrayList and sorting
            ArrayList<Integer> highscores = new ArrayList<Integer>();
            BufferedReader inFile = new BufferedReader(new FileReader("maps/" + name + "/highscores.txt"));
            String s = "";
            while (s != null) {
                s = inFile.readLine();
                if (s == null)
                    continue;
                highscores.add(Integer.parseInt(s));
            }
            inFile.close();
            Collections.sort(highscores);

            //  Clears file
            PrintWriter clear = new PrintWriter(new File("maps/" + name + "/highscores.txt"));
            clear.print("");
            clear.close();

            //  Writes sorted scores back to the file
            outFile = new BufferedWriter(new FileWriter("maps/" + name + "/highscores.txt", true));
            for (int score: highscores)
                outFile.write(score + "\n");
            outFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            System.out.println("Reading error!");
        }
    }

    //  Gets a specified amount of the top scores
    //  Return type: Returns list of high scores (LinkedList<Integer>)
    //  Parameters: Number of scores to get (eg. amount = 10 means top 10 highscores) (int)
    public LinkedList<Integer> getHighscores(int amount) {
        LinkedList<Integer> highscores = new LinkedList<Integer>();

        try {
            //  Reads scores
            BufferedReader inFile = new BufferedReader(new FileReader("maps/" + name + "/highscores.txt"));
            String s = "";

            //  Formats and adds scores to the list
            for (int i = 0; i < amount; i++) {
                s = inFile.readLine();
                if (s == null)
                    break;
                highscores.add(Integer.parseInt(s));
            }
            inFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        } catch (IOException e) {
            System.out.println("Reading error!");
        }

        return highscores;
    }

    //  Getters
    public Image getImage() {
        return image;
    }

    public Image getBackground() {
        return background;
    }

    public Image getThumbnail() {
        return thumbnail;
    }

    public Clip getMusic() {
        return music;
    }

    public LinkedList<Tile> getTiles() {
        return tiles;
    }

    public LinkedList<SpecialTile> getSpecial() {
        return special;
    }

    public int[][] getArr() {
        return arr;
    }
    
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getSpawn() {
        return spawn;
    }
}
