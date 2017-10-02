package boldorf.eversector.ships;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Paths;
import boldorf.eversector.Symbol;
import boldorf.eversector.actions.Scan;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.items.Expander;
import boldorf.eversector.items.Module;
import boldorf.eversector.items.Resource;
import boldorf.eversector.items.Weapon;
import boldorf.eversector.locations.*;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Station;
import boldorf.util.Utility;
import squidpony.squidgrid.FOV;
import squidpony.squidmath.Coord;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static boldorf.eversector.Main.*;
import static boldorf.eversector.faction.Relationship.RelationshipType.WAR;

/**
 * A spaceship which can travel through and interact with the map.
 *
 * @author Boldorf Smokebane
 */
public class Ship implements ColorStringObject, Comparable<Ship>
{
    /**
     * The first part of a ship's name.
     */
    private static final String[] NAME_PREFIX = new String[]{
            "Dark", "Death", "Ever", "Great", "Heavy", "Hyper", "Infini", "Light", "Ultra"
    };

    /**
     * The second part a ship's name.
     */
    private static final String[] NAME_SUFFIX = new String[]{
            "blade", "hawk", "seeker", "ship", "spear", "star", "talon", "voyager", "wing"
    };

    /**
     * The amount of fuel all ships start with.
     */
    public static final int FUEL = 15;

    /**
     * The amount of energy all ships start with.
     */
    public static final int ENERGY = 15;

    /**
     * The amount of ore capacity all ships start with.
     */
    public static final int ORE = 25;

    /**
     * The amount of hull all ships start with.
     */
    public static final int HULL = 5;

    /**
     * The amount of fuel all ships start with.
     */
    public static final int CREDITS = 100;

    /**
     * The name of the destroyed flag.
     */
    public static final String DESTROYED = "destroyed";

    /**
     * The name of the shielded flag.
     */
    public static final String SHIELDED = "shielded";

    /**
     * The name of the cloaked flag.
     */
    public static final String CLOAKED = "cloaked";

    /**
     * The radius of the FOV without any scanners.
     */
    public static final double FOV_RADIUS = 5.0;

    /**
     * The amount of credits gained by sending a distress signal.
     */
    public static final int DISTRESS_CREDITS = 100;

    /**
     * The amount of credits that a default ship is worth - must be manually updated.
     */
    public static final int BASE_VALUE = 400;

    /**
     * The limit to how many of each expander can be installed on a ship.
     */
    public static final int MAX_EXPANDERS = 100;

    /**
     * The amount by which each item looted from a destroyed ship will be divided by.
     */
    public static final int LOOT_MODIFIER = 2;

    /**
     * The ship's identification.
     */
    private String name;

    /**
     * The ship's AI.
     */
    private AI ai;

    /**
     * The ship's location.
     */
    private Location location;

    /**
     * All flags, as strings, possessed by the ship.
     */
    private List<String> flags;

    /**
     * The faction that the ship belongs to, null if unaligned.
     */
    private Faction faction;

    /**
     * The amount of credits possessed by the ship.
     */
    private int credits;

    /**
     * How the ship is perceived by factions based on their deeds.
     */
    private Reputation[] reputations;

    /**
     * The list of modules equipped on the ship.
     */
    private List<Module> modules;

    /**
     * All modules not installed on the ship.
     */
    private List<Module> cargo;

    /**
     * The resources on board the ship.
     */
    private final Resource[] resources;

    /**
     * Creates a ship in the given faction at the given location.
     *
     * @param location the location of the ship
     * @param faction  the faction the ship belongs to
     */
    public Ship(Location location, Faction faction)
    {
        String testName;
        do
        {
            testName = rng.getRandomElement(NAME_PREFIX) + rng.getRandomElement(NAME_SUFFIX) + "-" + String.format(
                    "%02d", rng.nextInt(100));
        } while (location.getGalaxy().getShipNames().contains(testName));

        this.name = testName;
        this.ai = new AI(this);
        this.location = location;
        this.flags = new ArrayList<>();
        this.faction = faction;
        this.credits = CREDITS;
        this.modules = new LinkedList<>();
        this.cargo = new LinkedList<>();
        this.resources = Station.copyResources();

        createReputations();
        setResourceDefaults();
    }

    /**
     * Creates the ship in a galaxy using a set of properties.
     *
     * @param galaxy     the galaxy the ship will use
     * @param properties the properties of the ship
     */
    public Ship(Galaxy galaxy, Properties properties)
    {
        // Basic hard-coded definitions
        name = "Player";
        ai = null;
        flags = new ArrayList<>();
        modules = new LinkedList<>();
        cargo = new LinkedList<>();
        resources = Station.copyResources();

        setResourceDefaults();

        for (String key : properties.stringPropertyNames())
        {
            String value = properties.getProperty(key);

            if ("r_".equals(key.substring(0, 2)))
            {
                Resource resource = getResource(key.split("_")[1]);
                if (resource != null)
                {
                    String[] amtFraction = value.split("/");

                    int amount = Math.abs(Utility.parseInt(amtFraction[0], getDefaultAmount(resource)));
                    int capacity = Math.abs(Utility.parseInt(amtFraction[1], getDefaultAmount(resource)));

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
                        location = Location.parseLocation(galaxy, value);
                        break;
                    case "faction":
                        faction = galaxy.getFaction(value);
                        break;
                    case "credits":
                        credits = Utility.parseInt(value, CREDITS);
                        break;
                    case "modules":
                        String[] moduleStrings = value.split(", ");
                        for (String moduleString : moduleStrings)
                        {
                            addModule(moduleString);
                        }
                }
            }
        }

        // Must be done after location is set up
        createReputations();
    }

