package maugrift.eversector.map;

import maugrift.apwt.glyphs.ColorChar;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.glyphs.ColorStringObject;
import maugrift.eversector.Paths;
import maugrift.eversector.Symbol;
import maugrift.eversector.faction.Faction;
import maugrift.eversector.items.*;
import maugrift.eversector.items.Module;
import maugrift.eversector.locations.SectorLocation;
import maugrift.eversector.ships.Ship;
import maugrift.eversector.Main;
import maugrift.eversector.items.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A station at which ships can refuel and purchase upgrades.
 *
 * @author Aaron Friesen
 */
public class Station implements ColorStringObject
{
	/**
	 * The descriptors for trade stations.
	 */
	private static final String[] NAME_TRADE = new String[] {
		"Factory",
		"Hub",
		"Manufactory",
		"Marketplace",
		"Vendor",
	};

	/**
	 * The descriptors for battle stations.
	 */
	private static final String[] NAME_BATTLE = new String[] {
		"Armory",
		"Arsenal",
		"Bunker",
		"Fortress",
		"Outpost",
	};

	/**
	 * The base modules that all stations sell, at their base prices. <b>To be
	 * removed in v0.8.</b>
	 */
	public static final Module[] MODULES = new Module[] {
		new Module(
			Module.SCANNER,
			"Scan enemy ships and reveal farther sectors.",
			150,
			false,
			Resource.ENERGY,
			3
		),
		new Module(
			Module.REFINERY,
			"Convert ore into fuel at a 1:1 ratio.",
			250,
			false,
			Resource.ENERGY,
			1
		),
		new Module(
			Module.SOLAR_ARRAY,
			"Produces energy each turn in orbit near a star.",
			250,
			false,
			Resource.ENERGY,
			-1
		),
		new Module(
			Module.WARP_DRIVE,
			"Spend energy to jump to any visible sector.",
			400,
			false,
			Resource.ENERGY,
			15
		),
		new Module(
			Module.SHIELD,
			"Halves damage from energy weapons while active.",
			200,
			true,
			Ship.SHIELDED,
			Resource.ENERGY,
			2
		),
		new Module(
			Module.CLOAKING_DEVICE,
			"You cannot be attacked or pursued while active.",
			300,
			true,
			Ship.CLOAKED,
			Resource.ENERGY,
			2
		),
		new Weapon(
			Weapon.LASER,
			null,
			100,
			2,
			Resource.ENERGY,
			2,
			Paths.LASER
		),
		new Weapon(
			Weapon.TORPEDO_TUBE,
			null,
			200,
			4,
			Resource.FUEL,
			2,
			Paths.TORPEDO
		),
		new Weapon(
			Weapon.PULSE_BEAM,
			null,
			500,
			7,
			Resource.ENERGY,
			10,
			Paths.PULSE
		)
	};

	/**
	 * The base resources that all stations sell, at the base prices for
	 * themselves and their expanders. <b>To be removed in v0.8.</b>
	 */
	public static final BaseResource[] RESOURCES = new BaseResource[] {
		new BaseResource(
			Resource.FUEL,
			null,
			10,
			true,
			new Expander(Resource.FUEL_EXPANDER,
				null,
				70
			)
		),
		new BaseResource(
			Resource.ENERGY,
			null,
			5,
			false,
			new Expander(Resource.ENERGY_EXPANDER,
				null,
				85
			)
		),
		new BaseResource(
			Resource.ORE,
			null,
			10,
			true,
			new Expander(Resource.ORE_EXPANDER,
				null,
				85
			)
		),
		new BaseResource(
			Resource.HULL,
			null,
			15,
			false,
			new Expander(Resource.HULL_EXPANDER,
				null,
				85,
				2
			)
		)
	};

	/**
	 * The base cost in credits to claim any celestial body.
	 */
	public static final int CLAIM_COST = 250;

	/**
	 * The name of the station.
	 */
	private final String name;

