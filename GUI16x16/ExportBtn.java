import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Point;
import java.io.*;
import java.io.IOException;

/**
 * When this button is clicked, the rgb data of the pixels will be exported to a .txt file
 * 
 * @author Karaleemota
 * @version (a version number or a date)
 */
public class ExportBtn extends Button
{
    /**
     * Act - do whatever the ExportBtn wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act() 
    {
        super.act();//call the Button class act
        export("output.txt");
    }
    public ExportBtn(String text, Point size)
    {
        super(text,size);
    }
    /**
     * Check if the button was clicked. If it was, export the pixels' data to the given text file
     */
    public void export(String filename)
    {
        if(wasClicked())//the button was clicked
        {
            //export to the txt file
            try 
            {
                FileWriter writer = new FileWriter(filename, false);
                Pixel[][] tempPixels = ((MyWorld)getWorld()).getPixels();//get the array of pixels from the world class
                //iterate through all the pixels to read their info
                for(int i = 0; i < 16; i++)
                {
                    for(int j = 0; j < 16; j++)
                    {
                        //get the string data to be written to txt.
                        Pixel tempPixel = tempPixels[i][j];//the current pixel we are currently on in the iteration
                        writer.write(tempPixel.getStringValue());
                        writer.write("\n");
                    }
                }
                writer.close();
            } catch (IOException e) 
            {
               e.printStackTrace();
            }
        }
    }
}
