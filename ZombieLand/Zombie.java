import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.Graphics;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.List;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A programmable zombie character.
 *
 * @author bdahlem
 * @version 1.0
 */
public abstract class Zombie extends Actor
{
    private int frame = 0;
    private int deadFrame = 0;
    private int NUM_FRAMES = 4;
    private int moveAngle = 0;
    private GreenfootImage[][] images;

    public static final int ZOMBIE_GROAN = 0;
    public static final int ZOMBIE_MED_GROAN = 1;
    public static final int ZOMBIE_LONG_GROAN = 2;
    public static final int ZOMBIE_LOUD_GROAN = 3;
    public static final int ZOMBIE_GRUNT = 4;
    public static final int ZOMBIE_SCREAM = 5;
    public static final int NUM_SOUNDS = 6;

    private static GreenfootSound[] sounds;

    private Thread thinker;

    private volatile int numBrains = 0;

    private volatile boolean undead = true;
    private volatile boolean won = false;

    private volatile boolean redBox = false;

    /**
     * Make a new zombie, you evil person.
     */
    public Zombie() {
        // Prepare the zombie walking animation frames
        images = new GreenfootImage[5][];
        preloadImages();
        preloadSounds();

        thinker = new Thread(new Runnable()
            {
                public void run() {
                    // Wait until the zombie is in a world
                    while (getWorld() == null);

                    synchronized (Zombie.class) {
                        try {                        
                            Zombie.class.wait();    // Wait for an act signal before beginning the plan

                            plan();                 // Follow the plan

                            Zombie.class.wait();    // Wait for an act signal after the plan ends for everything to settle down

                            if (stillTrying()) {    // If the Zombie hasn't solved its problems,
                                die();               // Kill it
                            }
                        }
                        catch (InterruptedException | java.lang.IllegalStateException e) {
                            // If the plan is interrupted, or the zombie was removed, causing an illegal state,
                            // end the zombie
                            //if (stillTrying())
                            die();
                        }
                    }
                }
            }
        );

        thinker.start();
    }

    /**
     * Perform one animation step.
     */
    public final void act()
    {
        synchronized (Zombie.class) {
            frame = (frame + 1) % NUM_FRAMES; // Show the next animation frame
            showAnimationFrame();

            if (!undead || won) {  // If the zombie is no more, stop doing things.                
                if (!thinker.isInterrupted())
                    thinker.interrupt();
                return;
            }

            // Every other animation frame, perform one action
            if(frame % 2 == 0) {
                Zombie.class.notify();  // release the lock to perform the action

                if (undead && Math.random() < 0.001) {            // Play a random sound randomly if still running
                    sounds[(int)(Math.random() * 2)].play();
                }
            }
        }
    }

    /**
     * The special thing about this zombie is that has a plan.
     * The zombie's plan is run in a separate thread.  Commands, such as move()
     * and turnRight() wait their turn so that they can happen asynchronously with
     * animations, etc.
     */
    public abstract void plan();

    /**
     * Determine if this zombie is still struggling to make it in this world.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (stillTrying()) // If the zombie hasn't passed on, and hasn't reached the goal
     *     {
     *         move();
     *     }
     * }
     * </pre> 
     * </p>
     */
    public boolean stillTrying()
    {
        boolean worldFinished = false;
        if (getWorld() != null) {
            worldFinished = ((ZombieLand)getWorld()).isFinished();
        }

        return undead && !won && !worldFinished;
    }

