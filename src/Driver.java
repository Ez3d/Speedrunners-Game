/*  Edward Zhou
    2021-01-27

    Culminating Game: Speedrunners
        -   See README for game details
 */

//  Driver class: Manages menus and launches the main game

//  Setup
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

@SuppressWarnings("serial")
public class Driver extends JPanel implements KeyListener {

    //  JComponents
    static JFrame frame;
    static Game gamePanel;
    static JPanel menuPanel, mapPanel, optionPanel, highscorePanel;

    static JJButton back1, back2, back3;
    static JJButton play, options, highscores, quit;
    static JSlider musicVolume, fxVolume;
    static float musicAdjust = 0, fxAdjust = 0;

    //  Window stuff
    static final int WINDOW_WIDTH = 1440;
    static final int WINDOW_HEIGHT = 768;
    static int currentScreen = 0;
    static int prevScreen = 0;

    //  Map selection
    static ArrayList<Map> maps = new ArrayList<Map>();
    static Map selectedMap;
    static JJButton map1, map2, map3;

    //  Highscores
    static JJButton score1, score2, score3;
    static JTextArea scoreList;

    //  Images
    static BufferedImage menuImage, optionImage, mapImage, highscoreImage;
    static Image fx, music;

    //  Sounds
    static Clip menuMusic;

    //  Custom font
    static Font thunderstorm, digital;

    //  Initializes all JComponents
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public static void initJComponents() {

        //  Load fonts
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            thunderstorm = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Thunderstorm.ttf")).deriveFont(30f);
            ge.registerFont(thunderstorm);

            digital = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Digital.ttf")).deriveFont(25f);
            ge.registerFont(digital);
        } catch (IOException | FontFormatException e) {
            System.out.println(e);
        }

        frame = new JFrame("Speedrunners");
        frame.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        //  Driver menu   -------------------------------------------------------------------------------------------
        menuPanel = new Driver();

        //  Switches to map panel
        play = new JJButton("PLAY", 3);
        play.setHorizontalAlignment(SwingConstants.LEFT);
        play.setBounds(100, 200, 300, 75);

        //  Switches to highscore panel
        highscores = new JJButton("HIGHSCORES", 4);
        highscores.setHorizontalAlignment(SwingConstants.LEFT);
        highscores.setBounds(100, 300, 250, 75);

        //  Switches to option panel
        options = new JJButton("OPTIONS", 2);
        options.setHorizontalAlignment(SwingConstants.LEFT);
        options.setBounds(100, 400, 200, 75);

        //  Exits program
        quit = new JJButton("QUIT");
        quit.setHorizontalAlignment(SwingConstants.LEFT);
        quit.setBounds(100, 500, 150, 75);
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuPanel.add(play);
        menuPanel.add(highscores);
        menuPanel.add(options);
        menuPanel.add(quit);

        //  Options menu   -------------------------------------------------------------------------------------------
        optionPanel = new Driver();

        //  Music volume slider
        musicVolume = new JSlider(JSlider.HORIZONTAL, -25, 6, 0);
        musicVolume.setFocusable(false);
        musicVolume.setBackground(new Color(4, 4, 40));
        musicVolume.setForeground(Color.WHITE);
        musicVolume.setBounds(WINDOW_WIDTH / 2 - 150, WINDOW_HEIGHT / 2 - 50, 300, 25);
        musicVolume.addChangeListener(new ChangeListener() {
            
            //  Updates whenever the slider is used
            public void stateChanged(ChangeEvent e) {
                musicAdjust = musicVolume.getValue();

                //  Mute when lowest
                if (musicAdjust == -25)
                    musicAdjust = -1000;

                FloatControl gainControl = (FloatControl) menuMusic.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(musicAdjust);
            }
        });

        //  Sound fx volume slider
        fxVolume = new JSlider(JSlider.HORIZONTAL, -25, 6, 0);
        fxVolume.setFocusable(false);
        fxVolume.setBackground(new Color(4, 4, 40));
        fxVolume.setForeground(Color.WHITE);
        fxVolume.setBounds(WINDOW_WIDTH / 2 - 150, WINDOW_HEIGHT / 2 + 50, 300, 25);
        fxVolume.addChangeListener(new ChangeListener() {
            
            //  Updates whenever the slider is used
            public void stateChanged(ChangeEvent e) {
                fxAdjust = fxVolume.getValue();

                //  Mute when lowest
                if (fxAdjust == -25)
                    fxAdjust = -1000;
                
                JJButton.updateVolume();
            }
        });

        //  Switches back to main menu
        back1 = new JJButton("BACK", 0);
        back1.setBounds((WINDOW_WIDTH - 150) / 2, WINDOW_HEIGHT / 2 + 225, 150, 50);