    /**
     * Creates an unaligned ship.
     *
     * @param location the location of the ship
     */
    public Ship(Location location)
    {
        this(location, null);
    }

    @Override
    public String toString()
    {
        return isPlayer() ? name : getClassification() + " " + name;
    }

    @Override
    public ColorString toColorString()
    {
        return isAligned() ? new ColorString(toString(), faction.getColor()) : new ColorString(toString());
    }

    /**
     * Gets the name of the ship.
     *
     * @return the name of the ship
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the AI controlling the ship.
     *
     * @return the Ai controlling the ship
     */
    public AI getAI()
    {
        return ai;
    }

    /**
     * Gets the faction that the ship belongs to.
     *
     * @return the faction that the ship belongs to
     */
    public Faction getFaction()
    {
        return faction;
    }

    /**
     * Gets the number of credits possessed by the ship.
     *
     * @return the number of credits possessed by the ship
     */
    public int getCredits()
    {
        return credits;
    }

    /**
     * Gets the resources on the ship.
     *
     * @return the resources on the ship
     */
    public Resource[] getResources()
    {
        return resources;
    }

    /**
     * Gets the modules installed on the ship.
     *
     * @return the modules installed on the ship
     */
    public List<Module> getModules()
    {
        return modules;
    }

    /**
     * Gets the modules stored as cargo in the ship.
     *
     * @return the modules stored as cargo in the ship
     */
    public List<Module> getCargo()
    {
        return cargo;
    }

    /**
     * Returns true if the ship is in danger of being destroyed while mining an asteroid.
     *
     * @return true if the ship is in danger of being destroyed while mining an asteroid
     */
    public boolean isDangerousToMine()
    {
        return getResource(Resource.HULL).getAmount() <= Planet.ASTEROID_DAMAGE;
    }

    /**
     * Returns true if the ship is in a sector.
     *
     * @return true if the ship is in a sector
     */
    public boolean isInSector()
    {
        return location instanceof SectorLocation;
    }

    /**
     * Returns true if the ship is landed on a planet.
     *
     * @return true if the ship is landed on a planet
     */
    public boolean isLanded()
    {
        return location instanceof PlanetLocation;
    }

    /**
     * Returns true if the ship is docked with a station.
     *
     * @return true if the ship is docked with a station
     */
    public boolean isDocked()
    {
        return location instanceof StationLocation;
    }

    /**
     * Returns true if the ship is in a battle.
     *
     * @return true if the ship is in a battle
     */
    public boolean isInBattle()
    {
        return location instanceof BattleLocation;
    }

    /**
     * Returns true if the ship is orbital in a sector. This means that it is in a sector, but not on a planet, at a
     * station, or in a battle.
     *
     * @return true if the ship is orbital in a sector
     */
    public boolean isOrbital()
    {
        return location instanceof SectorLocation &&
               !(location instanceof PlanetLocation || location instanceof StationLocation ||
                 location instanceof BattleLocation);
    }

    /**
     * Returns true if the ship is destroyed.
     *
     * @return true if the ship is destroyed
     */
    public boolean isDestroyed()
    {
        return hasFlag(DESTROYED);
    }

    /**
     * Returns true if the ship is shielded.
     *
     * @return true if the ship is shielded
     */
    public boolean isShielded()
    {
        return hasFlag(SHIELDED);
    }

    /**
     * Returns true if the ship is cloaked.
     *
     * @return true if the ship is cloaked
     */
    public boolean isCloaked()
    {
        return hasFlag(CLOAKED);
    }

    /**
     * Gets the location of the ship.
     *
     * @return the location of the ship
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * Gets the location of the ship, converted to a SectorLocation. Should only been used when the ship's location is
     * known.
     *
     * @return the location of the ship as a SectorLocation
     */
    public SectorLocation getSectorLocation()
    {
        return (SectorLocation) location;
    }

    /**
     * Gets the location of the ship, converted to a PlanetLocation. Should only been used when the ship's location is
     * known.
     *
     * @return the location of the ship as a PlanetLocation
     */
    public PlanetLocation getPlanetLocation()
    {
        return (PlanetLocation) location;
    }

    /**
     * Gets the location of the ship, converted to a StationLocation. Should only been used when the ship's location is
     * known.
     *
     * @return the location of the ship as a StationLocation
     */
    public StationLocation getStationLocation()
    {
        return (StationLocation) location;
    }

    /**
     * Gets the location of the ship, converted to a BattleLocation. Should only been used when the ship's location is
     * known.
     *
     * @return the location of the ship as a BattleLocation
     */
    public BattleLocation getBattleLocation()
    {
        return (BattleLocation) location;
    }

    /**
     * Returns true if there is a flag in the flags list with the given name.
     *
     * @param flag the string to find in the flags list
     * @return true if the flags list contains a String that matches the one provided
     */
    public boolean hasFlag(String flag)
    {
        return flags.contains(flag);
    }

    /**
     * Returns true if this ship is the player.
     *
     * @return true if this is equal to its galaxy's specified player
     */
    public boolean isPlayer()
    {
        return location.getGalaxy().getPlayer() == this;
    }

    /**
     * Returns true if this ship belongs to a faction.
     *
     * @return true if this ship belongs to a faction
     */
    public boolean isAligned()
    {
        return faction != null;
    }

    /**
     * Returns true if the ship is unaligned and their reputation is too low for any faction to accept them.
     *
     * @return true if the player is considered a pirate
     */
    public boolean isPirate()
    {
        if (isAligned())
        {
            return false;
        }

        boolean isPirate = true;
        for (Reputation rep : reputations)
        {
            if (rep.get() >= Reputation.REJECTION)
            {
                isPirate = false;
            }
        }

        return isPirate;
    }

