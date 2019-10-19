import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
 
/**
 * When this button is clicked, the rgb data of the pixels will be exported to a .txt file
 * 
 * @author Karaleemota
 * @version (a version number or a date)
 */
public class ImportBtn extends Button
{
    /**
     * Act - do whatever the ExportBtn wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act() 
    {
        super.act();//call the Button class act
        importFile(((MyWorld)getWorld()).getInputFile());//import pixel data from the .txt file specified in the import text field
    }
    public ImportBtn(String text, Point size)
    {
        super(text,size);
    }
    /**
     * Check if the button was clicked. If it was, import the pixels' data from a given text file to the pixels on the screen
     */
    public void importFile(String filename)
    {
        if(wasClicked())//the button was clicked
        {
            //imprt from a .txt file with the filename
            try 
            {
                FileReader reader = new FileReader(filename);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                int lineCnt = 0;//count which line we are currently looking at
                Pixel[][] currPixels = ((MyWorld)getWorld()).getPixels();//hold all the pixels in the gui in this variable
                //while there are lines to read, read each line
                while ((line = bufferedReader.readLine()) != null)
                {
                    //line contains the current line we are looking at
                    String[] rgbValues = line.split(" ");//seperate line by spaces
                    int red = Integer.valueOf(rgbValues[0]);//red should be the first value in the split string
                    int green = Integer.valueOf(rgbValues[1]);//green should be the 2nd value in the split string
                    int blue = Integer.valueOf(rgbValues[2]);//blue should be the 3rd value in the split string
                    //now assign the rgb values to the correct pixel in the GUI
                    int row = (int)(lineCnt/16);//row value of pixel in the pixel array
                    int col = lineCnt % 16;//column value of the pixel in the pixel array
                    //now that we know which pixel we are on, change the color of that pixel
                    currPixels[row][col].setColor(red, green, blue, 255);
                    lineCnt++;//increment the line count
                }
                reader.close();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
    }
}
