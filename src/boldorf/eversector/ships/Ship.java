package boldorf.eversector.ships;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.addColorMessage;
import static boldorf.eversector.Main.addError;
import static boldorf.eversector.Main.addMessage;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.rng;
import boldorf.eversector.faction.Faction;
import static boldorf.eversector.faction.Relationship.RelationshipType.*;
import boldorf.eversector.items.*;
import boldorf.eversector.locations.*;
import boldorf.eversector.map.*;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Paths;
import boldorf.eversector.storage.Reputations;
import boldorf.eversector.storage.Resources;
import boldorf.eversector.storage.Symbol;
import boldorf.util.Utility;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidmath.Coord;

/** A starship which can travel through and interact with the map. */
public class Ship implements ColorStringObject, Comparable<Ship>
{
    // Starting resource amounts
    public static final int FUEL    = 15;
    public static final int ENERGY  = 15;
    public static final int ORE     = 25;
    public static final int HULL    = 5;
    public static final int CREDITS = 100;
    
    public static final String DESTROYED = "destroyed";
    public static final String SHIELDED  = "shielded";
    public static final String CLOAKED   = "cloaked";
    
    /** The radius of the FOV without any scanners. */
    public static final double FOV_RADIUS = 5.0;
    
    /** The amount of credits gained by sending a distress signal. */
    public static final int DISTRESS_CREDITS = 100;
    
    /**
     * The amount of credits that a default ship is worth - must be manually
     * updated.
     */
    public static final int BASE_VALUE = 400;
    
    /**
     * The amount of hull that crash will damage down to, and ships at or below
     * it will be destroyed.
     */
    public static final int CRASH_THRESHOLD = 1;
    
    /** The limit to how many of each expander can be installed on a ship. */
    public static final int MAX_EXPANDERS = 100;
    
    /**
     * The amount by which each item looted from a destroyed ship will be
     * divided by.
     */
    public static final int LOOT_MODIFIER = 2; 
    
    /** The number of characters used when printing status. */
    public static final int SPACING = 12;
    
    /** The ship's identification. */
    private String name;
    /** The ship's AI. */
    private AI ai;
    /** The ship's location. */
    private Location location;
    /** Various booleans represented by Strings, their existence means true. */
    private List<String> flags;
    /** The faction that the ship belongs to, null if unaligned. */
    private Faction faction;
    /** The amount of credits possessed by the ship. */
    private int credits;
    /** How the ship is perceived by factions based on their deeds. */
    private Reputation[] reputations;
    /** The list of modules equipped on the ship. */
    private List<Module> modules;
    /** All modules not installed on the ship. */
    private List<Module> cargo;
    /** The resources on board the ship. */
    private Resource[] resources;
    
    /**
     * Creates a ship from a name, location, and faction.
     * @param name the name of the ship
     * @param location the location of the ship
     * @param faction the faction the ship belongs to
     */
    public Ship(String name, Location location, Faction faction)
    {
        this.name      = name;
        this.ai        = new AI(this);
        this.location  = location;
        this.flags     = new ArrayList<>();
        this.faction   = faction;
        this.credits   = CREDITS;
        this.modules   = new LinkedList<>();
        this.cargo     = new LinkedList<>();
        this.resources = Station.copyResources();
        
        createReputations();
        setResourceDefaults();
    }
    
    /**
     * Creates the ship from a map and a set of properties.
     * @param map the map the ship will use
     * @param properties the properties of the ship
     */
    public Ship(Map map, Properties properties)
    {
        // Basic hard-coded definitions
        name      = "Player";
        ai        = null;
        flags     = new ArrayList<>();
        modules   = new LinkedList<>();
        cargo     = new LinkedList<>();
        resources = Station.copyResources();
        
        setResourceDefaults();
        
        for (String key: properties.stringPropertyNames())
        {
            String value = properties.getProperty(key);
            
            if ("r_".equals(key.substring(0, 2)))
            {
                Resource resource = getResource(key.split("_")[1]);
                if (resource != null)
                {
                    String[] amtFraction = value.split("/");
                    
                    int amount = Math.abs(Utility.parseInt(amtFraction[0],
                            getDefaultAmount(resource)));
                    int capacity = Math.abs(Utility.parseInt(amtFraction[1],
                            getDefaultAmount(resource)));
                    
                    resource.setAmount(Math.min(amount, capacity));
                    resource.setCapacity(capacity);
                }
            }
            else
            {
                switch (key)
                {
                    case "name":
                        name = value;
                        break;
                    case "location":
                        location = Location.parseLocation(map, value);
                        break;
                    case "faction":
                        faction = map.getFaction(value);
                        break;
                    case "credits":
                        credits = Utility.parseInt(value, CREDITS);
                        break;
                    case "modules":
                        String[] moduleStrings = value.split(", ");
                        for (String moduleString: moduleStrings)
                            addModule(moduleString);
                }
            }
        }
        
        // Must be done after location is set up
        createReputations();
    }
    
    /**
     * Creates an unaligned ship from a name and Location.
     * @param name the name of the ship
     * @param location the Location of the ship
     */
    public Ship(String name, Location location)
        {this(name, location, null);}
    
    @Override
    public String toString()
        {return isPlayer() ? name : getClassification() + " " + name;}
    
    @Override
    public ColorString toColorString()
    {
        return isAligned() ? new ColorString(toString(), faction.getColor()) :
                new ColorString(toString());
    }
    
    public String       getName()      {return name;     }
    public AI           getAI()        {return ai;       }
    public Faction      getFaction()   {return faction;  }
    public int          getCredits()   {return credits;  }
    public Resource[]   getResources() {return resources;}
    public List<Module> getModules()   {return modules;  }
    public List<Module> getCargo()     {return cargo;    }
    
    public boolean isInSector() {return location instanceof SectorLocation; }
    public boolean isLanded()   {return location instanceof PlanetLocation; }
    public boolean isDocked()   {return location instanceof StationLocation;}
    public boolean isInBattle() {return location instanceof BattleLocation; }
    
    public boolean isOrbital() 
    {
        return location instanceof SectorLocation &&
                !(location instanceof PlanetLocation ||
                location instanceof StationLocation ||
                location instanceof BattleLocation);
    }
    
    public boolean isDestroyed() {return hasFlag(DESTROYED);}
    public boolean isShielded()  {return hasFlag(SHIELDED); }
    public boolean isCloaked()   {return hasFlag(CLOAKED);  }
    
    public Location getLocation()
        {return location;}
    
    public SectorLocation getSectorLocation()
        {return (SectorLocation) location;}
    
    public PlanetLocation getPlanetLocation()
        {return (PlanetLocation) location;}
    
    public StationLocation getStationLocation()
        {return (StationLocation) location;}
    
    public BattleLocation getBattleLocation()
        {return (BattleLocation) location;}
    
    /**
     * Returns true if there is a flag in the flags list with the given name.
     * @param flag the String to find in the flags list
     * @return true if the flags list contains a String that matches the one
     * provided
     */
    public boolean hasFlag(String flag)
        {return flags.contains(flag);}
    
    /**
     * Returns true if this ship is the player.
     * @return true if this is equal to the map's specified player
     */
    public boolean isPlayer()
        {return location.getMap().getPlayer() == this;}
    
    /**
     * Returns true if this ship belongs to a faction.
     * @return true if this ship's faction is not null
     */
    public boolean isAligned()
        {return faction != null;}
    
    /**
     * Returns true if the ship is unaligned and their reputation is too low
     * for any faction to accept them.
     * @return true if the player is considered a pirate
     */
    public boolean isPirate()
    {
        if (isAligned())
            return false;
        
        boolean isPirate = true;
        for (Reputation rep: reputations)
            if (rep.get() >= Reputations.REQ_REJECTION)
                isPirate = false;
        
        return isPirate;
    }
    
    /**
     * Returns true if this ship is the leader of its faction.
     * @return true if the leader of the ship's faction is itself
     */
    public boolean isLeader()
        {return isAligned() ? faction.isLeader(this) : false;}
    
    /**
     * Returns true if the specified ship is considered non-hostile (including
     * if this ship is a pirate).
     * @param ship the ship to check
     * @return true if: the ship is not a pirate and this ship is a pirate or
     * an ally (this is so reputation is lowered for piracy)
     */
    public boolean isPassive(Ship ship)
    {
        return ship.isAligned() && (!isAligned() || faction == ship.faction ||
               !ship.getFaction().isRelationship(WAR, faction));
    }
    
    /**
     * Returns true if the specified faction is hostile to this ship.
     * @param faction the faction to check
     * @return true if: the faction is not this ship's faction and is at war
     */
    public boolean isHostile(Faction faction)
    {
        return this.faction != faction && (!isAligned() ||
                this.faction.isRelationship(WAR, faction));
    }
    
    public void setName(String name)
        {this.name = name;}
    
    /**
     * Sets the ship's AI to the given AI.
     * @param ai the AI to assign to this ship
     */
    public void setAI(AI ai)
        {this.ai = ai;}
    
    /**
     * Sets the ship's faction without modifying reputation.
     * @param faction the faction to assign to this ship
     */
    public void setFaction(Faction faction)
        {this.faction = faction;}
    
