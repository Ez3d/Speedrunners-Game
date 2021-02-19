//  Modified JButton class so that all buttons are created with sound effects, colors, etc.

//  Setup
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import javax.sound.sampled.*;

@SuppressWarnings("serial")
public class JJButton extends JButton {

    //  Sound effects
    static Clip hover1, hover2, click;

    //  Colors
    private Color backgroundColor = new Color(4, 4, 40);
    private Color pressedBackgroundColor = new Color(248, 185, 105);
    
    //  Constructor for buttons that only redirect when clicked
    //  Parameters: Button text, screen to redirect to
    public JJButton(String name, int nextScreen) {
        super(name);
        super.setContentAreaFilled(false);

        //  Screen change
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Driver.changeScreen(nextScreen);
            }
        });

        setUp();
    }

    //  Constructor for buttons that do more than just redirect when clicked
    //  Parameters: Button text
    public JJButton(String name) {
        super(name);
        super.setContentAreaFilled(false);

        setUp();
    }

    //  Applies button attributes
    //  Return type: Changes global variables (void)
    //  Parameters: No parameter
    public void setUp() {
        addSounds();
        updateVolume();

        setBackground(backgroundColor);
        setForeground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(Driver.thunderstorm);
        setBorderPainted(false);
        setFocusable(false);
        setFocusPainted(false);
    }

    //  Adds sound effects
    //  Return type: Changes global variables (void)
    //  Parameters: No parameters
    public void addSounds() {
        //  Load sounds
        try {
            AudioInputStream stream;

            //  Sound effects
            //  Hover is duplicated to fix overlapping sound issues
            stream = AudioSystem.getAudioInputStream(new File("sounds/hover.wav"));
            hover1 = AudioSystem.getClip();
            hover1.open(stream);
            stream = AudioSystem.getAudioInputStream(new File("sounds/hover.wav"));
            hover2 = AudioSystem.getClip();
            hover2.open(stream);

            stream = AudioSystem.getAudioInputStream(new File("sounds/click.wav"));
            click = AudioSystem.getClip();
            click.open(stream);
        }
        catch (UnsupportedAudioFileException e) {
            System.out.println("File not supported!");
        }
        catch (Exception e) {
            System.out.println(e);
        }

        //  Adds sounds to mouse click and hover
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                click.stop();
                click.setFramePosition(0);
                click.start();
            }
            
            public void mouseEntered(MouseEvent e) {
                //  Starts second clip if the first one is still playing (reduces stutter)
                if (hover1.isActive()) {
                    hover2.setFramePosition(0);
                    hover2.start();
                    hover1.stop();
                }
                //  Otherwise, start first clip
                else {
                    hover1.setFramePosition(0);
                    hover1.start();
                    hover2.stop();
                }
            }
            
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    public static void updateVolume() {
        FloatControl gainControl1 = (FloatControl) hover1.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl1.setValue(Driver.fxAdjust);

        FloatControl gainControl2 = (FloatControl) hover2.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl2.setValue(Driver.fxAdjust);

        FloatControl gainControl3 = (FloatControl) click.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl3.setValue(Driver.fxAdjust);
    }

    //  PaintComponenet for drawing
    //  Return type: Changes global variables (void)
    //  Parameters: Graphics variable
    protected void paintComponent(Graphics g) {
        if (getModel().isPressed())
            g.setColor(pressedBackgroundColor);
        else
            g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        super.paintComponent(g);
    }
}