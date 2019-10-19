import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;
import greenfoot.MouseInfo;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import greenfoot.core.WorldHandler;

/**
 * Palette
 * <p>
 * A Color choosing GUI, including:<p>
 * 4 Sliders for changing the Red, Green, Blue, and Alpha values of a Color.<p>
 * 24 memory slots. Clicking on the slots will change which one will be selected.<p>
 * A "Find Color" Button that will "look" at the pixel where the mouse is (if inside the World). Clicking will then grab the Color at the mouse's position. Pressing escape will cancel it.<p>
 * There is the possibility that you have other Actors in the World that react to being clicked on, though you may wish to get a Color off of their images and don't want to invoke the Actors' behavior. To overcome this, inside of your World class constructor have a setPaintOrder() method with "Palette.Cover.class" as the first parameter.
 * <p>
 * Action listener: getResult()
 * 
 * @author Taylor Born
 * @version November 2011 - March 2014
 */
public class Palette extends Window
{
    private static GreenfootImage trans = new GreenfootImage(30, 30);
    private static Cover cover = new Cover();

    static
    {
        trans.setColor(Color.WHITE);
        trans.fill();
        trans.setColor(new Color(214, 214, 214));
        for (int i = 0; i < 4; i++)
            for (int k = 0; k < 4; k++)
                if (i % 2 == 0 ? k % 2 == 0 : k % 2 != 0)
                    trans.fillRect(k * 8, i * 8, 8, 8);
        trans.setColor(Color.BLACK);
        trans.drawRect(0, 0, trans.getWidth() - 1, trans.getHeight() - 1);
    }

    private Slider redSlider = new Slider(150, 0, 255, 255, 1);
    private Slider greenSlider = new Slider(150, 0, 255, 255, 1);
    private Slider blueSlider = new Slider(150, 0, 255, 255, 1);
    private Slider alphaSlider = new Slider(150, 0, 255, 255, 1);
    
    private ColorMemoryCell[] colors = new ColorMemoryCell[24];
    private CanvasUtility colorSample = new CanvasUtility(new GreenfootImage(70, 30));
    
    private int selected = 1;
    private Button btnCancel = new Button("Cancel", new Point(44, 22));
    private Button btnFind = new Button("Find Color", new Point(63, 22));
    private boolean lookingForColor;
    private Point lastMouse = new Point(-25, -25);
    private Color result;

    /**
     * A new Palette with some default colors stored.
     */
    public Palette()
    {
        super("Palette");
        
        Container c = new Container(new Point(2, 1));
        
        Container sliderC = new Container(new Point(2, 4));
        sliderC.addComponent(new Label("R"));
        sliderC.addComponent(redSlider);
        sliderC.addComponent(new Label("G"));
        sliderC.addComponent(greenSlider);
        sliderC.addComponent(new Label("B"));
        sliderC.addComponent(blueSlider);
        sliderC.addComponent(new Label("A"));
        sliderC.addComponent(alphaSlider);
        c.addComponent(sliderC);
        
        
        Container rightC = new Container(new Point(1, 4));
        rightC.addComponent(btnFind);
        rightC.addComponent(colorSample);
        
        Container colorCellsC = new Container(new Point(6, 4), 2);
        // Initialize Color memory cells.
        for (int i = 0; i < colors.length; i++)
        {
            colors[i] = new ColorMemoryCell(Color.WHITE);
            colorCellsC.addComponent(colors[i]);
        }
        rightC.addComponent(colorCellsC);
        Container btnC = new Container(new Point(2, 1));
        btnC.addComponent(btnCancel);
        rightC.addComponent(btnC);
        c.addComponent(rightC);
        addContainer(c);
        
        colors[0].setColor(Color.WHITE);
        colors[1].setColor(Color.BLUE);
        colors[2].setColor(new Color(153, 102, 51));
        colors[3].setColor(Color.CYAN);
        colors[4].setColor(Color.GREEN);
        colors[5].setColor(Color.MAGENTA);
        colors[6].setColor(Color.ORANGE);
        colors[7].setColor(new Color(127, 0, 127));
        colors[8].setColor(Color.RED);
        colors[9].setColor(Color.YELLOW);
        colors[10].setColor(Color.BLACK);
        // Initialize Slider values.
        redSlider.setValue(colors[0].getRed());
        greenSlider.setValue(colors[0].getGreen());
        blueSlider.setValue(colors[0].getBlue());
        alphaSlider.setValue(colors[0].getAlpha());
    }
    
