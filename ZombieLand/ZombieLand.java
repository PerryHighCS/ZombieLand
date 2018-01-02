import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;

/**
 * Write a description of class MyWorld here.
 * 
 * @author bdahlem
 * @version 1.0
 */
public class ZombieLand extends World
{
    Actor message = null;
    private boolean done = false;
    private List<GoalObject> goal;

    /**
     * Load the world description file and initialize the world
     */
    
    public ZombieLand()
    {  
    
        // Create a temporary world;
        super(1,1,64);
    
        try {
            // Create a Classloader to load the actors for the world
            URL url = (new File(".")).toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
        
            // Open and parse the world description XML File
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(this.getClass().getName() + ".xml"));
            doc.getDocumentElement().normalize();
        
            // Get a handle to the root of the world description
            Element document = doc.getDocumentElement();
            Element root = (Element)document.getElementsByTagName("world").item(0);
        
            ZombieLand firstWorld = loadWorld(root, cl);
            Greenfoot.setWorld(firstWorld);
        }
        catch (Exception e) {
        e.printStackTrace();
        }
    }
   
    /**
     * Create a ZombieLand with a given size;
     */
    public ZombieLand(int width, int height, int cellSize)
    {
        super(width, height, cellSize);
    }

    /**
     * Create a world based on an XML DOM description of the world and its actors
     * @param root The DOM element representing the root of the world
     * @param cl A classloader that can load the actor classes into the java runtime
     * @return the ZombieLand described by the XML DOM element and its children
     */
    private ZombieLand loadWorld(Element root, ClassLoader cl) 
    throws ClassNotFoundException, NoSuchMethodException, InstantiationException, 
    IllegalAccessException, 
    InvocationTargetException
    {
        // Set the world width and height
        int width = Integer.parseInt(root.getAttribute("width"));
        int height = Integer.parseInt(root.getAttribute("height"));
        ZombieLand realWorld = new ZombieLand(width, height, 64);

        // Get handles to the initial and objective description nodes
        Node initial = root.getElementsByTagName("initial").item(0);
        Node objective = root.getElementsByTagName("objective").item(0);

        // Load and place initial objects
        NodeList initialObjects = ((Element)initial).getElementsByTagName("object");
        for (int i = 0; i < initialObjects.getLength(); i++) {
            // Get the object description
            Element obj = (Element)initialObjects.item(i);

            // Determine the classname that describes the object
            String className = obj.getAttribute("classname");

            // Make sure the class is loaded into the Java runtime
            Class objClass = cl.loadClass(className);

            // Determine where the class instances are located
            NodeList locations = obj.getElementsByTagName("location");
            for (int j = 0; j < locations.getLength(); j++) {
                Element pos = (Element)locations.item(j);

                // Find out the coordinates of the location
                int x = Integer.parseInt(pos.getAttribute("x"));
                int y = Integer.parseInt(pos.getAttribute("y"));

                int dir = 0;
                if (pos.hasAttribute("dir")) {
                    dir = Integer.parseInt(pos.getAttribute("dir"));
                }
                
                // Find out how many instances are present in this location
                int count = 1;                
                if (pos.hasAttribute("count")) {
                    count = Integer.parseInt(pos.getAttribute("count"));
                }

                NodeList callList = pos.getElementsByTagName("call");
                List<String[]> calls = null;
                if (callList.getLength() > 0) {
                    calls = new ArrayList<>();

                    for (int k = 0; k < callList.getLength(); k++) {
                        Element method = ((Element)callList.item(k));
                        String[] callSignature = new String[2];
                        callSignature[0] = method.getAttribute("name");
                        callSignature[1] = method.getAttribute("value");

                        calls.add(callSignature);
                    }
                }
                
                if (className.equals("Brain")) {
                    // Create instances at this location
                    Constructor constructor = objClass.getConstructor(int.class);
                    
                    Actor a = (Actor)constructor.newInstance(count);
                    realWorld.addObject(a, x, y);
                    a.setRotation(dir);
                    
                    if (calls != null) {
                        for (String[] call : calls) {
                            Class[] params = {int.class};
                            Method m = objClass.getMethod(call[0], params);
                            m.invoke(a, Integer.parseInt(call[1]));
                        }
                    }
                }
                else if (className.equals("MyZombie")) {
                    // Determine how many brains this zombie is carrying
                    int numBrains = 0;
                    if (pos.hasAttribute("brains")) {
                        numBrains = Integer.parseInt(pos.getAttribute("brains"));
                    }
                    
                    // Create instances at this location
                    Constructor constructor  = objClass.getConstructor();
                    for (; count > 0; count--) {
                        Zombie z = (Zombie)constructor.newInstance();
                        
                        // Give the zombie some brains
                        if (numBrains > 0) {
                            try {
                                Field nb = Zombie.class.getDeclaredField("numBrains");
                                nb.setAccessible(true);
                                nb.set(z, numBrains);
                            }
                            catch (Exception e) {}
                        }
                        
                        // Add the zombie and face it the correct direction
                        realWorld.addObject(z, x, y);
                        z.setRotation(dir);
                        z.showAnimationFrame();
                        
                        // Make any setup calls
                        if (calls != null) {
                            for (String[] call : calls) {
                                Class[] params = {int.class};
                                Method m = objClass.getMethod(call[0], params);
                                m.invoke(z, Integer.parseInt(call[1]));
                            }
                        }
                    }
                }
                else {
                    // Create instances at this location
                    Constructor constructor = objClass.getConstructor();
                    for (; count > 0; count--) {
                        Actor a = (Actor)constructor.newInstance();
                        realWorld.addObject(a, x, y);
                        a.setRotation(dir);
    
                        if (calls != null) {
                            for (String[] call : calls) {
                                Class[] params = {int.class};
                                Method m = objClass.getMethod(call[0], params);
                                m.invoke(a, Integer.parseInt(call[1]));
                            }
                        }
                    }
                }
            }
        }

        // Load the solution for this world
        NodeList goalObjects = ((Element)objective).getElementsByTagName("object");
        realWorld.goal = loadGoals(goalObjects);

        // Determine the order to paint the actor classes
        Element paintOrderEl = ((Element)root.getElementsByTagName("paintOrder").item(0));

        if (paintOrderEl != null) {
            NodeList paintOrder = paintOrderEl.getElementsByTagName("class");

            Class[] classes = new Class[paintOrder.getLength()];
            for (int i = 0; i < classes.length; i++) {
                String className = ((Element)paintOrder.item(i)).getAttribute("name");
                classes[i] = Class.forName(className);
            }
            realWorld.setPaintOrder(classes);
        }

        // Return the loaded world
        return realWorld;
    }

    /**
     * Load the intended solution for this world.
     * 
     * @param goalNodes a list of XML DOM Elements describing the solution state of the actors in the world
     * @return a list of objects that can be compared to the actors in the world to determine if the
     *         solution was reached
     */
    private List<GoalObject> loadGoals(NodeList goalNodes)
    {
        List<GoalObject> goalList = new ArrayList<>();

        for (int i = 0; i < goalNodes.getLength(); i++) {
            Element gEl = (Element)goalNodes.item(i);

            String classname = gEl.getAttribute("classname");

            NodeList locations = gEl.getElementsByTagName("location");
            for (int j = 0; j < locations.getLength(); j++) {
                Element pos = (Element)locations.item(j);

                GoalObject gObj = new GoalObject();
                gObj.name = classname;                    
                gObj.x = Integer.parseInt(pos.getAttribute("x"));
                gObj.y = Integer.parseInt(pos.getAttribute("y"));

                if (pos.hasAttribute("count")) {
                    gObj.count = Integer.parseInt(pos.getAttribute("count"));
                }

                if (pos.hasAttribute("dir")) {
                    gObj.dir = Integer.parseInt(pos.getAttribute("dir"));
                }

                NodeList callList = pos.getElementsByTagName("call");
                if (callList.getLength() > 0) {
                    gObj.calls = new ArrayList<String[]>();

                    for (int k = 0; k < callList.getLength(); k++) {
                        Element method = ((Element)callList.item(k));
                        String[] callSignature = new String[2];
                        callSignature[0] = method.getAttribute("name");
                        callSignature[1] = method.getAttribute("value");

                        gObj.calls.add(callSignature);
                    }
                }    

                goalList.add(gObj);
            }
        }

        return goalList;
    }

    /**
     * Check the status of the Zombies every frame
     */
    public void act()
    {
        if (!done) {
            synchronized (Zombie.class) {

                if (checkZombies() ) {
                    if (checkGoal()) {
                    }
                }                    
            }
        }
    }

    /**
     * Display a message in the World
     */
    public void showMessage(String msg)
    {
        if (message != null) {
            removeObject(message);
        }

        message = new Actor(){public void act(){}};

        int xOffset = 0;
        int yOffset = 0;

        if (getWidth() % 2 == 0) {
            xOffset = getCellSize() / 2;
        }
        if (getHeight() % 2 == 0) {
            yOffset = getCellSize() / 2;
        }

        GreenfootImage img = new GreenfootImage(1,1);

        java.awt.Font f = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 30);
        java.awt.Image image = img.getAwtImage();
        java.awt.Graphics g = img.getAwtImage().createGraphics();
        g.setFont(f);
        java.awt.FontMetrics fm = g.getFontMetrics(f);

        int textWidth = fm.stringWidth(msg);
        int textHeight = fm.getHeight() + fm.getMaxDescent();
        int textBottom = textHeight - fm.getMaxDescent();

        img = new GreenfootImage((textWidth  + xOffset)* 2, textHeight + yOffset * 2);
        g = img.getAwtImage().createGraphics();
        g.setColor(java.awt.Color.BLACK);
        g.setFont(f); 

        int x = textWidth / 2 ;
        int y = textBottom;

        g.drawString(msg, x-1, y-1);
        g.drawString(msg, x, y-1);
        g.drawString(msg, x+1, y-1);
        g.drawString(msg, x-1, y);
        g.drawString(msg, x+1, y);
        g.drawString(msg, x-1, y+1);
        g.drawString(msg, x, y+1);
        g.drawString(msg, x+1, y+1);

        g.setColor(java.awt.Color.WHITE);
        g.drawString(msg, x, y);

        message.setImage(img);
        addObject(message, getWidth() / 2, getHeight() / 2);
    }

    /**
     * When the mission is ended, stop the world.
     */
    public void finish(boolean success)
    {
        done = true;
    }

    /**
     * When the mission is ended, stop the world.
     */
    public void finish(String msg, boolean success)
    {
        showMessage(msg);
        done = true;
    }

    /**
     * Check whether the scenario is complete.
     */
    public boolean isFinished()
    {
        return done;
    }

    /**
     * End the world if there aren't any zombies left.
     */
    public boolean checkZombies()
    {    
        if (!done) {
            List<Zombie> zombies = getObjects(Zombie.class);

            if (zombies.size() == 0) {
                finish("Zombie no more.", false);
                return false;
            }
            else {
                boolean allDead = true;
                for (Zombie z : zombies) {
                    if (!z.isDead()) {
                        allDead = false;
                    }
                }

                if (allDead) {
                    finish("Zombie dead.", false);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine if the goal has been reached
     */
    public boolean checkGoal()
    {
        try {
            // Create a Classloader to load the actors for the world
            URL url = (new File(".")).toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
            
            List<Actor> actors = getObjects(null);
            List<GoalObject> state = new ArrayList<GoalObject>();
            synchronized (Zombie.class) {
                for (Actor a : actors) {
                    GoalObject gObj = new GoalObject();
                    gObj.a = a;
                    gObj.name = a.getClass().getName();
    
                    gObj.x = a.getX();
                    gObj.y = a.getY();
                    gObj.dir = a.getRotation();
                    gObj.count = 1;
                    if (gObj.name.equals("Brain")) {
                        try {
                            Class objClass = cl.loadClass("Brain");
                            
                            Method m = objClass.getMethod("getNum", null);
                            Integer rval = (Integer)m.invoke(a, null);
                            gObj.count = rval;
                        }
                        catch (Exception e) {}
                    }
    
                    if (!gObj.name.contains("$")) {
                        boolean duplicate = false;
    
                        for (int i = 0; i < state.size(); i++) {
                            GoalObject o = state.get(i);
    
                            if (o.name.equals(gObj.name) &&
                                o.x == gObj.x &&
                                o.y == gObj.y) {
                                duplicate = true;
                                o.count = o.count + 1;
                                break;
                            }
                        }
    
                        if (!duplicate) {
                            state.add(gObj);
                        }
                    }
                }
    
                if (goal != null && state.size() == goal.size()) {
                    if (state.containsAll(goal)) {
                        finish("Zombie do good.", true);
                        return true;
                    }
                }
            }
        }
        catch (Exception e) {}
        
        return false;
    }

    private class GoalObject {
        public String name;
        public int count = 1;
        public int dir = Integer.MIN_VALUE;
        public int x;
        public int y;
        public List<String[]> calls;
        public Actor a;

        public boolean equals(Object o) {
            if (o instanceof GoalObject) {
                GoalObject other = (GoalObject)o;

                boolean calls = true;

                if (this.calls != null) {
                    if (this.name.equals(other.name)) {
                        for (String[] methodCall : this.calls) {
                            String methodName = methodCall[0];
                            try {
                                Class c = Class.forName(this.name);
                                Method m = c.getMethod(methodName, null);

                                String rval = m.invoke(other.a, null).toString();

                                if (!rval.equals(methodCall[1])){
                                    calls = false;
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (this.dir != Integer.MIN_VALUE) {
                    if (this.dir != other.dir) {
                        calls = false;
                    }
                }
                return this.name.equals(other.name) &&
                this.x == other.x &&
                this.y == other.y &&
                this.count == other.count &&
                calls == true;
            }
            return false;
        }
    }
}