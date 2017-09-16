package boldorf.eversector.map;

import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.items.*;
import boldorf.eversector.locations.SectorLocation;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.Symbol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.rng;

/**
 * A station at which ships can refuel and purchase upgrades.
 */
public class Station implements ColorStringObject
{
    /**
     * The base cost in credits to claim any celestial body.
     */
    public static final int CLAIM_COST = 250;

    public static final String TRADE = "Trade";
    public static final String BATTLE = "Battle";

    /**
     * The base modules that all stations sell, at their base prices.
     */
    public static Module[] MODULES = new Module[]{
            new Module("Scanner", "Reveals locations from a distance.", 150, false, new Action(Resource.ENERGY, 3)),
            new Module("Refinery", "Can convert ore into fuel at a 1:1 ratio.", 250, false,
                    new Action(Resource.ENERGY, 1)),
            new Module("Solar Array", "Passively generates energy each turn based on solar proximity.", 250, false,
                    new Action(Resource.ENERGY, 1)),
            new Module("Warp Drive", "Facilitates long-range sector jumps powered by energy.", 400, false,
                    new Action(Resource.ENERGY, 15)),
            new Module("Shield", "When active, halves damage from oncoming energy weapon fire.", 200, true,
                    Ship.SHIELDED, new Action(Resource.ENERGY, 2)),
            new Module("Cloaking Device", "When active, renders the ship impossible to track.", 300, true, Ship.CLOAKED,
                    new Action(Resource.ENERGY, 2)),
            new Weapon("Laser", "A focused laser used to cut open enemy hulls.", 100, 2,
                    new Action(Resource.ENERGY, 2)),
            new Weapon("Torpedo Tube", "Fires guided torpedoes capable of bypassing energy shields.", 200, 4,
                    new Action(Resource.FUEL, 2)),
            new Weapon("Pulse Beam", "A devastating laser capable of ripping through weak ships.", 500, 7,
                    new Action(Resource.ENERGY, 10))
    };

    /**
     * The base resources that all stations sell, at the base prices for themselves and their expanders.
     */
    public static BaseResource[] RESOURCES = new BaseResource[]{
            new BaseResource(Resource.FUEL, "A reactive mixture able to create highly efficient thrust.", 10, true,
                    new Expander(Resource.FUEL_EXPANDER, "A compact container able to withstand extreme pressures.",
                            70)),
            new BaseResource(Resource.ENERGY, "Highly concentrated electrical charge.", 5, false,
                    new Expander(Resource.ENERGY_EXPANDER,
                            "A supercapacitor capable of retaining charge for long durations.", 85)),
            new BaseResource(Resource.ORE, "A versatile compound that can be refined into fuel.", 10, true,
                    new Expander(Resource.ORE_EXPANDER,
                            "An interior hold designed for containing large quantities of ore.", 85)),
            new BaseResource(Resource.HULL, "Layered alloys and ceramics that protect the ship.", 15, false,
                    new Expander(Resource.HULL_EXPANDER, "A frame to allow the mounting of additional plating.", 85, 2))
    };

    private String name;

    /**
     * The type of station.
     */
    private String type;

    /**
     * The location of this station.
     */
    private final SectorLocation location;

    /**
     * The faction that owns this station.
     */
    private Faction faction;

    /**
     * The ships that are currently docked with the station.
     */
    private List<Ship> ships;

    /**
     * The modules that this station sells.
     */
    private Module[] modules;

    /**
     * The resources that this station sells.
     */
    private BaseResource[] resources;

    /**
     * Creates a station from a name, location, and faction.
     *
     * @param name     the name of the station
     * @param location the location of the station
     * @param faction  the faction the station belongs to
     */
    public Station(String name, SectorLocation location, Faction faction)
    {
        this.name = name;
        this.location = location;
        this.faction = faction;
        ships = new LinkedList<>();
        type = generateType();

        cloneItems();
        generatePrices();
    }

