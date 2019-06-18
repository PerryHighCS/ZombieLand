import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/**
 * Zombies like to hang around ZombieGoals. Like a mall foodcourt to a 90s teenager.
 * 
 * @author bdahlem
 * @version 1.0
 */
public class ZombieGoal extends ZombieDetector
{
    private int frame;
    private static final int NUM_FRAMES = 6;
    private static GreenfootImage[] images = loadFrames("Goal");
    
    public ZombieGoal() {        
        frame = (int)(Math.random() * NUM_FRAMES);
    }
    
    /**
     * Act - do whatever the ZombieGoal wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act() 
    {
        super.act();
        
        frame = (frame + 1) % NUM_FRAMES;
        setImage(images[frame]);
    }    
    
    /**
     * When a zombie reaches this cell, it has reached its goal.
     */
    public void detected()
    {
        getIntersectingObjects(Zombie.class).forEach((z) -> z.win());
    }
        
    /**
     * Create and fill an array of Greenfoot images by loading the files with
     * a given name followed by frame numbers
     */
    private static GreenfootImage[] loadFrames(String name)
    {
        GreenfootImage[] imageArr = new GreenfootImage[NUM_FRAMES];
        
        for (int i = 0; i < NUM_FRAMES; i++) {
            imageArr[i] = new GreenfootImage(name + "-" + i + ".png");
        }
        
        return imageArr;
    }
}
