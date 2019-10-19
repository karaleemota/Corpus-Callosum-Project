import greenfoot.GreenfootImage;
import greenfoot.Greenfoot;
import greenfoot.MouseInfo;
import greenfoot.World;
import java.util.ArrayList;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Point;

/**
 * Menu
 * <p>
 * A GUI component that displays navigable items in a menu fashion. In that items can contain list of items.<p>
 * Implements key combination shortcuts, enable/disable, checked status among individual item or groups of items.<p>
 * Upper left corner can be pinned to a Point.<p>
 * <p>
 * Action listener: getItemPressed()
 * 
 * @author Taylor Born
 * @version March 2011 - April 2014
 */
public class Menu extends WindowComponent
{
    private static int checkMarkWidth = 16;

    private Point home = new Point(0, 0);
    private Point lastMouse = new Point(-25, -25);
    private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
    private ArrayList<MenuSet> sets = new ArrayList<MenuSet>();
    private String active = "";
    private String itemPressed = null;
    private Point origin;
    private String keysPressed = "";

    /**
     * Create the Menu with the initial Menu "headings".
     * @param menuItems This first list are the Menu items that are always displayed on the "top" 
     * bar. Menu items can be added to this "top" bar with the adding methods, just don't need 
     * parent Menu items when doing so.
     */
    public Menu(ArrayList<String> menuItems)
    {
        for (String s : menuItems)
            items.add(new MenuItem(s));
    }
    
    /**
     * Create the Menu with the initial Menu "headings", with a top left desired origin position.
     * @param menuItems This first list are the Menu items that are always displayed on the "top" 
     * bar. Menu items can be added to this "top" bar with the adding methods, just don't need 
     * parent Menu items when doing so.
     * @param p Top left desired origin position. new Point(0, 0) will make sure the Menu is snug in the top left corner of the World
     */
    public Menu(ArrayList<String> menuItems, Point p)
    {
        this(menuItems);
        origin = p;
    }
    
    @Override
    public int getGUIHeight()
    {
        return font.getSize() + 1;
    }
    
