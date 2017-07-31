package boldorf.eversector.entities;

import boldorf.eversector.items.Weapon;
import boldorf.eversector.items.BaseResource;
import boldorf.eversector.items.Expander;
import boldorf.eversector.items.Resource;
import boldorf.eversector.items.Module;
import static boldorf.eversector.Main.rng;
import java.util.ArrayList;
import boldorf.eversector.storage.Paths;
import boldorf.util.FileManager;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.entities.locations.SectorLocation;
import boldorf.eversector.items.Item;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import boldorf.eversector.map.faction.Faction;

/** A station at which ships can refuel and purchase upgrades. */
public class Station extends CelestialBody implements ColorStringObject
{
    public static final String TRADE  = "Trade";
    public static final String BATTLE = "Battle";
    
    // TODO change some modules to items (Refinery and Solar Array)
    
    /**
     * The base modules that all stations sell, at their base prices.
     * @see #loadModules()
     */
    public static Module[] MODULES;
    
    /**
     * The base resources that all stations sell, at the base prices for
     * themselves and their expanders.
     * @see #loadResources()
     */
    public static BaseResource[] RESOURCES;
    
    /** The ships that are currently docked with the station. */
    private List<Ship> ships;
    
    /** The modules that an individual station sells. */
    private Module[] modules;
    
    /** The resources that an individual station sells. */
    private BaseResource[] resources;
    
    /** The type of station (trade or battle). */
    private String type;
    
    /**
     * Creates a station from a name, location, and faction.
     * @param name the name of the station
     * @param location the location of the station
     * @param faction the faction the station belongs to
     */
    public Station(String name, SectorLocation location, Faction faction)
    {
        super(name, location, faction);
        ships = new LinkedList<>();
        type = generateType();
        
        cloneItems();
        generatePrices();
    }
    
