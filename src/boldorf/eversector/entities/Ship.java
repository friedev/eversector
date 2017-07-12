package boldorf.eversector.entities;

import asciiPanel.AsciiPanel;
import boldorf.eversector.map.Map;
import boldorf.eversector.map.Sector;
import boldorf.eversector.items.Weapon;
import boldorf.eversector.items.Action;
import boldorf.eversector.items.Expander;
import boldorf.eversector.items.Resource;
import boldorf.eversector.items.Module;
import static boldorf.eversector.Main.rng;
import static boldorf.eversector.Main.addError;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Reputations;
import boldorf.eversector.storage.Resources;
import boldorf.util.Utility;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import static boldorf.eversector.Main.COLOR_FIELD;
import boldorf.util.Nameable;
import static boldorf.eversector.Main.addColorMessage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import boldorf.eversector.map.faction.Faction;
import boldorf.eversector.map.faction.Focus;
import boldorf.eversector.map.faction.RelationshipType;
import static boldorf.eversector.map.faction.RelationshipType.*;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import static boldorf.eversector.Main.addMessage;
import static boldorf.eversector.Main.attackers;
import static boldorf.eversector.Main.playSoundEffect;
import boldorf.eversector.storage.Paths;

/** A starship which can travel through and interact with the map. */
public class Ship extends Satellite implements ColorStringObject,
        Comparable<Ship>
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
    
    /** The minimum amount of credits required for an NPC to dock. */
    public static final int MIN_DOCK_CREDITS = 15;
    
    /**
     * The range of credits above the starting amount that can be gained as a 
     * reward when exploring.
     */
    public static final int EXPLORE_REWARD_RANGE = 301;
    
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
    
    /** The current coordinates of the ship. */
    private Coord location;
    /** The current sector the ship is in. */
    private Sector sector;
    /** The sector the ship is attempting to travel to. */
    private Sector destination;
    /** The map the ship is using. */
    private Map map;
    
    /** Various booleans represented by Strings, their existence means true. */
    private List<String> flags;
    /** The faction that the ship belongs to, null if unaligned. */
    private Faction faction;
    /** The station that the ship is docked with, null if not docked. */
    private Station dockedWith;
    /** The region that the ship is landed in, null if not landed. */
    private Region landedIn;
    
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
    
    // TODO make an UnlimitedResource class for credits?
    
    /**
     * Creates a ship from a name, Coord, map, orbit, and faction.
     * @param n the name of the ship
     * @param l the location of the ship as a Coord
     * @param m the map the ship will use
     * @param o the orbit of the ship
     * @param f the faction the ship belongs to
     */
    public Ship(String n, Coord l, Map m, int o, Faction f)
    {
        super(n, o);
        
        map = m;
        
        if (map.contains(l))
            location = l;
        else
            location = Coord.get(0, 0);
        
        sector = map.sectorAt(location);
        destination = null;
        flags  = new ArrayList<>();
        
        faction     = f;
        dockedWith  = null;
        landedIn    = null;
        
        credits     = CREDITS;
        modules     = new LinkedList<>();
        cargo       = new LinkedList<>();
        resources   = Station.copyResources();
        
        createReputations();
        setResourceDefaults();
    }
    
    /**
     * Creates the ship from a map and a set of properties.
     * @param m the map the ship will use
     * @param properties the properties of the ship
     */
    public Ship(Map m, Properties properties)
    {
        super("Player");
        map = m;
        
        // Basic hard-coded definitions
        flags     = new ArrayList<>();
        modules   = new LinkedList<>();
        cargo     = new LinkedList<>();
        resources = Station.copyResources();
        
        createReputations();
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
                        setName(value);
                        break;
                    case "location":
                        location = Utility.parseCoord(value);
                        sector   = map.sectorAt(location);
                        break;
                    case "orbit":
                        setOrbit(Utility.parseInt(value, 0));
                        break;
                    case "faction":
                        faction = map.getFaction(value);
                        break;
                    case "credits":
                        credits = Utility.parseInt(value, CREDITS);
                        break;
                    case "situation":
                        switch (value)
                        {
                            case "landed":
                                if (sector != null &&
                                        sector.isPlanetAt(getOrbit()) &&
                                        sector.getPlanetAt(getOrbit())
                                                .getType().canLandOn())
                                {
                                    landedIn = sector.getPlanetAt(getOrbit())
                                            .getRandomRegion();
                                }
                                dockedWith = null;
                                break;
                            case "docked":
                                landedIn = null;
                                if (sector != null)
                                    dockedWith = sector.getStationAt(getOrbit());
                                break;
                            default:
                                landedIn   = null;
                                dockedWith = null;
                                break;
                        }
                        break;
                    case "modules":
                        String[] moduleStrings = value.split(", ");
                        for (String moduleString: moduleStrings)
                            addModule(moduleString);
                }
            }
        }
    }
    
    /**
     * Creates a ship from a name, Coord, and map, setting the orbit to 0 and
     * faction to null.
     * @param n the name of the ship
     * @param l the location of the ship as a Coord
     * @param m the map the ship will use
     */
    public Ship(String n, Coord l, Map m)
        {this(n, l, m, 0, null);}
    
    /**
     * Creates a ship from a name, Coord, map, and orbit, setting the faction to
     * null.
     * @param n the name of the ship
     * @param l the location of the ship as a Coord
     * @param m the map the ship will use
     * @param o the orbit of the ship
     */
    public Ship(String n, Coord l, Map m, int o)
        {this(n, l, m, o, null);}
    
    /**
     * Creates a ship from a name, Coord, map, orbit, and faction, setting the
     * orbit to 0.
     * @param n the name of the ship
     * @param l the location of the ship as a Coord
     * @param m the map the ship will use
     * @param f the faction the ship belongs to
     */
    public Ship(String n, Coord l, Map m, Faction f)
        {this(n, l, m, 0, f);}
    
    @Override
    public String toString()
    {
        return isPlayer() ?
                super.toString() : getClassification() + " " + super.toString();
    }
    
    @Override
    public ColorString toColorString()
    {
        return isAligned() ? new ColorString(toString(), faction.getColor()) :
                new ColorString(toString());
    }
    
    public Coord        getLocation()  {return location;            }
    public Sector       getSector()    {return sector;              }
    public Map          getMap()       {return map;                 }
    public Faction      getFaction()   {return faction;             }
    public int          getCredits()   {return credits;             }
    public Station      dockedWith()   {return dockedWith;          }
    public Planet       landedOn()     {return landedIn.getPlanet();}
    public Region       landedIn()     {return landedIn;            }
    public boolean      isDocked()     {return dockedWith != null;  }
    public boolean      isLanded()     {return landedIn   != null;  }
    public Resource[]   getResources() {return resources;           }
    public List<Module> getModules()   {return modules;             }
    public List<Module> getCargo()     {return cargo;               }
    
    public boolean isInSector()  {return getOrbit() != 0;   }
    public boolean isDestroyed() {return hasFlag(DESTROYED);}
    public boolean isShielded()  {return hasFlag(SHIELDED); }
    public boolean isCloaked()   {return hasFlag(CLOAKED);  }
    
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
        {return map.getPlayer() == this;}
    
    /**
     * Returns true if the faction the ship belongs to is the one specified.
     * @param f the faction to compare
     * @return true if the specified faction and actual faction are the same
     */
    public boolean isInFaction(Faction f)
        {return faction == f;}
    
    /**
     * Returns true if this ship belongs to a faction.
     * @return true if this ship's faction is not null
     */
    public boolean isAligned()
        {return !isInFaction(null);}
    
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
    
    public void setSector(Sector s)
    {
        sector = s;
        sector.getShips().add(this);
    }
    
    /**
     * Sets the ship's faction without modifying reputation.
     * @param f the faction to assign to this ship
     */
    public void setFaction(Faction f)
        {faction = f;}
    
    /**
     * Sets the ship's location to the specified Coord.
     * @param p the Coord to move the ship to
     * @return true if the ship was moved
     */
    public boolean setLocation(Coord p)
    {
        if (isInSector())
        {
            addPlayerError("The ship cannot move between sectors while "
                    + "orbiting.");
            return false;
        }
        
        if (!map.contains(p))
        {
            addPlayerError("The destination entered is not on the map.");
            return false;
        }
        
        if (location.equals(p))
        {
            addPlayerError("The ship is already in the designated sector.");
            return false;
        }
        
        location = p;
        sector = map.sectorAt(location);
        
        if (isPlayer())
            reveal();
        
        return true;
    }
    
    /**
     * Generates a list of the ship's properties and returns them.
     * @return a Properties object with information about the ship
     */
    public Properties toProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("name", super.toString());
        properties.setProperty("location", location.toString());
        properties.setProperty("orbit", Integer.toString(getOrbit()));
        if (isAligned())
            properties.setProperty("faction", faction.getName());
        properties.setProperty("credits", Integer.toString(credits));
        
        String situation;
        if (isLanded())
            situation = "landed";
        else if (isDocked())
            situation = "docked";
        else if (isInSector())
            situation = "inSector";
        else
            situation = "interstellar";
        properties.setProperty("situation", situation);
        
        StringBuilder builder = new StringBuilder();
        for (Module module: modules)
            builder.append(module.getLowerCaseName()).append(", ");
        
        if (builder.length() > 0)
        {
            builder.delete(builder.length() - 2, builder.length());
            properties.setProperty("modules", builder.toString());
        }
        
        for (Resource resource: resources)
            properties.setProperty("r_" + resource.getLowerCaseName(),
                                           resource.getAmountAsFraction());
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
     * @param f the faction to get a reputation with
     * @return the Reputation object with the faction specified, null if not
     * found
     */
    public Reputation getReputation(Faction f)
    {
        if (f == null)
            return null;
        
        for (Reputation rep: reputations)
            if (rep.getFaction() == f)
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
        if (isDocked() && dockedWith.hasModule(name))
            addModule(dockedWith.getModule(name));
        else
            addModule(Station.getBaseModule(name));
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
        
        if (resource == null)
        {
            // Try finding if an expander was specified and continue purchase
            Expander expander = dockedWith.getExpander(name);
            
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
                        + " " + expander.makePlural().toLowerCase() + ".");
                return false;
            }
            
            resource.expand(quantity);
            changeCredits(dockedWith.getFaction(), -price);
            return true;
        }
        
        if (!resource.isSellable() && quantity < 0)
        {
            addError(resource + " cannot be sold.");
            return false;
        }
        
        int price = dockedWith.getResource(name).getPrice() * quantity;
        
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
        changeCredits(dockedWith.getFaction(), -price);
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
            return Math.min(credits /
                    dockedWith.getResource(resource.getName()).getPrice(),
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
                    credits /
                    dockedWith.getExpander(expander.getName()).getPrice());
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
        Module module = dockedWith.getModule(name);
        
        if (module == null || module.getName() == null)
        {
            if (Station.hasBaseModule(name))
            {
                addPlayerError(dockedWith + " does not sell modules of this "
                        + "type.");
                return false;
            }
            
            addPlayerError("Specified module does not exist.");
            return false;
        }
        
        if (!dockedWith.sells(module))
        {
            addPlayerError(dockedWith + " does not sell modules of this type.");
            return false;
        }
        
        if (module instanceof Weapon && isPirate())
        {
            addPlayerError(dockedWith + " refuses to sell weaponry to "
                    + "pirates.");
            
            return false;
        }
        
        int price = module.getPrice();
        
        if (!validateFunds(price))
            return false;
        
        // Capacity is checked by addModule
        addModule(module);
        changeCredits(dockedWith.getFaction(), -price);
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
        Module module = dockedWith.getModule(name);
        
        if (module == null)
        {
            if (Station.hasBaseModule(name))
            {
                addPlayerError(dockedWith + " will not accept a module of this "
                            + "type.");
                return false;
            }
            
            addPlayerError("Specified module does not exist.");
            return false;
        }
        
        if (!dockedWith.sells(module))
        {
            addPlayerError(dockedWith + " will not accept a module of this "
                        + "type.");
            return false;
        }
        
        // removeModule deals with the case where the module does not exist
        if (removeModule(module))
        {
            changeCredits(dockedWith.getFaction(), module.getPrice());
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
        
        // Invert to match cartesian system
        if (direction == Direction.UP)
            direction = Direction.DOWN;
        else if (direction == Direction.DOWN)
            direction = Direction.UP;
        
        Coord target = location.translate(direction);
        
        if (!map.contains(target))
        {
            addPlayerError("The destination entered is not on the map.");
            return false;
        }
        
        if (!validateResources(Actions.BURN, "initiate a burn"))
            return false;
        
        Resource resource = getResource(Actions.BURN.getResource());
        
        location = target;
        sector = map.sectorAt(location);
        resource.changeAmount(-Actions.BURN.getCost());
        
        if (isPlayer())
            reveal();
        
        return true;
    }
    
    /**
     * Will warp the ship to the designated coordinates.
     * @param p the Coord to warp to, must be on the map
     * @return true if the warp was completed
     */
    public boolean warpTo(Coord p)
    {
        if (!validateModule(Actions.WARP, "warp"))
            return false;
        
        if (!validateResources(Actions.WARP.getAction(), "charge warp drive"))
            return false;
        
        Resource resource = getResource(Actions.WARP.getResource());
        
        if (!setLocation(p))
            return false;
        
        resource.changeAmount(-Actions.WARP.getCost());
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
        
        if (Sector.EMPTY.equals(sector.getType()))
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
        
        int target = increase ? getOrbit() + 1 : getOrbit() - 1;
        
        if (!sector.isValidOrbit(target))
        {
            if (increase)
                return escape();
            
            addPlayerError("Invalid orbit. Must be between 1 and "
                        + sector.getOrbits() + ".");
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
        
        setOrbit(target);
        resource.changeAmount(-cost);
        return true;
    }
    
    public boolean relocate(boolean increase)
    {
        if (!isLanded())
        {
            addPlayerError("You must already be landed on a planet to "
                    + "relocate.");
            return false;
        }
        
        int target = landedOn().getRegions().indexOf(landedIn) +
                (increase ? 1 : -1);
        
        if (target < 0 || target >= landedOn().getRegions().size())
        {
            addPlayerError("Invalid region specified.");
            return false;
        }
        
        if (!validateResources(Actions.RELOCATE, "relocate"))
            return false;
        
        Resource resource = getResource(Actions.RELOCATE.getResource());
        resource.changeAmount(-Actions.RELOCATE.getCost());
        landedIn.getShips().remove(this);
        landedIn = landedOn().getRegions().get(target);
        landedIn.getShips().add(this);
        return true;
    }
    
    /**
     * Escapes gravitation influence of the current sector, if possible.
     * @return true if the escape was successful
     */
    public boolean escape()
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
        
        int cost = Actions.ESCAPE.getCost();
        if (!validateResources(Actions.ESCAPE.getResource(), cost,
                "escape the gravity of " + sector))
            return false;

        Resource resource = getResource(Actions.ESCAPE.getResource());

        setOrbit(0);
        resource.changeAmount(-cost);
        sector.getShips().remove(this);
        return true;
    }
    
    /** Reveals the player's sector (discovers surrounding sectors). */
    public void reveal()
        {map.reveal(location);}
    
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
        
        Resource resource = getResource(Actions.SCAN.getResource());
        resource.changeAmount(-Actions.SCAN.getCost());
        return true;
    }
    
    /**
     * Enters the current sector, if not already in one.
     * @return true if entrance was successful
     */
    public boolean enter()
    {
        if (isInSector())
        {
            addPlayerError("Ship is already in " + sector + ".");
            return false;
        }
        
        if (Sector.EMPTY.equals(sector.getType()))
        {
            addPlayerError("There is nothing in " + sector + ".");
            return false;
        }
        
        int cost = Actions.ENTER.getCost();
        if (!validateResources(Actions.ENTER.getResource(), cost,
                "enter into orbit around " + sector))
            return false;

        Resource resource = getResource(Actions.ENTER.getResource());

        resource.changeAmount(-cost);
        setSector(sector);
        setOrbit(sector.getOrbits());
        return true;
    }
    
    /**
     * Docks with a station on the same orbit, if possible.
     * @return true if docking was successful
     */
    public boolean dock()
    {
        if (getOrbit() == 0)
        {
            addPlayerError("Ship must be in orbit to dock.");
            return false;
        }
        
        if (!sector.isStationAt(getOrbit()))
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
            addPlayerError("The ship is already docked with " + dockedWith
                    + ".");
            return false;
        }
        
        Station station = sector.getStationAt(getOrbit());
        
        if (isHostile(station.getFaction()) && isAligned())
        {
            if (!canClaimStation(station, false))
            {
                addPlayerError(station + " is controlled by the hostile "
                        + station.getFaction() + ", who deny you entry.");
                return false;
            }
            
            forceDock();

            if (!claim())
            {
                undock();
                return false;
            }
        }
        else
        {
            forceDock();
        }
        
        repairModules();
        updatePrices();
        return true;
    }
    
    public void forceDock()
    {
        dockedWith = sector.getStationAt(getOrbit());
        dockedWith.getShips().add(this);
        sector.getShips().remove(this);
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
        
        dockedWith.getShips().remove(this);
        dockedWith = null;
        sector.getShips().add(this);
        return true;
    }
    
    /**
     * Lands on the planet at the ship's orbit, if possible.
     * @param region the index of the region to land in
     * @return true if the landing was successful
     */
    public boolean land(Region region)
    {
        if (!canLand())
            return false;
        
        Planet planet = sector.getPlanetAt(getOrbit());
        
        if (!planet.getRegions().contains(region))
        {
            addPlayerError("The specified region was not found on " + planet
                    + ".");
            return false;
        }
        
        if (!validateResources(Actions.LAND, "land on " + planet))
            return false;
        
        getResource(Actions.LAND.getResource())
                .changeAmount(-Actions.LAND.getCost());
        landedIn = region;
        landedIn.getShips().add(this);
        sector.getShips().remove(this);
        return true;
    }
    
    public boolean land(int regionIndex)
    {
        Planet planet = sector.getPlanetAt(getOrbit());
        
        if (regionIndex < 0 || regionIndex >= planet.getRegions().size())
        {
            addPlayerError("Invalid region specified.");
            return false;
        }
        
        
        return land(planet.getRegions().get(regionIndex));
    }
    
    /**
     * Crash lands on the planet at the ship's orbit, if possible.
     * @return true if the landing was successful
     */
    public boolean crashLand()
    {
        if (!canLand())
            return false;
        
        if (getAmountOf(Resources.HULL) > CRASH_THRESHOLD)
            getResource(Resources.HULL).setAmount(CRASH_THRESHOLD);
        else
            getResource(Resources.HULL).setAmount(0);
        
        landedIn = sector.getPlanetAt(getOrbit()).getRandomRegion();
        landedIn.getShips().add(this);
        sector.getShips().remove(this);
        return true;
    }
    
    /**
     * Returns true if there is a planet that the ship can land on at its orbit.
     * @return true if landing of any kind is possible in the ship's situation
     */
    public boolean canLand()
    {
        if (!isOrbiting())
        {
            addPlayerError("Ship must be at a planet's orbit to land.");
            return false;
        }
        
        if (isDocked())
        {
            addPlayerError("Ship cannot land while docked.");
            return false;
        }
        
        if (isLanded())
        {
            addPlayerError("The ship is already landed.");
            return false;
        }
        
        Planet planet = sector.getPlanetAt(getOrbit());
        
        if (planet == null)
        {
            addPlayerError("There is no planet at this orbit.");
            return false;
        }
        
        if (!planet.getType().canLandOn())
        {
            addPlayerError("The ship cannot land on "
                    + Nameable.getFullName(planet.getType().toString())
                    + ".");
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
        
        if (!validateResources(Actions.TAKEOFF, "takeoff from " + landedIn))
            return false;
        
        Resource resource = getResource(Actions.TAKEOFF.getResource());
        
        // landedOn is set to null below so that the message is correct
        resource.changeAmount(-Actions.TAKEOFF.getCost());
        landedIn.getShips().remove(this);
        sector.getShips().add(this);
        landedIn = null;
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
            ore = landedIn.getOre();
        else
            ore = map.getRandomOre();
        
        if (ore == null)
        {
            addPlayerError("There is no ore to mine in the "
                    + landedIn.toString().toLowerCase() + ".");
            return false;
        }
        
        int discard = getResource(Resources.ORE).changeAmountWithDiscard(ore
                .getDensity());
        resource.changeAmount(-Actions.MINE.getCost());
        
        if (!isLanded() && rng.nextBoolean())
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
                addMessage("Extracted 1 unit of " + ore.getLowerCaseName()
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
    
    // TODO add more canX() methods
    
    /**
     * Returns true if the ship is capable of mining in its current state.
     * @param print if true, will print any errors
     * @return true if a mine would succeed
     */
    public boolean canMine(boolean print)
    {
        Planet planet = sector.getPlanetAt(getOrbit());
        
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
            addPlayerError(module.getFullNameCapitalized()
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
                addPlayerError(module.getFullNameCapitalized()
                        + " is required.");
            else
                addPlayerError(module.getFullNameCapitalized()
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
            
            addPlayerError("Your " + module.getLowerCaseName() + " is too "
                    + "damaged to function.");
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
            addPlayerError("Insufficient " + resource.getLowerCaseName()
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
                addPlayerMessage("Your " + damagedModule.getLowerCaseName()
                        + " has been damaged by the impact.");
                
                if (damagedModule.isEffect(SHIELDED) && isShielded())
                    removeFlag(SHIELDED);
                else if (damagedModule.isEffect(CLOAKED) && isCloaked())
                    removeFlag(CLOAKED);
            }
            else
            {
                addPlayerMessage("Your " + damagedModule.getLowerCaseName()
                        + " has been destroyed by the impact!");
                modules.remove(damagedModule);
            }
        }
    }
    
    /**
     * Damages the ship by a random amount, to be used when exploring.
     * @return the amount of damage dealt to the ship
     */
    public int dealRandomDamage()
    {
        int damage = rng.nextInt(Ship.HULL) + 1;
        damage(damage, false);
        return damage;
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
                addPlayerMessage("Salvaged " + module.getFullName() + ".");
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
                            + Nameable.makePlural(resource.getExpander()
                                    .getLowerCaseName(), nExpanders) + ".");
                }
                
                int oldAmount = yourResource.getAmount();
                yourResource.changeAmountWithDiscard(resource.getAmount() /
                        LOOT_MODIFIER);
                
                int amountIncrease = yourResource.getAmount() - oldAmount;
                
                if (amountIncrease > 0)
                {
                    addPlayerMessage("Salvaged " + amountIncrease + " "
                            + resource.getLowerCaseName() + ".");
                }
            }
        }
    }
    
    /**
     * Performs the same function as warpTo(Coord), using two int coordinates
     * instead.
     * @param x the x coordinate to warp to, must be on the map
     * @param y the x coordinate to warp to, must be on the map
     * @return true if the warp was successful
     */
    public boolean warpTo(int x, int y)
        {return warpTo(Coord.get(x, y));}
    
    /**
     * Burns towards the given coordinates, return true if the sector was
     * reached or any progress was made.
     * @param x the x coordinate to move towards
     * @param y the y coordinate to move towards
     * @return true if any progress was made toward the coordinates
     */
    public boolean burnTowards(int x, int y)
    {
        if (x > location.getX())
            return burn(Direction.LEFT);
        if (x < location.getX())
            return burn(Direction.RIGHT);
        if (y > location.getY())
            return burn(Direction.UP);
        if (y < location.getY())
            return burn(Direction.DOWN);
        return true;
    }
    
    /**
     * Performs the same function as burnTowards, using a Coord instead of two
     * ints.
     * @param p the coordinates to move towards
     * @return true if any progress was made toward the coordinates
     */
    public boolean burnTowards(Coord p)
        {return burnTowards(p.getX(), p.getY());}
    
    // Normal attacks will be dealt with in Game, this attack is for AI ships
    
    /**
     * Starts an encounter with the specified ship (this version intended for 
     * AI).
     * @param ship the ship to attack
     * @return true if the ship attacked, false if not
     */
    public boolean attack(Ship ship)
    {
        if (ship == null)
        {
            // No print will be done because ship is null
            return false;
        }
        
        if (!willAttack())
        {
            if (validateResources(Actions.FLEE, "flee"))
            {
                ship.addPlayerColorMessage(toColorString()
                        .add(" attempts to flee."));
                return false;
            }
            
            return false;
        }
        
        if (!isShielded() && toggleActivation(Actions.SHIELD))
        {
            ship.addPlayerColorMessage(toColorString()
                    .add(" activates a shield."));
            // No return since shielding is a free action
        }
        
        if (canFire(Actions.PULSE, ship))
        {
            ship.addPlayerColorMessage(toColorString()
                    .add(" fires a pulse beam."));
            ship.playPlayerSound(Paths.PULSE);
            fire(Actions.PULSE, ship);
            return true;
        }
        
        if (willUseFuel(Actions.TORPEDO.getCost()) &&
                canFire(getWeapon(Actions.TORPEDO.getName()), ship))
        {
            ship.addPlayerColorMessage(toColorString()
                    .add(" fires a torpedo."));
            ship.playPlayerSound(Paths.TORPEDO);
            fire(Actions.TORPEDO, ship);
            return true;
        }
        
        if (canFire(Actions.LASER, ship))
        {
            ship.addPlayerColorMessage(toColorString().add(" fires a laser."));
            ship.playPlayerSound(Paths.LASER);
            fire(Actions.LASER, ship);
            return true;
        }
        
        // Note that the code above under !willAttack() is borrowed from here
        
        if (validateResources(Actions.FLEE, "flee"))
        {
            if (!isCloaked() && toggleActivation(Actions.CLOAK))
            {
                ship.addPlayerColorMessage(toColorString()
                        .add(" activates a cloaking device."));
                // No return since cloaking is a free action
            }
            
            ship.addPlayerColorMessage(toColorString()
                    .add(" attempts to flee."));
            return false;
        }
        
        return false;
    }
    
    /**
     * Joins the entered faction and adds the relevant reputation.
     * @param f the faction to join (use leaveFaction() instead of setting this
     * to null)
     */
    public void joinFaction(Faction f)
    {
        if (isInFaction(f))
            return;
        
        faction = f;
        changeReputation(f, Reputations.JOIN);
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
    {
        return isAligned() && !ship.isInFaction(faction) && !(ship.willAttack()
                           && !ship.isPlayer());
    }
    
    /**
     * Claims a celestial body for the ship's faction.
     * @return true if the celestial body was claimed
     */
    public boolean claim()
    {
        if (isLanded())
            return claim(landedIn);
        
        if (!canClaim(true))
            return false;
        
        CelestialBody claimableBody = getClaimableBody();
        changeCredits(claimableBody.getFaction(),
                -claimableBody.getClaimCost());
        
        if (ALLIANCE.equals(faction.getRelationship(
                claimableBody.getFaction())))
            changeReputation(faction, Reputations.CLAIM_ALLY);
        else
            changeReputation(faction, Reputations.CLAIM);
        
        changeReputation(claimableBody.getFaction(), -Reputations.CLAIM);
        
        // Claim must be done here so the faction relations can be checked
        claimableBody.claim(faction);
        return true;
    }
    
    public boolean claim(Region region)
    {
        if (!canClaimRegion(region))
            return false;

        changeCredits(region.getFaction(), -landedOn().getClaimCost());

        if (ALLIANCE == faction.getRelationship(region.getFaction()))
        {
            changeReputation(faction,
                    Reputations.CLAIM_ALLY / landedOn().getRegions().size());
        }
        else
        {
            changeReputation(faction,
                    Reputations.CLAIM / landedOn().getRegions().size());
        }

        changeReputation(region.getFaction(),
                -Reputations.CLAIM / landedOn().getRegions().size());

        // Claim must be done here so the faction relations can be checked
        region.claim(faction);
        return true;
    }
    
    /**
     * Returns true if the ship can claim a celestial body in the most general
     * sense.
     * @return true if the ship can claim a celestial body
     */
    private boolean canClaim(CelestialBody claiming, boolean print)
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
        
        if (credits < claiming.getClaimCost())
        {
            if (print)
            {
                addPlayerError("You cannot afford the "
                        + claiming.getClaimCost() + " credit cost to claim "
                        + "territory on " + claiming + ".");
            }
            return false;
        }
        
        return true;
    }
    
    private boolean canClaim(boolean print)
    {
        CelestialBody claiming = getClaimableBody();
        
        if (claiming == null)
        {
            if (print)
            {
                addPlayerError("Ship must be landed or docked to claim "
                        + "territory.");
            }
            return false;
        }
        
        return canClaim(claiming, print);
    }
    
    public boolean canClaimStation(Station station, boolean print)
    {
        if (!canClaim(station, print))
            return false;
        
        // If the body is already claimed by solely your faction, return false
        if (station.getFaction() == faction)
        {
            if (print)
            {
                addPlayerError(station + " is already claimed by the " + faction
                        + ".");
            }
            return false;
        }
        
        if (station.getNShips(station.getFaction()) > 0)
        {
            if (print)
            {
                addPlayerError("There are currently ships of the "
                        + station.getFaction() + " guarding " + station + ".");
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Returns true if the ship can claim a station.
     * @param print if true, will print any errors when claiming
     * @return true if the ship can claim a station
     */
    public boolean canClaimStation(boolean print)
    {
        if (!isDocked())
        {
            if (print)
                addPlayerError("Ship must be docked to claim a station.");
            return false;
        }
        
        return canClaimStation(dockedWith, print);
    }
    
    /**
     * Returns true if the ship can claim the given region.
     * @param region the region to check the ship's ability to claim on
     * @return true if the ship can claim the given region
     */
    public boolean canClaimRegion(Region region)
    {
        if (!canClaim(true))
            return false;
        
        if (region == null)
        {
            addPlayerError("Region not found.");
            return false;
        }
        
        if (!isLanded())
        {
            addPlayerError("Ship must be landed to claim a region.");
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
    
    /**
     * Returns the claimable celestial body that the ship is on.
     * @return whichever claimable celestial body the player is on, landedOn
     * will take precedence and null will be returned if neither are valid
     */
    public CelestialBody getClaimableBody()
    {
        // Returning null separately is unnecessary since it will be returned
        // anyway when the ship isn't docked
        return isLanded() ? landedOn() : dockedWith;
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
    
    public Ship vote(List<Ship> candidates)
    {
        int[] preferences = new int[candidates.size()];
        
        for (int i = 0; i < candidates.size(); i++)
        {
            Ship candidate = candidates.get(i);
            preferences[i] = 0;
            
            if (getHigherLevel() != null &&
                    getHigherLevel().equals(candidate.getHigherLevel()))
                preferences[i] += 2;
            
            if (location.equals(candidate.location))
                preferences[i] += 3;
            else if (location.isAdjacent(candidate.location))
                preferences[i]++;
            
            if (candidate.calculateShipValue() > calculateShipValue())
                preferences[i]++;
        }
        
        int highestPreference = 0;
        int index = 0;
        
        for (int i = 0; i < preferences.length; i++)
        {
            // Using > instead of >= biases the ship's vote towards the
            // candidate with the highest reputation
            if (preferences[i] > highestPreference)
            {
                highestPreference = preferences[i];
                index = i;
            }
        }
        
        return candidates.get(index);
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
        contents.add(new ColorString(Integer.toString(credits),
                COLOR_FIELD).add(" Credits"));
        
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
     * Manages the actions of a battle between two AI-controlled ships.
     * @param ship the ship that this ship is battling
     */
    private void controlAIBattle(Ship ship)
    {
        do
        {
            // If this ship chooses to flee but is pursued
            if (!attack(ship))
            {
                if (changeResourceBy(Actions.FLEE) &&
                        (!ship.willPursue(this) ||
                         !ship.changeResourceBy(Actions.PURSUE)))
                {
                    // If the other ship is powerful enough, they will convert
                    // this ship
                    if (ship.willConvert() && ship.convert(this))
                        return;
                    
                    // If the conversion fails, the fight continues
                }
                else
                {
                    break;
                }
            }
            
            // If the other ship flees but is pursued
            if (!ship.attack(this))
            {
                if (ship.changeResourceBy(Actions.PURSUE) &&
                        (!willPursue(ship) || !changeResourceBy(Actions.FLEE)))
                {
                    if (willConvert() && convert(ship))
                        return;
                }
            }
        } while (!isDestroyed() && !ship.isDestroyed());
        
        // If this ship is destroyed or cannot flee while the other ship lives
        if (isDestroyed() || (!validateResources(Actions.FLEE, "flee") &&
                !ship.isDestroyed()))
        {
            if (isLeader() && ship.isInFaction(faction))
            {
                faction.setLeader(ship);
                faction.addNews(ship + " has defeated our leader, " + toString()
                        + ", and has wrested control of the faction.");
            }
            else
            {
                if (ship.isPassive(this))
                    ship.changeReputation(ship.faction, Reputations.KILL_ALLY);
                else
                    ship.changeReputation(ship.faction, Reputations.KILL_ENEMY);
                
                ship.changeReputation(faction, Reputations.KILL_ALLY);
                
                if (isLeader())
                {
                    faction.addNews(ship + " of the " + ship.faction
                            + " has destroyed our leader, " + toString() + ".");
                }
                
                ship.loot(this);

                if (!isDestroyed())
                    destroy(false);
            }
        }
        else
        {
            if (ship.isLeader() && isInFaction(faction))
            {
                ship.faction.setLeader(this);
                ship.faction.addNews(toString() + " has defeated our leader, "
                        + ship + ", and has wrested control of the faction.");
            }
            else
            {
                if (isPassive(ship))
                    changeReputation(faction, Reputations.KILL_ALLY);
                else
                    changeReputation(faction, Reputations.KILL_ENEMY);
                
                changeReputation(ship.faction, Reputations.KILL_ALLY);
                
                if (ship.isLeader())
                {
                    ship.faction.addNews(toString() + " of the " + faction
                            + " has destroyed our leader, " + ship.toString()
                            + ".");
                }
                
                loot(ship);

                if (!ship.isDestroyed())
                    ship.destroy(false);
            }
        }
    }
    
    /**
     * Returns true if the specified ship is considered non-hostile (including
     * if this ship is a pirate).
     * @param ship the ship to check
     * @return true if: the ship is not a pirate and this ship is a pirate or
     * an ally (this is so reputation is lowered for piracy)
     */
    public boolean isPassive(Ship ship)
    {
        return ship.isAligned() && (!isAligned() ||
               isInFaction(ship.faction) ||
               !ship.getFaction().isRelationship(WAR, faction));
    }
    
    /**
     * Returns true if the specified faction is hostile to this ship.
     * @param otherFaction the faction to check
     * @return true if: the faction is not this ship's faction and is at war
     */
    public boolean isHostile(Faction otherFaction)
    {
        return !isInFaction(otherFaction) && (!isAligned() ||
                faction.isRelationship(WAR, otherFaction));
    }
    
    /**
     * Returns true if the NPC is willing to pursue a ship.
     * @param ship the ship that is fleeing
     * @return true if the attacking ship will pursue the fleeing ship
     */
    public boolean willPursue(Ship ship)
    {
        return ship.getAmountOf(Resources.HULL) <= getAmountOf(Resources.HULL)
                && hasWeapons() && willUseFuel(Actions.PURSUE.getCost());
    }
    
    /**
     * Returns true if the NPC is willing to enter a fight.
     * @return true if this ship will engage in any fights, based on its hull
     * strength and weaponry
     */
    public boolean willAttack()
    {
        return hasWeapons() &&
                getAmountOf(Resources.HULL) >= getCapOf(Resources.HULL) / 2;
    }
    
    /**
     * Returns true if the NPC is willing to convert another ship.
     * @return true if the NPC is willing to convert another ship
     */
    public boolean willConvert()
        {return getHighestLevel() >= Levels.BASE_LEVEL;}
    
    /**
     * Returns true if this ship is willing to claim a planet.
     * @return true if the NPC is willing to claim a planet
     */
    public boolean willClaim()
        {return getTotalLevel() >= Levels.BASE_LEVEL * Levels.LEVEL_AMT * 4;}
    
    /**
     * Returns true if the ship is willing the use the specified amount of fuel,
     * making their decision based off of their distance from a station.
     * @param fuel the amount of fuel to check that the ship will use
     * @return true if the ship's amount of fuel minus the specified amount of
     * fuel is greater than or equal to the approximate cost of reaching a
     * station
     */
    public boolean willUseFuel(int fuel)
    {
        return getAmountOf(Resources.FUEL) - fuel >= calculateStationDistance();
    }
    
    public boolean willExplore(Sector target)
    {
        return target == null ? false : getAmountOf(Resources.FUEL) >=
               calculateSectorDistance(target) && willClaim();
    }
    
    /**
     * Removes the ship from all collections and marks it as destroyed.
     * @param print if true, will print a message about the ship's destruction
     */
    public void destroy(boolean print)
    {
        sector.removeLetter((int) getName().charAt(getName().length() - 1));
        sector.getShips().remove(this);
        
        if (isDocked())
            dockedWith.getShips().remove(this);
        else if (isLanded())
            landedIn.getShips().remove(this);
        
        addFlag(DESTROYED);
        
        if (isPlayer())
        {
            playSoundEffect(Paths.DEATH);
            // TODO replace with constant checks in GameScreen
//            Main.display.setScreen(new EndScreen(Main.display,
//                    new ColorString("You have been destroyed.",
//                            Main.COLOR_ERROR), true));
        }
        else if (print)
        {
            addColorMessage(toColorString().add(" has been destroyed."));
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
    private void createReputations()
    {
        reputations = new Reputation[map.getFactions().length];
        
        for (int i = 0; i < reputations.length; i++)
            reputations[i] = new Reputation(map.getFactions()[i]);
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
    
    /** Resets the sector to the one at the ship's location. */
    public void updateSector()
        {sector = map.sectorAt(location);}
    
    /**
     * Resets the prices of all items on the ship to the current station's
     * prices, if docked.
     */
    public void updatePrices()
    {
        if (dockedWith == null)
            return;
        
        for (Module module: modules)
            if (module != null &&
                    dockedWith.getModule(module.getName()) != null)
                module.setPrice(dockedWith.getModule(module.getName())
                                                           .getPrice());
        for (Resource resource: resources)
        {
            if (resource != null &&
                    dockedWith.getResource(resource.getName()) != null)
            {
                resource.setPrice(dockedWith.getResource(resource.getName())
                                                                 .getPrice());
                resource.getExpander().setPrice(dockedWith
                        .getExpander(resource.getExpander().getName())
                        .getPrice());
            }
        }
    }
    
    /** Update effects that do something at the end of each turn. */
    public void updateContinuousEffects()
    {
        if (hasModule(Actions.SOLAR) && isInSector() && !isLanded())
        {
            changeResource(Actions.SOLAR.getResource(), Actions.SOLAR.getCost()
                    * sector.getStar().getSolarPowerAt(getOrbit()));
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
    
    /** Does a logical action based on the situation (intended for NPCs). */
    public void performAction()
    {
        // Removes shielding/cloaking if not necessary
        if (isShielded())
            toggleActivation(Actions.SHIELD);
        
        if (isCloaked())
            toggleActivation(Actions.CLOAK);
        
        if (isDocked())
        {
            buyItems();
            undock();
            return;
        }
        
        // Travel to a new sector if willing
        if (willClaim())
        {
            if (destination == null ||
                sector.getLocation().equals(destination.getLocation()))
            {
                Sector possibility = map.sectorAt(
                        map.adjacentTypeTo(Sector.STATION_SYSTEM, location));
                
                if (!isAligned())
                {
                    destination = possibility;
                }
                else if (possibility != null)
                {
                    String focus = faction.getFocus().getName();
                    RelationshipType relationship;
                    if (possibility.getFaction() == null)
                    {
                        relationship = null;
                    }
                    else
                    {
                        relationship = possibility.getFaction().getRelationship(
                                        faction);
                    }

                    if (Focus.INVADE.getName().equals(focus) &&
                            (relationship == null || WAR.equals(relationship)))
                    {
                        // If attacking and the sector is at war or disputed
                        destination = possibility;
                    }
                    else if (Focus.EXPAND.getName().equals(focus) &&
                            (relationship == null ||
                            PEACE.equals(relationship) ||
                            ALLIANCE.equals(relationship)))
                    {
                        // If claiming and the sector is unclaimed or peaceful
                        destination = possibility;
                    }
                    else if (Focus.DEFEND.getName().equals(focus))
                    {
                        // Stay in the current sector
                        destination = null;
                    }
                }
            }
            
            if (destination != null && willExplore(destination) &&
                    seekSector(destination))
                return;
        }
        
        // Must be done after destination updates to make sure powerful ships
        // update their destinations
        if (isDocked() && willClaim() && claim())
            return;
        
        // If the ship is in a position to mine
        if (isLanded() || (!isLanded() && !isDocked() &&
                sector.isPlanetAt(getOrbit()) &&
                sector.getPlanetAt(getOrbit()).getType().canMineFromOrbit()
                && getResource(Resources.HULL).getAmount()
                    > Planet.ASTEROID_DAMAGE))
        {
            if (mine())
                return;
            
            // Claim the planet if powerful enough
            // The isLanded() check is to confirm that the planet is claimable
            if (willClaim() && isLanded() && claim())
                return;
            
            // This method will ensure that other emergency options are used
            seekStation();
            return;
        }
        
        // If ship is running low on fuel while distant from a station, return
        // to the nearest station
        if (getAmountOf(Resources.FUEL) <= calculateStationDistance()
                + Actions.ORBIT.getCost())
        {
            seekStation();
            return;
        }
        
        // Attack a ship if not in the center
        if (!sector.isCenter())
        {
            // Find a ship at the same orbit
            Ship otherShip = sector.getFirstHostileShip(this);
            if (otherShip != null && willAttack() && !otherShip.isCloaked())
            {
                if (otherShip.isPlayer())
                {
                    attackers.add(this);
                    addColorMessage(
                            new ColorString("You are under attack from ")
                                    .add(toColorString()).add("!"));
                }
                else
                {
                    controlAIBattle(otherShip);
                }
                return;
            }
        }
        
        if (getResource(Resources.ORE).isFull())
        {
            seekStation();
            return;
        }
        
        if (!getResource(Resources.ENERGY).isEmpty())
        {
            Planet planet = sector.getPlanetAt(getOrbit());
            if (planet != null && planet.getType().canLandOn())
            {
                Region target = planet.getRandomOreRegion();
                if ((target == null && land(planet.getRandomRegion())) ||
                        (target != null && land(target)))
                    return;
            }
            
            int closestPlanet = sector.closestMineablePlanetTo(getOrbit());
            if (closestPlanet > 0 && orbit(getOrbit() < closestPlanet))
                return;
        }
        
        seekStation();
    }
    
    /** Buys an item from a list in order of importance. */
    private void buyItems()
    {
        boolean selling = true;
        while (selling && !cargo.isEmpty())
        {
            selling = false;
            for (Module module: cargo)
            {
                // Must never update selling without checking sellModule()
                // If a station will not accept a module, this loops forever
                if (module != null && sellModule(module.getName()))
                {
                    selling = true;
                    break;
                }
            }
        }
        
        buyResource(Resources.ORE,   -getMaxSellAmount(Resources.ORE));
        buyResource(Resources.FUEL,   getMaxBuyAmount(Resources.FUEL));
        buyResource(Resources.HULL,   getMaxBuyAmount(Resources.HULL));
        buyResource(Resources.ENERGY, getMaxBuyAmount(Resources.ENERGY));
        
        // Stop purchase before buying any modules if a purchase would render
        // the ship unable to claim territory, and they are willing to do so
        // The base price must be used in the case that the station doesn't sell
        // the module
        if (willClaim() && credits -
                Station.getBaseWeapon("Pulse Beam").getPrice()
                < CelestialBody.CLAIM_COST)
            return;
        
        if (!hasModule(Actions.PULSE))
            buyModule(Actions.PULSE);
        
        if (!hasModule(Actions.TORPEDO))
            buyModule(Actions.TORPEDO);
        
        if (!hasModule(Actions.LASER))
            buyModule(Actions.LASER);
        
        if (!hasModule(Actions.SHIELD))
            buyModule(Actions.SHIELD);
        
        if (!hasModule(Actions.REFINE))
            buyModule(Actions.REFINE);
        
        buyExpanders();
    }
    
    /** Purchases the expander with the highest priority (intended for NPCs). */
    private void buyExpanders()
    {
        int tanks  = getResource(Resources.FUEL  ).getNExpanders();
        int bays   = getResource(Resources.ORE   ).getNExpanders();
        int cells  = getResource(Resources.ENERGY).getNExpanders();
        int plates = getResource(Resources.HULL  ).getNExpanders();
        
        // If there are the least fuel tanks, buy more
        int maxTanks = getMaxBuyAmount(getExpander(Resources.FUEL_EXPANDER));
        if (maxTanks > 0 && tanks < bays && tanks < cells && tanks < plates)
            buyResource(Resources.FUEL_EXPANDER, maxTanks);
        
        // If there are fewer cargo bays than cells and plates, buy more
        int maxBays = getMaxBuyAmount(getExpander(Resources.ORE_EXPANDER));
        if (maxBays > 0 && bays < cells && bays < plates)
            buyResource(Resources.ORE_EXPANDER, maxBays);
        
        // If there are the fewer energy cells than hull plates, buy more
        int maxCells = getMaxBuyAmount(getExpander(Resources.ENERGY_EXPANDER));
        if (maxCells > 0 && cells < plates)
            buyResource(Resources.ENERGY_EXPANDER, maxCells);
        
        // Buy hull frames by default
        buyResource(Resources.HULL_EXPANDER,
                getMaxBuyAmount(getExpander(Resources.HULL_EXPANDER)));
    }
    
    /**
     * Will perform the next action in getting to a station, and will distress
     * or destroy the ship if not possible.
     */
    private void seekStation()
    {
        if (sector.isStationAt(getOrbit()) && !isDocked() && !isLanded())
        {
            if (isHostile(sector.getStationAt(getOrbit()).getFaction())
                    && isAligned())
            {
                Station station = sector.getStationAt(getOrbit());
                
                if (!dock() && credits < station.getClaimCost())
                {
                    if (!isInFaction(station.getFaction()) && isAligned())
                    {
                        leaveFaction();
                        
                        if (getReputation(station.getFaction()).get()
                                >= Reputations.REQ_REJECTION)
                            joinFaction(station.getFaction());
                        else
                            distressOrDestroy();
                        return;
                    }
                }
                return;
            }
            
            if (credits + getResource(Resources.ORE).getTotalPrice()
                    >= MIN_DOCK_CREDITS)
                dock();
            else
                distressOrDestroy();
            return;
        }
        
        if (isLanded() && takeoff())
            return;
        
        if (Sector.STATION_SYSTEM.equals(sector.getType()))
        {
            if (isInSector() && orbit(getOrbit() < sector.closestStationTo(
                    getOrbit(), faction)))
                return;
            
            if (enter())
                return;
        }
        
        if (isInSector() && escape())
            return;
        
        if (map.adjacentTypeTo(Sector.STATION_SYSTEM, location) != null &&
                burnTowards(map.adjacentTypeTo(Sector.STATION_SYSTEM, location)))
            return;
        
        if (doRandomBurn())
            return;
        
        if (refine())
            return;
        
        // On its own, this distressOrDestroy seems to harm unendangered ships
        if (getAmountOf(Resources.FUEL) < Actions.ORBIT.getCost())
            distressOrDestroy();
    }
    
    /**
     * Returns false if the ship is in the sector, otherwise will try to fly
     * there.
     * @param target the sector the ship will try to reach
     * @return true if seeking the sector, false if impossible
     */
    public boolean seekSector(Sector target)
    {
        if (target == null || !map.contains(target.getLocation()))
            return false;
        
        if (sector == target)
        {
            if (!isInSector())
                enter();
            
            return false;
        }
        
        if (!isInSector())
            return burnTowards(target.getLocation());
        
        if (isLanded())
            return takeoff();
        else if (isDocked())
            return undock();
        else
            return escape();
    }
    
    public boolean seekSector(Coord target)
        {return seekSector(map.sectorAt(target));}
    
    /**
     * Distress if respected enough, otherwise destroy the ship (intended for
     * NPCs).
     */
    private void distressOrDestroy()
    {
        if (canDistress())
            distress();
        else
            destroy(false);
    }
    
    /**
     * Returns the amount of fuel required to get to the specified sector.
     * @return the amount of fuel required to get to the sector, -1 if null or
     * too far away
     */
    private int calculateSectorDistance(Sector target)
    {
        if (target == null)
            return -1;
        
        if (target == sector)
            return (Actions.ORBIT.getCost() * 2);
        
        if (location.isAdjacent(target.getLocation()))
        {
            if (isLanded())
                return Actions.TAKEOFF.getCost() + Actions.BURN.getCost()
                        + (Actions.ORBIT.getCost() * 2);
            
            return Actions.BURN.getCost() + (Actions.ORBIT.getCost() * 2);
        }
        
        return -1;
    }
    
    /**
     * Returns the amount of fuel required to get to the nearest station.
     * @return the amount of fuel required to get to a station, -1 if further
     * than one sector away from one
     */
    private int calculateStationDistance()
    {
        if (isDocked() || sector.isStationAt(getOrbit()))
            return 0;
        
        if (Sector.STATION_SYSTEM.equals(sector.getType()))
        {
            int cost = 0;
            if (isLanded())
                cost += Actions.TAKEOFF.getCost();
            
            cost += Math.abs(sector.closestStationTo(getOrbit()) - getOrbit())
                    * Actions.ORBIT.getCost();
            return cost;
        }
        
        if (map.adjacentTypeTo(Sector.STATION_SYSTEM, location) != null)
        {
            int cost = 0;
            if (isLanded())
                cost += Actions.TAKEOFF.getCost();
            if (isInSector())
                cost += Actions.ESCAPE.getCost() + (Actions.ORBIT.getCost() *
                        (sector.getOrbits() - getOrbit()));
            
            cost += Actions.BURN.getCost() + Actions.ORBIT.getCost();
            return cost;
        }
        
        return -1;
    }
    
    /**
     * Burns in a random direction (intended for NPCs).
     * @return true if the burn was successful
     */
    private boolean doRandomBurn()
    {
        switch (rng.nextInt(4))
        {
            case 0:
                if (burn(Direction.LEFT))
                    return true;
            case 1:
                if (burn(Direction.RIGHT))
                    return true;
            case 2:
                if (burn(Direction.UP))
                    return true;
            case 3:
                if (burn(Direction.DOWN))
                    return true;
        }
        
        return false;
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