    /**
     * Move forward one step.
     */
    public final void move()
    {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();    // Wait for an act signal

                if (stillTrying()) {
                    boolean success = handleWall(); 
                    success = success && handleBucket();
                    if (success) {
                        super.move(1);
                    }
                    else {
                        undead = false;
                        die();
                    }
                }
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
     * Move forward x steps (this may look useful, but zombies don't understand numbers).
     */
    public final void move(int x)
    {
        while (x > 0 && !isDead() && !hasWon()) {
            this.move();
            x--;
        }
    }

    /**
     * Turn 90 degrees to the right.
     */
    public final void turnRight()
    {
        synchronized (Zombie.class) {            
            turn(1);            
        }
    }

    /**
     * Turn to the right a given number of times (this may look useful, but zombies don't understand numbers).
     * @param turns the number of times to turn 90 degrees to the right
     */
    public final void turn(int turns)
    {        
        synchronized (Zombie.class) {
            try 
            {
                Zombie.class.wait();

                int degrees = turns * 90;

                if (stillTrying()) {
                    getImage().setTransparency(0);
                    super.turn(degrees);
                    showAnimationFrame();
                    getImage().setTransparency(255);
                }
            }
            catch (InterruptedException e) {
            }
        }
    }   

    /**
     * Pick up brains if they exist.  End if not.
     */
    public final void takeBrain()
    {        
        ClassLoader cl = this.getClass().getClassLoader();

        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                if (stillTrying()) {
                    Class brainClass = cl.loadClass("Brain");
                    Actor a = getOneIntersectingObject(brainClass);

                    if (a != null) {
                        numBrains++;
                        Method remove = brainClass.getMethod("removeBrain", new Class[0]);
                        remove.invoke((Object)a, null);
                    }
                    else {
                        drawRed();
                        ((ZombieLand)getWorld()).finish("Zombie no get brain.", false);
                    }
                }
            }
            catch (ClassNotFoundException | InterruptedException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            }
        }
    }

