import greenfoot.World;
import greenfoot.GreenfootImage;
import java.awt.Point;

/**
 * Container
 * <p>
 * Container stores and aligns WindowComponents within "cells" of a grid/table structure.<p>
 * Keeps its WindowComponents in position relative to it within grid/table structure.
 * 
 * @author Taylor Born
 * @version March 2013 - March 2014
 */
public class Container extends WindowComponent
{
    // Grid of WindowComponents
    protected WindowComponent[][] components;
    // Number of pixels to pad between row
    protected int spacing;

    /**
     * New Container with specific capacity and spacing.
     * @param size The dimensions of the WindowComponent capacity of this Container in columns and rows.
     * @param spacing Number of pixels padded between adjacent rows and colums of WindowComponents.
     */
    public Container(Point size, int spacing)
    {
        components = new WindowComponent[(int)size.getY()][(int)size.getX()];
        this.spacing = spacing;
        setImage(new GreenfootImage(1, 1));
    }
    
    /**
     * New Container with specific capacity, with default spacing of 10 pixels.
     * @param size The dimensions of the WindowComponent capacity of this Container in columns and rows.
     */
    public Container(Point size)
    {
        this(size, 10);
    }
    
    public int getSpacing()
    {
        return spacing;
    }
    
    public int getNumberOfRows()
    {
        return components.length;
    }
    public int getNumberOfColumns()
    {
        return components[0].length;
    }
    