    @Override
    public String toString()
        {return type + " Station " + super.toString();}
    
    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(),
                isClaimed() ? getFaction().getColor() : null);
    }
    
    public String getType()
        {return type;}
    
    /**
     * Returns the symbol that corresponds to the station type.
     * @return the symbol representing the station type
     */
    public ColorChar getSymbol()
    {
        return new ColorChar(BATTLE.equals(type) ? '%' : '#',
                getFaction().getColor());
    }
    
    @Override
    public int getClaimCost()
        {return CLAIM_COST;}
    
    /**
     * Returns true if the station sells the specified module.
     * @param module the module to check
     * @return true if the station sells the module
     */
    public boolean sells(Module module)
    {
        if (module == null || module.getName() == null)
            return false;
        
        if (module.isBattle())
            return BATTLE.equals(type);
        
        return TRADE.equals(type);
    }
    
    public BaseResource[] getResources()
        {return resources;}
    
    public Module[] getModules()
        {return modules;}
    
    /**
     * Returns true if the station has any item with the specified name.
     * @param name the name of the item to find
     * @return true if a search for any type of item of this name does not
     * return null
     */
    public boolean isItem(String name)
        {return hasModule(name) || hasResource(name) || hasExpander(name);}
    
    /**
     * Returns a module on the station with the specified name.
     * @param name the name of the module to find
     * @return the module with the specified name, null if not found
     */
    public Module getModule(String name)
    {
        for (Module module: modules)
            if (name.equalsIgnoreCase(module.getName()))
                return module;
        
        return null;
    }
    
    /**
     * Returns true if the station has a module with the specified name.
     * @param name the name of the module to find
     * @return true if a search for the module does not return null
     */
    public boolean hasModule(String name)
        {return getModule(name) != null;}
    
    /**
     * Returns the default module with the specified name.
     * @param name the name of the base module to find
     * @return the base module with the specified name, null if not found
     */
    public static Module getBaseModule(String name)
    {
        for (Module module: MODULES)
            if (name.equalsIgnoreCase(module.getName()))
                return module;
        
        return null;
    }
    
    /**
     * Returns true if a base module with the specified name exists.
     * @param name the name of the base module to find
     * @return true if a search for the module does not return null
     */
    public static boolean hasBaseModule(String name)
        {return getBaseModule(name) != null;}
    
    /**
     * Returns the default weapon with the specified name.
     * @param name the name of the base weapon to find
     * @return the base weapon with the specified name, null if not found
     */
    public static Weapon getBaseWeapon(String name)
    {
        for (Module module: MODULES)
            if (name.equalsIgnoreCase(module.getName()) &&
                    module instanceof Weapon)
                return (Weapon) module;
        
        return null;
    }
    
    /**
     * Returns true if a base weapon with the specified name exists.
     * @param name the name of the base weapon to find
     * @return true if a search for the weapon does not return null
     */
    public static boolean hasBaseWeapon(String name)
    {
        for (Module module: MODULES)
            if (name.equalsIgnoreCase(module.getName()) &&
                    module instanceof Weapon)
                return true;
        
        return false;
    }
    
    public Item getItem(String name)
    {
        if (hasResource(name))
            return getResource(name);
        else if (hasExpander(name))
            return getExpander(name);
        else
            return getModule(name);
    }
    
    /**
     * Returns the resource with the specified name.
     * @param name the name of the resource to find
     * @return the resource with the specified name, null if not found
     */
    public BaseResource getResource(String name)
    {
        for (BaseResource resource: resources)
            if (name.equalsIgnoreCase(resource.getName()))
                return resource;
        
        return null;
    }
    
    /**
     * Returns true if the station has a base module with the specified name.
     * @param name the name of the base module to find
     * @return true if a search for the module does not return null
     */
    public boolean hasResource(String name)
        {return getResource(name) != null;}
    
    /**
     * Returns the expander with the specified name.
     * @param name the name of the expander to find
     * @return the expander with the specified name, null if not found
     */
    public Expander getExpander(String name)
    {
        for (BaseResource resource: resources)
            if (name.equalsIgnoreCase(resource.getExpander().getName()))
                return resource.getExpander();
        
        return null;
    }
    
    /**
     * Returns true if an expander with the specified name exists.
     * @param name the name of the expander to find
     * @return true if a search for the expander does not return null
     */
    public boolean hasExpander(String name)
        {return getExpander(name) != null;}
    
    /**
     * Returns the default module with the specified name.
     * @param name the name of the base resource to find
     * @return the base resource with the specified name, null if not found
     */
    public static BaseResource getBaseResource(String name)
    {
        for (BaseResource resource: RESOURCES)
            if (name.equalsIgnoreCase(resource.getName()))
                return resource;
        
        return null;
    }
    
    /**
     * Returns true if a base resource with the specified name exists.
     * @param name the name of the base resource to find
     * @return true if a search for the module does not return null
     */
    public static boolean hasBaseResource(String name)
        {return getBaseResource(name) != null;}
    
    /**
     * Finds an object with the specified name and calls its define method.
     * @param name the name of the object to define
     * @return true if the object was found and defined
     */
    public static boolean define(String name)
    {
        for (Module module: MODULES)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                module.define();
                return true;
            }
        }
        
        for (BaseResource resource: RESOURCES)
        {
            if (name.equalsIgnoreCase(resource.getName()))
            {
                resource.define();
                return true;
            }
            
            if (name.equalsIgnoreCase(resource.getExpander().getName()))
            {
                resource.getExpander().define();
                return true;
            }
        }
        
        // No matching object was found, so return false
        return false;
    }
    
    public List<Ship> getShips()
        {return ships;}
    
    /**
     * Returns the number of ships that belong to a specified faction.
     * @param faction the faction to count ships in
     * @return the number of ships docked with the station that belong to the
     * faction, will be non-negative
     */
    public int getNShips(Faction faction)
    {
        int nShips = 0;
        
        for (Ship ship: ships)
            if (ship.isInFaction(faction))
                nShips++;
        
        return nShips;
    }
    
    /**
     * Creates an array of resources from the BaseResources constant in Station.
     * @return an array of resources made from Station's array of BaseResources
     */
    public static Resource[] copyResources()
    {
        Resource[] copy = new Resource[RESOURCES.length];
        
        for (int i = 0; i < RESOURCES.length; i++)
            copy[i] = new Resource(RESOURCES[i]);
        
        return copy;
    }
    
    /**
     * Randomly selects the station's type.
     * @return the generated type, will be non-null
     */
    private String generateType()
        {return rng.nextBoolean() ? TRADE : BATTLE;}
    
    /** Randomly generates the prices of each module and resource. */
    private void generatePrices()
    {
        for (Module module: modules)
            module.generatePrice();
        
        for (BaseResource resource: resources)
        {
            resource.generatePrice();
            resource.getExpander().generatePrice();
        }
    }
    
    /**
     * Copies modules and resources from constants to individual object fields.
     */
    private void cloneItems()
    {
        // It is VERY important that every module/resource be copied with "new"
        // Otherwise, every station will have the same prices
        
        // Modules must be copied to an ArrayList first to avoid problems with
        // array lengths
        List<Module> moduleList = new ArrayList<>();
        
        for (Module module: MODULES)
        {
            if (sells(module))
            {
                if (module instanceof Weapon)
                    moduleList.add(new Weapon((Weapon) module));
                else
                    moduleList.add(new Module(module));
            }
        }
        
        modules = new Module[moduleList.size()];
        
        for (int i = 0; i < modules.length; i++)
            modules[i] = moduleList.get(i);
        
        resources = new BaseResource[RESOURCES.length];
        
        for (int i = 0; i < RESOURCES.length; i++)
            resources[i] = new BaseResource(RESOURCES[i]);
    }
    
    public static void initializeModules()
            throws FileNotFoundException, IOException
        {MODULES = loadModules();}
    
    public static void initializeResources()
            throws FileNotFoundException, IOException
        {RESOURCES = loadResources();}
    
    /**
     * Loads the properties files of modules and weapons from their respective
     * manifest files and returns an array of all of them.
     * @return an array of every module and weapon
     */
    private static Module[] loadModules()
            throws FileNotFoundException, IOException
    {
        List<String> modulePaths = FileManager.getFilesInFolder(Paths.MODULES);
        List<String> weaponPaths = FileManager.getFilesInFolder(Paths.WEAPONS);
        
        List<Module> list = new ArrayList<>();
        
        for (String modulePath: modulePaths)
            list.add(new Module(FileManager.load(modulePath)));
        
        for (String weaponPath: weaponPaths)
            list.add(new Weapon(FileManager.load(weaponPath)));
        
        Module[] array = new Module[list.size()];
        return list.toArray(array);
    }
    
    /**
     * Loads the properties files of resources and their expanders from their
     * respective manifest files and returns an array of BaseResources.
     * @return an array of BaseResources with expanders from the matching line
     * of their manifest file
     */
    private static BaseResource[] loadResources()
            throws FileNotFoundException, IOException
    {
        List<String> resourcePaths =
                FileManager.getFilesInFolder(Paths.RESOURCES);
        List<String> expanderPaths =
                FileManager.getFilesInFolder(Paths.EXPANDERS);
        
        if (resourcePaths.size() != expanderPaths.size())
        {
            throw new IOException(
                    "Number of resources and expanders do not match.");
        }
        
        List<BaseResource> list = new ArrayList<>();
        
        for (int i = 0; i < resourcePaths.size(); i++)
        {
            list.add(new BaseResource(FileManager.load(resourcePaths.get(i)),
                                      FileManager.load(expanderPaths.get(i))));
        }
        
        BaseResource[] array = new BaseResource[list.size()];
        return list.toArray(array);
    }
}