	/**
	 * True if the ship is a battle station.
	 */
	private final boolean battle;

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
	 * Creates a station owned by the given faction at the given location.
	 *
	 * @param location the location of the station
	 * @param faction  the faction the station belongs to
	 */
	public Station(SectorLocation location, Faction faction)
	{
		this.location = location;
		this.faction = faction;
		ships = new LinkedList<>();
		battle = Main.rng.nextBoolean();

		String testName;
		do {
			testName = (
				location.getSector().getStar().getName()
				+ " "
				+ Main.rng.getRandomElement(battle ? NAME_BATTLE : NAME_TRADE)
			);
		} while (location.getSector().getStation(testName) != null);

		name = testName;

		cloneItems();
		generatePrices();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public ColorString toColorString()
	{
		return new ColorString(
				toString(),
				isClaimed() ? getFaction().getColor() : null
			);
	}

	/**
	 * Gets the name of the station.
	 *
	 * @return the name of the station
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Returns true if the station is a battle station.
	 *
	 * @return true if the station is a battle station
	 */
	public boolean isBattle()
	{
		return battle;
	}

	/**
	 * Gets the location of the station.
	 *
	 * @return the location of the station
	 */
	public SectorLocation getLocation()
	{
		return location;
	}

	/**
	 * Gets the faction that owns the station.
	 *
	 * @return the faction that owns the station
	 */
	public Faction getFaction()
	{
		return faction;
	}

	/**
	 * Returns true if the station is claimed by a faction.
	 *
	 * @return true if the station is claimed by a faction
	 */
	public boolean isClaimed()
	{
		return faction != null;
	}

	/**
	 * Claims the celestial body for the specified faction.
	 *
	 * @param faction the faction that will claim the celestial body
	 */
	public void claim(Faction faction)
	{
		if (this.faction == faction) {
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
		return new ColorChar(
				battle
				? Symbol.BATTLE_STATION.get()
				: Symbol.TRADE_STATION.get(),
				getFaction().getColor()
			);
	}

	/**
	 * Returns true if the station sells the specified module.
	 *
	 * @param module the module to check
	 * @return true if the station sells the module
	 */
	public boolean sells(Module module)
	{
		if (module == null || module.getName() == null) {
			return false;
		}

		if (module.isBattle()) {
			return battle;
		}

		return !battle;
	}

	/**
	 * Gets all resources sold by the station.
	 *
	 * @return all resources sold by the station
	 */
	public BaseResource[] getResources()
	{
		return resources;
	}

	/**
	 * Gets all modules sold by the station
	 *
	 * @return all modules sold by the station
	 */
	public Module[] getModules()
	{
		return modules;
	}

	/**
	 * Returns true if the station has any item with the specified name.
	 *
	 * @param name the name of the item to find
	 * @return true if a search for any type of item of this name does not
	 *         return null
	 */
	public boolean isItem(String name)
	{
		return hasModule(name) || hasResource(name) || hasExpander(name);
	}

	/**
	 * Returns a module on the station with the specified name.
	 *
	 * @param name the name of the module to find
	 * @return the module with the specified name, null if not found
	 */
	public Module getModule(String name)
	{
		for (Module module : modules) {
			if (name.equalsIgnoreCase(module.getName())) {
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
	{
		return getModule(name) != null;
	}

	/**
	 * Returns the default module with the specified name.
	 *
	 * @param name the name of the base module to find
	 * @return the base module with the specified name, null if not found
	 */
	public static Module getBaseModule(String name)
	{
		for (Module module : MODULES) {
			if (name.equalsIgnoreCase(module.getName())) {
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
	{
		return getBaseModule(name) != null;
	}

	/**
	 * Returns the default weapon with the specified name.
	 *
	 * @param name the name of the base weapon to find
	 * @return the base weapon with the specified name, null if not found
	 */
	public static Weapon getBaseWeapon(String name)
	{
		for (Module module : MODULES) {
			if (name.equalsIgnoreCase(module.getName()) && module instanceof Weapon) {
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
		for (Module module : MODULES) {
			if (name.equalsIgnoreCase(module.getName()) && module instanceof Weapon) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets the first item with the given name.
	 *
	 * @param name the name to look for
	 * @return the first item found with the given name, null if not found
	 */
	public Item getItem(String name)
	{
		if (hasResource(name)) {
			return getResource(name);
		} else if (hasExpander(name)) {
			return getExpander(name);
		} else {
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
		for (BaseResource resource : resources) {
			if (name.equalsIgnoreCase(resource.getName())) {
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
	{
		return getResource(name) != null;
	}

	/**
	 * Returns the expander with the specified name.
	 *
	 * @param name the name of the expander to find
	 * @return the expander with the specified name, null if not found
	 */
	public Expander getExpander(String name)
	{
		for (BaseResource resource : resources) {
			if (name.equalsIgnoreCase(resource.getExpander().getName())) {
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
	{
		return getExpander(name) != null;
	}

	/**
	 * Returns the default module with the specified name.
	 *
	 * @param name the name of the base resource to find
	 * @return the base resource with the specified name, null if not found
	 */
	public static BaseResource getBaseResource(String name)
	{
		for (BaseResource resource : RESOURCES) {
			if (name.equalsIgnoreCase(resource.getName())) {
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
	{
		return getBaseResource(name) != null;
	}

	/**
	 * Finds an object with the specified name and calls its define method.
	 *
	 * @param name the name of the object to define
	 * @return true if the object was found and defined
	 */
	public static boolean define(String name)
	{
		for (Module module : MODULES) {
			if (name.equalsIgnoreCase(module.getName())) {
				module.define();
				return true;
			}
		}

		for (BaseResource resource : RESOURCES) {
			if (name.equalsIgnoreCase(resource.getName())) {
				resource.define();
				return true;
			}

			if (name.equalsIgnoreCase(resource.getExpander().getName())) {
				resource.getExpander().define();
				return true;
			}
		}

		// No matching object was found, so return false
		return false;
	}

	/**
	 * Gets the ships docked with the station.
	 *
	 * @return the ships
	 */
	public List<Ship> getShips()
	{
		return ships;
	}

	/**
	 * Returns the number of ships that belong to a specified faction.
	 *
	 * @param faction the faction to count ships in
	 * @return the number of ships docked with the station that belong to the
	 *         faction
	 */
	public int getNShips(Faction faction)
	{
		int nShips = 0;

		for (Ship ship : ships) {
			if (ship.getFaction() == faction) {
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

		for (int i = 0; i < RESOURCES.length; i++) {
			copy[i] = new Resource(RESOURCES[i]);
		}

		return copy;
	}

	/**
	 * Randomly generates the prices of each module and resource.
	 */
	private void generatePrices()
	{
		for (Module module : modules) {
			module.generatePrice();
		}

		for (BaseResource resource : resources) {
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

		for (Module module : MODULES) {
			if (sells(module)) {
				if (module instanceof Weapon) {
					moduleList.add(new Weapon((Weapon) module));
				} else {
					moduleList.add(new Module(module));
				}
			}
		}

		modules = moduleList.toArray(new Module[moduleList.size()]);
		resources = new BaseResource[RESOURCES.length];

		for (int i = 0; i < RESOURCES.length; i++) {
			resources[i] = new BaseResource(RESOURCES[i]);
		}
	}
}
