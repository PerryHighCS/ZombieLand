import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;
import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import javax.swing.JOptionPane;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * A tool for creating ZombieLand Scenarios.
 * 
 * @author Brian Dahlem
 * @version 1
 */
public class WorldBuilder extends World
{
    private int sizex;
    private int sizey;
    private int worldIndex;
    private WorldBuilder[] situation; /* The problem[0] and solution[1] */
    
    ToolPalette tp;

    /**
     * Constructor for objects of class WorldBuilder.
     * 
     */
    public WorldBuilder()
    {           
        this (5, 5, 0, null);
        File defaultFile = null;
        try {
            defaultFile = new File(this.getClass().getResource("ZombieLand.xml").toURI());
            loadWorldXML(defaultFile);
        }
        catch (Exception e) {
            System.err.println("Failed to load default ZombieLand.xml");
        }
    }

    public WorldBuilder(int horizsize, int vertsize, int worldIndex,
                        WorldBuilder[] situation)
    {
        // Create a new world builder
        super(horizsize, vertsize, 64); 
        
        tp = ToolPalette.getToolPalette();

        tp.setWorld(this);
        tp.setWorldSize(horizsize, vertsize);
        
        sizex = horizsize;
        sizey = vertsize;
        this.worldIndex = worldIndex;
        
        if (situation == null) {
            situation = new WorldBuilder[2];
            situation[worldIndex] = this;
            situation[1 - worldIndex] = new WorldBuilder(horizsize, vertsize, 1 - worldIndex, situation);
        }
        else {
            this.situation = situation;
            situation[worldIndex] = this;
        }
        
        int cellSize = this.getCellSize();

        GreenfootImage img = this.getBackground();
        
        tp.show();
    }

    @Override
    public void addObject(Actor a, int x, int y) {
        super.addObject(a, x, y);
    }
    
    @Override
    public void removeObject(Actor a) {
        super.removeObject(a);
    }
    
    public void showTools() 
    {
        if (tp != null) {
            tp.show();
        }
    }
    
