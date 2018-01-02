import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Write a description of class Fire here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Fire extends Actor
{
    private int frame = 0;
    private static final int NUM_FRAMES = 12;
    
    private static final int FIRE = 0;
    private static final int SMOKE = 1;
    private int fireMode;
    
    private GreenfootImage[][] images;
    
    public Fire() {
        images = new GreenfootImage[2][];
        preloadFrames();
        
        fireMode = FIRE;
        frame = (int)(Math.random() * NUM_FRAMES);
    }
    
    /**
     * Animate the fire
     */
    public void act() 
    {
        frame = (frame + 1) % NUM_FRAMES;
        if (frame == 0) {
            if (fireMode == SMOKE) {
                ((ZombieLand)getWorld()).checkZombies();
                getWorld().removeObject(this);
                return;
            }
        }
        
        checkForZombies();
        checkForWater();
        
        setImage(images[fireMode][frame]);
    }    
    
    /**
     * See if a zombie has wandered into this fire, burn it up if so.
     */
    private void checkForZombies()
    {
        while (fireMode == FIRE && isTouching(Zombie.class)) {
            Zombie z = (Zombie)getOneIntersectingObject(Zombie.class);
            z.playSound(Zombie.ZOMBIE_SCREAM);
            z.die(true);
            
            fireMode = SMOKE;
            frame = 0;
        }
    }
    
    /**
     * See if a bucket of water has been pushed into this fire, burn it up and
     * extinguish if so.
     */
    private void checkForWater()
    {
        ClassLoader cl = this.getClass().getClassLoader();
        
        try {
            Class bucketClass = cl.loadClass("Bucket");
            Actor a = getOneIntersectingObject(bucketClass);
            while (isTouching(bucketClass)) {
                removeTouching(bucketClass);
                fireMode = SMOKE;
                frame = 0;
            }
        }
        catch (ClassNotFoundException e) {
        }
    }
    
    /**
     * Load all of the fire and smoke frames for better animation.
     */
    private void preloadFrames()
    {
        images[FIRE] = loadFrames("Fire");
        images[SMOKE] = loadFrames("Smoke");
    }
    
    /**
     * Create and fill an array of Greenfoot images by loading the files with
     * a given name followed by frame numbers
     */
    private GreenfootImage[] loadFrames(String name)
    {
        GreenfootImage[] imageArr = new GreenfootImage[NUM_FRAMES];
        
        for (int i = 0; i < NUM_FRAMES; i++) {
            imageArr[i] = new GreenfootImage(name + "-" + i + ".png");
        }
        
        return imageArr;
    }
}
