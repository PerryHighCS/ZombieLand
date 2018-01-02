import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * A special version of the Fire class that is used to demonstrate
 * building a labyrinth.
 */
public class Fire extends Actor
{
    private int frame = 0;
    private static final int NUM_FRAMES = 12;
    
    private static final int FIRE = 0;
    private static final int SMOKE = 1;
    private int fireMode;
    
    private GreenfootImage[][] images;
    
    /**
     * Create a burning fire
     */
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
                getWorld().removeObject(this);
                return;
            }
        }
        
        setImage(images[fireMode][frame]);
    }    
    
    /**
     * Put out the fire
     */
    public void extinguish() {
        this.fireMode = SMOKE;
        this.frame = 0;
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
