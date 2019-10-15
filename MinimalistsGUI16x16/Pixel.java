import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Pixel here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Pixel extends Actor
{
    public Pixel()
    {
        GreenfootImage image = getImage();
        image.scale(image.getWidth() - 150, image.getHeight() - 150);
        setImage(image);
    }
    public void act() 
    {
        // Add your action code here.
    }    
}