    /**
     * Act.
     */
    @Override
    public void act() 
    {
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (Greenfoot.mouseMoved(null) || Greenfoot.mouseDragged(null))
            lastMouse.setLocation(mouse.getX(), mouse.getY());
        
        int height = font.getSize();
        
        int width = 0;
        
        int barWidth = 0;
        for (MenuItem mi : items)
            barWidth += mi.getWidth();
        
        if (active.isEmpty())
            width = barWidth;
        else
        {
            for (int i = 0; i < items.size(); i++)
                if (active.startsWith(items.get(i).getName() + "/"))
                {
                    width += items.get(i).getRunningWidth(active.substring(active.indexOf("/") + 1), true);
                    height += font.getSize() + items.get(i).getRunningHeight(active.substring(active.indexOf("/") + 1));
                    break;
                }
                else
                    width += items.get(i).getWidth();
            if (barWidth > width)
                width = barWidth;
        }
        
        // Increment size by 1 to include outer frame.
        // If width becomes even, make odd so location doesn't jump a pixel back and forth.
        if (++width % 2 == 0)
            width++;
        height++;
        
        if (origin != null)
            home.setLocation((int)origin.getX() + barWidth / 2, (int)origin.getY() + font.getSize() / 2);
        
        setLocation((int)home.getX() + (width - barWidth) / 2, (int)home.getY() + (height - font.getSize()) / 2);
        
        // Location of mouse relative to the Menu's image's top left corner.
        Point offsetMouse = new Point((int)lastMouse.getX() - (getX() - (width / 2)), (int)lastMouse.getY() - (getY() - (height / 2)));
        
        boolean clicked = Greenfoot.mouseClicked(this);
        if (!clicked && Greenfoot.mouseClicked(null))
            active = "";
        
        // Check if clicked on top MenuItems.
        if (clicked && offsetMouse.getY() < font.getSize())//offsetMouse.getX() > 0 && offsetMouse.getX() < width && offsetMouse.getY() > 0 && offsetMouse.getY() < font.getSize())
        {
            int runX = 0;
            for (MenuItem mi : items)
                if (runX + mi.getWidth() > offsetMouse.getX())
                {
                    if (active.isEmpty())
                    {
                        int x = getX();
                        int y = getY();
                        World w = getWorld();
                        removeFromWorld();
                        w.addObject(this, x, y);
                        active = mi.getName() + "/";
                    }
                    else
                        active = "";
                    clicked = false;
                    break;
                }
                else
                    runX += mi.getWidth();
            if (clicked)
            {
                active = "";
                clicked = false;
            }
        }
        
        GreenfootImage image = getImage();
        if (image == null || image.getWidth() != width || image.getHeight() != height) {
            image = new GreenfootImage(width, height);
            setImage(image);
        }
        else
            image.clear();
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        
        int runWidth = 0;
        for (MenuItem barItem : items)
        {
//             image.setColor(Color.BLACK);
            
            // Check if mouse moved over this top MenuItem, which case update to be the active path.
            if (!active.isEmpty() && offsetMouse.getX() > runWidth && offsetMouse.getX() <= runWidth + barItem.getWidth() && offsetMouse.getY() > 0 && offsetMouse.getY() <= font.getSize())
            {
                active = barItem.getName() + "/";
                if (clicked)
                    itemPressed = active;
            }
            
            // If this top MenuItem is beginning of active path.
            if (active.startsWith(barItem.getName() + "/"))
            {
                // Active path past top MenuItem
                String s = active.substring(active.indexOf("/") + 1);
                
                // Keep track of extending lengths.
                int runningX = runWidth;
                int runningY = font.getSize();
                
                // Keep track of what MenuItem we are at.
                MenuItem item = barItem;
                
                // If active path stops with top MenuItem, and it has list of MenuItems to show.
                boolean sneakpeek = s.isEmpty() && item.isEnabled() && !item.getItems().isEmpty();
                
                // Keep track of running path.
                String address = item.getName() + "/";
                
                // Keep going while there is more still to active path or current MenuItem has list of MenuItems to show.
                while (!s.isEmpty() || sneakpeek)
                {
                    int indexOfNext = -1;
                    
                    // Go through each MenuItem in list from current MenuItem.
                    for (int i = 0; i < item.getItems().size(); i++)
                    {
                        image.setColor(backColor);
                        if (item.getItems().get(i).isEnabled())
                            if (!sneakpeek && item.getItems().get(i).getName().equals(s.substring(0, s.indexOf("/"))))
                            {
                                indexOfNext = i;
                                image.setColor(hoverColor);
                            }
                        int w = item.getLargestChild();
                        
                        // Check if mouse moved over this MenuItem, which case update to be the active path.
                        if (offsetMouse.getX() > runningX && offsetMouse.getX() <= runningX + w && offsetMouse.getY() > runningY + i * font.getSize() && offsetMouse.getY() <= runningY + i * font.getSize() + font.getSize())
                        {
                            active = address + item.getItems().get(i).getName() + "/";
                            if (item.getItems().get(i).isEnabled())
                            {
                                if (clicked)
                                {
                                    itemPressed = active;
                                    checkSets(item.getItems().get(i));
                                }
                                image.setColor(hoverColor);
                            }
                            else
                                image.setColor(backColor);
                        }
                        
                        // Draw MenuItem background.
                        image.fillRect(runningX, runningY + i * font.getSize(), w, font.getSize());
                        
                        // If have list of MenuItems, indicate with arrow.
                        if (!item.getItems().get(i).getItems().isEmpty())
                        {
                            if (item.getItems().get(i).isEnabled())
                                image.setColor(textColor);
                            else
                                image.setColor(disableColor);
                            int[] xs = new int[3];
                            xs[0] = runningX + w - 8;
                            xs[1] = runningX + w - 1;
                            xs[2] = runningX + w - 8;
                            int[] ys = new int[3];
                            ys[0] = runningY + i * font.getSize() + 2;
                            ys[1] = runningY + i * font.getSize() + font.getSize() / 2;
                            ys[2] = runningY + i * font.getSize() + font.getSize() - 1;
                            image.fillPolygon(xs, ys, 3);
                        }
                        
                        // If checked, draw checkmark.
                        int sy = (font.getSize() - 8) / 2;
                        if (item.getItems().get(i).isChecked() && runningY + i * font.getSize() + sy + 8 < image.getHeight())
                        {
                            image.setColorAt(runningX + 2 + 6, runningY + i * font.getSize() + sy + 0, new Color(121, 121, 121));
                            image.setColorAt(runningX + 2 + 7, runningY + i * font.getSize() + sy + 0, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 8, runningY + i * font.getSize() + sy + 0, new Color(194, 194, 194));
                            image.setColorAt(runningX + 2 + 5, runningY + i * font.getSize() + sy + 1, new Color(138, 138, 138));
                            image.setColorAt(runningX + 2 + 6, runningY + i * font.getSize() + sy + 1, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 7, runningY + i * font.getSize() + sy + 1, new Color(189, 189, 189));
                            image.setColorAt(runningX + 2 + 5, runningY + i * font.getSize() + sy + 2, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 6, runningY + i * font.getSize() + sy + 2, new Color(137, 137, 137));
                            image.setColorAt(runningX + 2 + 4, runningY + i * font.getSize() + sy + 3, new Color(138, 138, 138));
                            image.setColorAt(runningX + 2 + 5, runningY + i * font.getSize() + sy + 3, new Color(27, 27, 27));
                            image.setColorAt(runningX + 2 + 1, runningY + i * font.getSize() + sy + 4, new Color(43, 43, 43));
                            image.setColorAt(runningX + 2 + 2, runningY + i * font.getSize() + sy + 4, new Color(168, 168, 168));
                            image.setColorAt(runningX + 2 + 4, runningY + i * font.getSize() + sy + 4, new Color(28, 28, 28));
                            image.setColorAt(runningX + 2 + 5, runningY + i * font.getSize() + sy + 4, new Color(151, 151, 151));
                            image.setColorAt(runningX + 2 + 1, runningY + i * font.getSize() + sy + 5, new Color(122, 122, 122));
                            image.setColorAt(runningX + 2 + 2, runningY + i * font.getSize() + sy + 5, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 3, runningY + i * font.getSize() + sy + 5, new Color(93, 93, 93));
                            image.setColorAt(runningX + 2 + 4, runningY + i * font.getSize() + sy + 5, new Color(16, 16, 16));
                            image.setColorAt(runningX + 2 + 2, runningY + i * font.getSize() + sy + 6, new Color(42, 42, 42));
                            image.setColorAt(runningX + 2 + 3, runningY + i * font.getSize() + sy + 6, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 4, runningY + i * font.getSize() + sy + 6, new Color(93, 93, 93));
                            image.setColorAt(runningX + 2 + 2, runningY + i * font.getSize() + sy + 7, new Color(190, 190, 190));
                            image.setColorAt(runningX + 2 + 3, runningY + i * font.getSize() + sy + 7, new Color(0, 0, 0));
                            image.setColorAt(runningX + 2 + 4, runningY + i * font.getSize() + sy + 7, new Color(190, 190, 190));
                            image.setColorAt(runningX + 2 + 3, runningY + i * font.getSize() + sy + 8, new Color(131, 131, 131));
                        }
                        
                        // Draw frame of MenuItem.
                        image.setColor(borderColor);
                        image.drawRect(runningX, runningY + i * font.getSize(), w, font.getSize());
                        
                        // Draw MenuItem text.
                        g.setColor(!item.getItems().get(i).isEnabled() ? disableColor : textColor);
                        g.drawString(item.getItems().get(i).getName(), runningX + 2 + (item.getItems().get(i).isMemberOfSet() ? checkMarkWidth : 0), runningY + i * font.getSize() + font.getSize() - 1);
                        if (item.getItems().get(i).getShortCut() != null)
                            g.drawString(item.getItems().get(i).getShortCut(), runningX + w - item.getItems().get(i).getWidthOfShortCut() - 2, runningY + i * font.getSize() + font.getSize() - 1);
                        //image.drawString((item.getItems().get(i).isChecked() ? "? " : "") + item.getItems().get(i).getName(), runningX + 2 + (item.getItems().get(i).isMemberOfSet() && !item.getItems().get(i).isChecked() ? checkMarkWidth : 0), runningY + i * font.getSize() + font.getSize() - 1);
                    }
                    
                    if (!sneakpeek)
                    {
                        s = s.substring(s.indexOf("/") + 1);
                        if (indexOfNext != -1)
                        {
                            runningX += item.getLargestChild();
                            runningY += indexOfNext * font.getSize();
                            item = item.getItems().get(indexOfNext);
                            address += item.getName() + "/";
                            if (s.isEmpty() && !item.getItems().isEmpty())
                                sneakpeek = true;
                        }
                    }
                    else
                    {
                        s = "";
                        sneakpeek = false;
                    }
                }
                image.setColor(hoverColor);
            }
            else
                image.setColor(backColor);
            
            // Draw top MenuItem's background.
            image.fillRect(runWidth, 0, barItem.getWidth(), font.getSize());
            // Frame.
            g.setColor(borderColor);
            g.drawRect(runWidth, 0, barItem.getWidth(), font.getSize());
            // Text.
            g.setColor(textColor);
            g.drawString(barItem.getName(), runWidth + 2, font.getSize() - 1);
            
            // Update running x length.
            runWidth += barItem.getWidth();
        }
        
        g.dispose();
        
        // If clicked and didn't find any MenuItem that was what mouse clicked on, clear active path.
        if (clicked)
            active = "";
        
        scMI = null;
        scCount = 0;
        
        // Check MenuItems to find one that is
        for (MenuItem mi : items)
            checkForShortCuts(mi, "");
        // If found MenuItem with shortcut that has been completed.
        if (scMI != null)
        {
            keysPressed += " " + scMI.getShortCut() + " ";
            checkSets(scMI);
        }
    }
    