    /**
     * Sets the ship's location to the specified location.
     * @param destination the location to move the ship to
     */
    public void setLocation(Location destination)
    {
        if (destination == null)
            return;
        
        if (destination instanceof StationLocation &&
                !(location instanceof StationLocation))
        {
            getSectorLocation().getStation().getShips().add(this);
            location.getSector().getShips().remove(this);
        }
        else if (destination instanceof PlanetLocation)
        {
            if (location instanceof PlanetLocation)
            {
                ((PlanetLocation) location).getRegion().getShips().remove(this);
                ((PlanetLocation) destination).getRegion().getShips().add(this);
            }
            else
            {
                ((PlanetLocation) destination).getRegion().getShips().add(this);
                location.getSector().getShips().remove(this);
            }
        }
        else if (destination instanceof SectorLocation)
        {
            if (!(location instanceof SectorLocation))
            {
                location.getSector().getShips().add(this);
            }
            else if (location instanceof PlanetLocation)
            {
                location.getSector().getShips().add(this);
                getPlanetLocation().getRegion().getShips().remove(this);
            }
            else if (location instanceof StationLocation)
            {
                location.getSector().getShips().add(this);
                getSectorLocation().getStation().getShips().remove(this);
            }
        }
        else if (location instanceof SectorLocation &&
                !(destination instanceof SectorLocation))
        {
            location.getSector().getShips().remove(this);
        }
        
        location = destination;
    }
    
    public double getFOVRadius()
        {return hasModule(Actions.SCAN) ? FOV_RADIUS * 2.0 : FOV_RADIUS;}
    
    public List<Coord> getFOV()
    {
        double[][] light = new FOV().calculateFOV(
                getLocation().getMap().getResistanceMap(),
                getLocation().getCoord().x,
                getLocation().getCoord().y,
                getFOVRadius());
        
        List<Coord> fov = new ArrayList<>();
        for (int y = 0; y < light.length; y++)
            for (int x = 0; x < light[y].length; x++)
                if (light[x][y] > 0.0)
                    fov.add(Coord.get(x, y));
    
        return fov;
    }
    
    /**
     * Generates a list of the ship's properties and returns them.
     * @return a Properties object with information about the ship
     */
    public Properties toProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("name", name);
        properties.setProperty("location", location.toString());
        if (isAligned())
            properties.setProperty("faction", faction.getName());
        properties.setProperty("credits", Integer.toString(credits));
        
        StringBuilder builder = new StringBuilder();
        for (Module module: modules)
            builder.append(module.getName().toLowerCase()).append(", ");
        
        if (builder.length() > 0)
        {
            builder.delete(builder.length() - 2, builder.length());
            properties.setProperty("modules", builder.toString());
        }
        
        for (Resource resource: resources)
        {
            properties.setProperty("r_" + resource.getName().toLowerCase(),
                    resource.getAmountAsFraction());
        }
        