        optionPanel.add(musicVolume);
        optionPanel.add(fxVolume);
        optionPanel.add(back1);

        //  Map selection   -------------------------------------------------------------------------------------------
        mapPanel = new Driver();

        //  Selects map and switches to game panel
        //  Fuji
        map1 = new JJButton("");
        map1.setBounds(320, 350, 200, 200);
        map1.setBorder(BorderFactory.createEmptyBorder());
        map1.setIcon(new ImageIcon(maps.get(0).getThumbnail()));
        map1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedMap = new Map("fuji");
                
                gamePanel = new Game();
                changeScreen(1);
            }
        });

        //  Hong Kong
        map2 = new JJButton("");
        map2.setBounds(620, 350, 200, 200);
        map2.setBorder(BorderFactory.createEmptyBorder());
        map2.setIcon(new ImageIcon(maps.get(1).getThumbnail()));
        map2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedMap = new Map("hong_kong");

                gamePanel = new Game();
                changeScreen(1);
            }
        });

        //  New York
        map3 = new JJButton("");
        map3.setBounds(920, 350, 200, 200);
        map3.setBorder(BorderFactory.createEmptyBorder());
        map3.setIcon(new ImageIcon(maps.get(2).getThumbnail()));
        map3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedMap = new Map("new_york");

                gamePanel = new Game();
                changeScreen(1);
            }
        });

        //  Switches back to main menu
        back2 = new JJButton("BACK", 0);
        back2.setBounds((WINDOW_WIDTH - 150) / 2, WINDOW_HEIGHT / 2 + 225, 150, 50);

        mapPanel.add(map1);
        mapPanel.add(map2);
        mapPanel.add(map3);
        mapPanel.add(back2);

        //  Highscore display panel   -------------------------------------------------------------------------------------------
        highscorePanel = new Driver();

        //  Brings up scores for Fuji
        score1 = new JJButton("");
        score1.setBounds(120, 350, 200, 200);
        score1.setBorder(BorderFactory.createEmptyBorder());
        score1.setIcon(new ImageIcon(maps.get(0).getThumbnail()));
        score1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreList.setText("\n  FUJI\n");
                int counter = 1;
                for (int i: maps.get(0).getHighscores(10)) {
                    scoreList.append(String.format("%n %2d.%5d:%02d.%d%n", counter, i / 600, (i / 10) % 60, i % 10));
                    counter++;
                }
            }
        });

        //  Brings up scores for Hong Kong
        score2 = new JJButton("");
        score2.setBounds(420, 350, 200, 200);
        score2.setBorder(BorderFactory.createEmptyBorder());
        score2.setIcon(new ImageIcon(maps.get(1).getThumbnail()));
        score2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreList.setText("\n  HONG KONG\n");
                int counter = 1;
                for (int i: maps.get(1).getHighscores(10)) {
                    scoreList.append(String.format("%n %2d.%5d:%02d.%d%n", counter, i / 600, (i / 10) % 60, i % 10));
                    counter++;
                }
            }
        });

        //  Brings up scores for New York
        score3 = new JJButton("");
        score3.setBounds(720, 350, 200, 200);
        score3.setBorder(BorderFactory.createEmptyBorder());
        score3.setIcon(new ImageIcon(maps.get(2).getThumbnail()));
        score3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scoreList.setText("\n  NEW YORK\n");
                int counter = 1;
                for (int i: maps.get(2).getHighscores(10)) {
                    scoreList.append(String.format("%n %2d.%5d:%02d.%d%n", counter, i / 600, (i / 10) % 60, i % 10));
                    counter++;
                }
            }
        });

        scoreList = new JTextArea();
        scoreList.setBounds(WINDOW_WIDTH / 2 + 400, WINDOW_HEIGHT / 2 - 100, 185, 400);
        scoreList.setBackground(new Color(4, 4, 40));
        scoreList.setForeground(Color.WHITE);
        scoreList.setFont(digital);
        scoreList.setFocusable(false);

        //  Switches back to main menu
        back3 = new JJButton("BACK", 0);
        back3.setBounds((WINDOW_WIDTH - 150) / 2, WINDOW_HEIGHT / 2 + 225, 150, 50);

        highscorePanel.add(score1);
        highscorePanel.add(score2);
        highscorePanel.add(score3);
        highscorePanel.add(scoreList);
        highscorePanel.add(back3);
    }

    //  Reads images from file and stores them
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public static void loadImages() {
        try {
            menuImage = ImageIO.read(new File("images/menuImage.png"));
            optionImage = ImageIO.read(new File("images/optionImage.png"));
            mapImage = ImageIO.read(new File("images/mapImage.png"));
            highscoreImage = ImageIO.read(new File("images/highscoreImage.png"));
            music = Toolkit.getDefaultToolkit().getImage("images/music.png");
            fx = Toolkit.getDefaultToolkit().getImage("images/fx.png");
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    //  Reads wav files and stores them
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public static void loadSounds() {
        try {
            //  Menu music
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File("music/music.wav"));
            menuMusic = AudioSystem.getClip();
            menuMusic.open(stream);
        }
        catch (UnsupportedAudioFileException e) {
            System.out.println("File not supported!");
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
    
    //  Changes menu
    //  0 = Driver menu
    //  1 = Game
    //  2 = Options
    //  3 = Map select
    //  4 = Highscore display
    //  Return type: Changes global variables (void)
    //  Parameters: Screen to change to (int)
    public static void changeScreen(int i) {
        //  Remove all panels (removeAll() does not work properly for some reason)
        frame.remove(menuPanel);
        if (gamePanel != null)
            frame.remove(gamePanel);
        frame.remove(optionPanel);
        frame.remove(mapPanel);
        frame.remove(highscorePanel);

        prevScreen = currentScreen;
        currentScreen = i;
        Game.isPaused = true;

        //  Main menu
        if (i == 0) {
            frame.add(menuPanel);
            menuPanel.requestFocus();

            //  Plays music
            menuMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
        //  Game screen
        else if (i == 1) {
            menuMusic.stop();
            menuMusic.setFramePosition(0);
            frame.add(gamePanel);
            gamePanel.requestFocus();
        }
        //  Options menu
        else if (i == 2) {
            frame.add(optionPanel);
            optionPanel.requestFocus();
        }
        //  Map selection
        else if (i == 3) {
            frame.add(mapPanel);
            mapPanel.requestFocus();
        }
        else if (i == 4) {
            frame.add(highscorePanel);
            highscorePanel.requestFocus();
        }

        frame.revalidate();
        frame.pack();
        frame.repaint();
    }

    //  Keyboard input
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ESCAPE) {
            if (currentScreen == 2 || currentScreen == 3 || currentScreen == 4)
                changeScreen(0);
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    //  PaintComponent for graphics
    //  Return type: Changes global variables (void)
    //  Parameters: Graphics variable
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(254, 54, 180));

        //  Main menu
        if (currentScreen == 0) {
            g.drawImage(menuImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);

            g.fillRect(110, 210, 300, 75);
            g.fillRect(110, 310, 250, 75);
            g.fillRect(110, 410, 200, 75);
            g.fillRect(110, 510, 150, 75);
        }
        //  Options menu
        else if (currentScreen == 2) {
            g.drawImage(optionImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);
            g.drawImage(music, WINDOW_WIDTH / 2 + 165, WINDOW_HEIGHT / 2 - 65, 50, 50, this);
            g.drawImage(fx, WINDOW_WIDTH / 2 + 160, WINDOW_HEIGHT / 2 + 34, 60, 60, this);

            g.fillRect(WINDOW_WIDTH / 2 - 140, WINDOW_HEIGHT / 2 - 40, 300, 25);
            g.fillRect(WINDOW_WIDTH / 2 - 140, WINDOW_HEIGHT / 2 + 60, 300, 25);
            g.fillRect((WINDOW_WIDTH - 150) / 2 + 10, WINDOW_HEIGHT / 2 + 235, 150, 50);
        }
        //  Map selection
        else if (currentScreen == 3) {
            g.drawImage(mapImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);

            g.fillRect(330, 360, 200, 200);
            g.fillRect(630, 360, 200, 200);
            g.fillRect(930, 360, 200, 200);
            g.fillRect((WINDOW_WIDTH - 150) / 2 + 10, WINDOW_HEIGHT / 2 + 235, 150, 50);
        }
        //  Highscore display
        else if (currentScreen == 4) {
            g.drawImage(highscoreImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);

            g.fillRect(130, 360, 200, 200);
            g.fillRect(430, 360, 200, 200);
            g.fillRect(730, 360, 200, 200);
            g.fillRect((WINDOW_WIDTH - 150) / 2 + 10, WINDOW_HEIGHT / 2 + 235, 150, 50);
        }
    }
    
    //  Constructor
    public Driver() {
        setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout(null);
        setFocusable(true);
        
        addKeyListener(this);
    }

    //  Main method
    public static void main(String[] args) {
        
        //  Create maps
        maps.add(new Map("fuji"));
        maps.add(new Map("hong_kong"));
        maps.add(new Map("new_york"));

        loadImages();
        loadSounds();
        initJComponents();
        changeScreen(0);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
    }
}
