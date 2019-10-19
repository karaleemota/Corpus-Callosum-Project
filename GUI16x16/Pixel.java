import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Color;
/**
 * Write a description of class Pixel here.
 * 
 * @author karaleemota
 * @version 10/16/19
 */
public class Pixel extends Actor
{
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50; //width and height should be the same because it is a square shaped pixel
    
    private GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);//make new square image
    private int scale = 0; //scale of the pixel size
    public Pixel()
    {
        setColor(0,0,0,255);//initialize pixel to be color black
    }
    public Pixel(int scale)//will use 150 as the scale for now in the world class
    {
        setColor(0,0,0,0);//initialize pixel to be color black
        image.scale(image.getWidth() - scale, image.getHeight() - scale);
    }
    /**
     * Sets the color of the pixel. parameters are the rgba values for the color
    */
    public void setColor(int r, int g, int b, int a)
    {
        image.setColor(new Color(r,g,b,a));
        image.fill();
        setImage(image);
    }
    public void act() 
    {
        //check if the user has clicked on this pixel
        if (Greenfoot.mouseClicked(this)) 
        {
           //set the color to the palette color
           Color newColor = ((MyWorld)getWorld()).getPalette().getColor();
           setColor(newColor.getRed(),newColor.getGreen(),newColor.getBlue(),newColor.getAlpha());
        }
    }    
    /**
     * Write the pixel data in a String. The rgba values will be written in a single line
     * Ex: "rrr gggg bb"
     */
    public String getStringValue()
    {
        String data = "";
        data += String.valueOf(image.getColor().getRed()) + " ";//add the red value of the pixel color to the string
        data += String.valueOf(image.getColor().getGreen()) + " ";//add the green value of the pixel color to the string
        data += String.valueOf(image.getColor().getBlue());//add the blue value of the pixel color to the string
        return data;
    }
}