    private MenuItem scMI;
    private int scCount;
    
    private void checkForShortCuts(MenuItem mi, String s)
    {
        if (!mi.isEnabled())
            return;
        if (mi.checkShortCut())
        {
            if (!keysPressed.contains(" " + mi.getShortCut() + " "))
            {
                // Number of keys within shortcut combination.
                int c = 0;
                for (String ss = mi.getShortCut().replace("ctrl", "control"); true; ss = ss.substring(ss.indexOf("+") + 1))
                {
                    c++;
                    if (ss.indexOf("+") == -1)
                        break;
                }
                // If number of keys within shortcut combination for this MenuItem is greater than previously found MenuItem.
                if (c > scCount)
                {
                    scCount = c;
                    scMI = mi;
                    itemPressed = s + mi.getName() + "/";
                }
            }
        }
        else
            keysPressed = keysPressed.replace(" " + mi.getShortCut() + " ", "");
        for (MenuItem m : mi.getItems())
            checkForShortCuts(m, s + mi.getName() + "/");
    }
    
    /**
     * Mark MenuItem with a check, unchecking MenuItems within the same set.
     * @param mi MenuItem to be marked.
     */
    private void checkSets(MenuItem mi)
    {
        for (MenuSet set : sets)
            set.check(mi);
    }
    
