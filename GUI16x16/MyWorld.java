import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Point;
/**
 * Write a description of class MyWorld here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class MyWorld extends World
{
    //color palette in the world
    private Palette palette = new Palette();
    //make button that writes the pixel rbg data to a .txt file when pressed
    private ExportBtn exportBtn = new ExportBtn("Export", new Point(150, 50));
    //make button that reads pixel data from a file, and shows that data on the screen in the pixels
    private ImportBtn importBtn = new ImportBtn("Import", new Point(150,50));
    private Pixel[][] pixels = new Pixel[16][16];
    /**
     * Constructor for objects of class MyWorld.
     * 
     */
    public MyWorld()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(1200, 830, 1); 
        //loop to make 16x16 pixels
        for(int i = 0; i < 16; i++)
        {
            for(int j = 0; j < 16; j++)
            {
                Pixel newPixel = new Pixel();//make new pixel object to be added to world
                addObject(newPixel,30+(i*51),30+(j*51));//add pixel object to the world
                pixels[i][j] = newPixel;
            }
        }
        //add the color palette to the world
        addObject(palette,1010,300);
        //add an Export Button to the GUI. When the export button is clicked, the pixels rgb values will be written in a .txt file
        addObject(exportBtn,1010,500);
        //add import button the the gui. When the import button is clicked, the pixels on the screen will display the data saved in the file given to import
        addObject(importBtn,1010, 700);
    }
    public Palette getPalette()
    {
        return palette;
    }
    public Pixel[][] getPixels()
    {
        return pixels;
    }
}