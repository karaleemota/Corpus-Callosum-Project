import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;
import greenfoot.World;
import greenfoot.MouseInfo;
import greenfoot.core.WorldHandler;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Window
 * <p>
 * A boxed screen that has a title and a close Button.<p>
 * When added to World, becomes the top Window.<p>
 * Acts as a container for Containers. When this Window is pressed on by mouse, it is brought to the front (with its current Container) and becomes top Window.<p>
 * Can be clicked and dragged around the World (but is kept from reaching outside the World), keeping its current Container kept in its appropriate location relative to this Window.<p>
 * Clicking close Button, or if is top Window and press escape will close this Window. (Removing itself and its current Container from the World).<p>
 * 
 * @author Taylor Born
 * @version February 2013 - April 2014
 */
public abstract class Window extends GUI_Component
{
    private static Window topWindow;
    private static boolean escapePressed;

    /**
     * Get the Window on top of all others.
     * @return The Window on top of all others.
     */
    public static Window getTopWindow()
    {
        return topWindow;
    }
    
    public static boolean escapeClosedWindow()
    {
        if (escapePressed)
        {
            if (topWindow == null)
                escapePressed = false;
            return true;
        }
        return false;
    }
    
    public static void fixWindowDistribution()
    {
        for (Window w : (List<Window>)WorldHandler.getInstance().getWorld().getObjects(Window.class))
            w.bringToFront();
    }

    private String title;
    private Point size;
    private Point originalSize;
    private Point pressedAt;
    private boolean dragging;
    private Point pos;
    private boolean alwaysOpenToDefault;
    private boolean closeWhenLoseFocus;
    private boolean bringingToFront;
    private Button btnClose = new Button("X", new Point(13, 13));
    private Menu menu;
    private Point menuSnug = new Point(0, 0);
    private ArrayList<Window> helperWindows = new ArrayList<Window>();
    private ArrayList<Container> containers = new ArrayList<Container>();
    private int currentContainer = -1;

    /**
     * Create a new Window.
     * @param title String that appears at the top left corner of the Window.
     * @param alwaysOpenToDefault Whether or not this Window will always relocate to its default location when added to the World.
     * @param closeWhenLoseFocus Whether or not this Window will close if it loses focus. (And its current Container does not have focus).
     * @param minWidth The minimum width of the Window.
     * @param minHeight The mimimum height of the Window.
     * @see getDefaultLocation()
     */
    public Window(String title, boolean alwaysOpenToDefault, boolean closeWhenLoseFocus, int minWidth, int minHeight)
    {
        this.title = title;
        Graphics2D g = (Graphics2D)(new GreenfootImage(1, 1)).getAwtImage().getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.dispose();
        size = new Point((int)Math.max(minWidth < 20 ? 20 : minWidth, titleWidth + 3 * 2 + btnClose.getGUIWidth()), minHeight < 20 ? 20 : minHeight);
        keepSizeInWorld();
        originalSize = new Point(size);
        setImage(draw());
        pos = getDefaultLocation();
        this.alwaysOpenToDefault = alwaysOpenToDefault;
        this.closeWhenLoseFocus = closeWhenLoseFocus;
    }
    
    /**
     * Create a new Window.
     * @param title String that appears at the top left corner of the Window.
     * @param alwaysOpenToDefault Whether or not this Window will always relocate to its default location when added to the World.
     * @param closeWhenLoseFocus Whether or not this Window will close if it loses focus. (And its current Container does not have focus).
     * @see getDefaultLocation()
     */
    public Window(String title, boolean alwaysOpenToDefault, boolean closeWhenLoseFocus)
    {
        this(title, alwaysOpenToDefault, closeWhenLoseFocus, 0, 0);
    }
    
    /**
     * Create a new Window.
     * @param title String that appears at the top left corner of the Window.
     * @param alwaysOpenToDefault Whether or not this Window will always relocate to its default location when added to the World.
     * @see getDefaultLocation()
     */
    public Window(String title, boolean alwaysOpenToDefault)
    {
        this(title, alwaysOpenToDefault, false, 0, 0);
    }
    
    /**
     * Create a new Window.
     * @param title String that appears at the top left corner of the Window.
     */
    public Window(String title)
    {
        this(title, false, false, 0, 0);
    }
    