    /**
     * The action listener for the Menu.
     * @return String representation of what Menu item was last clicked in the format of the
     * path getting to it. For example if an "Arrow" Menu item was clicked: "Insert/Shape/Arrow"
     */
    public String getItemPressed()
    {
        String s = itemPressed;
        itemPressed = null;
        if (s != null)
            return s.substring(0, s.length() - 1);
        return null;
    }
    
    /**
     * Add an additional Menu item to the Menu.
     * @param address Specify the path to the location to add something, with the item being 
     * added listed at the end. For example, to add "Square" in the list at "Insert/Shape", 
     * input this: "Insert/Shape/Square". Note that the parent Menu items must exist.
     * @see #addItems(String, ArrayList)
     */
    public void addItem(String address)
    {
        if (!address.contains("/"))
            items.add(new MenuItem(address));
        else
        {
            for (MenuItem mi : items)
                if (mi.getName().equals(address.substring(0, address.indexOf("/"))))
                {
                    mi.add(address.substring(address.indexOf("/") + 1));
                    break;
                }
        }
    }
    
    public void clearBranch(String address)
    {
        for (MenuItem mi : items)
            if (mi.getName().equals(address.substring(0, address.indexOf("/"))))
            {
                mi.clearBranch(address.substring(address.indexOf("/") + 1));
                break;
            }
    }
    