    @Override
    public String toString()
    {return type + " Station " + name;}

    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(), isClaimed() ? getFaction().getColor() : null);
    }

    public String getName()
    {return name;}

    public String getType()
    {return type;}

    public SectorLocation getLocation()
    {return location;}

    public Faction getFaction()
    {return faction;}

    public boolean isClaimed()
    {return faction != null;}

    /**
     * Claims the celestial body for the specified faction.
     *
     * @param faction the faction that will claim the celestial body
     */
    public void claim(Faction faction)
    {
        if (this.faction == faction)
        {
            return;
        }

        this.faction = faction;
        location.getSector().updateFaction();
    }

    /**
     * Returns the symbol that corresponds to the station type.
     *
     * @return the symbol representing the station type
     */
    public ColorChar getSymbol()
    {
        return new ColorChar(BATTLE.equals(type) ? Symbol.BATTLE_STATION.get() : Symbol.TRADE_STATION.get(),
                getFaction().getColor());
    }

    /**
     * Returns true if the station sells the specified module.
     *
     * @param module the module to check
     * @return true if the station sells the module
     */
    public boolean sells(Module module)
    {
        if (module == null || module.getName() == null)
        {
            return false;
        }

        if (module.isBattle())
        {
            return BATTLE.equals(type);
        }

        return TRADE.equals(type);
    }

    public BaseResource[] getResources()
    {return resources;}

    public Module[] getModules()
    {return modules;}

    /**
     * Returns true if the station has any item with the specified name.
     *
     * @param name the name of the item to find
     * @return true if a search for any type of item of this name does not return null
     */
    public boolean isItem(String name)
    {return hasModule(name) || hasResource(name) || hasExpander(name);}

    /**
     * Returns a module on the station with the specified name.
     *
     * @param name the name of the module to find
     * @return the module with the specified name, null if not found
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
     * Returns true if the station has a module with the specified name.
     *
     * @param name the name of the module to find
     * @return true if a search for the module does not return null
     */
    public boolean hasModule(String name)
    {return getModule(name) != null;}

    /**
     * Returns the default module with the specified name.
     *
     * @param name the name of the base module to find
     * @return the base module with the specified name, null if not found
     */
    public static Module getBaseModule(String name)
    {
        for (Module module : MODULES)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                return module;
            }
        }

        return null;
    }

    /**
     * Returns true if a base module with the specified name exists.
     *
     * @param name the name of the base module to find
     * @return true if a search for the module does not return null
     */
    public static boolean hasBaseModule(String name)
    {return getBaseModule(name) != null;}

    /**
     * Returns the default weapon with the specified name.
     *
     * @param name the name of the base weapon to find
     * @return the base weapon with the specified name, null if not found
     */
    public static Weapon getBaseWeapon(String name)
    {
        for (Module module : MODULES)
        {
            if (name.equalsIgnoreCase(module.getName()) && module instanceof Weapon)
            {
                return (Weapon) module;
            }
        }

        return null;
    }

    /**
     * Returns true if a base weapon with the specified name exists.
     *
     * @param name the name of the base weapon to find
     * @return true if a search for the weapon does not return null
     */
    public static boolean hasBaseWeapon(String name)
    {
        for (Module module : MODULES)
        {
            if (name.equalsIgnoreCase(module.getName()) && module instanceof Weapon)
            {
                return true;
            }
        }

        return false;
    }

    public Item getItem(String name)
    {
        if (hasResource(name))
        {
            return getResource(name);
        }
        else if (hasExpander(name))
        {
            return getExpander(name);
        }
        else
        {
            return getModule(name);
        }
    }

    /**
     * Returns the resource with the specified name.
     *
     * @param name the name of the resource to find
     * @return the resource with the specified name, null if not found
     */
    public BaseResource getResource(String name)
    {
        for (BaseResource resource : resources)
        {
            if (name.equalsIgnoreCase(resource.getName()))
            {
                return resource;
            }
        }

        return null;
    }

    /**
     * Returns true if the station has a base module with the specified name.
     *
     * @param name the name of the base module to find
     * @return true if a search for the module does not return null
     */
    public boolean hasResource(String name)
    {return getResource(name) != null;}

    /**
     * Returns the expander with the specified name.
     *
     * @param name the name of the expander to find
     * @return the expander with the specified name, null if not found
     */
    public Expander getExpander(String name)
    {
        for (BaseResource resource : resources)
        {
            if (name.equalsIgnoreCase(resource.getExpander().getName()))
            {
                return resource.getExpander();
            }
        }

        return null;
    }

    /**
     * Returns true if an expander with the specified name exists.
     *
     * @param name the name of the expander to find
     * @return true if a search for the expander does not return null
     */
    public boolean hasExpander(String name)
    {return getExpander(name) != null;}

    /**
     * Returns the default module with the specified name.
     *
     * @param name the name of the base resource to find
     * @return the base resource with the specified name, null if not found
     */
    public static BaseResource getBaseResource(String name)
    {
        for (BaseResource resource : RESOURCES)
        {
            if (name.equalsIgnoreCase(resource.getName()))
            {
                return resource;
            }
        }

        return null;
    }

    /**
     * Returns true if a base resource with the specified name exists.
     *
     * @param name the name of the base resource to find
     * @return true if a search for the module does not return null
     */
    public static boolean hasBaseResource(String name)
    {return getBaseResource(name) != null;}

    /**
     * Finds an object with the specified name and calls its define method.
     *
     * @param name the name of the object to define
     * @return true if the object was found and defined
     */
    public static boolean define(String name)
    {
        for (Module module : MODULES)
        {
            if (name.equalsIgnoreCase(module.getName()))
            {
                module.define();
                return true;
            }
        }

        for (BaseResource resource : RESOURCES)
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
     *
     * @param faction the faction to count ships in
     * @return the number of ships docked with the station that belong to the faction, will be non-negative
     */
    public int getNShips(Faction faction)
    {
        int nShips = 0;

        for (Ship ship : ships)
        {
            if (ship.getFaction() == faction)
            {
                nShips++;
            }
        }

        return nShips;
    }

    /**
     * Creates an array of resources from the BaseResources constant in Station.
     *
     * @return an array of resources made from Station's array of BaseResources
     */
    public static Resource[] copyResources()
    {
        Resource[] copy = new Resource[RESOURCES.length];

        for (int i = 0; i < RESOURCES.length; i++)
        {
            copy[i] = new Resource(RESOURCES[i]);
        }

        return copy;
    }

    /**
     * Randomly selects the station's type.
     *
     * @return the generated type, will be non-null
     */
    private String generateType()
    {return rng.nextBoolean() ? TRADE : BATTLE;}

    /**
     * Randomly generates the prices of each module and resource.
     */
    private void generatePrices()
    {
        for (Module module : modules)
        {
            module.generatePrice();
        }

        for (BaseResource resource : resources)
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

        for (Module module : MODULES)
        {
            if (sells(module))
            {
                if (module instanceof Weapon)
                {
                    moduleList.add(new Weapon((Weapon) module));
                }
                else
                {
                    moduleList.add(new Module(module));
                }
            }
        }

        modules = new Module[moduleList.size()];

        for (int i = 0; i < modules.length; i++)
        {
            modules[i] = moduleList.get(i);
        }

        resources = new BaseResource[RESOURCES.length];

        for (int i = 0; i < RESOURCES.length; i++)
        {
            resources[i] = new BaseResource(RESOURCES[i]);
        }
    }
}