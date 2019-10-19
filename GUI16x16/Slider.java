import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;
import greenfoot.MouseInfo;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Point;

/**
 * Slider
 * <p>
 * Visually and interactively change a numerical value between a range.<p>
 * Click and drag bar to left and right or press on side arrows to change value.
 * 
 * @author Taylor Born
 * @version March 2011 - March 2014
 */
public class Slider  extends WindowComponent
{
    private int size;
    private double low;
    private double high;
    private double value;
    private double increment;
    private Point lastMouse = new Point(-25, -25);
    private boolean dragging;
    private int heldAt;
    private int holdArrow;
    private boolean changed;
    private int s;

    /**
     * Create a new Slider.
     * @param size The width of the Slider.
     * @param low The minimum value of the Slider.
     * @param high The maximum value of the Slider.
     * @param initial The initial value of the Slider.
     * @param increment The amount the value of the Slider will increment.
     */
    public Slider(int size, double low, double high, double initial, double increment)
    {
        this.size = size;
        this.low = low;
        this.high = high;
        value = initial;
        this.increment = increment;
        s = (int)((initial - low) / (high - low) * (size - 50));
        
        setImage(new GreenfootImage(size, 38));
        act();
    }
    
    /**
     * Act.
     */
    @Override
    public void act()
    {
        super.act();
        
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (Greenfoot.mouseMoved(null) || Greenfoot.mouseDragged(null))
            lastMouse.setLocation(mouse.getX(), mouse.getY());
        
        boolean mouseDown = false;
        if (Greenfoot.mousePressed(this))
            mouseDown = true;
        if (Greenfoot.mouseClicked(null) || Greenfoot.mouseDragEnded(null))
        {
            mouseDown = false;
            dragging = false;
            holdArrow = 0;
        }
            
        GreenfootImage image = getImage();
        image.clear();
        
        Point offsetMouse = !inWorld() ? new Point(-1, -1) : new Point((int)lastMouse.getX() - (getX() - size / 2), (int)lastMouse.getY() - (getY() - 19));
        
        if (holdArrow > 0)
        {
            if (holdArrow < 30)
                holdArrow++;
            else if (holdArrow % 30 == 0)
                addIncrement();
        }
        else if (holdArrow < 0)
        {
            if (holdArrow > -30)
                holdArrow--;
            else if (holdArrow % 30 == 0)
                subtractIncrement();
        }
        
        if (Greenfoot.mousePressed(this) && offsetMouse.getX() > 0 && offsetMouse.getX() < size && offsetMouse.getY() > 12 && offsetMouse.getY() < 26)
            if (offsetMouse.getX() < 10)
            {
                holdArrow = -1;
                subtractIncrement();
            }
            else if (offsetMouse.getX() > size - 10)
            {
                holdArrow = 1;
                addIncrement();
            }
        
        boolean mouseOver = offsetMouse.getX() > 0 && offsetMouse.getX() < size && offsetMouse.getY() > 12 && offsetMouse.getY() < 26;
        
        boolean mouseOverBar = offsetMouse.getX() > 10 + s && offsetMouse.getX() < 10 + s + 30 && mouseOver;
        
        if (mouseDown && mouseOverBar)
        {
            dragging = true;
            heldAt = (int)offsetMouse.getX() - 10 - s;
        }
        if (dragging && Greenfoot.mouseDragged(null))
        {
            s += offsetMouse.getX() - 10 - s - heldAt;
            if (s < 0)
                s = 0;
            if (s > size - 50)
                s = size - 50;
            double v = low + (double)s / (size - 50) * (high - low);
            v -= v % increment;
            
            if (v != value)
                changed = true;
            value = v;
            if (value < low)
                value = low;
        }
        
        image.setColor(backColor);
        image.fillRect(0, 12, size - 1, 14);
        
        image.setColor(Color.RED);
        image.fillRect(10 + s, 12, 30, 14);
        
        image.setColor(borderColor);
        image.drawRect(0, 12, size - 1, 14);
        image.drawLine(10, 12, 10, 26);
        image.drawLine(size - 10, 12, size - 10, 26);
        
        image.drawLine(7, 16, 2, 19);
        image.drawLine(7, 22, 2, 19);
        
        image.drawLine(size - 8, 16, size - 3, 19);
        image.drawLine(size - 8, 22, size - 3, 19);
        
        Graphics2D g = image.getAwtImage().createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        g.setColor(textColor);
        if (dragging || (mouseOver && mouseOverThis()) || holdArrow != 0)
            g.drawString(value + "", 10 + s, 10);
        
        g.drawString(low + "", 0, 38);
        
        g.drawString(high + "", size - g.getFontMetrics().stringWidth(Double.toString(high)), 38);
        g.dispose();
    }
    
    /**
     * Get the increment for the Slider.
     * @return The increment for the Slider.
     */
    public double getIncrement()
    {
        return increment;
    }
    
    /**
     * Set the amount the value of the Slider will increment.
     * @param i The new increment.
     */
    public void setIncrement(double i)
    {
        increment = i;
    }
    
    /**
     * Increase the Slider's value by the increment.
     */
    public void addIncrement()
    {
        value += increment;
        if (value > high)
            value = high;
        s = (int)((value - low) / (high - low) * (size - 50));
        changed = true;
    }
    
    /**
     * Decrease the Slider's value by the increment.
     */
    public void subtractIncrement()
    {
        value -= increment;
        if (value < low)
            value = low;
        s = (int)((value - low) / (high - low) * (size - 50));
        changed = true;
    }
    
    /**
     * Get the value of the Slider.
     * @return The value of the Slider.
     */
    public double getValue()
    {
        return value;
    }
    
    /**
     * Set the value of the Slider.
     * @param v The new value for the Slider.
     */
    public void setValue(double v)
    {
        value = v;
        s = (int)((value - low) / (high - low) * (size - 50));
        changed = true;
    }
    
    /**
     * Get the minimum value for the Slider.
     * @return The minimum value for the Slider.
     */
    public double getLow()
    {
        return low;
    }
    
    /**
     * Set the minimum value for the Slider.
     * @param l The new minimum value for the Slider.
     */
    public void setLow(double l)
    {
        low = l;
    }
    
    /**
     * Get the maximum value for the Slider.
     * @return The maximum value for the Slider.
     */
    public double getHigh()
    {
        return high;
    }
    
    /**
     * Set the maximum value for the Slider.
     * @param h The new maximum value for the Slider.
     */
    public void setHigh(double h)
    {
        high = h;
    }
    
    /**
     * The action listener for the Slider.
     * @return Whether the Slider's value has changed or not.
     */
    public boolean hasChanged()
    {
        boolean c = changed;
        changed = false;
        return c;
    }
    
}