    /**
     * A new Palette with some default colors stored, with Color given stored and active.
     * @param c The Color the Palette will have contained within its first memory slot (being the one first selected).
     */
    public Palette(Color c)
    {
        this();
        colors[0].setColor(c);
        redSlider.setValue(c.getRed());
        greenSlider.setValue(c.getGreen());
        blueSlider.setValue(c.getBlue());
        alphaSlider.setValue(c.getAlpha());
    }
    
    /**
     * Act.
     */
    @Override
    public void act()
    {
        super.act();
        
        MouseInfo mouse = Greenfoot.getMouseInfo();
        
        // Update record of mouse location.
        if (Greenfoot.mouseDragged(null) || Greenfoot.mouseMoved(null))
            lastMouse.setLocation(mouse.getX(), mouse.getY());
        
        Point mousePos = null;
        if (Greenfoot.mousePressed(this))
            mousePos = new Point(mouse.getX() - (getX() - getImage().getWidth() / 2), mouse.getY() - (getY() - getImage().getHeight() / 2));
        if (Greenfoot.mouseClicked(null) && lookingForColor)
        {
            if (lastMouse.getY() > -1 && lastMouse.getY() < getWorld().getHeight() && lastMouse.getX() > -1 && lastMouse.getX() < getWorld().getWidth())
                setColor(new Color(WorldHandler.getInstance().getSnapShot().getRGB((int)lastMouse.getX(), (int)lastMouse.getY())));
            lookingForColor = false;
            getWorld().removeObject(cover);
        }
        
        // If looking for Color from World and its Actors.
        if (lookingForColor)
        {
            if (lastMouse.getX() > -1 && lastMouse.getX() < getWorld().getWidth() && lastMouse.getY() > -1 && lastMouse.getY() < getWorld().getHeight())
            {
                GreenfootImage pic = new GreenfootImage(70, 30);
                pic.drawRect(0, 0, 29, pic.getHeight() - 1);
                
                BufferedImage b = WorldHandler.getInstance().getSnapShot();
                for (int i = -3; i < 4; i++)
                    if (lastMouse.getY() + i > -1 && lastMouse.getY() + i < getWorld().getHeight())
                        for (int k = -3; k < 4; k++)
                            if (lastMouse.getX() + k > -1 && lastMouse.getX() + k < getWorld().getWidth())
                            {
                                pic.setColor(new Color(b.getRGB((int)lastMouse.getX() + k, (int)lastMouse.getY() + i)));
                                pic.fillRect(1 + (k + 3) * 4, 1 + (i + 3) * 4, 4, 4);
                            }
                 pic.setColor(Color.BLACK);
                 pic.drawRect(12, 12, 5, 5);
                 
                 pic.setColor(new Color(b.getRGB((int)lastMouse.getX(), (int)lastMouse.getY())));
                 pic.fillRect(40, 0, 29, 29);
                 pic.setColor(Color.BLACK);
                 pic.drawRect(40, 0, 29, 29);
                 colorSample.setImage(pic);
            }
            else
                colorSample.clear();
        }
        // Draw rectangle of selected Color.
        else
        {
            GreenfootImage pic = new GreenfootImage(70, 30);
            int mx = pic.getWidth() / 2 - trans.getWidth() / 2;
            pic.drawImage(trans, mx, 0);
            pic.setColor(colors[selected].getColor());
            pic.fillRect(mx + 1, 1, 28, 28);
            colorSample.setImage(pic);
        }
        
        for (int i = 0; i < colors.length; i++)
            if (selected != i && colors[i].mousePressedOnThisOrComponents())
            {
                colors[selected].select(false);
                colors[i].select(true);
                
                selected = i;
                redSlider.setValue(colors[i].getRed());
                greenSlider.setValue(colors[i].getGreen());
                blueSlider.setValue(colors[i].getBlue());
                alphaSlider.setValue(colors[i].getAlpha());
            }
        
        // Listen for when sliders have been changed.
        if (redSlider.hasChanged() || greenSlider.hasChanged() || blueSlider.hasChanged() || alphaSlider.hasChanged())
            // Update selected Color.
            colors[selected].setColor(new Color((int)redSlider.getValue(), (int)greenSlider.getValue(), (int)blueSlider.getValue(), (int)alphaSlider.getValue()));
        
        // Listen for Button clicks.
        if (btnFind.wasClicked())
        {
            lookingForColor = true;
            getWorld().addObject(cover, getWorld().getWidth() / 2, getWorld().getHeight() / 2);
        }
    }
    