    /**
     * Insert given WindowComponent into this Container's structure at the first vacant position (iterating left to right, top to bottom).<p>
     * Meant to be called immediately following construction to fill this Container with all WindowComponents it will ever contain.
     * @param wc The WindowComponent to be added.
     * @see addComponent(WindowComponent Point)
     */
    protected void addComponent(WindowComponent wc)
    {
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] == null)
                {
                    components[r][c] = wc;
                    return;
                }
    }
    
    /**
     * Insert given WindowComponent into this Container's structure at the specified position.<p>
     * Meant to be called immediately following construction to fill this Container with all WindowComponents it will ever contain.
     * @param wc The WindowComponent to be added.
     * @param loc The column and row coordinate position within this Container's structure where the WindowComponent is to be added.
     * @see addComponent(WindowComponent)
     */
    protected void addComponent(WindowComponent wc, Point loc)
    {
        if (loc.getY() < 0 || loc.getY() > components.length - 1 || loc.getX() < 0 || loc.getX() > components[0].length - 1)
            return;
        components[(int)loc.getY()][(int)loc.getX()] = wc;
    }
    
    /**
     * Act.
     * Listens for when contained WindowComponents need to be added-to / removed-from World based on their status on being hidden.<p>
     * Calls to align its WindowComponents in the World when adding one to the World.
     */
    @Override
    public void act()
    {
        super.act();
        
        // Remember if added WindowComponent to World.
        boolean added = false;
        
        boolean guiSizeChanged = false;
        
        // Iterate over all WindowComponents of this Container.
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null) {
                    // If WindowComponent is set to be hidden and yet is in the World, remove it from World.
                    if (components[r][c].isHidden()) {
                        if (components[r][c].inWorld())
                            components[r][c].removeFromWorld();
                    }
                    // If WindowComponent is not set to be hidden and yet is not in the World, add it to World.
                    else if (!components[r][c].inWorld()) {
                        getWorld().addObject(components[r][c], 0, 0);
                        // We added a WindowComponent to World.
                        added = true;
                    }
                    
                    if (components[r][c].hasChangedGUISize())
                        guiSizeChanged = true;
                }
        
        // If added WindowComponent to World, align it in grid/table structure.
        if (added || guiSizeChanged)
            alignComponents(false);
    }

    /**
     * Set location of this Container within the World, and call to set locations of its WindowComponents relative to this location.
     * @param x X-coordinate for where to be in the World.
     * @param y Y-coordinate for where to be in the World.
     */
    @Override
    public void setLocation(int x, int y)
    {
        super.setLocation(x, y);
        alignComponents(false);
    }
    
    /**
     * Set locations in World, or add into World, - this Container's WindowComponents to locations relative to this Container's location and the grid/table structure.
     * @param addToWorld Whether or not the WindowComponents are to added to World.
     */
    private void alignComponents(boolean addToWorld)
    {
        // Store largest width of every column.
        int[] columnWidths = new int[components[0].length];
        // Store largest height of every row.
        int[] rowHeights = new int[components.length];
        
        // Store amount of space before each row. (sum of all rows and spacing before a particular row).
        int[] rowLeads = new int[components.length];
        
        for (int r = 0; r < components.length; r++) {
            
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null) {
                    
                    if (components[r][c].hasChangedGUISize()) // Burn listener
                    {}
                    
                    // Width.
                    int l = components[r][c].getGUIWidth();
                    // If found bigger width.
                    if (l > columnWidths[c])
                        columnWidths[c] = l;
                    // Height.
                    l = components[r][c].getGUIHeight();
                    // If found bigger height.
                    if (l > rowHeights[r])
                        rowHeights[r] = l;
                }
            
            // If not first row, store amount of space before this row.
            if (r != 0) {
                
                // Spacing before previous row.
                rowLeads[r] = rowLeads[r - 1];
                // Height of previous row.
                int rh = rowHeights[r - 1];
                // If previous row was not empty, add its height and spacing to its leading spacing for this current row's leading spacing.
                if (rh != 0)
                    rowLeads[r] += spacing + rh;
            }
        }
        
        // Store amount of space before each column. (sum of all columns and spacing before a particular column).
        int[] columnLeads = new int[components[0].length];
        for (int c = 1; c < columnWidths.length; c++)
        {
            // Spacing before previous column.
            columnLeads[c] = columnLeads[c - 1];
            // Width of previous column.
            int cw = columnWidths[c - 1];
            // If previous column was not empty, add its width and spacing to its leading spacing for this current column's leading spacing.
            if (cw != 0)
                columnLeads[c] += spacing + cw;
        }
        
        // Size of this Container.
        int w = getGUIWidth();
        int h = getGUIHeight();
        
        // Iterate over WindowComponents and set their location or add them to World at appropriate locations.
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null)
                    // Adding to World.
                    if (addToWorld) {
                        // If the WindowComponent is not set to hide.
                        if (!components[r][c].isHidden())
                            // Tell WindowComponent to add itself to World given World and leftside and width of cell it is asigned to.
                            components[r][c].addToWorldInContainerCell(getWorld(), getX() - w / 2 + columnLeads[c], columnWidths[c], getY() - h / 2 + rowLeads[r], rowHeights[r]);
                    }
                    // Setting location. If in World.
                    else if (components[r][c].inWorld())
                        // Tell WindowComponent to set its location given leftside and width of cell it is asigned to.
                        components[r][c].setLocationInContainerCell(getX() - w / 2 + columnLeads[c], columnWidths[c], getY() - h / 2 + rowLeads[r], rowHeights[r]);
    }
    
    /**
     * Get the width in pixels, this Container occupies.<p>
     * Calculated by summing each largest-width-WindowComponent's width of every column, and the spacing between each column.
     * @return Width in pixels, this Container occupies.
     */
    @Override
    public int getGUIWidth()
    {
        // Resulting sum.
        int totalW = 0;
        
        // Visit each column.
        for (int c = 0; c < components[0].length; c++) {
            
            // Remember largest found width.
            int w = 0;
            
            // Visit each row.
            for (int r = 0; r < components.length; r++)
                if (components[r][c] != null) {
                    // Get WindowComponent's width and record it if is largest found so far.
                    int l = components[r][c].getGUIWidth();
                    if (l > w)
                        w = l;
                }
            
            // If column not empty.
            if (w != 0)
                // If previous columns are empty, sum is just this column's width.
                if (totalW == 0)
                    totalW = w;
                else
                    // There is column before this one, add spacing and this column's width.
                    totalW += spacing + w;
        }
        
        return totalW;// < minWidth ? minWidth : totalW;
    }
    
    /**
     * Get the height in pixels, this Container occupies.<p>
     * Calculated by summing each largest-height-WindowComponent's height of every row, and the spacing between each row.
     * @return Height in pixels, this Container occupies.
     */
    @Override
    public int getGUIHeight()
    {
        // Resulting sum.
        int totalH = 0;
        
        // Visit each row.
        for (int r = 0; r < components.length; r++)
        {
            // Remember largest found height.
            int h = 0;
            
            // Visit each column.
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null)
                {
                    // Get WindowComponent's height and record it if is largest found so far.
                    int l = components[r][c].getGUIHeight();
                    if (l > h)
                        h = l;
                }
            
            // If row not empty.
            if (h != 0)
                // If previous rows are empty, sum is just this row's width.
                if (totalH == 0)
                    totalH = h;
                else
                    // There is row before this one, add spacing and this row's height.
                    totalH += spacing + h;
        }
        
        return totalH;// < minHeight ? minHeight : totalH;
    }
    
    /**
     * Called when this Container is added to the World.<p>
     * Calls to insert its WindowComponents into the World relative to new location in World.
     * @param world World to be added to.
     */
    @Override
    public void addedToWorld(World world)
    {
        super.addedToWorld(world);
        alignComponents(true);
    }
    
    /**
     * Remove this Container and its WindowComponents from the World.
     */
    @Override
    public void removeFromWorld()
    {
        if (getWorld() == null)
            return;
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null)
                    components[r][c].removeFromWorld();
        super.removeFromWorld();
    }
    
    /**
     * Check if this Container or one of its WindowComponents has focus.
     * @return Whether or not this Container or one of its WindowComponents has focus.
     */
    @Override
    public boolean hasFocus()
    {
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null && components[r][c].hasFocus())
                    return true;
        return false;
    }
    
    /**
     * Check if one of this Container's WindowComponents have been pressed on by the mouse.
     * @return Whether or not one of this Container's WindowComponents have been pressed on by the mouse.
     */
    @Override
    public boolean mousePressedOnThisOrComponents()
    {
        for (int r = 0; r < components.length; r++)
            for (int c = 0; c < components[0].length; c++)
                if (components[r][c] != null && components[r][c].mousePressedOnThisOrComponents())
                    return true;
        return false;
    }
}