    public void saveWorldXML(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.newDocument();
    
            // create the root element node
            Element scenario = doc.createElement("scenario");
            doc.appendChild(scenario);
            
            Element world = doc.createElement("world");    
            world.setAttribute("width", "" + sizex);
            world.setAttribute("height", "" + sizey);
            world.appendChild(situation[0].buildWorldTree(doc, 0));
            world.appendChild(situation[1].buildWorldTree(doc, 1));
            scenario.appendChild(world);
            
            prettyPrint(doc, file);
        }
        catch (javax.xml.parsers.ParserConfigurationException e) {            
            JOptionPane.showMessageDialog(null, e, "Could not save .XML", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Element buildWorldTree(Document doc, int index)
    {
        List<Actor> actors = getObjects(null);
        Element el;
        
        if (index == 0) {
            el = doc.createElement("initial");
        }
        else {
            el = doc.createElement("objective");
        }
        
        for (Actor a : actors) {
            Element obj;
            
            if (index == 0) {
                obj = buildObject(doc, a);
            }
            else {
                obj = buildGoalObject(doc, a);
            }
            
            mergeObjectInto(obj, el);
        }

        return el;
    }

    private Element buildObject(Document doc, Actor a)
    {
        Element obj = doc.createElement("object");

        obj.setAttribute("classname", a.getClass().getName());

        Element location = doc.createElement("location");
        location.setAttribute("x", "" + a.getX());
        location.setAttribute("y", "" + a.getY());
        location.setAttribute("dir", "" + a.getRotation());

        
        if (a instanceof MyZombie) {
            MyZombie z = (MyZombie)a;
            if (z.getNumBrains() > 0) {
                location.setAttribute("brains", "" + z.getNumBrains());
            }
        }
        
        obj.appendChild(location);

        return obj;
    }

    private Element buildGoalObject(Document doc, Actor a)
    {
        Element obj = doc.createElement("object");

        obj.setAttribute("classname", a.getClass().getName());

        Element location = doc.createElement("location");
        location.setAttribute("x", "" + a.getX());
        location.setAttribute("y", "" + a.getY());
        /*
        location.setAttribute("dir", "" + a.getRotation());
        */

        if (a instanceof MyZombie) {
            Element call = doc.createElement("call");
            call.setAttribute("name", "hasWon");
            call.setAttribute("value", "true");
            location.appendChild(call);
        }
        
        obj.appendChild(location);

        return obj;
    }

    private void mergeObjectInto(Element obj, Element parent)
    {
        NodeList others = parent.getElementsByTagName("object");
        Element newLocation = (Element)obj.getElementsByTagName("location").item(0);
        String newx = newLocation.getAttribute("x");
        String newy = newLocation.getAttribute("y");

        String classname = obj.getAttribute("classname");

        for (int i = 0; i < others.getLength(); i++) {
            Element el = (Element)others.item(i);
            if (el.getAttribute("classname") == classname) {
                NodeList locations = ((Element)others.item(i)).getElementsByTagName("location");

                for (int j = 0; j < locations.getLength(); j++) {
                    Element location = (Element)locations.item(j);

                    if (location.getAttribute("x").equals(newx) &&
                    location.getAttribute("y").equals(newy)) {

                        int count = 2;
                        if (location.hasAttribute("count")) {
                            count = Integer.parseInt(location.getAttribute("count")) + 1;
                        }

                        location.setAttribute("count", "" + count);

                        return;
                    }
                }

                el.appendChild(newLocation);
                return;
            }
        }

        parent.appendChild(obj);
    }

    private static final void prettyPrint(Document xml, File file) {
        try {
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            
            // For some reason, transformer.transform won't write directly to the
            // file in a OneDrive folder... so transform to a String 
            //StreamResult result = new StreamResult(pkgFile);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            tf.transform(new DOMSource(xml), result);      
            
            // Save the string to the pkgfile
            String strResult = writer.toString();            
            try (PrintStream out = new PrintStream(new FileOutputStream(file.getAbsolutePath()))) {
                out.print(strResult);
                out.flush();
                out.close();
            }
        }
        catch (javax.xml.transform.TransformerException | java.io.FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, e, "Could not save .XML", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadWorldXML(File file)
    {
        try {
            // Create a Classloader to load the actors for the world
            URL url = (new File(".")).toURL();
            URL[] urls = new URL[]{url};
            ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
            // Open and parse the world description XML File
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
        
            // Get a handle to the root of the world description
            Element document = doc.getDocumentElement();
            Element root = (Element)document.getElementsByTagName("world").item(0);
            
            // Set the world width and height
            int width = Integer.parseInt(root.getAttribute("width"));
            int height = Integer.parseInt(root.getAttribute("height"));
            
            situation = new WorldBuilder[2];
            situation[0] = new WorldBuilder(width, height, 0, situation);
            situation[1] = new WorldBuilder(width, height, 1, situation);
            
            loadWorld((Element)root.getElementsByTagName("initial").item(0), cl, situation[0], true);
            loadWorld((Element)root.getElementsByTagName("objective").item(0), cl, situation[1], false);
            showProblem();
        }
        catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            
            JOptionPane.showMessageDialog(null, errors, "Could not load .XML", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Create a world based on an XML DOM description of the world and its actors
     * @param root The DOM element representing the root of the world
     * @param cl A classloader that can load the actor classes into the java runtime
     * @return the ZombieLand described by the XML DOM element and its children
     */
    private void loadWorld(Element root, ClassLoader cl, WorldBuilder w, boolean makeCalls) 
    throws ClassNotFoundException, NoSuchMethodException, InstantiationException, 
    IllegalAccessException, 
    InvocationTargetException
    {
        // Load and place initial objects
        NodeList objects = root.getElementsByTagName("object");
        for (int i = 0; i < objects.getLength(); i++) {
            // Get the object description
            Element obj = (Element)objects.item(i);

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
                
                int numBrains = 0;                
                if (pos.hasAttribute("brains")) {
                    numBrains = Integer.parseInt(pos.getAttribute("brains"));
                }
                
                NodeList callList = pos.getElementsByTagName("call");
                List<String[]> calls = null;
                if (makeCalls && callList.getLength() > 0) {
                    calls = new ArrayList<>();

                    for (int k = 0; k < callList.getLength(); k++) {
                        Element method = ((Element)callList.item(k));
                        String[] callSignature = new String[2];
                        callSignature[0] = method.getAttribute("name");
                        callSignature[1] = method.getAttribute("value");

                        calls.add(callSignature);
                    }
                }
                
                Constructor constructor;
                // Create instances at this location
                if (className.equals("MyZombie")) {
                    constructor = objClass.getConstructor(int.class);
                }
                else {
                    constructor = objClass.getConstructor();
                }
                for (; count > 0; count--) {
                    Actor a;
                    if (className.equals("MyZombie")) {
                        a = (Actor)constructor.newInstance(numBrains);
                    }
                    else {
                        a = (Actor)constructor.newInstance();
                    }
                    
                    w.addObject(a, x, y);
                    a.setRotation(dir);

                    if (makeCalls && calls != null) {
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
    
    public void resize(int xsize, int ysize)
    {
        if (situation == null) {
            return;
        }
        for (int i = 0; i < situation.length; i++) {
            WorldBuilder oldWorld = situation[i];
            
            // Create a new world with the specified size
            WorldBuilder w = new WorldBuilder(xsize, ysize, i, situation);
            
            // move all actors from this world to the new world
            for (Actor a : oldWorld.getObjects(Actor.class))
            {
                // Find out the location of the actor
                int x = a.getX();
                int y = a.getY();
    
                // move the actor to fit in the world
                x = (x < xsize) ? x : xsize - 1;
    
                // make sure that actor fits in the world
                y = (y < ysize) ? y : ysize - 1;
                
                // move the actor
                w.addObject(a, x, y);            
            }
        }      
        // Replace this world with the new world
        Greenfoot.setWorld(situation[worldIndex]);
    }
    
    public void showProblem()
    {
        Greenfoot.setWorld(situation[0]);
        tp.problemShown();
    }
    
    public void showSolution()
    {
        Greenfoot.setWorld(situation[1]);
        tp.solutionShown();
    }
    
    public void generateProblem()
    {
        copyActors(situation[1], situation[0]);
        Greenfoot.setWorld(situation[0]);
        
        tp.problemShown();
    }
    
    public void generateSolution()
    {
        copyActors(situation[0], situation[1]);
        Greenfoot.setWorld(situation[1]);
        
        tp.solutionShown();
    }
    
    private void copyActors(World from, World to) 
    {
        // Remove all actors in destination world
        for (Actor a : to.getObjects(Actor.class)) {
            to.removeObject(a);
        }
        
        for (Actor a : from.getObjects(Actor.class)) {
            try {
                Actor b = a.getClass().newInstance();
                to.addObject(b, a.getX(), a.getY());
                
                if (a instanceof Brain) {
                    ((Brain)b).setNum(((Brain)a).getNum());
                }
                else if (a instanceof MyZombie) {
                    ((MyZombie)b).setNumBrains(((MyZombie)a).getNumBrains());
                }
            }
            catch (Exception e) {
            }
        }
    }
}