    @Override
    protected void initializeOpen()
    {
        super.initializeOpen();
        result = null;
        if (selected != 0)
        {
            colors[selected].select(false);
            colors[0].select(true);
        }
        selected = 0;
    }
    
    /**
     * Overwriting Window's callToEscape so that if looking for a Color among the World and its Actors, pressing escape cancels this action, otherwise closes like Window.
     */
    @Override
    protected void callToEscape()
    {
        if (lookingForColor)
        {
            lookingForColor = false;
            getWorld().addObject(btnFind, getX() + 25 + 125 / 2, getY() - getImage().getHeight() / 2 + 11);
            getWorld().removeObject(cover);
        }
    }
    
    /**
     * Does what toggleShow() does but gives a Color value to be the initial selected Color.
     * @param c Color to be initially selected Color.
     * @see toggleShow()
     */
    public void toggleShow(Color c)
    {
        colors[0].setColor(c);
        redSlider.setValue(c.getRed());
        greenSlider.setValue(c.getGreen());
        blueSlider.setValue(c.getBlue());
        alphaSlider.setValue(c.getAlpha());
        toggleShow();
    }
    
    /**
     * Set the current active memory slot to the given Color and updates Sliders.
     * @param c The Color to set as active Color.
     */
    public void setColor(Color c)
    {
        colors[selected].setColor(c);
        redSlider.setValue(c.getRed());
        greenSlider.setValue(c.getGreen());
        blueSlider.setValue(c.getBlue());
        alphaSlider.setValue(c.getAlpha());
    }
    
    /**
     * Return the color from the sliders on this palette
     */
    public Color getColor()
    {
        return new Color((int)redSlider.getValue(), (int)greenSlider.getValue(), (int)blueSlider.getValue(), (int)alphaSlider.getValue());
    }
    
    private class ColorMemoryCell extends WindowComponent
    {
        private Color c;
        private boolean selected;
        
        public ColorMemoryCell(Color c)
        {
            this.c = c;
            update();
        }
        private void update()
        {
            this.c = c;
            GreenfootImage pic = new GreenfootImage(20, 20);
            pic.setColor(c);
            pic.fillRect(1, 1, pic.getWidth() - 3, pic.getHeight() - 3);
            pic.setColor(Color.BLACK);
            pic.drawRect(1, 1, pic.getWidth() - 3, pic.getHeight() - 3);
            if (selected)
            {
                pic.setColor(Color.RED);
                pic.drawRect(0, 0, pic.getWidth() - 1, pic.getHeight() - 1);
            }
            setImage(pic);
        }
        public void select(boolean s)
        {
            selected = s;
            update();
        }
        public Color getColor()
        {
            return c;
        }
        public void setColor(Color c)
        {
            this.c = c;
            update();
        }
        public int getRed()
        {
            return c.getRed();
        }
        public int getGreen()
        {
            return c.getGreen();
        }
        public int getBlue()
        {
            return c.getBlue();
        }
        public int getAlpha()
        {
            return c.getAlpha();
        }
    }
    
    private class CanvasUtility extends WindowComponent
    {
        public CanvasUtility(GreenfootImage pic)
        {
            setImage(pic);
        }
        public void clear()
        {
            getImage().clear();
        }
    }
    
    private static class Cover extends GUI_Component
    {
        public Cover()
        {
            setImage(new GreenfootImage(WorldHandler.getInstance().getWorld().getWidth(), WorldHandler.getInstance().getWorld().getHeight()));
        }
    }
}