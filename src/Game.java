//  The main game class. All game logic and game-related graphics are processed here.

//  Setup
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;

@SuppressWarnings("serial")
public class Game extends JPanel implements Runnable, KeyListener {

    static final int WINDOW_WIDTH = Driver.WINDOW_WIDTH;
    static final int WINDOW_HEIGHT = Driver.WINDOW_HEIGHT;

    // Technical variables
    final int FPS = 120;
    int tick = 0;
    int animationTick = 0;
    Thread thread;
    static boolean isPaused;

    Image offScreenImage;
    Graphics offScreenBuffer;
    boolean drawHitboxes = false;

    //  Map variables
    Map map = Driver.selectedMap;
    int selectedMap = 0;
    boolean onRamp = false;

    // Player variables
    Player player = new Player();
    Rectangle playerHitbox = player.getHitbox();
    int xCoord = map.getSpawn()[0] * 32;
    int yCoord = map.getSpawn()[1] * 32;

    // Physics variables
    final double xAccel = 0.2;
    final double xDecel = 0.23;
    final double GRAV = 0.18;
    final int rampVel = 6;

    int xPos = coordToPos(xCoord, 'x');
    int yPos = coordToPos(yCoord, 'y');

    double xVel = 0;
    double xVelMaxDefault = 8;
    double xVelMax = xVelMaxDefault;

    double yVel = 0;
    double jumpVel = 8.5;
    double termVel = 8;
    double yVelMax = termVel;
    boolean canDoubleJump = true;
    boolean canWallJump = false;

    boolean jump, left, right;
    boolean jumpHeld = false;
    boolean isAirborne = false;
    boolean isJumping;
    boolean isDoubleJumping;

    //  Grappling hook
    int xRoot, yRoot, xEnd, yEnd;
    int hookRange = 480;
    double hookLength;
    double hookAngle;
    double principle;
    int shootVel = 20;
    double hookVel = 0;
    final double GRAV_HOOK = 0.03;
    double xVelHook = 0;
    double yVelHook = 0;

    int grappleDirection;
    int swingDirection = 1;     //  1 for normal, -1 for backwards
    boolean buttonHeld = false;
    boolean isHooked = false;
    boolean isShoot = false;
    boolean released = true;

    //  Special
    double boostVelMax = 16;
    double boostDecel = 0.05;

    double jumpPadVel = 14;

    boolean flagHit = false;

    //  Timer
    int timer = 0;
    boolean isTiming = true;
    JLabel timerLabel;
    Font timerFont;
    boolean isHighScore = false;

    //  UI
    boolean countdown = true;
    boolean isFinished = false;
    Image go, one, two, three, finish, newBest;

    static JJButton resume, exit;
    static JJButton back;
    static JJButton again, mainmenu;
    static JLabel endTime;

    boolean pauseDrawn = false;
    boolean finishDrawn = false;

    //  Converts coordinates to where the surroundings should be placed (player centered)
    //  Return type: Returns position (int)
    //  Parameters: Coord to convert, x or y axis
    public int coordToPos(int coord, char axis) {
        if (axis == 'x') 
            return WINDOW_WIDTH / 2 - coord;
        else
            return 448 - coord;
    }

    //  Converts coordinates to static positions on the window
    //  Return type: Returns window coords (int)
    //  Parameters: Coord to convert, x or y axis
    public int coordToWindow(int coord, char axis) {
        if (axis == 'x') 
            return coord + xPos;
        else
            return coord + yPos;
    }