        return properties;
    }
    
    public boolean changeCredits(Faction faction, int change)
    {
        if (changeCredits(change))
        {
            if (faction != null)
                faction.changeEconomy(-change);
            return true;
        }
        return false;
    }
    
    /**
     * Changes the ship's amount of credits by a given amount.
     * @param change the amount to change credits by
     * @return true if the change was completed
     */
    public boolean changeCredits(int change)
    {
        if (credits + change < 0)
            return false;
        
        credits += change;
        return true;
    }
    
    /**
     * Returns the reputation object with the faction specified, null if not
     * found.
     * @param faction the faction to get a reputation with
     * @return the Reputation object with the faction specified, null if not
     * found
     */
    public Reputation getReputation(Faction faction)
    {
        if (faction == null)
            return null;
        
        for (Reputation rep: reputations)
            if (rep.getFaction() == faction)
                return rep;
        
        return null;
    }
    
    /** Slowly adjusts reputation towards zero, if not already zero. */
    public void fadeReputations()
    {
        for (Reputation rep: reputations)
        {
            if (rep.get() != 0)
            {
                int change = Math.max(1, Math.abs(rep.get()) /
                        Reputation.FADE_MODIFIER);

                if (rep.get() > 0)
                    rep.change(-change);
                else
                    rep.change(change);
            }
        }
    }
    
    /**
     * Changes the ship's reputation by a specified amount.
     * @param otherFaction the faction with which to change the ship's reputation
     * @param change the amount by which to change the ship's reputation 
     */
    public void changeReputation(Faction otherFaction, int change)
    {
        Reputation rep = getReputation(otherFaction);
        
        if (rep == null)
            return;
        
        rep.change(change);
        
        if (rep.get() < Reputations.REQ_REJECTION &&
                rep.getFaction() == faction)
        {
            // Print before faction is left so that null is not printed
            addPlayerColorMessage(new ColorString("The ")
                    .add(faction)
                .add(" has rejected you on account of your transgressions."));
            
            Faction oldFaction = faction;
            
            // Must directly leave faction so that this method does not recurse
            faction = null;
            
            if (isLeader())
                oldFaction.holdElection();
        }
    }
    
    /**
     * Changes the ship's reputation among all factions by the specified amount.
     * @param change the amount by which to change the ship's reputation for
     * every faction
     */
    public void changeGlobalReputation(int change)
    {
        for (Reputation rep: reputations)
            changeReputation(rep.getFaction(), change);
    }
    
    /**
     * Returns the Reputation object for the faction that the player is most
     * respected in, besides the faction they are a part of.
     * @return the Reputation object containing the popular faction and the
     * ship's reputation in it
     */
    public Reputation getMostPopularOtherFaction()
    {
        Reputation highestRep = null;
        
        for (Reputation rep: reputations)
            if ((highestRep == null || rep.get() > highestRep.get()) &&
                    rep.getFaction() != faction)
                highestRep = rep;
        
        return highestRep;
    }
    
    /**
     * Adds the given String as a flag if it does not exist already.
     * @param flag adds the provided String into the flags list
     * @return true if the addition was successful
     */
    public final boolean addFlag(String flag)
        {return flags.contains(flag) ? false : flags.add(flag);}
    
    /**
     * Removes the given String from the flag list.
     * @param flag the String to remove from the flags list
     * @return true if the removal was successful
     */
    public final boolean removeFlag(String flag)
        {return flags.remove(flag);}
    
    /**
     * Returns the constant default amount of a resource.
     * @param resource the name of the resource to get the default of
     * @return the constant amount for the resource of the given name
     */
    public static int getDefaultAmount(String resource)
    {
        switch (resource.toLowerCase().trim())
        {
            case "credits":
                return CREDITS;
            case "fuel":
                return FUEL;
            case "energy":
                return ENERGY;
            case "ore":
                return ORE;
            case "hull":
                return HULL;
            default:
                return 0;
        }
    }
    
    /**
     * Performs the same function as getDefaultAmount(String), but uses the
     * resource's name instead.
     * @param resource the Resource whose name will be used
     * @return the constant amount for the resource of the given name
     */
    public static int getDefaultAmount(Resource resource)
        {return getDefaultAmount(resource.getName());}
    
    /**
     * Calculates the value of the ship.
     * @return the value of everything on the ship, will be non-negative
     */
    public int calculateShipValue()
    {
        int value = credits;
        
        for (Resource resource: resources)
        {
            value += resource.getTotalValue();
            value += resource.getNExpanders() *
                     resource.getExpander().getValue();
        }
        
        for (Module module: modules)
            value += module.getValue();
        
        return value;
    }
    
    /**
     * Changes the amount of a resource on the ship.
     * @param name the name of the resource to change
     * @param change the amount to change the resource by
     * @return true if the change was completed
     */
    public boolean changeResource(String name, int change)
    {
        Resource resource = getResource(name);
        
        if (resource == null)
            return false;
        
        return resource.changeAmount(change);
    }
    
    /**
     * Calculates the amount of damage done by a weapon to the ship.
     * @param weapon the weapon used to deal damage
     * @return the amount of damage done by the weapon
     */
    public int getDamageFrom(Weapon weapon)
    {
        int damage = weapon.getDamage();
        
        if (weapon.isEnergy() && isShielded() && hasModule(Actions.SHIELD))
        {
            if (changeResourceBy(getModule(Actions.SHIELD).getAction()))
                damage /= 2;
            else
                removeFlag(SHIELDED);
        }
        
        return Math.max(damage, 1);
    }
    
    /**
     * Returns true if the ship has any weapons.
     * @return true if there is at least one weapon on the ship
     */
    public boolean hasWeapons()
    {
        for (Module module: modules)
            if (module != null && module instanceof Weapon)
                return true;
        
        return false;
    }
    
    /**
     * Returns true if the ship has any modules that can be activated.
     * @return true if there is at least one module with an effect on the ship
     */
    public boolean hasActivationModules()
    {
        for (Module module: modules)
            if (module != null && module.hasEffect())
                return true;
        
        return false;
    }
    
    /**
     * Returns true if the ship is equipped with the specified module.
     * @param name the name of the module to find
     * @return true if the module is equipped on the ship
     */
    public boolean hasModule(String name)
    {
        for (Module module: modules)
            if (module != null && name.equalsIgnoreCase(module.getName()))
                return true;
        
        return false;
    }
    
    /**
     * Performs the same function as hasModule(String), except with a
     * pre-existing module.
     * @param module the module to find
     * @return whether a module of the same name exists on the ship
     */
    public boolean hasModule(Module module)
        {return hasModule(module.getName());}
    
    /**
     * Scans through the ship's modules and returns the first module with the
     * specified name.
     * @param name the name of the module to find
     * @return the first module encountered with the specified name, null if not
     * found
     */
    public Module getModule(String name)
    {
        for (Module module: modules)
            if (name.equalsIgnoreCase(module.getName()))
                return module;
        
        return null;
    }
    
    /**
     * Performs the same function as hasModule(String), except with a
     * pre-existing module.
     * @param module the module to find
     * @return the first module encountered with the same name as the specified
     * module, null if not found
     */
    public Module getModule(Module module)
        {return getModule(module.getName());}
    
    /**
     * Scans through the ship's cargo and returns the first module with the
     * specified name.
     * @param name the name of the module to find
     * @return the first module encountered in cargo with the specified name,
     * null if not found
     */
    public Module getCargoModule(String name)
    {
        for (Module module: cargo)
            if (name.equalsIgnoreCase(module.getName()))
                return module;
        
        return null;
    }
    
    /**
     * Returns the amount of modules with the specified name on board the ship.
     * @param name the name of the modules to find
     * @return the number of modules with the specified name, will be
     * non-negative
     */
    public int getModuleAmount(String name)
    {
        int amount = 0;
        
        for (Module module: modules)
            if (name.equalsIgnoreCase(module.getName()))
                amount++;
        
        for (Module module: cargo)
            if (name.equalsIgnoreCase(module.getName()))
                amount++;
        
        return amount;
    }
    
    /**
     * Performs the same function as getModuleAmount(String), using the module's
     * name instead.
     * @param module the module to find the amount of, must be non-null
     * @return the number of modules with the same name, will be non-negative
     */
    public int getModuleAmount(Module module)
        {return module == null ? 0: getModuleAmount(module.getName());}
    
    /**
     * Performs the same function as getModule(), but will only return the
     * module if it is a weapon.
     * @param name the name of the weapon to find
     * @return the first weapon installed of the same name, null if not found
     */
    public Weapon getWeapon(String name)
    {
        Module module = getModule(name);
        if (module != null && module instanceof Weapon)
            return (Weapon) module;
        
        return null;
    }
    
    /**
     * Returns the number of modules installed in the ship.
     * @return the number of modules in the ship's array, will be non-negative
     */
    public int getModulesUsed()
        {return modules.size();}
    
    /**
     * Returns the number of modules in cargo.
     * @return the number of modules in cargo, will be non-negative
     */
    public int getCargoModules()
        {return cargo.size();}
    
    /**
     * Returns the number of weapons installed in the ship.
     * @return the number of modules that are weapons in the ship's array, will
     * be non-negative
     */
    public int getWeaponsUsed()
    {
        int counter = 0;
        
        for (Module module: modules)
            if (module instanceof Weapon)
                counter++;
        
        return counter;
    }
    
    /**
     * Adds the specified module to the ship's module array, or cargo if already
     * installed.
     * @param module the module to add
     */
    public void addModule(Module module)
    {
        if (hasModule(module.getName()))
            cargo.add(module);
        else
            modules.add(module);
    }
    
    /**
     * Performs the same function as addModule(Module), finding a module of the
     * designated name to add - must be final as it is used in a constructor.
     * @param name the name of the module to find and add
     */
    public final void addModule(String name)
    {
        if (isDocked())
        {
            Module module = getSectorLocation().getStation().getModule(name);
            if (module != null)
                addModule(module);
        }
        else
        {
            addModule(Station.getBaseModule(name));
        }
    }
    
    /**
     * Locates and removes a module of a designated name.
     * @param name the name of the module
     * @return true if the module was removed
     */
    public boolean removeModule(String name)
    {
        if (cargo.remove(getCargoModule(name)))
            return true;
        
        if (modules.remove(getModule(name)))
            return true;
        
        addPlayerError("Specified module not found on ship.");
        return false;
    }
    
    /**
     * Performs the same function as removeModule(String), using the module's
     * name as the name of the removed module.
     * @param module the module whose name will be used in the removal
     * @return true if the module was removed
     */
    public boolean removeModule(Module module)
        {return removeModule(module.getName());}
    
    /**
     * Finds a resource on the ship with the name provided and returns it.
     * @param name the name of the resource to find
     * @return the resource with the specified name, null if not found
     */
    public final Resource getResource(String name)
    {
        // Must be final since it is used in construction
        for (Resource resource: resources)
            if (name.equalsIgnoreCase(resource.getName()))
                return resource;
        
        return null;
    }
    
    /**
     * Return an expander from a resource on the ship with the specified name.
     * @param expanderName the name of the expander to find
     * @return the expander with the specified name, null if not found
     */
    public Expander getExpander(String expanderName)
    {
        for (Resource resource: resources)
            if (expanderName.equalsIgnoreCase(resource.getExpander().getName()))
                return resource.getExpander();
        
        return null;
    }
    
    /**
     * Returns a resource on the ship with an expander of a specified name.
     * @param expanderName the name of the expander on the resource to find
     * @return a resource on the ship with the designated expander name, null if
     * not found
     */
    public Resource getResourceFromExpander(String expanderName)
    {
        for (Resource resource: resources)
            if (expanderName.equalsIgnoreCase(resource.getExpander().getName()))
                return resource;
        
        return null;
    }
    
    /**
     * Returns true if the ship has a resource with the name provided.
     * @param name the name of the resource to find
     * @return true if a search for the resource does not return null
     */
    public boolean hasResource(String name)
        {return getResource(name) != null;}
    
    /**
     * Returns the amount of a resource on the ship with a specified name.
     * @param name the name of the resource to get the amount of
     * @return the amount of the resource, -1 if not found
     */
    public int getAmountOf(String name)
    {
        Resource resource = getResource(name);
        return resource == null ? -1 : resource.getAmount();
    }
    
    /**
     * Returns the capacity of a resource on the ship with a specified name.
     * @param name the name of the resource to get the capacity of
     * @return the capacity of the resource, -1 if not found
     */
    public int getCapOf(String name)
    {
        Resource resource = getResource(name);
        return resource == null ? -1 : resource.getCapacity();
    }
    
    /**
     * Changes an action's resource by the cost of that action.
     * @param action the action to use in the change
     * @return true if the change was completed
     */
    public boolean changeResourceBy(Action action)
    {
        return action == null ? false : getResource(action.getResource())
                .changeAmount(-action.getCost());
    }
    
    /**
     * Buys a resource on the ship with a specified name, increasing by a
     * specified amount for a cost relative to the current station's prices.
     * @param name the name of the resource to buy
     * @param quantity the amount of the resource to buy; will sell the resource
     * if negative
     * @return true if the purchase was successful
     */
    public boolean buyResource(String name, int quantity)
    {
        if (!validateDocking())
            return false;
        
        if (quantity == 0)
        {
            addPlayerError("Quantity of items in transaction must be "
                    + "positive.");
            return false;
        }
        
        Resource resource = getResource(name);
        Station station = getSectorLocation().getStation();
        
        if (resource == null)
        {
            // Try finding if an expander was specified and continue purchase
            Expander expander = station.getExpander(name);
            
            if (expander == null)
            {
                addPlayerError("The specified item does not exist.");
                return false;
            }
            
            resource = getResourceFromExpander(expander.getName());
            int price = expander.getPrice() * quantity;
            
            if (!validateFunds(price))
                return false;
            
            if (!resource.canExpand(quantity))
            {
                addPlayerError("Inadequate expanders to sell; have "
                            + resource.getNExpanders() + ", need "
                            + Math.abs(quantity) + ".");
                return false;
            }
            
            if (resource.getNExpanders() + quantity > MAX_EXPANDERS)
            {
                addPlayerError("The ship cannot store over " + MAX_EXPANDERS
                        + " " + expander.getName().toLowerCase() + "s.");
                return false;
            }
            
            changeCredits(station.getFaction(), resource.getPrice() *
                    Math.max(0, resource.getAmount() - resource.getCapacity()));
            resource.expand(quantity);
            changeCredits(station.getFaction(), -price);
            return true;
        }
        
        if (!resource.canSell() && quantity < 0)
        {
            addError(resource + " cannot be sold.");
            return false;
        }
        
        int price = station.getResource(name).getPrice() * quantity;
        
        if (!validateFunds(price))
            return false;
        
        if (!resource.canHold(quantity))
        {
            if (isPlayer())
            {
                if (quantity > 0)
                {
                    addError("Inadequate storage; have "
                            + resource.getCapacity() + ", need "
                            + (resource.getAmount() + quantity) + ".");
                }
                else
                {
                    addError("Inadequate resources to sell; have "
                            + resource.getAmount() + ", need "
                            + Math.abs(quantity) + ".");
                }
            }
            return false;
        }
        
        resource.changeAmount(quantity);
        changeCredits(station.getFaction(), -price);
        return true;
    }
    
    /**
     * Performs the same function as getMaxBuyAmount(Resource), with the name of
     * the resource instead.
     * @param name the name of the resource being purchased
     * @return the highest amount of the resource that can be purchased, -1 if
     * the resource was not found
     */
    public int getMaxBuyAmount(String name)
    {
        if (hasResource(name))
            return getMaxBuyAmount(getResource(name));
        
        if (getExpander(name) != null)
            return getMaxBuyAmount(getExpander(name));
        
        return -1;
    }
    
    /**
     * Returns the highest amount of a resource that can be purchased.
     * @param resource the resource being purchased
     * @return the highest amount of the resource that can be purchased, -1 if
     * the resource was not found
     */
    public int getMaxBuyAmount(Resource resource)
    {
        if (resource == null)
            return -1;
        
        if (isDocked())
        {
            return Math.min(credits / getSectorLocation().getStation()
                    .getResource(resource.getName()).getPrice(),
                    resource.getCapacity() - resource.getAmount());
        }
        
        return Math.min(credits / resource.getPrice(),
                resource.getCapacity() - resource.getAmount());
    }
    
    /**
     * Returns the most expanders of a specified type that can be purchased.
     * @param expander the expander being purchased
     * @return the most expanders that can be purchased, -1 if the expander was
     * not found
     */
    public int getMaxBuyAmount(Expander expander)
    {
        if (expander == null)
            return -1;
        
        if (isDocked())
        {
            return Math.min(MAX_EXPANDERS -
                    getResourceFromExpander(expander.getName()).getNExpanders(),
                    credits / getSectorLocation().getStation()
                            .getExpander(expander.getName()).getPrice());
        }
        
        return Math.min(MAX_EXPANDERS -
                getResourceFromExpander(expander.getName()).getNExpanders(),
                credits / expander.getPrice());
    }
    
    /**
     * Returns the highest amount of a resource that can be sold, which will be
     * the amount of the resource.
     * @param name the name of the resource
     * @return the highest amount of the resource that can be sold, -1 if the
     * resource was not found
     */
    public int getMaxSellAmount(String name)
    {
        Resource resource = getResource(name);
        
        if (resource == null)
        {
            resource = getResourceFromExpander(name);
            return resource == null ? -1 : resource.getNExpanders();
        }
            
        return resource.getAmount();
    }
    
    /**
     * Returns the highest amount of ore that can be refined into fuel.
     * @return the amount of ore possessed or the remaining space for fuel,
     * depending on which is lower
     */
    public int getMaxRefineAmount()
    {
        return Math.min(getAmountOf(Resources.ORE),
                getResource(Resources.FUEL).getRemainingSpace());
    }
    
    /**
     * Buys a module with the specified name from the current station and adds
     * it to the ship.
     * @param name the name of the module to add
     * @return true if the purchase was successful
     */
    public boolean buyModule(String name)
    {
        if (!validateDocking())
            return false;
        
        // Module must be retrieved after it is known that the ship is docked
        Station station = getSectorLocation().getStation();
        Module module = station.getModule(name);
        
        if (module == null || module.getName() == null)
        {
            if (Station.hasBaseModule(name))
            {
                addPlayerError(station + " does not sell modules of this "
                        + "type.");
                return false;
            }
            
            addPlayerError("Specified module does not exist.");
            return false;
        }
        
        if (!station.sells(module))
        {
            addPlayerError(station + " does not sell modules of this type.");
            return false;
        }
        
        if (module instanceof Weapon && isPirate())
        {
            addPlayerError(station + " refuses to sell weaponry to "
                    + "pirates.");
            
            return false;
        }
        
        int price = module.getPrice();
        
        if (!validateFunds(price))
            return false;
        
        // Capacity is checked by addModule
        addModule(module);
        changeCredits(station.getFaction(), -price);
        return true;
    }
    
    /**
     * Buys the module with the name of the one provided from the current
     * station and adds it to the ship.
     * @param module the module whose name will be used to find the module to
     * add
     * @return true if the purchase was successful
     */
    public boolean buyModule(Module module)
        {return buyModule(module.getName());}
    
    /**
     * Sells a module with a specified name from the ship to the current
     * station.
     * @param name the name of the module to sell
     * @return true if the sale was successful
     */
    public boolean sellModule(String name)
    {
        if (!validateDocking())
            return false;
        
        // Module must be retrieved after it is known that the ship is docked
        Station station = getSectorLocation().getStation();
        Module module = station.getModule(name);
        
        if (module == null)
        {
            if (Station.hasBaseModule(name))
            {
                addPlayerError(station + " will not accept a module of this "
                            + "type.");
                return false;
            }
            
            addPlayerError("Specified module does not exist.");
            return false;
        }
        
        if (!station.sells(module))
        {
            addPlayerError(station + " will not accept a module of this "
                        + "type.");
            return false;
        }
        
        // removeModule deals with the case where the module does not exist
        if (removeModule(module))
        {
            changeCredits(station.getFaction(), module.getPrice());
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if the player's credits are sufficient for a purchase of a
     * specified price.
     * @param price the price of the item to be purchased
     * funds
     * @return true if the player has enough credits for the purchase
     */
    public boolean validateFunds(int price)
    {
        if (price > credits)
        {
            addPlayerError("Insufficient funds; have " + credits
                    + " credits, need " + price + ".");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the ship is docked, and optionally prints a message if not.
     * @return true if the player is docked
     */
    public boolean validateDocking()
    {
        if (!isDocked())
        {
            addPlayerError("Ship must be docked with a station to buy and sell "
                    + "items.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Will burn in the designated coordinates.
     * @param direction the Direction in which to burn
     * @return true if burn was completed
     */
    public boolean burn(Direction direction)
    {
        if (isInSector())
        {
            addPlayerError("Ship must escape the sector before performing an "
                    + "interstellar burn.");
            return false;
        }
        
        if (direction.isDiagonal())
        {
            addPlayerError("Diagonal burns are not allowed.");
            return false;
        }
        
        if (!validateResources(Actions.BURN, "initiate a burn"))
            return false;
        
        Location target = location.move(direction);
        if (target == null)
            return false;
        
        setLocation(target);
        getResource(Actions.BURN.getResource())
                .changeAmount(-Actions.BURN.getCost());
        
        return true;
    }
    
    /**
     * Will warp the ship to the designated coordinates.
     * @param destination the Coord to warp to, must be on the map
     * @return true if the warp was completed
     */
    public boolean warpTo(Coord destination)
    {
        if (!validateModule(Actions.WARP, "warp"))
            return false;
        
        if (!validateResources(Actions.WARP.getAction(), "charge warp drive"))
            return false;
        
        Location targetLocation = location.moveTo(destination);
        
        if (targetLocation == null)
            return false;
        
        setLocation(targetLocation);
        getResource(Actions.WARP.getResource())
                .changeAmount(-Actions.WARP.getCost());
        return true;
    }
    
    /**
     * Enter a neighboring orbit around a star, if possible.
     * @param increase if true, will increase the orbit number; if false, will
     * decrease it
     * @return true if the maneuver was completed
     */
    public boolean orbit(boolean increase)
    {
        if (!isInSector())
        {
            addPlayerError("Ship must be in a sector to orbit it.");
            return false;
        }
        
        if (location.getSector().isEmpty())
        {
            addPlayerError("There is nothing to orbit in this sector.");
            return false;
        }
        
        if (isLanded())
        {
            addPlayerError("Ship must be orbital before attempting a "
                    + "maneuver.");
            return false;
        }
        
        if (isDocked())
        {
            addPlayerError("Ship must undock before attempting an orbital "
                    + "maneuver.");
            return false;
        }
        
        int orbit = getSectorLocation().getOrbit();
        int target = increase ? orbit + 1 : orbit - 1;
        
        if (!location.getSector().isValidOrbit(target))
        {
            if (increase)
                return escape();
            
            addPlayerError("Invalid orbit. Must be between 1 and "
                        + location.getSector().getOrbits() + ".");
            return false;
        }
        
        Resource resource = getResource(Actions.ORBIT.getResource());
        
        if (resource == null)
        {
            addPlayerError("Resource not found.");
            return false;
        }
        
        int cost = Actions.ORBIT.getCost();
        
        if (!validateResources(resource, cost, "perform an orbital maneuver"))
            return false;
        
        setLocation(getSectorLocation().setOrbit(target));
        resource.changeAmount(-cost);
        return true;
    }
    
    public boolean relocate(Direction direction)
    {
        if (!isLanded())
        {
            addPlayerError("You must already be landed on a planet to "
                    + "relocate.");
            return false;
        }
        
        if (direction.isDiagonal())
        {
            addPlayerError("Diagonal relocation is not allowed.");
            return false;
        }
        
        PlanetLocation target = getPlanetLocation().moveRegion(direction);
        
        if (target == null)
        {
            addPlayerError("Invalid region specified.");
            return false;
        }
        
        if (!validateResources(Actions.RELOCATE, "relocate"))
            return false;
        
        getResource(Actions.RELOCATE.getResource())
                .changeAmount(-Actions.RELOCATE.getCost());
        setLocation(target);
        return true;
    }
    
    public boolean canEscape()
    {
        if (isLanded())
        {
            addPlayerError("Ship must be orbital before attempting an escape.");
            return false;
        }
        
        if (isDocked())
        {
            addPlayerError("Ship must undock before attempting an escape.");
            return false;
        }
        
        if (!isInSector())
        {
            addPlayerError("Ship must be in a sector to escape from one.");
            return false;
        }
        
        if (getSectorLocation().getOrbit() < location.getSector().getOrbits())
        {
            addPlayerError("Ship must be at the furthest orbit of "
                    + location.getSector() + "to attempt an escape.");
            return false;
        }
        
        return validateResources(Actions.ESCAPE, "escape the gravity of "
                + location.getSector());
    }
    
    /**
     * Escapes gravitation influence of the current sector, if possible.
     * @return true if the escape was successful
     */
    public boolean escape()
    {
        if (!canEscape())
            return false;

        getResource(Actions.ESCAPE.getResource())
                .changeAmount(-Actions.ESCAPE.getCost());
        setLocation(getSectorLocation().escapeSector());
        return true;
    }
    
    /**
     * Performs the scanning action, although all results must be performed
     * elsewhere due to varying situations and targets.
     * @return true if scan was completed
     */
    public boolean scan()
    {
        if (!validateModule(Actions.SCAN, "conduct a scan"))
            return false;
        
        if (!validateResources(Actions.SCAN.getAction(), "conduct a scan"))
            return false;
        
        if (isDocked())
        {
            addPlayerError("You may not conduct a scan while docked.");
            return false;
        }
        
        getResource(Actions.SCAN.getResource())
                .changeAmount(-Actions.SCAN.getCost());
        return true;
    }
    
    /**
     * Enters the current sector, if not already in one.
     * @return true if entrance was successful
     */
    public boolean enter()
    {
        Sector sector = location.getSector();
        
        if (isInSector())
        {
            addPlayerError("Ship is already in " + sector + ".");
            return false;
        }
        
        if (sector.isEmpty())
        {
            addPlayerError("There is nothing in " + sector + ".");
            return false;
        }
        
        if (!validateResources(Actions.ENTER,
                "enter into orbit around " + sector))
            return false;

        getResource(Actions.ENTER.getResource())
                .changeAmount(-Actions.ENTER.getCost());
        setLocation(location.enterSector());
        return true;
    }
    
    /**
     * Docks with a station on the same orbit, if possible.
     * @return true if docking was successful
     */
    public boolean dock()
    {
        if (!isInSector())
        {
            addPlayerError("Ship must be in orbit to dock.");
            return false;
        }
        
        Station station = getSectorLocation().getStation();
        
        if (station == null)
        {
            addPlayerError("There is no station at this orbit.");
            return false;
        }
        
        if (isLanded())
        {
            addPlayerError("The ship cannot dock while landed.");
            return false;
        }
        
        if (isDocked())
        {
            addPlayerError("The ship is already docked with " + station + ".");
            return false;
        }
        
        if (isHostile(station.getFaction()) && isAligned())
        {
            setLocation(getSectorLocation().dock());

            if (!claim(false))
            {
                addPlayerError(station + " is controlled by the hostile "
                        + station.getFaction() + ", who deny you entry.");
                undock();
                return false;
            }
        }
        else
        {
            setLocation(getSectorLocation().dock());
        }
        
        repairModules();
        updatePrices();
        return true;
    }
    
    /**
     * Undocks with the currently docked station, if possible.
     * @return true if undocking was successful
     */
    public boolean undock()
    {
        if (!isDocked())
        {
            addPlayerError("The ship is not docked.");
            return false;
        }
        
        setLocation(getStationLocation().undock());
        return true;
    }
    
    /**
     * Lands on the planet at the ship's orbit, if possible.
     * @param coord the index of the region to land in
     * @return true if the landing was successful
     */
    public boolean land(Coord coord)
    {
        if (!canLand())
            return false;
        
        Planet planet = getSectorLocation().getPlanet();
        
        if (!planet.contains(coord))
        {
            addPlayerError("The specified region was not found on " + planet
                    + ".");
            return false;
        }
        
        getResource(Actions.LAND.getResource())
                .changeAmount(-Actions.LAND.getCost());
        setLocation(getSectorLocation().land(coord));
        return true;
    }
    
    /**
     * Crash lands on the planet at the ship's orbit, if possible.
     * @return true if the landing was successful
     */
    public boolean crashLand()
    {
        if (!canCrashLand())
            return false;
        
        if (getAmountOf(Resources.HULL) > CRASH_THRESHOLD)
        {
            getResource(Resources.HULL).setAmount(CRASH_THRESHOLD);
        }
        else
        {
            getResource(Resources.HULL).setAmount(0);
            destroy(false);
            return true;
        }
        
        Planet planet = getSectorLocation().getPlanet();
        setLocation(getSectorLocation().land(planet.getRandomCoord()));
        return true;
    }
    
    public boolean canLand()
    {
        if (!canCrashLand())
            return false;
        
        return validateResources(Actions.LAND,
                "land on " + getSectorLocation().getPlanet());
    }
    
    public boolean canCrashLand()
        {return canCrashLand(true);}
    
    /**
     * Returns true if there is a planet that the ship can land on at its orbit.
     * @param print if true, will print error messages for the player
     * @return true if landing of any kind is possible in the ship's situation
     */
    public boolean canCrashLand(boolean print)
    {
        if (!isInSector())
        {
            if (print)
                addPlayerError("Ship must be at a planet's orbit to land.");
            return false;
        }
        
        if (isDocked())
        {
            if (print)
                addPlayerError("Ship cannot land while docked.");
            return false;
        }
        
        if (isLanded())
        {
            if (print)
                addPlayerError("The ship is already landed.");
            return false;
        }
        
        Planet planet = getSectorLocation().getPlanet();
        
        if (planet == null)
        {
            if (print)
                addPlayerError("There is no planet at this orbit.");
            return false;
        }
        
        if (!planet.getType().canLandOn())
        {
            if (print)
            {
                addPlayerError("The ship cannot land on "
                        + Utility.addArticle(planet.getType().toString())
                        + ".");
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Takes off from the planet that the ship is currently landed on, if
     * possible.
     * @return true if the takeoff was successful
     */
    public boolean takeoff()
    {
        if (!isLanded())
        {
            addPlayerError("The ship is not landed.");
            return false;
        }
        
        if (!validateResources(Actions.TAKEOFF, "takeoff from the "
                + getPlanetLocation().getRegion()))
            return false;
        
        getResource(Actions.TAKEOFF.getResource())
                .changeAmount(-Actions.TAKEOFF.getCost());
        setLocation(getPlanetLocation().takeoff());
        return true;
    }
    
    /**
     * Extracts ore from the planet currently landed on, if possible.
     * @param print if true, will print information about the mining process
     * @return the number of units of ore discarded, -1 if no mining occurred
     */
    public boolean mine(boolean print)
    {
        if (!canMine(print))
            return false;
        
        Resource resource = getResource(Actions.MINE.getResource());
        
        Ore ore;
        if (isLanded())
            ore = getPlanetLocation().getRegion().getOre();
        else
            ore = location.getMap().getRandomOre();
        
        if (ore == null)
        {
            addPlayerError("There is no ore to mine in the "
                    + getPlanetLocation().getRegion().toString().toLowerCase()
                    + ".");
            return false;
        }
        
        int discard = getResource(Resources.ORE).changeAmountWithDiscard(ore
                .getDensity());
        resource.changeAmount(-Actions.MINE.getCost());
        
        if (isLanded())
        {
            Region region = getPlanetLocation().getRegion();
            region.extractOre(ore.getDensity());
            if (!region.hasOre())
            {
                addPlayerMessage("You have mined the " + region + " dry.");
                changeGlobalReputation(Reputations.MINE_DRY);
            }
        }
        else if (rng.nextBoolean())
        {
            // Chance of taking damage if mining from an asteroid belt
            damage(Planet.ASTEROID_DAMAGE, false);
            addPlayerMessage("Collided with an asteroid, dealing "
                    + Planet.ASTEROID_DAMAGE + " damage.");
        }
        
        if (isPlayer())
        {
            if (print)
            {
                addMessage("Extracted 1 unit of " + ore.getName().toLowerCase()
                        + ".");
                
                if (discard > 0)
                {
                    addMessage("Maximum ore capacity exceeded; " + discard
                            + " units discarded.");
                }
            }
        }
        
        changeGlobalReputation(Reputations.MINE);
        return true;
    }
    
    public boolean mine()
        {return mine(false);}
    
    /**
     * Returns true if the ship is capable of mining in its current state.
     * @param print if true, will print any errors
     * @return true if a mine would succeed
     */
    public boolean canMine(boolean print)
    {
        if (!isInSector())
        {
            if (print)
                addPlayerError("The ship must be in a sector to mine.");
            return false;
        }
        
        Planet planet = getSectorLocation().getPlanet();
        
        if (planet == null)
        {
            if (print)
                addPlayerError("There is no planet here to mine from.");
            return false;
        }
        
        if (!isLanded() && !planet.getType().canMineFromOrbit())
        {
            if (print)
                addPlayerError("Ship must be landed here to mine for ore.");
            return false;
        }
        
        if (!validateResources(Actions.MINE, "initiate mining operation"))
            return false;
        
        Resource ore = getResource(Resources.ORE);
        
        if (ore.isFull())
        {
            if (print)
                addPlayerError("Ore storage full; cannot acquire more.");
            return false;
        }
        
        return true;
    }
    
    public boolean canMine()
        {return canMine(false);}
    
    /**
     * Refines one unit of ore into one unit of fuel, if possible.
     * @return true if the refining was successful
     */
    public boolean refine()
    {
        if (!validateModule(Actions.REFINE, "refine ore"))
            return false;
        
        Resource ore = getResource(Resources.ORE);
        if (!ore.canHold(-1))
        {
            addPlayerError("Ship has no ore to refine.");
            return false;
        }
        
        Resource fuel = getResource(Resources.FUEL);
        if (!fuel.canHold(1))
        {
            addPlayerError("Insufficient fuel storage.");
            return false;
        }
        
        ore.changeAmount(-1);
        fuel.changeAmount(1);
        return true;
    }
    
    /**
     * Toggles the flag of a module on the ship, if possible.
     * @param name the name of the module to activate/deactivate
     * @return true if the module was activated or deactivated
     */
    public boolean toggleActivation(String name)
        {return toggleActivation(getModule(name));}
    
    /**
     * Toggles the flag of a module on the ship, if possible.
     * @param module the module to activate/deactivate
     * @return true if the module was activated or deactivated
     */
    public boolean toggleActivation(Module module)
    {
        if (module == null)
        {
            addPlayerError("Module not found on the ship.");
            return false;
        }
        
        if (!validateModule(module))
            return false;
        
        String effect = module.getEffect();
        
        if (effect == null)
        {
            addPlayerError(Utility.addCapitalizedArticle(module.getName())
                    + " cannot be activated.");
            return false;
        }
        
        if (hasFlag(effect))
        {
            addPlayerMessage("Your " + module.toString().toLowerCase()
                    + " has been deactivated.");
            removeFlag(effect);
            return true;
        }
        
        if (!validateResources(module.getAction(), "activate " + module))
            return false;

        addPlayerMessage("Your " + module.toString().toLowerCase()
                + " has been activated and will drain "
                + module.getAction() + " per turn of use.");

        addFlag(effect);
        return true;
    }
    
    /**
     * Fires on a designated ship using a weapon, if possible.
     * @param weapon the weapon to fire with
     * @param ship the ship to fire upon
     * @return true if the attack was successful
     */
    public boolean fire(Weapon weapon, Ship ship)
    {
        if (!canFire(weapon, ship))
            return false;
        
        // Print must be done first so destruction message comes afterwards
        if (isPlayer())
        {
            if (ship.isShielded() && weapon.isEnergy())
                addMessage("Attack diminished by enemy shield.");
            else
                addMessage("Attack successful; hit confirmed.");
        }
        
        ship.damageWith(weapon, false);
        getResource(weapon.getResource()).changeAmount(-weapon.getCost());
        return true;
    }
    
    /**
     * Returns true if it is possible to fire the given weapon at the given
     * ship.
     * @param weapon the weapon to fire with
     * @param ship the ship to fire upon
     * @return true if an attack with the given weapon would succeed on the
     * given ship
     */
    public boolean canFire(Weapon weapon, Ship ship)
    {
        if (weapon == null || !Station.hasBaseWeapon(weapon.getName()))
        {
            addPlayerError("The specified weapon does not exist.");
            return false;
        }
        
        if (!validateModule(weapon, "fire"))
            return false;
        
        if (ship == null)
        {
            addPlayerError("The specified ship was not found.");
            return false;
        }
        
        return validateResources(weapon.getAction(), "fire");
    }
    
    public Battle startBattle(Ship opponent)
    {
        if (!location.equals(opponent.location))
            return null;
        
        Battle battle = new Battle(this, opponent);
        setLocation(getSectorLocation().joinBattle(battle));
        opponent.setLocation(location);
        
        List<Ship> others = getSectorLocation().getShips();
        for (Ship other: others)
            if (other.ai != null && !other.isInBattle())
                other.ai.joinBattle(battle);
        
        if (opponent.isPlayer())
        {
            Main.pendingBattle = battle;
            opponent.addPlayerColorMessage(
                    new ColorString("You are under attack from ").add(this)
                            .add("!"));
            return battle;
        }
        
        battle.processBattle();
        return battle;
    }
    
    /**
     * Checks if the ship is equipped with a specified module and that it is
     * undamaged, and optionally prints a custom message if not.
     * @param module the module to validate
     * @param action the String to print as the need for the module
     * @return true if the ship at least one undamaged module equipped
     */
    public boolean validateModule(Module module, String action)
    {
        // The ship can technically have this installed because it doesn't exist
        if (module == null)
            return true;
        
        if (!hasModule(module))
        {
            if (action == null)
                addPlayerError(Utility.addCapitalizedArticle(module.getName())
                        + " is required.");
            else
                addPlayerError(Utility.addCapitalizedArticle(module.getName())
                        + " is required to " + action + ".");
            return false;
        }
        
        if (module.isDamaged())
        {
            if (getModuleAmount(module) > 1)
            {
                // Check for spares
                for (Module m: cargo)
                    if (m.getName().equalsIgnoreCase(module.getName()) &&
                            !m.isDamaged())
                        return true;
            }
            
            addPlayerError("Your " + module.getName().toLowerCase()
                    + " is too damaged to function.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the ship is equipped with a specified module and that it is
     * undamaged, and optionally prints a custom message if not.
     * @param module the name of the module to validate
     * @param action the String to print as the need for the module
     * @return true if the ship at least one undamaged module equipped
     */
    public boolean validateModule(String module, String action)
        {return validateModule(getModule(module), action);}
    
    /**
     * Checks if the ship is equipped with a specified module and that it is
     * undamaged, and optionally prints a message if not.
     * @param module the module to validate
     * @return true if the ship at least one undamaged module equipped
     */
    public boolean validateModule(Module module)
        {return validateModule(module, null);}
    
    /**
     * Checks if the ship is equipped with a specified module and that it is
     * undamaged, and optionally prints a message if not.
     * @param module the name of the module to validate
     * @return true if the ship at least one undamaged module equipped
     */
    public boolean validateModule(String module)
        {return validateModule(getModule(module), null);}
    
    /**
     * Checks if the ship has enough of the specified resource, and optionally
     * prints a message if not.
     * @param resource the resource to validate
     * @param cost the amount of the resource that the ship must possess
     * @param actionString the String to print as the need for resources
     * @return true if the ship has enough resources for the cost
     */
    public boolean validateResources(Resource resource, int cost,
            String actionString)
    {
        if (resource != null && resource.getAmount() < cost)
        {
            addPlayerError("Insufficient " + resource.getName().toLowerCase()
                    + " reserves to " + actionString + "; have "
                    + resource.getAmount() + ", need " + cost + ".");
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if the ship has enough of the specified resource, and optionally
     * prints a message if not.
     * @param resource the name of the resource to validate
     * @param cost the amount of the resource that the ship must possess
     * @param actionString the String to print as the need for resources
     * @return true if the ship has enough resources for the cost
     */
    public boolean validateResources(String resource, int cost,
            String actionString)
        {return validateResources(getResource(resource), cost, actionString);}
    
    /**
     * Checks if the ship has the required resources to perform an action, and
     * optionally prints a message if not.
     * @param action the action from which to find the resource and cost
     * @param actionString the String to print as the need for resources
     * @return true if the ship has enough resources for the cost
     */
    public boolean validateResources(Action action, String actionString)
    {
        return validateResources(getResource(action.getResource()),
                action.getCost(), actionString);
    }
    
    /**
     * Decreases hull strength by the damage of a given weapon, and destroys the
     * ship if the weapon deals too much damage.
     * @param weapon the weapon to damage the ship with
     * @param print if true, will print if the ship was destroyed
     */
    public void damageWith(Weapon weapon, boolean print)
        {damage(getDamageFrom(weapon), print);}
    
    /**
     * Decreases hull strength by a given amount, destroys the ship if the
     * damage is too great, and has the possibility of damaging modules.
     * @param damage the amount of damage to deal to the ship
     * @param print if true, will print if the ship was destroyed
     */
    public void damage(int damage, boolean print)
    {
        if (!getResource(Resources.HULL).changeAmount(-damage) ||
             getResource(Resources.HULL).isEmpty())
        {
            getResource(Resources.HULL).setAmount(0);
            destroy(print);
        }
        
        // Damages a module if the damage is above a threshold that is
        // proportional to the number of modules installed
        if (!modules.isEmpty() &&
                damage >= getResource(Resources.HULL).getCapacity()
                / modules.size())
        {
            Module damagedModule =
                    modules.get(rng.nextInt(modules.size()));
            
            if (damagedModule.damage())
            {
                addPlayerMessage("Your " + damagedModule.getName().toLowerCase()
                        + " has been damaged by the impact.");
                
                if (damagedModule.isEffect(SHIELDED) && isShielded())
                    removeFlag(SHIELDED);
                else if (damagedModule.isEffect(CLOAKED) && isCloaked())
                    removeFlag(CLOAKED);
            }
            else
            {
                addPlayerMessage("Your " + damagedModule.getName().toLowerCase()
                        + " has been destroyed by the impact!");
                modules.remove(damagedModule);
            }
        }
    }
    
    /**
     * Gather as much loot as possible from the given ship.
     * @param ship the ship to loot
     */
    public void loot(Ship ship)
    {
        /*
        addPlayerColorMessage(new ColorString("Salvaged ")
                .add(new ColorString(Integer.toString(salvagedCredits),
                        COLOR_FIELD))
                .add(" credits."));
        */
        
        int salvagedCredits = ship.credits / LOOT_MODIFIER;
        if (salvagedCredits > 0)
        {
            credits += salvagedCredits;
            addPlayerMessage("Salvaged " + salvagedCredits + " credits.");
        }
        
        for (Module module: ship.modules)
        {
            if (module != null &&
                    rng.nextDouble() <= (1.0 / (double) LOOT_MODIFIER))
            {
                addModule(module);
                addPlayerMessage("Salvaged "
                        + Utility.addArticle(module.getName()) + ".");
            }
        }
        
        for (Resource resource: ship.resources)
        {
            if (resource != null)
            {
                Resource yourResource = getResource(resource.getName());
                int nExpanders = resource.getNExpanders() / LOOT_MODIFIER;
                yourResource.expand(Math.min(MAX_EXPANDERS
                        - yourResource.getNExpanders(), nExpanders));
                
                if (nExpanders > 0)
                {
                    addPlayerMessage("Salvaged " + nExpanders + " "
                            + Utility.makePlural(resource.getExpander()
                                    .getName().toLowerCase(), nExpanders)
                            + ".");
                }
                
                int oldAmount = yourResource.getAmount();
                yourResource.changeAmountWithDiscard(resource.getAmount() /
                        LOOT_MODIFIER);
                
                int amountIncrease = yourResource.getAmount() - oldAmount;
                
                if (amountIncrease > 0)
                {
                    addPlayerMessage("Salvaged " + amountIncrease + " "
                            + resource.getName().toLowerCase() + ".");
                }
            }
        }
    }
    
    /**
     * Joins the entered faction and adds the relevant reputation.
     * @param faction the faction to join (use leaveFaction() instead of setting
     * this to null)
     */
    public void joinFaction(Faction faction)
    {
        if (this.faction == faction)
            return;
        
        this.faction = faction;
        changeReputation(faction, Reputations.JOIN);
    }
    
    /**
     * Leaves a faction, resetting the ship's faction and incurring a reputation
     * penalty if the ship was in a faction.
     */
    public void leaveFaction()
    {
        if (!isAligned())
            return;
        
        Faction oldFaction = faction;
        boolean wasLeader  = isLeader();
        
        faction = null;
        changeReputation(oldFaction, Reputations.LEAVE);
        
        if (wasLeader)
            oldFaction.holdElection();
    }
    
    /**
     * Converts another ship to this ship's faction.
     * @param ship the ship that is being converted
     * @return true if the ship was converted
     */
    public boolean convert(Ship ship)
    {
        if (!canConvert(ship))
            return false;
        
        Faction shipFaction = ship.faction;
        
        if (ship.isAligned())
            ship.leaveFaction();
        
        ship.joinFaction(faction);
        
        changeReputation(faction,      Reputations.CONVERT);
        changeReputation(shipFaction, -Reputations.CONVERT);
        
        ship.changeReputation(faction,      Reputations.CONVERT);
        ship.changeReputation(shipFaction, -Reputations.CONVERT);
        
        ship.addPlayerColorMessage(toColorString()
                .add(" has converted you to the ").add(faction).add("."));
        return true;
    }
    
    /**
     * Returns true if the ship is able to convert another ship to its faction.
     * @param ship the ship that is being converted
     * @return true if the ship can be converted
     */
    public boolean canConvert(Ship ship)
        {return isAligned() && faction != ship.faction && !ship.isPlayer();}
    
    /**
     * Claims a celestial body for the ship's faction.
     * @param print if true and this is the player, will print error messages
     * @return true if the celestial body was claimed
     */
    public boolean claim(boolean print)
    {
        if (!canClaim(print))
            return false;
        
        if (isLanded())
        {
            Region region = getPlanetLocation().getRegion();
            Planet planet = getSectorLocation().getPlanet();
            int nRegions = planet.getNRegions();
            changeCredits(region.getFaction(), -planet.getClaimCost());

            if (ALLIANCE == faction.getRelationship(region.getFaction()))
                changeReputation(faction, Reputations.CLAIM_ALLY / nRegions);
            else
                changeReputation(faction, Reputations.CLAIM / nRegions);

            changeReputation(region.getFaction(),
                    -Reputations.CLAIM / nRegions);

            // Claim must be done here so the faction relations can be checked
            region.claim(faction);
            return true;
        }
        
        if (!isDocked())
            return false;
        
        Station station = getSectorLocation().getStation();
        changeCredits(station.getFaction(), -Station.CLAIM_COST);
        
        if (ALLIANCE.equals(faction.getRelationship(
                station.getFaction())))
            changeReputation(faction, Reputations.CLAIM_ALLY);
        else
            changeReputation(faction, Reputations.CLAIM);
        
        changeReputation(station.getFaction(), -Reputations.CLAIM);
        
        // Claim must be done here so the faction relations can be checked
        station.claim(faction);
        return true;
    }
    
    public boolean canClaim(boolean print)
    {
        return (isLanded() && canClaim(getPlanetLocation().getPlanet(), print))
                || (isDocked() && canClaim(getSectorLocation().getStation(),
                        print));
    }
    
    public boolean canClaim(Planet planet, boolean print)
    {
        if (!isAligned())
        {
            if (print)
            {
                addPlayerError("You must be part of a faction to claim "
                        + "territory.");
            }
            return false;
        }
        
        if (credits < planet.getClaimCost())
        {
            if (print)
            {
                addPlayerError("You cannot afford the "
                        + planet.getClaimCost() + " credit cost to claim "
                        + "territory on " + planet + ".");
            }
            return false;
        }

        Region region = getPlanetLocation().getRegion();
        if (!region.hasOre())
        {
            addPlayerError("The " + region.toString().toLowerCase()
                    + " cannot be claimed.");
            return false;
        }
        
        if (region.getFaction() == faction)
        {
            addPlayerError("The " + region.toString().toLowerCase() + " is "
                    + "already claimed by the " + faction + ".");
            return false;
        }

        if (region.getNShips(region.getFaction()) > 0)
        {
            addPlayerError("There are currently ships of the "
                    + region.getFaction() + " guarding the "
                    + region.toString().toLowerCase() + ".");
            return false;
        }

        return true;
    }
    
    public boolean canClaim(Station station, boolean print)
    {
        if (!isAligned())
        {
            if (print)
            {
                addPlayerError("You must be part of a faction to claim "
                        + "territory.");
            }
            return false;
        }
        
        if (credits < Station.CLAIM_COST)
        {
            if (print)
            {
                addPlayerError("You cannot afford the "
                        + Station.CLAIM_COST + " credit cost to claim "
                        + station + ".");
            }
            return false;
        }
        
        // If the body is already claimed by solely your faction, return false
        if (station.getFaction() == faction)
        {
            if (print)
            {
                addPlayerError(station + " is already claimed by the "
                        + faction + ".");
            }
            return false;
        }
        
        if (station.getNShips(station.getFaction()) > 0)
        {
            if (print)
            {
                addPlayerError("There are currently ships of the "
                        + station.getFaction() + " guarding " + station
                        + ".");
            }
            return false;
        }
        
        return true;
    }
    
    public Faction getDistressResponder()
    {
        if (isPirate())
        {
            addPlayerMessage("There is no response.");
            return null;
        }
        
        if (isAligned())
        {
            if (!canDistress())
            {
                // Otherwise they will refuse, giving others a chance to help
                addPlayerColorMessage(new ColorString("The ").add(faction)
                        .add(" refuses to help you."));
            }
            else if (faction.getEconomyCredits() < DISTRESS_CREDITS)
            {
                addPlayerColorMessage(new ColorString("The ").add(faction)
                        .add(" cannot afford to help you."));
            }
            else
            {
                // The ship's faction will help if reputation is high enough
                return faction;
            }
        }
        
        Reputation offerReputation = getMostPopularOtherFaction();
        Faction offerFaction = offerReputation.getFaction();
        
        if (offerFaction == null ||
                offerFaction.getEconomyCredits() < DISTRESS_CREDITS ||
                offerReputation.get() + Reputations.JOIN + Reputations.DISTRESS
                <= Reputations.REQ_REJECTION)
        {
            return null;
        }
        
        return offerFaction;
    }
    
    /** Sends a distress signal and receives help from a faction if possible. */
    public void distress()
        {distress(getDistressResponder());}
    
    /**
     * Sends a distress signal and receives help from a faction if possible.
     * @param responder the faction that will respond
     */
    public void distress(Faction responder)
    {
        if (responder == null)
            return;
        
        if (responder != faction)
            joinFaction(responder);
        
        addPlayerColorMessage(new ColorString("The ").add(faction)
                .add(" responds and warps supplies to your location."));
        changeCredits(faction, DISTRESS_CREDITS);
        faction.changeEconomy(-refill());
        changeReputation(faction, Reputations.DISTRESS);
    }
    
    /**
     * Returns true if the ship's reputation is high enough to distress, but not
     * other factors such as actual faction involvement.
     * @return true if the ship's reputation is high enough to distress
     */
    public boolean canDistress()
    {
        if (!isAligned())
            return false;
        
        return getReputation(faction).get()
                + Math.abs(Reputations.REQ_REJECTION)
                > Math.abs(Reputations.DISTRESS);
    }
    
    /**
     * Removes the ship from all collections and marks it as destroyed.
     * @param print if true, will print a message about the ship's destruction
     */
    public void destroy(boolean print)
    {
        location.getSector().removeLetter((int) name.charAt(name.length() - 1));
        location.getSector().getShips().remove(this);
        
        if (isDocked())
            getSectorLocation().getStation().getShips().remove(this);
        else if (isLanded())
            getPlanetLocation().getRegion().getShips().remove(this);
        
        addFlag(DESTROYED);
        
        if (isPlayer())
            playSoundEffect(Paths.DEATH);
        else if (print)
            addColorMessage(toColorString().add(" has been destroyed."));
    }
    
    /**
     * Refills all of the ship's resources (except ore).
     * @return the total cost of the materials refilled
     */
    public int refill()
    {
        int cost = 0;
        
        cost += getResource(Resources.FUEL).fill()
                * getResource(Resources.FUEL).getValue();
        
        cost += getResource(Resources.ENERGY).fill()
                * getResource(Resources.ENERGY).getValue();
        
        cost += getResource(Resources.HULL).fill()
                * getResource(Resources.HULL).getValue();
        
        return cost;
    }
    
    /** Repairs all the modules on the ship. */
    public void repairModules()
    {
        for (Module module: modules)
            module.repair();
        
        for (Module module: cargo)
            module.repair();
    }
    
    public List<ColorString> getStatusList()
    {
        List<ColorString> contents = new LinkedList<>();
        contents.add(new ColorString("Credits: ").add(new ColorString(
                Integer.toString(credits) + Symbol.CREDITS, COLOR_FIELD)));
        
        for (Resource resource: resources)
        {
            contents.add(resource.getAmountAsColoredFraction().add(
                    " " + resource.getName()));
        }
        
        if (getModulesUsed() > 0)
        {
            contents.add(null);
            for (Module module: modules)
            {
                ColorString moduleString = new ColorString(module.toString());
                if (module.isDamaged())
                {
                    moduleString.add(new ColorString(" (Damaged)",
                            AsciiPanel.brightRed));
                }
                else if (hasFlag(module.getEffect()))
                {
                    moduleString.add(new ColorString(" (Active)",
                            AsciiPanel.brightGreen));
                }
                contents.add(moduleString);
                
            }
            for (Module module: cargo)
            {
                ColorString moduleString = new ColorString(module.toString())
                        .add(new ColorString(" (Cargo)", AsciiPanel.yellow));
                if (module.isDamaged())
                {
                    moduleString.add(new ColorString(" (Damaged)",
                                    AsciiPanel.brightRed));
                }
                contents.add(moduleString);
            }
        }
        
        return contents;
    }
    
    /**
     * Adds the given ColorString as a message, only if this ship is the player.
     * Intended for player-specific messages.
     * @param s the ColorString to add as a message if this ship is the player
     */
    public void addPlayerColorMessage(ColorString s)
    {
        if (isPlayer())
            addColorMessage(s);
    }
    
    /**
     * Adds the given String as a message, only if this ship is the player.
     * Intended for player-specific messages.
     * @param s the String to add as a message if this ship is the player
     */
    public void addPlayerMessage(String s)
    {
        if (isPlayer())
            addMessage(s);
    }
    
    /**
     * Adds the given String as an error, only if this ship is the player.
     * Intended for player-specific errors.
     * @param s the String to add as an error if this ship is the player
     */
    public void addPlayerError(String s)
    {
        if (isPlayer())
            addError(s);
    }
    
    public void playPlayerSound(String path)
    {
        if (isPlayer())
            playSoundEffect(path);
    }
    
    /**
     * Generates a relevant classification for the ship based off its abilities
     * in both battle and mining.
     * @return a String that describes the ship's strength and specialization
     */
    public String getClassification()
    {
        int battleLevel = Math.min(getBattleLevel(), Levels.MAX_LEVEL);
        int miningLevel = Math.min(getMiningLevel(), Levels.MAX_LEVEL);
        
        if (battleLevel < Levels.BASE_LEVEL && miningLevel < Levels.BASE_LEVEL)
            return Levels.LOWEST_LEVEL;
        
        if (battleLevel >= Levels.MAX_LEVEL && miningLevel >= Levels.MAX_LEVEL)
            return Levels.HIGHEST_LEVEL;
        
        if (battleLevel > miningLevel)
            return Levels.BATTLE_LEVELS[battleLevel - 1];
        else
            return Levels.MINING_LEVELS[miningLevel - 1];
    }
    
    /**
     * Returns the type of level that the ship has the most of.
     * @return the level type with the highest amount, null if they are equal
     */
    public String getHigherLevel()
    {
        if (getAbsoluteMiningLevel() > getAbsoluteBattleLevel())
            return "mining";
        
        if (getAbsoluteMiningLevel() < getAbsoluteBattleLevel())
            return "battle";
        
        return null;
    }
    
    /**
     * Returns the absolute battle level or mining level, whichever is greater.
     * @return the greater of: absolute battle level or absolute mining level,
     * will be non-negative
     */
    public int getHighestLevel()
        {return Math.max(getAbsoluteBattleLevel(), getAbsoluteMiningLevel());}
    
    /**
     * Returns the sum of the absolute battle and mining levels.
     * @return the sum of the absolute battle level and the absolute mining
     * level, will be non-negative
     */
    public int getTotalLevel()
        {return getAbsoluteBattleLevel() + getAbsoluteMiningLevel();}
    
    /**
     * Returns the ship's "level" of power in battle, to be used in determining
     * a classification.
     * @return an integer based on the ship's amount of weaponry and hull
     * capacity, will be non-negative
     */
    private int getBattleLevel()
        {return getAbsoluteBattleLevel() / Levels.LEVEL_AMT;}
    
    /**
     * Returns the ship's absolute "level" of power in battle, to be used in
     * determining a classification.
     * @return an integer based on the ship's amount of weaponry and hull
     * capacity, will be non-negative
     */
    public int getAbsoluteBattleLevel()
    {
        int level = 0;
        level += getWeaponsUsed() * Levels.LEVEL_AMT;
        level += getResource(Resources.HULL).getNExpanders() * 2;
        return level;
    }
    
    /**
     * Returns the ship's "level" of mining ability, to be used in determining
     * a classification.
     * @return an integer based on the ship's amount of non-combat modules and
     * ore capacity, will be non-negative
     */
    private int getMiningLevel()
        {return getAbsoluteMiningLevel() / Levels.LEVEL_AMT;}
    
    /**
     * Returns the ship's absolute "level" of mining ability, to be used in
     * determining a classification.
     * @return an integer based on the ship's amount of non-combat modules and
     * ore capacity, will be non-negative
     */
    public int getAbsoluteMiningLevel()
    {
        int level = 0;
        level += (getModulesUsed() - getWeaponsUsed()) * Levels.LEVEL_AMT;
        level += getResource(Resources.ORE).getNExpanders() * 2;
        return level;
    }
    
    /**
     * Resets the prices of all items on the ship to the current station's
     * prices, if docked.
     */
    public void updatePrices()
    {
        if (!isDocked())
            return;
        
        Station station = getSectorLocation().getStation();
        
        for (Module module: modules)
            if (module != null && station.getModule(module.getName()) != null)
                module.setPrice(station.getModule(module.getName()).getPrice());
        
        for (Resource resource: resources)
        {
            if (resource != null &&
                    station.getResource(resource.getName()) != null)
            {
                resource.setPrice(station.getResource(resource.getName())
                        .getPrice());
                
                // This currently updates the expander's price for ALL ships
                resource.getExpander().setPrice(station.getExpander(
                        resource.getExpander().getName()).getPrice());
            }
        }
    }
    
    /** Update effects that do something at the end of each turn. */
    public void updateContinuousEffects()
    {
        if (hasModule(Actions.SOLAR) && isInSector() && !isLanded())
        {
            changeResource(Actions.SOLAR.getResource(), Actions.SOLAR.getCost()
                    * location.getSector().getStar().getSolarPowerAt(
                            getSectorLocation().getOrbit()));
        }
        
        if (isShielded() && !changeResourceBy(Actions.SHIELD.getAction()))
        {
            addPlayerMessage("The ship has run out of energy to stay "
                    + "shielded.");
            removeFlag(SHIELDED);
        }
        
        if (isCloaked() && !changeResourceBy(Actions.CLOAK.getAction()))
        {
            addPlayerMessage("The ship has run out of energy to stay cloaked.");
            removeFlag(CLOAKED);
        }
    }
    
    /** Sets the ship's resources to the constant defaults. */
    private void setResourceDefaults()
    {
        Resource current = getResource(Resources.FUEL);
        current.setBaseCapacity(FUEL);
        current.setAmount(FUEL);
        
        current = getResource(Resources.ENERGY);
        current.setBaseCapacity(ENERGY);
        current.setAmount(ENERGY);
        
        current = getResource(Resources.ORE);
        current.setBaseCapacity(ORE);
        current.setAmount(0);
        
        current = getResource(Resources.HULL);
        current.setBaseCapacity(HULL);
        current.setAmount(HULL);
    }
    
    /** Creates a Reputation object for each faction in the game. */
    public void createReputations()
    {
        reputations = new Reputation[location.getMap().getFactions().length];
        
        for (int i = 0; i < reputations.length; i++)
            reputations[i] = new Reputation(location.getMap().getFactions()[i]);
    }

    @Override
    public int compareTo(Ship other)
    {
        Reputation r1 = getReputation(faction);
        Reputation r2 = other.getReputation(faction);
        
        if (r1 == null)
        {
            if (r2 == null)
                return 0;
            else
                return -1;
        }
        else if (r2 == null)
        {
            return 1;
        }
        
        return r1.compareTo(r2);
    }
}