    /**
     * Add an additional Menu item to the Menu that may be marked with and without a check mark. When clicking
     * on this item, it will be marked if not marked, or unmarked if already marked.
     * @param address Specify the path to the location to add something, with the item being 
     * added listed at the end. For example, to add "Square" in the list at "Insert/Shape", 
     * input this: "Insert/Shape/Square". Note that the parent Menu items must exist.
     * @param checked If the item will be initially marked with a check mark.
     */
    public void addToggleItem(String address, boolean checked)
    {
        String n = address;
        while (n.contains("/"))
            n = n.substring(n.indexOf("/") + 1);
        String s = address.substring(0, address.length() - n.length());
        ArrayList<String> list = new ArrayList<String>();
        list.add(n);
        addItemsAsSet(s, list, checked ? 0 : -1);
    }
    
    /**
     * Add multiple additional Menu items to the Menu into a single address.
     * @param address The address for where to add the Menu items. For example, "Insert/Shape/" 
     * will specify to add the Menu items under "Shape". Note that these parent Menu items must exist.
     * @param menuItems The list of Menu items to add to the Menu. For example, "Circle/" will add
     * a Menu item with the name "Circle" at the location specified by the address param.
     * @see #addItem(String)
     */
    public void addItems(String address, ArrayList<String> menuItems)
    {
        for (String str : menuItems)
            addItem(address + str);
    }
    
    /**
     * In addition to what addItems(String, ArrayList<String>) method does, the items added will belong to a set where one of them may be marked with a check mark.
     * When one item is clicked, it will be marked with a check mark while the others will be unmarked.
     * @param address The address for where to add the Menu items. For example, "Insert/Shape/" 
     * will specify to add the Menu items under "Shape". Note that these parent Menu items must exist.
     * @param items The list of Menu items to add to the Menu. For example, "Circle/" will add
     * a Menu item with the name "Circle" at the location specified by the address param.
     * @param itemSelected The item that will be marked with a check mark initially. May be null.
     * @see #addItems(String, ArrayList)
     * @see #addToggleItem(String, boolean)
     */
    public void addItemsAsSet(String address, ArrayList<String> items, int itemSelected)
    {
        addItems(address, items);
        if (address == null || address.length() == 0 || address.charAt(address.length() - 1) != '/' || items == null || items.isEmpty())
            return;
        MenuItem m = null; //getItem(address.substring(0, address.length() - 1));//null;
        for (MenuItem mi : this.items)
            if (mi.getName().equals(address.substring(0, address.indexOf("/"))))
            {
                m = mi;
                address = address.substring(address.indexOf("/") + 1);
                break;
            }
        if (m == null)
            return;
        while (address.length() > 0)
        {
            boolean found = false;
            for (MenuItem mi : m.getItems())
            {
                if (mi.getName().equals(address.substring(0, address.indexOf("/"))))
                {
                    m = mi;
                    address = address.substring(address.indexOf("/") + 1);
                    found = true;
                    break;
                }
            }
            if (!found)
                return;
        }
        ArrayList<MenuItem> setItems = new ArrayList<MenuItem>();
        MenuItem st = null;
        for (MenuItem mi : m.getItems())
        {
            if (items.contains(mi.getName()))
            {
                setItems.add(mi);
                mi.addedToSet();
            }
            if (itemSelected != -1 && mi.getName().equals(items.get(itemSelected)))
            {
                st = mi;
                st.toggleCheck();
            }
        }
        sets.add(new MenuSet(setItems, st));
    }
    