    /**
     * Put down a brain if the Zombie has one, if the zombie is not holding a brain, it will put down its own brain, ending its afterlife.
     */
    public final void putBrain()
    {

        ClassLoader cl = this.getClass().getClassLoader();

        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                if (stillTrying()) {
                    if (numBrains > 0) {
                        numBrains--;

                        Class brainClass = cl.loadClass("Brain");
                        Actor a = getOneIntersectingObject(brainClass);

                        if (a == null) {
                            Constructor constructor = brainClass.getConstructor();
                            a = (Actor)constructor.newInstance();

                            getWorld().addObject(a, getX(), getY());
                        }
                        else {
                            Method add = brainClass.getMethod("addBrain", new Class[0]);
                            add.invoke((Object)a, null);
                        }
                    }
                    else {
                        drawRed();
                        ((ZombieLand)getWorld()).finish("Zombie no have brain.", false);
                    }
                }
            }
            catch (ClassNotFoundException | InterruptedException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                //                ((ZombieLand)getWorld()).finish("Zombie no have brain.", false);
                //                e.printStackTrace();
            }
        }
    }

    /**
     * Check if this actor is touching an object with the given classname (this won't help out your plan)
     * @param classname The name of the object type to check for
     */
    public final boolean isTouching(String classname) {
        List<Actor> objects = getObjectsAtOffset(0, 0, null);

        for (Actor a : objects) {
            if (a.getClass().getName().equals(classname)){
                return true;
            }
        }
        return false;
    }

    /**
     * Remove one object that the zombie is touching (this may look useful, but it really won't work in your plan)
     * @param classname the name of the type of object to remove
     */
    public final void removeTouching(String classname) {
        List<Actor> objects = getObjectsAtOffset(0, 0, null);

        for (Actor a : objects) {
            if (a.getClass().getName().equals(classname)){
                getWorld().removeObject(a);
                return;
            }
        }
    }

    /**
     * Check if this Zombie is carrying a brain.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (haveBrains()) // If the zombie is carrying at least one brain
     *     {
     *         putBrain();   // put one down
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean haveBrains()
    {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                return numBrains > 0;
            }
            catch (InterruptedException e) {
            }

            return false;
        }
    }

    /**
     * Check if there is a brain where the zombie is standing.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isBrainHere()) // If the zombie is standing on a brain
     *     {
     *         takeBrain();   // pick it up
     *     }
     * }
     * </pre>
     * </p>
     */
    public final boolean isBrainHere() {
        synchronized (Zombie.class)
        {
            return (isTouching("Brain"));
        }
    }

    /**
     * Check if there is a wall or the edge of the world in front of the zombie.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (isFrontClear()) // If the zombie is not facing a wall
     *     {
     *         move();         // take a step forward
     *     }
     * }
     * </pre> 
     * </p>
     */
    public final boolean isFrontClear() {
        synchronized (Zombie.class) {
            try {
                Zombie.class.wait();
                return checkFront("Wall", 1) == null &&
                checkFront(null, 1) != this;
            }
            catch (InterruptedException e) {
            }
            return false;
        }
    }

    /**
     * Die, for reals this time.
     */
    public final void die()
    {
        synchronized (Zombie.class) {
            try {
                if (undead) {
                    Zombie.class.wait();
                    drawRed();
                    undead = false;
                    sounds[ZOMBIE_SCREAM].play();
                }
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
     * Die, for reals this time.
     */
    public final void die(boolean fast)
    {
        synchronized (Zombie.class) {
            if (!fast) {        
                die();
            }
            else {
                undead = false;
                thinker.interrupt();

                World w = getWorld();
                if (w != null) {
                    getWorld().removeObject(this);
                }
            }                            
        }
    }

    /**
     * Play a sound
     */
    public void playSound(int index) 
    {
        sounds[index].play();
    }

    /**
     * Check if this zombie is dead, or just undead
     */
    public final boolean isDead()
    {
        return undead == false;
    }

    /**
     * Tell this Zombie it has reached its goal in afterlife!
     */
    public final void win()
    {
        synchronized (Zombie.class) {
            if (!won && undead) {
                won = true;
                sounds[ZOMBIE_GRUNT].play();
            }
        }
    }

    /**
     * Check if this zombie has accomplished everything it could hope for.
     * <p>
     * Example:
     * <pre>
     * {@code
     *    if (!hasWon()) // If the zombie hasn't reached the goal
     *     {
     *         move();    // step towards it
     *     }
     * }
     * </pre> 
     * </p>
     */
    public boolean hasWon()
    {
        return won;
    }

    public void drawRed()
    {
        redBox = true;
    }

    /**
     * Show the next animation frame based on the direction the zombie is facing.
     */
    public void showAnimationFrame()
    {
        int dir = getRotation() / 90;

        GreenfootImage img = null;

        if (stillTrying()) {
            // If the zombie is carrying brains, add the number to the current frame.
            if (numBrains > 0) {
                GreenfootImage brainsLabel = new GreenfootImage("" + numBrains, 14, Color.WHITE, Color.BLACK);
                BufferedImage blImg = brainsLabel.getAwtImage();
                GreenfootImage frm = new GreenfootImage(images[dir][frame]);
                BufferedImage frmImg = frm.getAwtImage();

                img = new GreenfootImage(frm.getWidth(), frm.getHeight());
                BufferedImage bi = img.getAwtImage();
                Graphics2D graphics = (Graphics2D)bi.getGraphics();
                graphics.drawImage(frmImg, null, 0, 0);
                graphics.rotate(Math.toRadians(-getRotation()), frm.getWidth() / 2, frm.getHeight() / 2);
                graphics.drawImage(blImg, null, 0, 0);
                graphics.rotate(Math.toRadians(getRotation()), frm.getWidth() / 2, frm.getHeight() / 2);
            }
            else {
                img = images[dir][frame];
            }
        }
        else if (won) {
            setRotation(90);
            img = images[1][1];
        }
        else /* if (!undead) */ {
            setRotation(0);
            if (deadFrame == 0) {
                deadFrame = dir;
            }
            img = images[4][deadFrame];
        }

        if (redBox) {
            GreenfootImage sprite = img;
            img = new GreenfootImage(sprite.getWidth(), sprite.getHeight());
            img.setColor(Color.RED);
            img.fillRect(1, 1, img.getWidth() - 2, img.getHeight() - 2);
            img.drawImage(sprite, 0, 0);
        }

        setImage(img);
    }

    /**
     * Handle a wall in front of the zombie.  Everything ends if we crash into a wall.
     */
    private boolean handleWall()
    {
        if (checkFront("Wall", 1) != null || checkFront(null, 1) == this) {
            drawRed();
            ((ZombieLand)getWorld()).finish("Zombie hit wall.", false);
            return false;
        }
        return true;
    }

    /**
     * Handle a bucket in front of the zombie.  Running into a bucket tries to push it.  If it can't be pushed,
     * everything ends.
     */
    private boolean handleBucket()
    {
        Actor bucket = checkFront("Bucket", 1);
        if (bucket != null)
        {
            if (tryPush(bucket, getRotation()) == false) {
                drawRed();
                ((ZombieLand)getWorld()).finish("Bucket no move.", false);
                return false;
            }
        }
        return true;
    }

    /**
     * Attempt to push an object in a given direction
     */
    private boolean tryPush(Actor item, int dir)
    {
        dir = dir / 90;
        int dx = 0;
        int dy = 0;

        if (dir == 0) {
            dx = 1;
        }
        else if (dir == 1) {
            dy = 1;
        }
        else if (dir == 2) {
            dx = -1;
        }
        else {
            dy = -1;
        }

        if (checkFront("Wall", 2) == null && checkFront(null, 2) != this) {
            item.setLocation(item.getX()+dx, item.getY()+dy);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Check for an object of a particular class in front of the zombie or if that distance is beyond the
     * edge of the world
     *
     * @param classname The class to check for.  If null, look for the edge of the world
     * @param distance The distance (in cells) to the front to look for the object/edge
     * @return The object at the distance, or a reference to this zombie if the front is off the edge of
     *         the world, null if no object of the given class is at that distance or the world does not
     *         end within that distance.
     */
    private Actor checkFront(String classname, int distance)
    {
        int dir = getRotation() / 90;
        int dx = 0;
        int dy = 0;

        if (dir == 0) {
            dx = 1;
        }
        else if (dir == 1) {
            dy = 1;
        }
        else if (dir == 2) {
            dx = -1;
        }
        else {
            dy = -1;
        }

        dx *= distance;
        dy *= distance;

        if (classname != null) {
            List<Actor> objects = getObjectsAtOffset(dx, dy, null);

            for (Actor a : objects) {
                if (a.getClass().getName().equals(classname)){
                    return a;
                }
            }

            return null;
        }
        else {
            int nextX = getX() + dx;
            int nextY = getY() + dy;
            if ((nextX >= 0 && nextX < getWorld().getWidth()) &&
            (nextY >= 0 && nextY < getWorld().getHeight())) {
                return getOneObjectAtOffset(dx, dy, null);
            }
            else {
                return this;
            }
        }
    }

    /**
     * Load the zombie walking images for faster shambling animations.
     */
    private void preloadImages()
    {
        images[0] = loadImages("zombie-right");
        images[1] = loadImages("zombie-down");
        images[2] = loadImages("zombie-left");
        images[3] = loadImages("zombie-up");

        images[4] = loadImages("zombie-dead");
    }

    /**
     * Create and fill an array of Greenfoot images by loading the files with
     * a given name followed by frame numbers
     */
    private GreenfootImage[] loadImages(String name)
    {
        GreenfootImage[] imageArr = new GreenfootImage[NUM_FRAMES];

        for (int i = 0; i < NUM_FRAMES; i++) {
            imageArr[i] =  new GreenfootImage(name + "-" + i + ".png");
        }

        return imageArr;
    }

    /**
     * Load the zombie sounds for faster playback.
     */
    private void preloadSounds()
    {
        if (sounds == null) {
            sounds = new GreenfootSound[NUM_SOUNDS];
            sounds[ZOMBIE_GROAN] = new GreenfootSound("ZombieGroan.wav");
            sounds[ZOMBIE_MED_GROAN] = new GreenfootSound("ZombieMedGroan.wav");
            sounds[ZOMBIE_LONG_GROAN] = new GreenfootSound("ZombieLongGroan.wav");
            sounds[ZOMBIE_LOUD_GROAN] = new GreenfootSound("ZombieLoudGroan.wav");
            sounds[ZOMBIE_GRUNT] = new GreenfootSound("ZombieGrunt.wav");
            sounds[ZOMBIE_SCREAM] = new GreenfootSound("ZombieScream.wav");
        }
    }
}
