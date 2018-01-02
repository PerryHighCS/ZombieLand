import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Graphics;
import java.awt.FontMetrics;

/**
 * Write a description of class Brain here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Brain extends Actor
{
    private GreenfootImage baseImage;
    private int numBrains;
    
    public Brain() {
        baseImage = getImage();
        numBrains = 1;
        
        showNumBrainsHere();
    }
    
    public void act() {
        showNumBrainsHere();
    }
    
    public void addedToWorld(World w) {
        showNumBrainsHere();
    }
    
    /**
     * Set the number of brains in this pile
     */
    public void setNum(int num)
    {
        synchronized(this.getClass())
        {
            try
            {
                this.numBrains = num;
                showNumBrainsHere();
            }
            catch (Exception e)
            {
            }
        }
    }
    
    public int getNum()
    {
        return numBrains;
    }
    
    /**
     * Update the image of this brain with the number of brains occupying this
     * cell.
     */
    private void showNumBrainsHere()
    {
        synchronized(this.getClass())
        {
            try
            {
                int numBrains = this.numBrains;
                
                for (Brain b : getIntersectingObjects(Brain.class)) {
                    numBrains += b.numBrains;
                }
                
                if (numBrains > 1) {
                    
                    String msg = "" + (numBrains);
                    
                    GreenfootImage img = new GreenfootImage(baseImage);
                    GreenfootImage num = new GreenfootImage(msg, 28, Color.WHITE, 
                        new Color(0,0,0,0), new Color(0,0,0,0));
                        
                    int x = (img.getWidth() - num.getWidth()) / 2;
                    int y = img.getHeight() - num.getHeight();
                    img.drawImage(num, x, y);
                    
                    setImage(img);
                    
                    for (Brain b : getIntersectingObjects(Brain.class)) {
                        b.setImage(img);
                    }
                }
                else {
                    setImage(baseImage);
                }
                
                //this.getClass().notify();
            }
            catch (Exception e)
            {
            }
        }
    }
}