    /**
     * Create a shortcut to a specific item. This makes it not necessary to navigate through the Menu.
     * @param address The location of item. For example "File/Print".
     * @param shortCut Key combination that will provoke the call as though the item was clicked. Examples: "ctrl+p", "ctrl+shift+up", "tab+5".
     */
    public void addShortCut(String address, String shortCut)
    {
        MenuItem m = getItem(address);
        if (m != null)
            m.setShortCut(shortCut);
    }
    
    /**
     * Cause as though the specific item was clicked on. This is handy for when there are icons
     * elsewhere that the user may press that should do the same thing and you would wish check
     * marks to change in a set that the item belongs to. Note getItemPressed() will return as
     * though this item was clicked.
     * @param address The location of item. For example "File/Print".
     * @see getItemPressed()
     */
    public void provokeItem(String address)
    {
        itemPressed = address + "/";
        MenuItem m = getItem(address);
        checkSets(m);
    }
    
    /**
     * Change the enable state of an item.
     * @param address The location of item.
     * @param enable Whether the item will be enabled.
     */
    public void enableItem(String address, boolean enable)
    {
        MenuItem m = getItem(address);
        if (m != null)
            m.enable(enable);
    }
    
    /**
     * 
     */
    public boolean isEnabled(String address)
    {
        MenuItem m = getItem(address);
        if (m == null)
            return false;
        return m.isChecked();
    }
    
    /**
     * 
     */
    public boolean isChecked(String address)
    {
        MenuItem m = getItem(address);
        if (m == null)
            return false;
        return m.isChecked();
    }
    
    /**
     * 
     */
    private MenuItem getItem(String address)
    {
        // Check if address is bogus.
        if (address == null || address.isEmpty() || address.charAt(address.length() - 1) == '/')
            return null;
        MenuItem m = null;
        for (MenuItem mi : items)
        {
            if (mi.getName().equals(address.contains("/") ? address.substring(0, address.indexOf("/")) : address))
                m = mi;
        }
        if (m == null || !address.contains("/"))
            return m;
        address = address.substring(address.indexOf("/") + 1);
        while (address.length() > 0)
        {
            for (MenuItem mi : m.getItems())
            {
                if (!address.contains("/"))
                {
                    if (mi.getName().equals(address))
                        return mi;
                }
                else if (mi.getName().equals(address.substring(0, address.indexOf("/"))))
                {
                    m = mi;
                    address = address.substring(address.indexOf("/") + 1);
                    break;
                }
            }
        }
        return null;
    }
    
    /**
     * Whether the menu is being navigated
     * @return Whether the menu is being navigated
     */
    public boolean isActive()
    {
        return active.length() != 0;
    }
    
    /**
     * 
     */
    @Override
    public void addedToWorld(World world)
    {
        super.addedToWorld(world);
        if (origin != null)
        {
            int barWidth = 0;
            for (MenuItem mi : items)
                barWidth += mi.getWidth();
            home.setLocation((int)origin.getX() + barWidth / 2, (int)origin.getY() + font.getSize() / 2);
            setLocation((int)home.getX() + (getImage().getWidth() - barWidth) / 2, (int)home.getY() + (getImage().getHeight() - font.getSize()) / 2);
        }
        else
            home.setLocation(getX(), getY());
    }

    private class MenuItem
    {
        private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
        private String name;
        private boolean checked;
        private boolean memberOfSet;
        private String shortCut;
        private boolean enabled = true;
        
        public MenuItem(String n)
        {
            name = n;
        }
        