    /**
     * Returns true if this ship is the leader of its faction.
     *
     * @return true if the leader of the ship's faction is itself
     */
    public boolean isLeader()
    {
        return isAligned() && faction.getLeader() == this;
    }

    /**
     * Returns true if the specified ship is considered non-hostile (including if this ship is a pirate).
     *
     * @param ship the ship to check
     * @return true if: the ship is not a pirate and this ship is a pirate or an ally (this is so reputation is lowered
     * for piracy)
     */
    public boolean isPassive(Ship ship)
    {
        return ship.isAligned() && (!isAligned() || faction == ship.faction || !ship.getFaction().isRelationship(WAR,
                faction));
    }

    /**
     * Returns true if the specified faction is hostile to this ship.
     *
     * @param faction the faction to check
     * @return true if: the faction is not this ship's faction and is at war
     */
    public boolean isHostile(Faction faction)
    {
        return this.faction != faction && (!isAligned() || this.faction.isRelationship(WAR, faction));
    }

    /**
     * Sets the name of the ship to the given string.
     *
     * @param name the string to become the ship's name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the ship's AI to the given AI.
     *
     * @param ai the AI to assign to this ship
     */
    public void setAI(AI ai)
    {
        this.ai = ai;
    }

    /**
     * Sets the ship's faction without modifying reputation.
     *
     * @param faction the faction to assign to this ship
     */
    public void setFaction(Faction faction)
    {
        this.faction = faction;
    }

    /**
     * Sets the ship's location to the specified location.
     *
     * @param destination the location to move the ship to
     */
    public void setLocation(Location destination)
    {
        if (destination == null)
        {
            return;
        }

        if (destination instanceof StationLocation && !(location instanceof StationLocation))
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
        else if (location instanceof SectorLocation)
        {
            location.getSector().getShips().remove(this);
        }

        location = destination;
    }

    /**
     * Gets the radius of the ship's field of view, in sectors.
     *
     * @return the radius of the ship's FOV, in sectors
     */
    public double getFOVRadius()
    {
        return hasModule(Scan.MODULE) ? FOV_RADIUS * 2.0 : FOV_RADIUS;
    }

    /**
     * Gets a list of all points in the ship's field of view.
     *
     * @return a list of all points in the ship's FOV
     */
    public List<Coord> getFOV()
    {
        double[][] light = new FOV().calculateFOV(getLocation().getGalaxy().getResistanceMap(),
                getLocation().getCoord().x, getLocation().getCoord().y, getFOVRadius());

        List<Coord> fov = new ArrayList<>();
        for (int y = 0; y < light.length; y++)
        {
            for (int x = 0; x < light[y].length; x++)
            {
                if (light[x][y] > 0.0)
                {
                    fov.add(Coord.get(x, y));
                }
            }
        }

        return fov;
    }

    /**
     * Generates a list of the ship's properties and returns them.
     *
     * @return a Properties object with information about the ship
     */
    public Properties toProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("name", name);
        properties.setProperty("location", location.toString());
        if (isAligned())
        {
            properties.setProperty("faction", faction.getName());
        }
        properties.setProperty("credits", Integer.toString(credits));

        StringBuilder builder = new StringBuilder();
        for (Module module : modules)
        {
            builder.append(module.getName().toLowerCase()).append(", ");
        }

        if (builder.length() > 0)
        {
            builder.delete(builder.length() - 2, builder.length());
            properties.setProperty("modules", builder.toString());
        }

        for (Resource resource : resources)
        {
            properties.setProperty("r_" + resource.getName().toLowerCase(), resource.getAmountAsFraction());
        }