    private void keepSizeInWorld()
    {
        World world = WorldHandler.getInstance().getWorld();
        if (size.getX() > world.getWidth())
            size.setLocation(world.getWidth(), (int)size.getY());
        if (size.getY() > world.getHeight())
            size.setLocation((int)size.getX(), world.getHeight());
    }
    
    /**
     * Add given Container to this Window's list of Containers.<p>
     * Given Container becomes the current Container if no Container exists before it.<p>
     * Meant to be called within constructor only.
     * @param c Container to be added.
     */
    protected void addContainer(Container c)
    {
        containers.add(c);
        if (currentContainer == -1)
            currentContainer = containers.size() - 1;
    }
    
    /**
     * Add the current Container to the World.
     */
    private void addContainerToWorld()
    {
        Container c = containers.get(currentContainer);
        getWorld().addObject(c, getX(), getY() - getImage().getHeight() / 2 + 23 + (menu != null ? 13 : 0) + c.getGUIHeight() / 2);
    }
    
    private void adjustSize()
    {
        size.setLocation((int)Math.max(getMinWidthAccordingToTitle(), originalSize.getX()), (int)originalSize.getY());
        
        if (currentContainer != -1)
        {
            Container c = containers.get(currentContainer);
            int width = c.getGUIWidth() + 3 * 2;
            if (width > size.getX())
                size.setLocation(width, (int)size.getY());
            int height = c.getGUIHeight() + 23 + 3 + (menu != null ? menu.getGUIHeight() : 0);
            if (height > size.getY())
                size.setLocation((int)size.getX(), height);
        }
        
        keepSizeInWorld();
        if (needToRedraw || getImage().getWidth() != size.getX() || getImage().getHeight() != size.getY()) {
            setImage(draw());
            if (inWorld())
                setLocation(getX(), getY());
        }
    }
    
    /**
     * Set the current Container to the i'th Container in list.
     * @param i The index from list of Containers for the current Container.
     */
    private void setContainer(int i)
    {
        if (i > -2 && i < containers.size())
            if (currentContainer != i)
            {
                if (currentContainer != -1)
                    containers.get(currentContainer).removeFromWorld();
                
                currentContainer = i;
                if (i != -1)
                    addContainerToWorld();
            }
    }
    
    /**
     * Set the current (shown) Container of this Window.
     * @param c The Container to be set as current. Null to be no current Container.
     */
    protected void setContainer(Container c)
    {
        setContainer(containers.indexOf(c));
    }
    
    protected int getMinWidthAccordingToTitle()
    {
        Graphics2D g = getImage().getAwtImage().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.dispose();
        return (int)Math.max((int)originalSize.getX(), titleWidth + 3 * 2 + btnClose.getGUIWidth());
    }
    
    public void setTitle(String t)
    {
        title = t;
        needToRedraw = true;
        adjustSize();
    }
    
    /**
     * Create a Menu to be set at the top left of the Window, just below the title.<p>
     * Meant to be called within constructor.
     * @return The Menu that was created, that can then be built (put together).
     */
    protected Menu createMenu()
    {
        Menu m = new Menu(new ArrayList<String>(), menuSnug);
        menu = m;
        return menu;
    }
    
    /**
     * Add a Window that is to close when this Window closes.<p>
     * Meant to be called within constructor.
     * @param hw Window that is to close when this Window closes.
     */
    protected void addHelperWindow(Window hw)
    {
        helperWindows.add(hw);
    }
    
    private boolean noFocusCloseDelay = true;
    
    /**
     * Act.
     * Checks if the close Button was pressed, which case the Window will close.<p>
     * Checks if "escape" key is pressed while top Window, which case will call callToEscape().<p>
     * Handles when should call bringToFront().<p>
     * Handles being dragged around the World.
     * @see bringToFront()
     */
    @Override
    public void act()
    {
        super.act();
       /* if (btnClose.wasClicked())
        {
            toggleShow();
            return;
        }*/
        if (closeWhenLoseFocus && !hasFocus())
        {
            // If this Window acts before its Components, then one of them could be clicked on, making this
            // Window lose focus, and the Component clicked on would not gain focus until it had acted, so
            // this Window would close. That is why this act cycle delay is here.
            if (noFocusCloseDelay)
                noFocusCloseDelay = false;
            else
                toggleShow();
            return;
        }
        noFocusCloseDelay = true;
        
        adjustSize();
        
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (Greenfoot.mousePressed(this))
        {
            pressedAt = new Point(getX() - mouse.getX(), getY() - mouse.getY());
            if (topWindow != this)
                bringToFront();
        }
        if (topWindow != this && mousePressedOnThisOrComponents())
            bringToFront();
        if (Greenfoot.mouseDragged(null) && pressedAt != null)
            dragging = true;
        if (dragging)
        {
            if (Greenfoot.mouseDragged(null))
                setLocation(mouse.getX() + (int)pressedAt.getX(), mouse.getY() + (int)pressedAt.getY());
            if (Greenfoot.mouseClicked(null) || Greenfoot.mouseDragEnded(null))
            {
                pressedAt = null;
                dragging = false;
            }
        }
        if (Greenfoot.mouseClicked(this))
            pressedAt = null;
        if (Greenfoot.isKeyDown("escape"))
        {
            if (isTopWindow() && !escapePressed)
            {
                callToEscape();
                escapePressed = true;
            }
        }
        else
            escapePressed = false;
    }
    
