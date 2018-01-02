import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * A ZombieDetector that counts how many time a zombie has stepped on it.
 * 
 * @author bdahlem 
 * @version 1.0
 */
public class PassCounter extends ZombieDetector
{
    private int passCount = 0;
    private boolean showCount;
        
    /**
     * Create a ZombieDetector that shows the number of times a zombie has
     * stepped on it
     */
    public PassCounter(boolean display)
    {        
        GreenfootImage img = new GreenfootImage(64, 64);
        setImage(img);

        showCount = display;
        showCount();
    }
        
    /**
     * When a zombie is detected, increment the pass counter.
     */
    public void detected()
    {
        passCount++;
        showCount();
    }
    
    /**
     * Retrieve the number of times a zombie has stepped on the detector.
     */
    public int getPasses()
    {
        return passCount;
    }
    
    /**
     * Turn on the counter display
     */
    public void displayPasses(boolean display)
    {
        showCount = display;
    }
    
    /**
     * Update the counter display
     */
    private void showCount()
    {
        if (showCount)
        {
            GreenfootImage img = getImage();
            img.clear();
            img.setFont(new Font(true, false, 15));
            img.drawString("" + passCount, 3, 61);
            setImage(img);
        }
    }
}