        return properties;
    }

    /**
     * Changes the number of credits possessed by the ship, taking or giving the inverse to the specified faction's
     * economy.
     *
     * @param faction the faction who will receive the inverse of the change
     * @param change  the change in credits
     * @return true if the ship had enough credits to make the change
     */
    public boolean changeCredits(Faction faction, int change)
    {
        if (changeCredits(change))
        {
            if (faction != null)
            {
                faction.changeEconomy(-change);
            }
            return true;
        }
        return false;
    }

    /**
     * Changes the ship's amount of credits by a given amount.
     *
     * @param change the amount to change credits by
     * @return true if the change was completed
     */
    public boolean changeCredits(int change)
    {
        if (credits + change < 0)
        {
            return false;
        }

        credits += change;
        return true;
    }

    /**
     * Returns the reputation object with the faction specified, null if not found.
     *
     * @param faction the faction to get a reputation with
     * @return the Reputation object with the faction specified, null if not found
     */
    public Reputation getReputation(Faction faction)
    {
        if (faction == null)
        {
            return null;
        }

        for (Reputation rep : reputations)
        {
            if (rep.getFaction() == faction)
            {
                return rep;
            }
        }

        return null;
    }

    /**
     * Slowly adjusts reputation towards zero.
     */
    public void fadeReputations()
    {
        for (Reputation rep : reputations)
        {
            if (rep.get() != 0)
            {
                int change = Math.max(1, Math.abs(rep.get()) / rep.getFaction().getAverageReputation());

                if (rep.get() > 0)
                {
                    rep.change(-change);
                }
                else
                {
                    rep.change(change);
                }
            }
        }
    }

    /**
     * Changes the ship's reputation by a specified amount.
     *
     * @param otherFaction the faction with which to change the ship's reputation
     * @param change       the amount by which to change the ship's reputation
     */
    public void changeReputation(Faction otherFaction, int change)
    {
        Reputation rep = getReputation(otherFaction);

        if (rep == null)
        {
            return;
        }

        rep.change(change);

        if (rep.get() < Reputation.REJECTION && rep.getFaction() == faction)
        {
            // Print before faction is left so that null is not printed
            addPlayerColorMessage(new ColorString("The ").add(faction)
                                                         .add(" has rejected you on account of your transgressions."));

            Faction oldFaction = faction;

            // Must directly leave faction so that this method does not recurse
            faction = null;

            if (isLeader())
            {
                oldFaction.holdElection();
            }
        }
    }

    /**
     * Changes the ship's reputation among all factions by the specified amount.
     *
     * @param change the amount by which to change the ship's reputation for every faction
     */
    public void changeGlobalReputation(int change)
    {
        for (Reputation rep : reputations)
        {
            changeReputation(rep.getFaction(), change);
        }
    }

    /**
     * Returns the Reputation object for the faction that the player is most respected in, besides the faction they are
     * a part of.
     *
     * @return the Reputation object containing the popular faction and the ship's reputation in it
     */
    public Reputation getMostPopularOtherFaction()
    {
        Reputation highestRep = null;

        for (Reputation rep : reputations)
        {
            if ((highestRep == null || rep.get() > highestRep.get()) && rep.getFaction() != faction)
            {
                highestRep = rep;
            }
        }

        return highestRep;
    }

    /**
     * Adds the given String as a flag if it does not exist already.
     *
     * @param flag adds the provided String into the flags list
     * @return true if the addition was successful
     */
    public final boolean addFlag(String flag)
    {
        return !flags.contains(flag) && flags.add(flag);
    }

    /**
     * Removes the given String from the flag list.
     *
     * @param flag the String to remove from the flags list
     * @return true if the removal was successful
     */
    public final boolean removeFlag(String flag)
    {
        return flags.remove(flag);
    }

    /**
     * Returns the constant default amount of a resource.
     *
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
     * Performs the same function as getDefaultAmount(String), but uses the resource's name instead.
     *
     * @param resource the Resource whose name will be used
     * @return the constant amount for the resource of the given name
     */
    public static int getDefaultAmount(Resource resource)
    {
        return getDefaultAmount(resource.getName());
    }

    /**
     * Calculates the value of the ship.
     *
     * @return the value of everything on the ship
     */
    public int calculateShipValue()
    {
        int value = credits;

        for (Resource resource : resources)
        {
            value += resource.getTotalValue();
            value += resource.getNExpanders() * resource.getExpander().getValue();
        }

        for (Module module : modules)
        {
            value += module.getValue();
        }

        return value;
    }

    /**
     * Calculates the amount of damage done by a weapon to the ship.
     *
     * @param weapon the weapon used to deal damage
     * @return the amount of damage done by the weapon
     */
    public int getDamageFrom(Weapon weapon)
    {
        int damage = weapon.getDamage();

        if (Resource.ENERGY.equals(weapon.getActionResource()) && isShielded())
        {
            damage /= 2;
        }

        return Math.max(damage, 1);
    }

    /**
     * Returns true if the ship has any weapons.
     *
     * @return true if there is at least one weapon on the ship
     */
    public boolean hasWeapons()
    {
        for (Module module : modules)
        {
            if (module != null && module instanceof Weapon)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the ship has any modules that can be activated.
     *
     * @return true if there is at least one module with an effect on the ship
     */
    public boolean hasActivationModules()
    {
        for (Module module : modules)
        {
            if (module != null && module.hasEffect())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the ship is equipped with the specified module.
     *
     * @param name the name of the module to find
     * @return true if the module is equipped on the ship
     */
    public boolean hasModule(String name)
    {
        for (Module module : modules)
        {
            if (module != null && name.equalsIgnoreCase(module.getName()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Performs the same function as hasModule(String), except with a pre-existing module.
     *
     * @param module the module to find
     * @return whether a module of the same name exists on the ship
     */
    public boolean hasModule(Module module)
    {
        return hasModule(module.getName());
    }

    /**
     * Scans through the ship's modules and returns the first module with the specified name.
     *
     * @param name the name of the module to find
     * @return the first module encountered with the specified name, null if not found
     */
    public Module getModule(String name)
    {
        for (Module module : modules)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                return module;
            }
        }

        return null;
    }

    /**
     * Performs the same function as hasModule(String), except with a pre-existing module.
     *
     * @param module the module to find
     * @return the first module encountered with the same name as the specified module, null if not found
     */
    public Module getModule(Module module)
    {
        return getModule(module.getName());
    }

    /**
     * Scans through the ship's cargo and returns the first module with the specified name.
     *
     * @param name the name of the module to find
     * @return the first module encountered in cargo with the specified name, null if not found
     */
    public Module getCargoModule(String name)
    {
        for (Module module : cargo)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                return module;
            }
        }

        return null;
    }

    /**
     * Returns the amount of modules with the specified name on board the ship.
     *
     * @param name the name of the modules to find
     * @return the number of modules with the specified name
     */
    public int getModuleAmount(String name)
    {
        int amount = 0;

        for (Module module : modules)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                amount++;
            }
        }

        for (Module module : cargo)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                amount++;
            }
        }

        return amount;
    }

    /**
     * Performs the same function as getModuleAmount(String), using the module's name instead.
     *
     * @param module the module to find the amount of, must be non-null
     * @return the number of modules with the same name
     */
    public int getModuleAmount(Module module)
    {
        return module == null ? 0 : getModuleAmount(module.getName());
    }

    /**
     * Performs the same function as getModule(), but will only return the module if it is a weapon.
     *
     * @param name the name of the weapon to find
     * @return the first weapon installed of the same name, null if not found
     */
    public Weapon getWeapon(String name)
    {
        Module module = getModule(name);
        if (module != null && module instanceof Weapon)
        {
            return (Weapon) module;
        }

        return null;
    }

    /**
     * Returns the number of weapons installed in the ship.
     *
     * @return the number of modules that are weapons in the ship's array
     */
    public List<Weapon> getWeapons()
    {
        List<Weapon> weapons = new LinkedList<>();

        for (Module module : modules)
        {
            if (module instanceof Weapon)
            {
                weapons.add((Weapon) module);
            }
        }

        return weapons;
    }

    /**
     * Adds the specified module to the ship's module array, or cargo if already installed.
     *
     * @param module the module to add
     */
    public void addModule(Module module)
    {
        if (hasModule(module.getName()))
        {
            cargo.add(module);
        }
        else
        {
            modules.add(module);
        }
    }

    /**
     * Performs the same function as addModule(Module), finding a module of the designated name to add - must be final
     * as it is used in a constructor.
     *
     * @param name the name of the module to find and add
     */
    public final void addModule(String name)
    {
        if (isDocked())
        {
            Module module = getSectorLocation().getStation().getModule(name);
            if (module != null)
            {
                addModule(module);
            }
        }
        else
        {
            addModule(Station.getBaseModule(name));
        }
    }

    /**
     * Locates and removes a module of a designated name.
     *
     * @param name the name of the module
     * @return true if the module was removed
     */
    public boolean removeModule(String name)
    {
        if (cargo.remove(getCargoModule(name)))
        {
            return true;
        }

        if (modules.remove(getModule(name)))
        {
            return true;
        }

        addPlayerError("Specified module not found on ship.");
        return false;
    }

    /**
     * Performs the same function as removeModule(String), using the module's name as the name of the removed module.
     *
     * @param module the module whose name will be used in the removal
     * @return true if the module was removed
     */
    public boolean removeModule(Module module)
    {
        return removeModule(module.getName());
    }

    /**
     * Finds a resource on the ship with the name provided and returns it.
     *
     * @param name the name of the resource to find
     * @return the resource with the specified name, null if not found
     */
    public final Resource getResource(String name)
    {
        // Must be final since it is used in construction
        for (Resource resource : resources)
        {
            if (name.equalsIgnoreCase(resource.getName()))
            {
                return resource;
            }
        }

        return null;
    }

    /**
     * Return an expander from a resource on the ship with the specified name.
     *
     * @param expanderName the name of the expander to find
     * @return the expander with the specified name, null if not found
     */
    public Expander getExpander(String expanderName)
    {
        for (Resource resource : resources)
        {
            if (expanderName.equalsIgnoreCase(resource.getExpander().getName()))
            {
                return resource.getExpander();
            }
        }

        return null;
    }

    /**
     * Returns a resource on the ship with an expander of a specified name.
     *
     * @param expanderName the name of the expander on the resource to find
     * @return a resource on the ship with the designated expander name, null if not found
     */
    public Resource getResourceFromExpander(String expanderName)
    {
        for (Resource resource : resources)
        {
            if (expanderName.equalsIgnoreCase(resource.getExpander().getName()))
            {
                return resource;
            }
        }

        return null;
    }

    /**
     * Performs the same function as getMaxBuyAmount(Resource), with the name of the resource instead.
     *
     * @param name the name of the resource being purchased
     * @return the highest amount of the resource that can be purchased, -1 if the resource was not found
     */
    public int getMaxBuyAmount(String name)
    {
        if (getResource(name) != null)
        {
            return getMaxBuyAmount(getResource(name));
        }

        if (getExpander(name) != null)
        {
            return getMaxBuyAmount(getExpander(name));
        }

        return -1;
    }

    /**
     * Returns the highest amount of a resource that can be purchased.
     *
     * @param resource the resource being purchased
     * @return the highest amount of the resource that can be purchased, -1 if the resource was not found
     */
    public int getMaxBuyAmount(Resource resource)
    {
        if (resource == null)
        {
            return -1;
        }

        if (isDocked())
        {
            return Math.min(credits / getSectorLocation().getStation().getResource(resource.getName()).getPrice(),
                    resource.getCapacity() - resource.getAmount());
        }

        return Math.min(credits / resource.getPrice(), resource.getCapacity() - resource.getAmount());
    }

    /**
     * Returns the most expanders of a specified type that can be purchased.
     *
     * @param expander the expander being purchased
     * @return the most expanders that can be purchased, -1 if the expander was not found
     */
    public int getMaxBuyAmount(Expander expander)
    {
        if (expander == null)
        {
            return -1;
        }

        if (isDocked())
        {
            return Math.min(MAX_EXPANDERS - getResourceFromExpander(expander.getName()).getNExpanders(),
                    credits / getSectorLocation().getStation().getExpander(expander.getName()).getPrice());
        }

        return Math.min(MAX_EXPANDERS - getResourceFromExpander(expander.getName()).getNExpanders(),
                credits / expander.getPrice());
    }

    /**
     * Returns the highest amount of a resource that can be sold, which will be the amount of the resource.
     *
     * @param name the name of the resource
     * @return the highest amount of the resource that can be sold, -1 if the resource was not found
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
     * Decreases hull strength by the damage of a given weapon, and destroys the ship if the weapon deals too much
     * damage.
     *
     * @param weapon the weapon to damage the ship with
     * @param print  if true, will print if the ship was destroyed
     */
    public void damageWith(Weapon weapon, boolean print)
    {
        damage(getDamageFrom(weapon), print);
    }

    /**
     * Decreases hull strength by a given amount, destroys the ship if the damage is too great, and has the possibility
     * of damaging modules.
     *
     * @param damage the amount of damage to deal to the ship
     * @param print  if true, will print if the ship was destroyed
     */
    public void damage(int damage, boolean print)
    {
        if (!getResource(Resource.HULL).changeAmount(-damage) || getResource(Resource.HULL).isEmpty())
        {
            getResource(Resource.HULL).setAmount(0);
            destroy(print);
        }

        // Damages a module if the damage is above a threshold that is
        // proportional to the number of modules installed
        if (!modules.isEmpty() && damage >= getResource(Resource.HULL).getCapacity() / modules.size())
        {
            Module damagedModule = modules.get(rng.nextInt(modules.size()));

            if (damagedModule.damage())
            {
                addPlayerMessage("Your " + damagedModule.getName().toLowerCase() + " has been damaged by the impact.");

                if (damagedModule.isEffect(SHIELDED) && isShielded())
                {
                    removeFlag(SHIELDED);
                }
                else if (damagedModule.isEffect(CLOAKED) && isCloaked())
                {
                    removeFlag(CLOAKED);
                }
            }
            else
            {
                addPlayerMessage(
                        "Your " + damagedModule.getName().toLowerCase() + " has been destroyed by the impact!");
                modules.remove(damagedModule);
            }
        }
    }

    /**
     * Joins the entered faction and adds the relevant reputation.
     *
     * @param faction the faction to join (use leaveFaction() instead of setting this to null)
     */
    public void joinFaction(Faction faction)
    {
        if (this.faction == faction)
        {
            return;
        }

        this.faction = faction;
        changeReputation(faction, Reputation.JOIN);
    }

    /**
     * Leaves a faction, resetting the ship's faction and incurring a reputation penalty if the ship was in a faction.
     */
    public void leaveFaction()
    {
        if (!isAligned())
        {
            return;
        }

        Faction oldFaction = faction;
        boolean wasLeader = isLeader();

        faction = null;
        changeReputation(oldFaction, Reputation.LEAVE);

        if (wasLeader)
        {
            oldFaction.holdElection();
        }
    }

    /**
     * Gets the faction who would respond to a distress signal from this ship.
     *
     * @return the faction who would respond to a distress signal from this ship
     */
    public Faction getDistressResponder()
    {
        if (isPirate())
        {
            addPlayerMessage("There is no response.");
            changeGlobalReputation(Reputation.DISTRESS_ATTEMPT);
            return null;
        }

        if (isAligned())
        {
            if (getReputation(faction).get() >= Reputation.DISTRESS)
            {
                // Otherwise they will refuse, giving others a chance to help
                addPlayerColorMessage(new ColorString("The ").add(faction).add(" refuses to help you."));
                changeReputation(faction, Reputation.DISTRESS_ATTEMPT);
            }
            else if (faction.getEconomyCredits() < DISTRESS_CREDITS)
            {
                addPlayerColorMessage(new ColorString("The ").add(faction).add(" cannot afford to help you."));
            }
            else
            {
                // The ship's faction will help if reputation is high enough
                return faction;
            }
        }

        Reputation offerReputation = getMostPopularOtherFaction();
        Faction offerFaction = offerReputation.getFaction();

        if (offerFaction == null || offerFaction.getEconomyCredits() < DISTRESS_CREDITS ||
            offerReputation.get() + Reputation.JOIN + Reputation.DISTRESS < 0)
        {
            if (!isAligned())
            {
                addPlayerMessage("There is no response.");
                changeGlobalReputation(Reputation.DISTRESS_ATTEMPT);
            }
            return null;
        }

        return offerFaction;
    }

    /**
     * Removes the ship from all collections and marks it as destroyed.
     *
     * @param print if true, will print a message about the ship's destruction
     */
    public void destroy(boolean print)
    {
        if (isDestroyed())
        {
            return;
        }

        location.getSector().getShips().remove(this);

        if (isDocked())
        {
            getSectorLocation().getStation().getShips().remove(this);
        }
        else if (isLanded())
        {
            getPlanetLocation().getRegion().getShips().remove(this);
        }

        addFlag(DESTROYED);

        if (isPlayer())
        {
            playSoundEffect(Paths.DEATH);
        }
        else if (print)
        {
            addColorMessage(toColorString().add(" has been destroyed."));
        }
    }

    /**
     * Refills all of the ship's resources (except ore).
     *
     * @return the total cost of the materials refilled
     */
    public int refill()
    {
        int cost = 0;
        cost += getResource(Resource.FUEL).fill() * getResource(Resource.FUEL).getValue();
        cost += getResource(Resource.ENERGY).fill() * getResource(Resource.ENERGY).getValue();
        cost += getResource(Resource.HULL).fill() * getResource(Resource.HULL).getValue();
        return cost;
    }

    /**
     * Repairs all the modules on the ship.
     */
    public void repairModules()
    {
        for (Module module : modules)
        {
            module.repair();
        }

        for (Module module : cargo)
        {
            module.repair();
        }
    }

    /**
     * Checks if the player's credits are sufficient for a purchase of a specified price.
     *
     * @param price the price of the item to be purchased funds
     * @return the error message to display if the check fails, null if successful
     */
    public String validateFunds(int price)
    {
        return price > credits ? "Insufficient funds; have " + credits + " credits, need " + price + "." : null;
    }

    /**
     * Checks if the ship is docked, and optionally prints a message if not.
     *
     * @return the error message to display if the check fails, null if successful
     */
    public String validateDocking()
    {
        return isDocked() ? null : "Ship must be docked with a station to buy and sell items.";
    }

    /**
     * Checks if the ship is equipped with a specified module and that it is undamaged, and optionally prints a custom
     * message if not.
     *
     * @param module the name of the module to validate
     * @param action the String to print as the need for the module
     * @return the error message to display if the check fails, null if successful
     */
    public String validateModule(String module, String action)
    {
        // The ship can technically have this installed because it doesn't exist
        if (module == null || !hasModule(module))
        {
            return Utility.addCapitalizedArticle(module) + " is required" + (action == null ? "" : " to " + action) +
                   ".";
        }

        Module moduleObj = getModule(module);

        if (moduleObj.isDamaged())
        {
            if (getModuleAmount(moduleObj) > 1)
            {
                // Check for spares
                for (Module cargoModule : cargo)
                {
                    if (cargoModule.getName().equalsIgnoreCase(moduleObj.getName()) && !cargoModule.isDamaged())
                    {
                        return null;
                    }
                }
            }

            return "Your " + moduleObj.getName().toLowerCase() + " is too damaged to function.";
        }

        return null;
    }

    /**
     * Checks if the ship is equipped with a specified module and that it is undamaged, and optionally prints a message
     * if not.
     *
     * @param module the name of the module to validate
     * @return the error message to display if the check fails, null if successful
     */
    public String validateModule(String module)
    {
        return validateModule(module, null);
    }

    /**
     * Checks if the ship has enough of the specified resource, and optionally prints a message if not.
     *
     * @param resource     the resource to validate
     * @param cost         the amount of the resource that the ship must possess
     * @param actionString the String to use as the need for resources
     * @return the error message to display if the check fails, null if successful
     */
    public String validateResources(Resource resource, int cost, String actionString)
    {
        if (resource != null && resource.getAmount() < cost)
        {
            return "Insufficient " + resource.getName().toLowerCase() + " reserves to " + actionString + "; " +
                   "have " + resource.getAmount() + ", need " + cost + ".";
        }

        return null;
    }

    /**
     * Checks if the ship has enough of the specified resource, and optionally prints a message if not.
     *
     * @param resource     the name of the resource to validate
     * @param cost         the amount of the resource that the ship must possess
     * @param actionString the String to print as the need for resources
     * @return the error message to display if the check fails, null if successful
     */
    public String validateResources(String resource, int cost, String actionString)
    {
        return validateResources(getResource(resource), cost, actionString);
    }

    /**
     * Gets a list of ColorStrings describing the status of this ship.
     *
     * @return a list of ColorStrings describing the status of this ship
     */
    public List<ColorString> getStatusList()
    {
        List<ColorString> contents = new LinkedList<>();
        contents.add(new ColorString("Credits: ").add(
                new ColorString(Integer.toString(credits) + Symbol.CREDITS, COLOR_FIELD)));

        for (Resource resource : resources)
        {
            contents.add(resource.getAmountAsColoredFraction().add(" " + resource.getName()));
        }

        if (!modules.isEmpty())
        {
            contents.add(null);
            for (Module module : modules)
            {
                ColorString moduleString = new ColorString(module.toString());
                if (module.isDamaged())
                {
                    moduleString.add(new ColorString(" (Damaged)", AsciiPanel.brightRed));
                }
                else if (hasFlag(module.getEffect()))
                {
                    moduleString.add(new ColorString(" (Active)", AsciiPanel.brightGreen));
                }
                contents.add(moduleString);
            }
            for (Module module : cargo)
            {
                ColorString moduleString = new ColorString(module.toString()).add(
                        new ColorString(" (Cargo)", AsciiPanel.yellow));
                if (module.isDamaged())
                {
                    moduleString.add(new ColorString(" (Damaged)", AsciiPanel.brightRed));
                }
                contents.add(moduleString);
            }
        }

        return contents;
    }

    /**
     * Adds the given ColorString as a message, only if this ship is the player. Intended for player-specific messages.
     *
     * @param message the ColorString to add as a message if this ship is the player
     */
    public void addPlayerColorMessage(ColorString message)
    {
        if (isPlayer())
        {
            addColorMessage(message);
        }
    }

    /**
     * Adds the given String as a message, only if this ship is the player. Intended for player-specific messages.
     *
     * @param message the String to add as a message if this ship is the player
     */
    public void addPlayerMessage(String message)
    {
        if (isPlayer())
        {
            addMessage(message);
        }
    }

    /**
     * Adds the given String as an error, only if this ship is the player. Intended for player-specific errors.
     *
     * @param error the String to add as an error if this ship is the player
     */
    public void addPlayerError(String error)
    {
        if (isPlayer())
        {
            addError(error);
        }
    }

    /**
     * Plays the sound at the given path, only if this ship is the player.
     *
     * @param path the path of the sound to play if this ship is the player
     */
    public void playPlayerSound(String path)
    {
        if (isPlayer())
        {
            playSoundEffect(path);
        }
    }

    /**
     * Generates a relevant classification for the ship based off its abilities in both battle and mining.
     *
     * @return a String that describes the ship's strength and specialization
     */
    public String getClassification()
    {
        int battleLevel = Math.min(getBattleLevel(), Levels.MAX_LEVEL);
        int miningLevel = Math.min(getMiningLevel(), Levels.MAX_LEVEL);

        if (battleLevel < Levels.BASE_LEVEL && miningLevel < Levels.BASE_LEVEL)
        {
            return Levels.LOWEST_LEVEL;
        }

        if (battleLevel >= Levels.MAX_LEVEL && miningLevel >= Levels.MAX_LEVEL)
        {
            return Levels.HIGHEST_LEVEL;
        }

        if (battleLevel > miningLevel)
        {
            return Levels.BATTLE_LEVELS[battleLevel - 1];
        }
        else
        {
            return Levels.MINING_LEVELS[miningLevel - 1];
        }
    }

    /**
     * Returns the type of level that the ship has the most of.
     *
     * @return the level type with the highest amount, null if they are equal
     */
    public String getHigherLevel()
    {
        if (getAbsoluteMiningLevel() > getAbsoluteBattleLevel())
        {
            return "mining";
        }

        if (getAbsoluteMiningLevel() < getAbsoluteBattleLevel())
        {
            return "battle";
        }

        return null;
    }

    /**
     * Returns the absolute battle level or mining level, whichever is greater.
     *
     * @return the greater of: absolute battle level or absolute mining level
     */
    public int getHighestLevel()
    {
        return Math.max(getAbsoluteBattleLevel(), getAbsoluteMiningLevel());
    }

    /**
     * Returns the sum of the absolute battle and mining levels.
     *
     * @return the sum of the absolute battle level and the absolute mining level
     */
    public int getTotalLevel()
    {
        return getAbsoluteBattleLevel() + getAbsoluteMiningLevel();
    }

    /**
     * Returns the ship's "level" of power in battle, to be used in determining a classification.
     *
     * @return an integer based on the ship's amount of weaponry and hull capacity
     */
    private int getBattleLevel()
    {
        return getAbsoluteBattleLevel() / Levels.LEVEL_AMOUNT;
    }

    /**
     * Returns the ship's absolute "level" of power in battle, to be used in determining a classification.
     *
     * @return an integer based on the ship's amount of weaponry and hull capacity
     */
    public int getAbsoluteBattleLevel()
    {
        if (!hasWeapons())
        {
            return 0;
        }

        int level = 0;
        level += getWeapons().size() * Levels.LEVEL_AMOUNT;
        level += getResource(Resource.HULL).getNExpanders() * 2;
        return level;
    }

    /**
     * Returns the ship's "level" of mining ability, to be used in determining a classification.
     *
     * @return an integer based on the ship's amount of non-combat modules and ore capacity
     */
    private int getMiningLevel()
    {
        return getAbsoluteMiningLevel() / Levels.LEVEL_AMOUNT;
    }

    /**
     * Returns the ship's absolute "level" of mining ability, to be used in determining a classification.
     *
     * @return an integer based on the ship's amount of non-combat modules and ore capacity
     */
    public int getAbsoluteMiningLevel()
    {
        int level = 0;
        level += (modules.size() - getWeapons().size()) * Levels.LEVEL_AMOUNT;
        level += getResource(Resource.ORE).getNExpanders() * 2;
        return level;
    }

    /**
     * Resets the prices of all items on the ship to the current station's prices, if docked.
     */
    public void updatePrices()
    {
        if (!isDocked())
        {
            return;
        }

        Station station = getSectorLocation().getStation();

        for (Module module : modules)
        {
            if (module != null && station.hasModule(module.getName()))
            {
                module.setPrice(station.getModule(module.getName()).getPrice());
            }
        }

        for (Resource resource : resources)
        {
            if (resource != null && station.hasResource(resource.getName()))
            {
                resource.setPrice(station.getResource(resource.getName()).getPrice());
                resource.getExpander().setPrice(station.getExpander(resource.getExpander().getName()).getPrice());
            }
        }
    }

    /**
     * Update effects that do something at the end of each turn.
     */
    public void updateContinuousEffects()
    {
        for (Module module : modules)
        {
            if (module.hasEffect() && flags.contains(module.getEffect()))
            {
                if (!getResource(module.getActionResource()).changeAmount(-module.getActionCost()))
                {
                    removeFlag(module.getEffect());
                }
            }
        }

        // if (hasModule(Action.SOLAR) && isInSector() && !isLanded())
        // {
        //     getResource(Action.SOLAR.getAction().getResource()).changeAmountWithDiscard(
        //             Action.SOLAR.getAction().getCost() *
        //             location.getSector().getStar().getSolarPowerAt(getSectorLocation().getOrbit()));
        // }
    }

    /**
     * Sets the ship's resources to the constant defaults.
     */
    private void setResourceDefaults()
    {
        Resource current = getResource(Resource.FUEL);
        current.setBaseCapacity(FUEL);
        current.setAmount(FUEL);

        current = getResource(Resource.ENERGY);
        current.setBaseCapacity(ENERGY);
        current.setAmount(ENERGY);

        current = getResource(Resource.ORE);
        current.setBaseCapacity(ORE);
        current.setAmount(0);

        current = getResource(Resource.HULL);
        current.setBaseCapacity(HULL);
        current.setAmount(HULL);
    }

    /**
     * Creates a Reputation object for each faction in the game.
     */
    public void createReputations()
    {
        reputations = new Reputation[location.getGalaxy().getFactions().length];

        for (int i = 0; i < reputations.length; i++)
        {
            reputations[i] = new Reputation(location.getGalaxy().getFactions()[i]);
        }
    }

    @Override
    public int compareTo(Ship other)
    {
        Reputation r1 = getReputation(faction);
        Reputation r2 = other.getReputation(faction);

        if (r1 == null)
        {
            if (r2 == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }
        else if (r2 == null)
        {
            return 1;
        }

        return r1.compareTo(r2);
    }
}
