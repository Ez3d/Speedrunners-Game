//  Tile object class for collisions and ramps

import java.awt.*;

public class Tile {

    protected Rectangle hitbox;

    //  Type of Ramp
    //  0 = no ramp
    //  1 = ramp with right angle at bottom right
    //  2 = ramp with right angle at bottom left
    //  3 = ramp with right angle at top right
    //  4 = ramp with right angle at top left
    protected int rampType;

    protected boolean left = true;
    protected boolean right = true;
    protected boolean top = true;
    protected boolean bot = true;
    protected boolean isHookable;
    protected boolean isClimable;

    protected int tileSize;

    //  x and y are relative to the top left corner of the map
    //  x and y are NOT the same as rect.x and rect.y
    protected int x;
    protected int y;

    //  Constructor
    //  Parameters: position (int) x 2, tileSize (int), type of ramp (int), hookability (boolean), climability (boolean)
    public Tile(int x, int y, int tileSize, int rampType, boolean isHookable, boolean isClimable) {
        this.x = x;
        this.y = y;
        this.tileSize = tileSize;

        this.rampType = rampType;
        this.isHookable = isHookable;
        this.isClimable = isClimable;

        hitbox = new Rectangle(x, y, tileSize, tileSize);
    }

    public Tile() {
        
    }

    //  Getters
    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getRampType() {
        return rampType;
    }

    public int getSize() {
        return tileSize;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public boolean hasLeft() {
        return left;
    }

    public boolean hasRight() {
        return right;
    }

    public boolean hasTop() {
        return top;
    }

    public boolean hasBot() {
        return bot;
    }

    public boolean canHook() {
        return isHookable;
    }

    public boolean canClimb() {
        return isClimable;
    }

    //  Setters
    public void setLeft(Boolean bool) {
        left = bool;
    }

    public void setRight(Boolean bool) {
        right = bool;
    }

    public void setTop(Boolean bool) {
        top = bool;
    }

    public void setBot(Boolean bool) {
        bot = bool;
    }
}
