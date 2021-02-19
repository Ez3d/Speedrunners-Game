//  Creates player Object and creates and stores related data and character models

import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Player {
    
    private int height = 64;
    private int width = 32;

    //  Direction the player is facing
    //  -1 = left, 1 = right
    private int direction = 1;

    //  Player sprites
    //  Each 2D array holds two arrays, one for left-facing and one for right-facing
    private Image[][] still = new Image[2][2];
    private Image[][] run = new Image[2][8];
    private Image[][] jump = new Image[2][10];
    private Image[][] doubleJump = new Image[2][10];
    private Image[][] fall = new Image[2][6];
    private BufferedImage[][] grapple = new BufferedImage[2][2];

    private Rectangle hitbox;

    //  Constructor
    public Player() {
        loadModels();
        hitbox = new Rectangle(Game.WINDOW_WIDTH / 2, 512 - height, width, height);
    }

    //  Reads player model images and stores them
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void loadModels() {
        File[] files;

        //  Still images
        files = new File("models/still/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            still[0][i] = Toolkit.getDefaultToolkit().getImage("models/still/left/" + files[i].getName());
            still[1][i] = Toolkit.getDefaultToolkit().getImage("models/still/right/" + files[i].getName());
        }

        //  Run
        files = new File("models/run/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            run[0][i] = Toolkit.getDefaultToolkit().getImage("models/run/left/" + files[i].getName());
            run[1][i] = Toolkit.getDefaultToolkit().getImage("models/run/right/" + files[i].getName());
        }

        //  Jump
        files = new File("models/jump/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            jump[0][i] = Toolkit.getDefaultToolkit().getImage("models/jump/left/" + files[i].getName());
            jump[1][i] = Toolkit.getDefaultToolkit().getImage("models/jump/right/" + files[i].getName());
        }

        //  Double jump
        files = new File("models/doubleJump/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            doubleJump[0][i] = Toolkit.getDefaultToolkit().getImage("models/doubleJump/left/" + files[i].getName());
            doubleJump[1][i] = Toolkit.getDefaultToolkit().getImage("models/doubleJump/right/" + files[i].getName());
        }

        //  Fall
        files = new File("models/fall/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            fall[0][i] = Toolkit.getDefaultToolkit().getImage("models/fall/left/" + files[i].getName());
            fall[1][i] = Toolkit.getDefaultToolkit().getImage("models/fall/right/" + files[i].getName());
        }

        //  Grapple
        files = new File("models/grapple/left").listFiles();
        for (int i = 0; i < files.length; i++) {
            try {
                grapple[0][i] = ImageIO.read(new File("models/grapple/left/" + files[i].getName()));
                grapple[1][i] = ImageIO.read(new File("models/grapple/right/" + files[i].getName()));
            }
            catch (IOException e) {
                
            }
        }
    }

    //  Getters
    public Image[][] still() {
        return still;
    }

    public Image[][] run() {
        return run;
    }

    public Image[][] jump() {
        return jump;
    }

    public Image[][] doubleJump() {
        return doubleJump;
    }

    public Image[][] fall() {
        return fall;
    }

    public BufferedImage[][] grapple(){
        return grapple;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getDirection() {
        return direction;
    }

    //  Setters
    public void setDirection(int i) {
        direction = i;
    }
}
