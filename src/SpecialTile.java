//  Special tiles that fulfil specific purposes (not for collisions)

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class SpecialTile extends Tile {

    //  0 = Flag
    //  1 = Speed boost left
    //  2 = Speed boost right
    //  3 = Jump pad
    private int type;

    private BufferedImage model;
    
    //  Constructor
    //  Parameters: position (int) x 2, tileSize (int), type of special tile (int)
    public SpecialTile(int x, int y, int tileSize, int type) {
        super(x, y, tileSize, 0, false, false);

        this.type = type;
        left = false;
        right = false;
        top = false;
        bot = false;

        //  Set model
        try {
            if (type == 0)
                model = ImageIO.read(new File("models/special/flag.png"));
            else if (type == 1)
                model = ImageIO.read(new File("models/special/lspeedboost.png"));
            else if (type == 2)
                model = ImageIO.read(new File("models/special/rspeedboost.png"));
            else if (type == 3)
                model = ImageIO.read(new File("models/special/jumppad.png"));
        }
        catch (IOException e) {
                
        }
    }

    //  Getters
    public int getType() {
        return type;
    }

    public BufferedImage getModel() {
        return model;
    }
}