        public void add(String s)
        {
            if (!s.contains("/"))
                items.add(new MenuItem(s));
            else
            {
                for (MenuItem mi : items)
                    if (mi.getName().equals(s.substring(0, s.indexOf("/"))))
                    {
                        mi.add(s.substring(s.indexOf("/") + 1));
                        break;
                    }
            }
        }
        public void clearBranch(String s)
        {
            if (!s.contains("/"))
                items.clear();
            else
            {
                for (MenuItem mi : items)
                    if (mi.getName().equals(s.substring(0, s.indexOf("/"))))
                    {
                        mi.clearBranch(s.substring(s.indexOf("/") + 1));
                        break;
                    }
            }
        }
        public int getWidth()
        {
            return getWidthOf(name) + 4;
        }
        public int getWidthOf(String s)
        {
            Graphics2D g = (new GreenfootImage(1, 1)).getAwtImage().createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(font);
            int w = g.getFontMetrics().stringWidth(s);
            g.dispose();
            return w;
        }
        
        public int getRunningWidth(String s, boolean first)
        {
            int w = getLargestChild();
            if (!s.isEmpty())
                for (MenuItem mi : items)
                    if (mi.getName().equals(s.substring(0, s.indexOf("/"))))
                        return w + mi.getRunningWidth(s.substring(s.indexOf("/") + 1), false);
            return w;
        }
        public int getLargestChild()
        {
            int n = 0;
            for (MenuItem mi : items)
            {
                int var = mi.getWidth() + (!mi.getItems().isEmpty() ? 10 : 0) + (mi.isMemberOfSet() ? checkMarkWidth : 0) + (mi.getShortCut() != null ? getWidthOf("   " + mi.getShortCut()) : 0);
                if (var > n)
                    n = var;
            }
            return n;
        }
        
        public int getRunningHeight(String s)
        {
            if (items.isEmpty())
                return 0;
            int h = (items.size() - 1) * font.getSize();
            if (!s.isEmpty())
                for (MenuItem mi : items)
                    if (mi.getName().equals(s.substring(0, s.indexOf("/"))))
                        return h + mi.getRunningHeight(s.substring(s.indexOf("/") + 1));
            return h;
        }
        public ArrayList<MenuItem> getItems()
        {
            return items;
        }
        public String getName()
        {
            return name;
        }
        public void setShortCut(String s)
        {
            shortCut = s;
        }
        public String getShortCut()
        {
            return shortCut;
        }
        public int getWidthOfShortCut()
        {
            return getWidthOf(shortCut);
        }
        public boolean checkShortCut()
        {
            if (shortCut == null || !enabled)
                return false;
            for (String s = shortCut; !s.isEmpty(); s = (s.contains("+") ? s.substring(s.indexOf("+") + 1) : ""))
            {
                String ss = (s.contains("+") ? s.substring(0, s.indexOf("+")) : s);
                if (ss.equals("ctrl"))
                {
                    if (!Greenfoot.isKeyDown("control"))
                        return false;
                }
                else if (!Greenfoot.isKeyDown(ss))
                    return false;
            }
            return true;
        }
        public boolean isChecked()
        {
            return checked;
        }
        public void toggleCheck()
        {
            checked = !checked;
        }
        public void addedToSet()
        {
            memberOfSet = true;
        }
        public boolean isMemberOfSet()
        {
            return memberOfSet;
        }
        public boolean isEnabled()
        {
            return enabled;
        }
        public void enable(boolean e)
        {
            enabled = e;
        }
    }
    private class MenuSet
    {
        private ArrayList<MenuItem> items = new ArrayList<MenuItem>();
        private MenuItem checkItem;
        
        public MenuSet(ArrayList<MenuItem> items, MenuItem m)
        {
            this.items = items;
            checkItem = m;
        }
        public void check(MenuItem mi)
        {
            if (!items.contains(mi))
                return;
            if (items.size() == 1)
                mi.toggleCheck();
            else if (!mi.isChecked())
            {
                if (checkItem != null)
                    checkItem.toggleCheck();
                mi.toggleCheck();
                checkItem = mi;
            }
        }
    }
}