    //  Keyboard input
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        //  Left
        if (key == KeyEvent.VK_A && !isHooked && !isPaused) {
            left = true;
            right = false;
            player.setDirection(-1);
        }
        //  Right
        else if (key == KeyEvent.VK_D && !isHooked && !isPaused) {
            right = true;
            left = false;
            player.setDirection(1);
        }
        //  Jump
        else if (key == KeyEvent.VK_W && !jumpHeld && !isPaused) {
            jump = true;
            jumpHeld = true;
        }
        //  Grapple
        else if (key == KeyEvent.VK_G && !isPaused) {
            if (!isShoot && !isHooked && !buttonHeld) {
                grappleDirection = player.getDirection();
                //  Hook fires from centre of player hitbox
                xRoot = xCoord + player.getWidth() / 2 * (1 + player.getDirection()) + player.getDirection() * 10;
                yRoot = yCoord;

                //  Creates hook head
                xEnd = xRoot;
                yEnd = yRoot;

                isShoot = true;
                buttonHeld = true;
                released = false;
            }
        }
        //  Toggle hitboxes (for testing)
        else if (key == KeyEvent.VK_H) {
            drawHitboxes = !drawHitboxes;
        }
        //  Pause
        else if (key == KeyEvent.VK_ESCAPE && !countdown && !flagHit) {
            if (pauseDrawn) {
                clearComponents();
                isPaused = false;
                map.getMusic().loop(Clip.LOOP_CONTINUOUSLY);
            }
            else {
                isPaused = true;
                drawPause();
                map.getMusic().stop();
            }
        }
        //  O and P are here for testing purposes
        else if (key == KeyEvent.VK_O) {
            System.out.println();
        }
        else if (key == KeyEvent.VK_P) {
            map.addHighscore((int)(Math.random() * (100 - 1) + 1));
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    //  Keyboard input
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_A) {
            left = false;
        } else if (key == KeyEvent.VK_D) {
            right = false;
        } else if (key == KeyEvent.VK_W) {
            jump = false;
            jumpHeld = false;
        } else if (key == KeyEvent.VK_G) {
            isHooked = false;
            buttonHeld = false;
            released = true;
        }
    }

    //  Deals with regular character movement
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void move() {
        //  Normal movement
        if (!isHooked) {
            //  Horizontal movement
            if (noTiles(2, 'l') && noTiles(3, 'l') && noTiles(4, 'l') && left && xVel > -xVelMax) {
                xVel -= xAccel;
            } 
            else if (noTiles(2, 'r') && noTiles(3, 'r') && noTiles(4, 'r') && right && xVel < xVelMax) {
                xVel += xAccel;
            }

            if (xVel != 0)
                manageSpeed();

            //  Vertical movement

            //  Wall jump
            if (canWallJump && jump) {
                yVel = jumpVel;
                canWallJump = false;
                jump = false;
            }
            //  Double jump
            else if (canDoubleJump && isAirborne && jump) {
                animationTick = 0;
                isDoubleJumping = true;
                isJumping = false;
                yVel = jumpVel;
                canDoubleJump = false;
                jump = false;
            } 
            //  Jump
            else if (!isAirborne && jump) {
                animationTick = 0;
                isJumping = true;
                isAirborne = true;
                yVel = jumpVel;
                jump = false;
                onRamp = false;
            } 
            //  Gravity
            else if (isAirborne) {
                yVel -= GRAV;
            }

            //  Limit falling speed
            if (yVel < -yVelMax) {
                yVel = -yVelMax;
            }

            //  Position updated
            xCoord += xVel;
            yCoord -= yVel;

            xPos = coordToPos(xCoord, 'x');
            yPos = coordToPos(yCoord, 'y');
        }
        //  Hooked motion
        else {
            moveHooked();
        }
    }

    //  Checks if tiles of a certain type exist in a certain direction
    //  Return type: Returns if there are tiles (boolean)
    //  Parameters: Tile type to search for (int), direction to search (char)
    public boolean noTiles(int type, char direction) {

        int leftBound = xCoord - 1;
        int rightBound = xCoord + player.getWidth();
        int topBound = yCoord - 1;
        int botBound = yCoord + player.getHeight();

        //  Prevents checking outside of map
        if (xCoord / 32 <= 0 || xCoord / 32 >= map.getWidth() / 32 || yCoord / 32 <= 0 || yCoord / 32 >= map.getHeight() / 32)
            return false;

        //  Checks up to three tiles for left and right (character is 2 blocks tall so it can span three blocks)
        //  Checks up to two tiles (character is 1 block wide so it can span two blocks)
        
        //  Check left
        if (direction == 'l') {
            if (map.getArr()[yCoord / 32][leftBound / 32] != type && 
            map.getArr()[(botBound - 1) / 32][leftBound / 32] != type &&
            map.getArr()[(botBound - 1) / 32 - 1][leftBound / 32] != type)
                return true;
        }
        //  Check right
        else if (direction == 'r') {
            if (map.getArr()[yCoord / 32][rightBound / 32] != type && 
            map.getArr()[(botBound - 1) / 32][rightBound / 32] != type &&
            map.getArr()[(botBound - 1) / 32 - 1][rightBound / 32] != type)
                return true;
        }
        //  Check top
        else if (direction == 't') {
            if (map.getArr()[topBound / 32][xCoord / 32] != type && 
            map.getArr()[topBound / 32][xCoord / 32 + 1] != type)
                return true;
        }
        //  Check bottom
        else if (direction == 'b') {
            if (map.getArr()[botBound / 32][xCoord / 32] != type && 
            map.getArr()[botBound / 32][xCoord / 32 + 1] != type)
                return true;
        }
        return false;
    }

    //  Deals with grappling hook movement
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void moveHooked() {
        onRamp = false;

        //  Account for gravity
        if (isAirborne) {
            if (xCoord + player.getWidth() / 2 < xEnd)
                hookVel += GRAV_HOOK;
            else
                hookVel -= GRAV_HOOK;
        }

        //  Stop above floor
        if (!noTiles(2, 'b'))
            hookVel = 0;

        //  Adjust hook angle based on velocity
        hookAngle += hookVel;

        setPrinciple();

        //  Use trig to find xCoord
        xCoord = (int) (xEnd - Math.cos(Math.toRadians(hookAngle)) * hookLength) - player.getWidth() / 2;
        xPos = coordToPos(xCoord, 'x');

        //  Use trig to find yCoord
        yCoord = (int) (yEnd + Math.sin(Math.toRadians(hookAngle)) * hookLength) - player.getHeight() / 2;
        yPos = coordToPos(yCoord, 'y');

        //  Sets velocities so that motion is kept after unhooking
        xVel = hookVel * Math.sin(Math.toRadians(hookAngle)) * hookLength / hookRange * 12;
        yVel = -hookVel * Math.cos(Math.toRadians(hookAngle)) * hookLength / hookRange * 12;
    }

    //  Finds the positive principle angle 
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void setPrinciple() {
        //  Since % in java is remainder and not modulo, find modulo
        principle = hookAngle % 360;
        if (principle < 0)
            principle += 360;
    }

    //  Limits and controls speed
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void manageSpeed() {
        //  Decrements speed boost
        if (xVelMax > xVelMaxDefault) {
            xVelMax -= boostDecel;
            if (xVelMax < xVelMaxDefault) {
                xVelMax = xVelMaxDefault;
            }
        }

        //  Limits xVel
        if (xVel > xVelMax) {
            xVel = xVelMax;
        } 
        else if (xVel < -xVelMax) {
            xVel = -xVelMax;
        } 
        //  Decceleration
        else if (!left && !right && xVel > 0) {
            if (isAirborne)
                xVel -= xDecel / 8;
            else
                xVel -= xDecel;
            if (xVel < 0) 
                xVel = 0;
        } 
        //  Decceleration
        else if (!left && !right && xVel < 0) {
            if (isAirborne)
                xVel += xDecel / 8;
            else
                xVel += xDecel;

            if (xVel > 0) 
                xVel = 0;
        }
    }

    //  Shoots grappling hook
    //  Grappling hooks are fired at a 45 degree angle in the direction you are facing
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void shootGrapple() {
        //  Moves the hook end
        xEnd += grappleDirection * shootVel;
        yEnd -= shootVel;

        //  Keeps hook in bounds
        if (yEnd >= map.getHeight() || yEnd < 0 || xEnd >= map.getWidth() || xEnd < 0) {
            isShoot = false;
            isHooked = false;
        }
        //  Keeps hook in hookRange
        else if (Math.abs(xEnd - xRoot) > hookRange) {
            isShoot = false;
            isHooked = false;
        }
        //  Checks collisions with hookable tiles
        else if (map.getArr()[yEnd / 32][xEnd / 32] == 3) {
            isShoot = false;
            if (!released)
                isHooked = true;
            xRoot = xCoord + player.getWidth() / 2;
            yRoot = yCoord + player.getHeight() / 2;
            xEnd = xEnd / 32 * 32 + 16;
            yEnd = yEnd / 32 * 32 + 16;

            //  Find hook length (pythagorean theorem)
            hookLength = Math.sqrt(Math.pow(xEnd - xRoot, 2) + Math.pow(yEnd - yRoot, 2));

            //  Find hook angle
            hookAngle = Math.toDegrees(Math.acos((xEnd - (xCoord + player.getWidth() / 2)) / hookLength));

            //  Set initial velocity in hook movement
            if (xCoord + player.getWidth() / 2 < xEnd) {
                hookVel = Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2)) * hookRange / hookLength / 10;
                if (yVel > 0 && yVel > Math.abs(xVel) || xVel < 0)
                    hookVel = -hookVel;
            }
            else {
                hookVel = -Math.sqrt(Math.pow(xVel, 2) + Math.pow(yVel, 2)) * hookRange / hookLength / 10;
                if (yVel > 0 && yVel > Math.abs(xVel) || xVel > 0)
                    hookVel = -hookVel;
            }

            canDoubleJump = true;
        } 
        //  Stops hook if it hits a non-air tile
        else if (map.getArr()[yEnd / 32][xEnd / 32] != 0) {
            isShoot = false;
            isHooked = false;
        }
    }

    //  Checks for tile collisions
    //  Theres a lot of clutter here because I need to check all directions for many kinds of movement (regular, hooked)
    //  It's hard to follow (even for me) but I spent lots of time thinking about it so just trust the numbers work (:
    //  Return type: Changes global variables (void)
    //  Parameters: Tile to check (Tile)
    public void checkCollision(Tile tile) {
        Rectangle tileHitbox = tile.getHitbox();

		if (playerHitbox.intersects(tileHitbox)) {
            onRamp = false;
            
            //  Bounds of two hitboxes
            //  1 indicates player, 2 indicates tile
			double left1 = playerHitbox.getX();
			double right1 = playerHitbox.getX() + playerHitbox.getWidth();
			double top1 = playerHitbox.getY();
			double bot1 = playerHitbox.getY() + playerHitbox.getHeight();
			double left2 = tileHitbox.getX();
			double right2 = tileHitbox.getX() + tileHitbox.getWidth();
			double top2 = tileHitbox.getY();
            double bot2 = tileHitbox.getY() + tileHitbox.getHeight();
            
            //  Regular collisions
            if (!isHooked) {
                //  Square tiles
                if (tile.getRampType() == 0) {

                    //  Player collides from left
                    if (tile.hasLeft() &&
                        right1 >= left2 && 
                        left1 <= left2 && 
                        right1 - left2 < bot1 - top2 && 
                        right1 - left2 < bot2 - top1) {

                        xCoord = tile.getX() - player.getWidth();
                        xPos = coordToPos(xCoord, 'x');
                        xVel = 0;
                        if (tile.canClimb())
                            canWallJump = true;
                    }
                    //  Player collides from right
                    else if (tile.hasRight() && 
                        left1 <= right2 &&
                        right1 >= right2 && 
                        right2 - left1 < bot1 - top2 && 
                        right2 - left1 < bot2 - top1) {

                        xCoord = tile.getX() + 32;
                        xPos = coordToPos(xCoord, 'x');
                        xVel = 0;
                        if (tile.canClimb())
                            canWallJump = true;
                    }
                    //  Player collides from top
                    else if (tile.hasTop() && bot1 > top2 && top1 < top2) {
                        yCoord = tile.getY() - player.getHeight();
                        yPos = coordToPos(yCoord, 'y');
                        isAirborne = false;
                        isJumping = false;
                        isDoubleJumping = false;
                        canDoubleJump = true;
                        yVel = 0;
                    }
                    //  Player collides from bottom
                    else if(tile.hasBot() && top1 < bot2 && bot1 > bot2) {
                        yCoord = tile.getY() + 32;
                        yPos = coordToPos(yCoord, 'y');
                        yVel = 0;
                    }
                    hookVel = 0;
                }
                //  Ramps
                //  Ramps still have square hitboxes
                //  Ramp movement is achieved by converting the amount of horizontal penetration to vertical movement
                else if (tile.getRampType() == 1) {
                    int penetration = (int) (right1 - left2);
                    
                    //  Player collides from ramp side
                    if (right1 >= left2 && left1 <= right2 && yCoord >= tile.getY() + tile.getSize() - penetration - player.getHeight()) {
                        if (right1 >= right2) {
                            yCoord = tile.getY() - player.getHeight();
                        }
                        else {
                            yCoord = tile.getY() + tile.getSize() - penetration - player.getHeight() - rampVel;
                        }
                        yPos = coordToPos(yCoord, 'y');
                        yVel = 0;
                        onRamp = true;
                        isAirborne = false;
                        isJumping = false;
                        canDoubleJump = true;
                        isDoubleJumping = false;
                    }
                }
                else if (tile.getRampType() == 2) {
                    int penetration = (int) (right2 - left1);
                    
                    //  Player collides from ramp side
                    if (left1 <= right2 && right1 >= left2 && yCoord >= tile.getY() + tile.getSize() - penetration - player.getHeight()) {
                        if (left1 <= left2) {
                            yCoord = tile.getY() - player.getHeight();
                        }
                        else {
                            yCoord = tile.getY() + tile.getSize() - penetration - player.getHeight() - rampVel;
                        }
                        yPos = coordToPos(yCoord, 'y');
                        yVel = 0;
                        onRamp = true;
                        isAirborne = false;
                        isJumping = false;
                        canDoubleJump = true;
                        isDoubleJumping = false;
                    }
                }
                else if (tile.getRampType() == 3) {
                    int penetration = (int) (right1 - left2);
                    
                    //  Player collides from ramp side
                    if (right1 >= left2 && left1 <= right2 && yCoord <= tile.getY() + penetration) {
                        if (right1 >= right2) {
                            yCoord = tile.getY() + tile.getSize() + 1;
                        }
                        else {
                            yCoord = tile.getY() + penetration + 1;
                        }
                        yPos = coordToPos(yCoord, 'y');
                        yVel = 0;
                    }
                }
                else if (tile.getRampType() == 4) {
                    int penetration = (int) (right2 - left1);
                    
                    //  Player collides from ramp side
                    if (left1 <= right2 && right1 >= left2 && yCoord <= tile.getY() + penetration) {
                        if (left1 <= left2) {
                            yCoord = tile.getY() + tile.getSize() + 1;
                        }
                        else {
                            yCoord = tile.getY() + penetration + 1;
                        }
                        yPos = coordToPos(yCoord, 'y');
                        yVel = 0;
                    }
                }
            }
            //  Hooked collisions
            else {
                //  Player collides from left
                if (tile.hasLeft() &&
                    right1 >= left2 && 
                    left1 <= left2 && 
                    right1 - left2 < bot1 - top2 && 
                    right1 - left2 < bot2 - top1) {

                    if (principle < 180) {
                        hookAngle = Math.toDegrees(Math.acos((xEnd - (tile.getX() - player.getWidth() / 2)) / hookLength));
                    }
                    else if (principle < 360) {
                        hookAngle = -Math.toDegrees(Math.acos((xEnd - (tile.getX() - player.getWidth() / 2)) / hookLength));
                    }
                }
                //  Player collides from right
                else if (tile.hasRight() && 
                    left1 <= right2 &&
                    right1 >= right2 && 
                    right2 - left1 < bot1 - top2 && 
                    right2 - left1 < bot2 - top1) {
                        
                    if (principle < 180) {
                        hookAngle = Math.toDegrees(Math.acos((xEnd - (tile.getX() + tile.getSize() + player.getWidth() / 2)) / hookLength));
                    }
                    else if (principle < 360) {
                        hookAngle = -Math.toDegrees(Math.acos((xEnd - (tile.getX() + tile.getSize() + player.getWidth() / 2)) / hookLength));
                    }
                }
                //  Player collides from top
                else if (tile.hasTop() && bot1 > top2 && top1 < top2) {
                    if (principle < 90 || principle > 270) {
                        hookAngle = -Math.toDegrees(Math.asin((yEnd - (tile.getY() - player.getHeight() / 2)) / hookLength));
                    }
                    else if (principle > 90 && principle < 270) {
                        hookAngle = 180 + Math.toDegrees(Math.asin((yEnd - (tile.getY() - player.getHeight() / 2)) / hookLength));
                    }
                }
                //  Player collides from bottom
                else if (tile.hasBot() && top1 < bot2 && bot1 > bot2) {
                    if (principle < 90 || principle > 270) {
                        hookAngle = Math.toDegrees(Math.asin((tile.getY() + tile.getSize() + player.getHeight() / 2 - yEnd) / hookLength));
                    }
                    else if (principle > 90 && principle < 270) {
                        hookAngle = 180 - Math.toDegrees(Math.asin((tile.getY() + tile.getSize() + player.getHeight() / 2  - yEnd) / hookLength));
                    }
                }
                //  Rebounds with one tenth of the impact speed
                hookVel = -hookVel / 10;
                setPrinciple();
                
                //  Unhooks when touching floor
                if (tile.getRampType() == 1 || tile.getRampType() == 2)
                    isHooked = false;
            }
        }
    }

    //  Checks for collisions with special tiles
    //  Return type: Changes global variables (void)
    //  Parameters: Tile to check (SpecialTile)
    public void checkSpecial(SpecialTile tile) {
        Rectangle tileHitbox = tile.getHitbox();

        if (playerHitbox.intersects(tileHitbox)) {
            //  Finish flag
            if (tile.getType() == 0 && flagHit == false) {
                flagHit = true;
                isTiming = false;
                isPaused = true;

                //  Sees if the score is a highscore
                if (map.getHighscores(1).size() == 0 || timer < map.getHighscores(1).get(0))
                    isHighScore = true;

                //  Adds time to scores
                map.addHighscore(timer);
                drawFinish();
            }
            //  Speed boost
            else if (tile.getType() == 1 || tile.getType() == 2) {
                xVelMax = boostVelMax;
            }
            //  Jump pad
            else if (tile.getType() == 3) {
                yVel = jumpPadVel;
            }
        }
    }

    //  Draws pause menu
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void drawPause() {
        pauseDrawn = true;

        add(resume);
        add(exit);
    }

    //  Draws finish screen
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void drawFinish() {
        finishDrawn = true;

        endTime.setText(timerLabel.getText());
        
        add(again);
        add(mainmenu);
        add(endTime);
    }

    //  Removes JComponents (removeAll() is wonky)
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void clearComponents() {
        pauseDrawn = false;
        finishDrawn = false;

        remove(resume);
        remove(exit);

        remove(back);

        remove(again);
        remove(mainmenu);
    }

    //  PaintComponent for graphics
    //  Return type: Changes global variables (void)
    //  Parameters: Graphics variable
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        //  Set up the offscreen buffer the first time paint() is called
        //  Draws all images once to prevent future flickering (not sure why this happens)
        if (offScreenBuffer == null) {
            offScreenImage = createImage(WINDOW_WIDTH, WINDOW_HEIGHT);
            offScreenBuffer = offScreenImage.getGraphics();

            //  Draws all images off-screen
            for (int i = 0; i < 2; i++) {
                for (Image img: player.still()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
                for (Image img: player.run()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
                for (Image img: player.jump()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
                for (Image img: player.doubleJump()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
                for (Image img: player.fall()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
                for (BufferedImage img: player.grapple()[i])
                    offScreenBuffer.drawImage(img, -100, -100, 100, 100, this);
            }
            offScreenBuffer.drawImage(map.getBackground(), -100, -100, 100, 100, this);
        }

        //  Set background
        offScreenBuffer.drawImage(map.getBackground(), 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, this);

        Graphics2D g2 = (Graphics2D) offScreenBuffer;

        //  Map
        offScreenBuffer.drawImage(map.getImage(), xPos, yPos, map.getWidth(), map.getHeight(), this);

        int d;
        if (player.getDirection() == -1)
            d = 0;
        else
            d = 1;

        //  Draw player
        //  Shooting hook
        if (isShoot) {
            offScreenBuffer.drawImage(player.still()[d][0], 
            coordToWindow(xCoord, 'x') - 34, coordToWindow(yCoord, 'y') + 64 - 90, 100, 90, this);
        }
        //  Hooked
        else if (isHooked) {
            // Rotation information
            double rotation;
            int model;

            if (player.getDirection() == -1) {
                rotation = Math.toRadians(10 - hookAngle);
                if (hookAngle < 90 || hookAngle > 270)
                    model = 1;
                else
                    model = 0;
            }
            else {
                rotation = Math.toRadians(170 - hookAngle);
                if (hookAngle < 90 || hookAngle > 270)
                    model = 0;
                else
                    model = 1;
            }

            //  Rotate player
            AffineTransform tx = AffineTransform.getRotateInstance(rotation, 500, 450);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

            // Draw rotated image
            if (player.getDirection() == - 1)
                offScreenBuffer.drawImage(op.filter(player.grapple()[d][model], null), 
                coordToWindow(xCoord, 'x') - 95, coordToWindow(yCoord, 'y') - 75, 200, 180, this);
            else
                offScreenBuffer.drawImage(op.filter(player.grapple()[d][model], null), 
                coordToWindow(xCoord, 'x') - 50, coordToWindow(yCoord, 'y') - 75, 200, 180, this);
        }
        //  Jump animation
        else if (isJumping) {
            offScreenBuffer.drawImage(player.jump()[d][animationTick / 3 % player.jump()[d].length], 
            coordToWindow(xCoord, 'x') - 34, coordToWindow(yCoord, 'y') + 64 - 90, 100, 90, this);
            animationTick++;

            //  Stops animation
            if (animationTick >= 33) {
                isJumping = false;
                animationTick = 0;
            }
        }
        //  Double jump animation
        else if (isDoubleJumping) {
            offScreenBuffer.drawImage(player.doubleJump()[d][animationTick / 3 % player.doubleJump()[d].length], 
            coordToWindow(xCoord, 'x') - 34, coordToWindow(yCoord, 'y') + 64 - 90, 100, 90, this);
            animationTick++;

            //  Stops animation
            if (animationTick >= 33) {
                isDoubleJumping = false;
                animationTick = 0;
            }
        }
        //  Fall animation
        else if (isAirborne) {
            offScreenBuffer.drawImage(player.fall()[d][animationTick / 5 % player.fall()[d].length], 
            coordToWindow(xCoord, 'x') - 34, coordToWindow(yCoord, 'y') + 64 - 90, 100, 90, this);
            animationTick++;

            //  Repeats animation
            animationTick %= 35;
        }
        //  Run animation
        else if (left || right)
            offScreenBuffer.drawImage(player.run()[d][tick / 5 % player.run()[d].length], 
            coordToWindow(xCoord, 'x') + 16 - 45, coordToWindow(yCoord, 'y') + 64 - 72, 90, 72, this);
        //  Stand model
        else {
            offScreenBuffer.drawImage(player.still()[d][1], 
            coordToWindow(xCoord, 'x') - 34, coordToWindow(yCoord, 'y') + 64 - 90, 100, 90, this);
        }

        //  Tile hitboxes
        for (Tile tile: map.getTiles()) {
            if (drawHitboxes) {
                g2.setColor(Color.CYAN);
                g2.fill(tile.getHitbox());
            }
        }

        //  Special tile hitboxes
        for (SpecialTile tile: map.getSpecial()) {
            if (drawHitboxes) {
                g2.setColor(Color.YELLOW);
                g2.fill(tile.getHitbox());
            }
        }

        //  Player hitbox
        if (drawHitboxes) {
            g2.setColor(Color.MAGENTA);
            g2.fill(playerHitbox);
        }

        //  Grapple
        if (isShoot || isHooked) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(5));
            g2.drawLine(coordToWindow(xCoord, 'x') + player.getWidth() / 2 * (1 + player.getDirection()) + player.getDirection() * 10, 
                coordToWindow(yCoord, 'y'), 
                coordToWindow(xEnd, 'x'), coordToWindow(yEnd, 'y'));
        }

        //  Moves offScreenImage onto screen
        g.drawImage(offScreenImage, 0, 0, this);

        //  Draws timer
        if (!countdown) {
            g.setColor(Color.RED.darker());
            g.fillRect(WINDOW_WIDTH / 2 - 70, 10, 162, 70);

            g.setColor(Color.BLACK);
            g.fillRect(WINDOW_WIDTH / 2 - 60, 20, 142, 50);
        }

        //  Tints screen dark
        if (isPaused) {
            g.setColor(new Color(4, 4, 40, 175));
            g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        //  Draw countdown
        if (countdown) {
            if (tick <= 120)
                g.drawImage(three, WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2 - 120, 240, 240, this);
            else if (tick <= 240)
                g.drawImage(two, WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2 - 120, 240, 240, this);
            else if (tick <= 360)
                g.drawImage(one, WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2 - 120, 240, 240, this);
            else if (tick <= 480)
                g.drawImage(go, WINDOW_WIDTH / 2 - 120, WINDOW_HEIGHT / 2 - 120, 240, 240, this);
            
            //  Finishes countdown and starts the game
            else {
                countdown = false;
                isPaused = false;
                map.getMusic().setFramePosition(0);
                map.getMusic().loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        //  Draw finish
        if (finishDrawn) {
            g.drawImage(finish, WINDOW_WIDTH / 2 - 240 + 20, WINDOW_HEIGHT / 2 - 240, 480, 240, this);

            //  Draws newBest if score is a highscore
            if (isHighScore) {
                g.drawImage(newBest, WINDOW_WIDTH / 2 - 240, WINDOW_HEIGHT / 2 - 240, 120, 60, this);
            }

            g.setColor(new Color(4, 4, 40));
            g.fillRect(WINDOW_WIDTH / 2 - 125, WINDOW_HEIGHT / 2 - 13, 250, 75);
        }
    }

    //  Code that needs to be run every loop
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void update() {
        //  Adds tiles and checks for tile collisions
        for (Tile tile: map.getTiles()) {
            tile.getHitbox().x = coordToWindow(tile.getX(), 'x');
            tile.getHitbox().y = coordToWindow(tile.getY(), 'y');
            checkCollision(tile);
        }

        //  Adds special tilesCheck for special tile collisions
        for (SpecialTile tile: map.getSpecial()) {
            tile.getHitbox().x = coordToWindow(tile.getX(), 'x');
            tile.getHitbox().y = coordToWindow(tile.getY(), 'y');
            checkSpecial(tile);
        }

        //  Updates onRamp
        if (noTiles(5, 'b') && noTiles(6, 'b'))
            onRamp = false;

        //  Checks for movement off of a platform by checking the tile below the player
        if (noTiles(2, 'b')) {
            if (onRamp)
                yVel = -rampVel;
            else
                isAirborne = true;
        }

        //  Checks for grappling
        if (isShoot)
            shootGrapple();
        
        if (xVel == 0 && yVel == 0) {
            isHooked = false;
        }

        //  Checks if wall jump is possible
        if (noTiles(4, 'l') && noTiles(4, 'r'))
            canWallJump = false;

        //  Updates timer every tenth of a second
        if (isTiming && tick % (FPS / 10) == 0) {
            timer++;
            timerLabel.setText(String.format("%d:%02d.%d", timer / 600, (timer / 10) % 60, timer % 10));
        }
    }

    //  Code that needs to be run at the very beginning
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void setUp() {

        //  Load font
        try {
            timerFont = Font.createFont(Font.TRUETYPE_FONT, new File("fonts/Digital.ttf")).deriveFont(40f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(timerFont);
        } catch (IOException | FontFormatException e) {
            System.out.println(e);
        }

        //  Create menu buttons

        //  Pause menu   -------------------------------------------------------------------------------------------

        //  Switches back to game panel
        resume = new JJButton("RESUME");
        resume.setBounds((WINDOW_WIDTH - 200) / 2, (WINDOW_HEIGHT - 50) / 2 - 55, 200, 50);
        resume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearComponents();
                isPaused = false;
                map.getMusic().loop(Clip.LOOP_CONTINUOUSLY);
            }
        });

        //  Returns to main menu
        exit = new JJButton("EXIT", 0);
        exit.setBounds((WINDOW_WIDTH - 200) / 2, (WINDOW_HEIGHT - 50) / 2, 200, 50);

        //  Options menu   -------------------------------------------------------------------------------------------

        //  Switches back to previous panel
        back = new JJButton("BACK");
        back.setBounds((WINDOW_WIDTH - 200) / 2, (WINDOW_HEIGHT - 50) / 2, 200, 50);
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearComponents();

                drawPause();
            }
        });

        //  Finish screen   -------------------------------------------------------------------------------------------
        
        //  Starts a new game
        again = new JJButton("PLAY AGAIN");
        again.setBounds(WINDOW_WIDTH / 2 - 210, WINDOW_HEIGHT / 2 + 100, 200, 50);
        again.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearComponents();

                map.getMusic().stop();

                Driver.changeScreen(3);
            }
        });

        //  Returns to main menu
        mainmenu = new JJButton("EXIT");
        mainmenu.setBounds(WINDOW_WIDTH / 2 + 10, WINDOW_HEIGHT / 2 + 100, 200, 50);
        mainmenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearComponents();

                map.getMusic().stop();

                Driver.changeScreen(0);
            }
        });

        //  Label shows final time
        endTime = new JLabel();
        endTime.setHorizontalAlignment(SwingConstants.CENTER);
        endTime.setFont(timerFont.deriveFont(80f));
        endTime.setForeground(Color.WHITE);
        endTime.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        endTime.setBounds(WINDOW_WIDTH / 2 - 150, WINDOW_HEIGHT / 2 - 50, 300, 150);




        //  Add timer
        timerLabel = new JLabel();
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timerLabel.setFont(timerFont);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        timerLabel.setBounds(WINDOW_WIDTH / 2 - 65, 12, 150, 75);
        add(timerLabel);

        //  Load UI Images
        go = Toolkit.getDefaultToolkit().getImage("images/go.png");
        one = Toolkit.getDefaultToolkit().getImage("images/one.png");
        two = Toolkit.getDefaultToolkit().getImage("images/two.png");
        three = Toolkit.getDefaultToolkit().getImage("images/three.png");
        finish = Toolkit.getDefaultToolkit().getImage("images/finish.png");
        newBest = Toolkit.getDefaultToolkit().getImage("images/newBest.png");
    }
    
    //  Main game loop
    public void run() {
        setUp();

        while (true) {
            tick++;
            if (!isPaused) {
                update();
                move();
            }
            this.repaint();
            try {
                Thread.sleep(1000 / FPS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //  Constructor
    public Game() {
        isPaused = true;

        setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        setFocusable(true);
        setLayout(null);
        
        addKeyListener(this);

        thread = new Thread(this);
        thread.start();
    }
}