    /**
     * Called when the "escape" key has been pressed. (And this Window was the top Window).<p>
     * Closes this Window.<p>
     * Overwrite to remove effect or to capture the "escape" key pressed event to handle some current state.
     */
    protected void callToEscape()
    {
        toggleShow();
    }
    
    private GreenfootImage image;
    private boolean needToRedraw = true;
    
    @Override
    protected void redraw()
    {
        needToRedraw = true;
    }
    
    /**
     * Draw a new GreenfootImage for this Window with size of this Window, with its title String drawn at the top left corner.
     * @return The GreenfootImage for this Window.
     */
    private GreenfootImage draw()
    {
        if (image == null || image.getWidth() != size.getX() || image.getHeight() != size.getY())
            image = new GreenfootImage((int)size.getX(), (int)size.getY());
        else if (backColor.getAlpha() != 255)
            image.clear();
        image.setColor(backColor);
        image.fill();
        image.setColor(borderColor);
        image.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
        image.setColor(new Color((backColor.getRed() + borderColor.getRed()) / 2, (backColor.getGreen() + borderColor.getGreen()) / 2, (backColor.getBlue() + borderColor.getBlue()) / 2));
        image.drawRect(1, 1, image.getWidth() - 3, image.getHeight() - 3);
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(textColor);
        g.setFont(font);
        g.drawString(title, 3, 14);
        g.dispose();
        needToRedraw = false;
        return image;
    }
    
    /**
     * Check if this Window or its current Container has focus.
     * @return Whether or not this Window or its current Container has focus.
     */
    @Override
    public boolean hasFocus()
    {
        return super.hasFocus() || (currentContainer != -1 && containers.get(currentContainer).hasFocus());
    }
    
    /**
     * Check if this Window or its current Container has been pressed on by the mouse.<p>
     * Called within act(), to determine if should call bringToFront().
     * @return Whether or not this Window or its current Container has been pressed on by the mouse.
     */
    public boolean mousePressedOnThisOrComponents()
    {
        if (currentContainer != -1)
            if (containers.get(currentContainer).mousePressedOnThisOrComponents())
                return true;
        return false;
    }
    
    /**
     * Set this Window to be on top of all other Windows.
     */
    public void bringToFront()
    {
        bringingToFront = true;
        
        removeFromWorld();
        addToScreen();
        
        bringingToFront = false;
    }
    protected boolean isBringingToFront()
    {
        return bringingToFront;
    }
    
    /**
     * Inherited from Actor, set the location of this Window within the World.<p>
     * Does not allow itself from reaching off the sides of the World.<p>
     * Sets appropriate locations for each WindowComponent in list.
     * @param x X-coordinate in World.
     * @param y Y-coordinate in World.
     */
    public void setLocation(int x, int y)
    {
        if (x - getImage().getWidth() / 2 < 0)
            x = getImage().getWidth() / 2;
        else if (x + getImage().getWidth() / 2 > getWorld().getWidth())
            x = getWorld().getWidth() - getImage().getWidth() / 2;
        if (y - getImage().getHeight() / 2 < 0)
            y = getImage().getHeight() / 2;
        else if (y + getImage().getHeight() / 2 > getWorld().getHeight())
            y = getWorld().getHeight() - getImage().getHeight() / 2;
        super.setLocation(x, y);
        
        if (currentContainer != -1 && containers.get(currentContainer).inWorld())
            containers.get(currentContainer).setLocation(x, y - getImage().getHeight() / 2 + 23 + (menu != null ? 13 : 0) + containers.get(currentContainer).getGUIHeight() / 2);
        
        btnClose.setLocation(x + getImage().getWidth() / 2 - 6 - (getImage().getWidth() % 2 == 0 ? 1 : 0), y - getImage().getHeight() / 2 + 6);
        snugMenu();
        
        pos = new Point(getX(), getY());
    }
    
    private void snugMenu()
    {
        menuSnug.setLocation(getX() - getImage().getWidth() / 2, getY() - getImage().getHeight() / 2 + 20);
        if (menu != null)
            menu.setLocation(0, 0);
    }
    
    /**
     * Remove this Window and its WindowComponents from the World.
     */
    public void removeFromWorld()
    {
        if (getWorld() == null)
            return;
        if (!bringingToFront)
            for (Window hw : helperWindows)
                if (hw.inWorld())
                    hw.toggleShow();
        if (currentContainer != -1)
            containers.get(currentContainer).removeFromWorld();
        btnClose.removeFromWorld();
        if (menu != null)
            menu.removeFromWorld();
        
        World world = getWorld();
        super.removeFromWorld();
        // Find next top Window.
        if (topWindow == this)
        {
            List<Window> windows = (List<Window>)world.getObjects(Window.class);
            topWindow = windows.isEmpty() ? null : windows.get(windows.size() - 1);
        }
    }
    
    /**
     * Set whethor or not this Window is to be hidden.<p>
     * If is set to show, will be added to or removed from World according to given hidden status.
     * @param h Wethor or not this Window is to be hidden.
     */
    public void hide(boolean h)
    {
        super.hide(h);
        if (willShow())
        {
            if (h)
                removeFromWorld();
            else
                addToScreen();
        }
    }
    
    /**
     * Switch between being set to show and not.<p>
     * If hidden status is false, will be added or removed from World according to new show status.
     */
    public void toggleShow()
    {
        super.toggleShow();
        if (!isHidden())
            if (willShow())
            {
                adjustSize();
                if (alwaysOpenToDefault)
                    pos = getDefaultLocation();
                addToScreen();
            }
            else
                removeFromWorld();
    }
    
    /**
     * Add this Window to the World.
     */
    private void addToScreen()
    {
        if (!inWorld())
            WorldHandler.getInstance().getWorld().addObject(this, (int)pos.getX(), (int)pos.getY());
    }
    
//     protected int getWidthOfCurrentWorld()
//     {
//         return WorldHandler.getInstance().getWorld().getWidth();
//     }
//     protected int getHeightOfCurrentWorld()
//     {
//         return WorldHandler.getInstance().getWorld().getHeight();
//     }
    
    /**
     * Get the default location at which to be added into the World.<p>
     * Default is the middle of the World.<p>
     * Overwrite to change.
     * @return The default location at which to be added into the World.
     */
    protected Point getDefaultLocation()
    {
        return new Point(WorldHandler.getInstance().getWorld().getWidth() / 2, WorldHandler.getInstance().getWorld().getHeight() / 2);
    }
    
    /**
     * Check whether or not this Window is on top of all other Windows.
     * @return Whether or not this Window is on top of all other Windows.
     */
    public boolean isTopWindow()
    {
        return this == topWindow;
    }
    
    /**
     * Inherited from Actor, is called when this Window is added to World.<p>
     * Adds each WindowComponent from list to World as well (except during duration of a bringToFront() call) if the WindowComponent is not hiding.
     * @param world World to be added to.
     * @see bringToFront()
     */
    @Override
    public void addedToWorld(World world)
    {
        super.addedToWorld(world);
        if (currentContainer != -1)
            addContainerToWorld();
        adjustSize();
        world.addObject(btnClose, getX() + getImage().getWidth() / 2 - 6 - (getImage().getWidth() % 2 == 0 ? 1 : 0), getY() - getImage().getHeight() / 2 + 6);
        if (menu != null)
        {
            world.addObject(menu, 0, 0);
            snugMenu();
        }
        if (!bringingToFront)
            initializeOpen();
        topWindow = this;
        
        noFocusCloseDelay = true;
    }
    
    /**
     * Called when the Window is added to the World (except during duration of a bringToFront() call).<p>
     * If this Window closes when it loses focus, the method gives the Window focus so to not close immediately.<p>
     * Overwrite to initialize/reset subclass statuses.
     * @see bringToFront()
     */
    protected void initializeOpen()
    {
        if (closeWhenLoseFocus)
            giveFocus();
    }
}