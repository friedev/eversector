package maugrift.eversector.map;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorChar;
import maugrift.apwt.glyphs.ColorString;
import maugrift.eversector.Symbol;
import maugrift.eversector.actions.Action;
import maugrift.eversector.actions.Dock;
import maugrift.eversector.faction.Faction;
import maugrift.eversector.faction.Relationship;
import maugrift.eversector.locations.Location;
import maugrift.eversector.locations.SectorLocation;
import maugrift.eversector.ships.Ship;
import maugrift.eversector.Main;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidmath.Coord;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A 2D array of sectors representing a galaxy.
 *
 * @author Aaron Friesen
 */
public class Galaxy
{
	/**
	 * The default number of sectors on each side of the central sector.
	 */
	private static final int MIN_RADIUS = 25;

	/**
	 * The possible range of sectors on each side of the central sector.
	 */
	private static final int RADIUS_RANGE = 25;

	/**
	 * The minimum number of faction that will be present in the game.
	 */
	private static final int MIN_FACTIONS = 2;

	/**
	 * The maximum increase in factions over the minimum.
	 */
	private static final int FACTION_RANGE = 4;

	/**
	 * The fewest types of ore that can exist.
	 */
	private static final int MIN_ORE = 4;

	/**
	 * The range of possible amounts of ore types over the minimum.
	 */
	private static final int ORE_RANGE = 3;

	/**
	 * The size of each nebula generated, in sectors.
	 */
	private static final int NEBULA_SIZE = 100;

	/**
	 * The number of turns that are simulated before the galaxy is used.
	 */
	public static final int SIMULATED_TURNS = 50;

	/**
	 * The frequency at which to update a relationship between factions, to be
	 * divided by the number of factions.
	 */
	private static final int RELATION_UPDATE_FREQ = 120;

	/**
	 * The amount of tries that can be made to update a relationship before the
	 * update is skipped.
	 */
	private static final int MAX_RELATIONSHIP_UPDATE_TRIES = 5;

	/**
	 * The frequency at which to elect for new faction leaders, in turns.
	 */
	private static final int ELECTION_FREQ = 150;

	/**
	 * The frequency at which to fade ship reputations toward zero, in turns.
	 */
	private static final int REPUTATION_FADE_FREQ = 4;

	/**
	 * The sectors in the galaxy.
	 */
	private Sector[][] sectors;

	/**
	 * The player.
	 */
	private Ship player;

	/**
	 * All NPC ships in the galaxy.
	 */
	private List<Ship> ships;

	/**
	 * The factions in the galaxy.
	 */
	private Faction[] factions;

	/**
	 * The types of ore in the galaxy.
	 */
	private Ore[] oreTypes;

	/**
	 * The number of turns that have passed since the start of the game.
	 */
	private int turn;

	/**
	 * The names of all star system names in the galaxy, to avoid duplication.
	 */
	private List<String> starNames;

	/**
	 * The names of all ships in the galaxy, to avoid duplication.
	 */
	private List<String> shipNames;

	/**
	 * Generates a galaxy with the default size.
	 */
	public Galaxy()
	{
		this(MIN_RADIUS + Main.rng.nextInt(RADIUS_RANGE));
	}

	/**
	 * Generates a galaxy of a specified size.
	 *
	 * @param size the side length of the galaxy in sectors
	 */
	public Galaxy(int size)
	{
		sectors = new Sector[size * 2 + 1][size * 2 + 1];
		ships = new LinkedList<>();
		factions = new Faction[Main.rng.nextInt(FACTION_RANGE) + MIN_FACTIONS];
		oreTypes = generateOreTypes();
		turn = -SIMULATED_TURNS;
		starNames = new LinkedList<>();
		shipNames = new LinkedList<>();

		// Factions must be created first so they can be assigned to ships
		createFactions();
		init();
	}

	/**
	 * Returns the array of sectors in the galaxy.
	 *
	 * @return the array of sectors in the galaxy
	 */
	public Sector[][] getSectors()
	{
		return sectors;
	}

	/**
	 * Gets the ships in the galaxy.
	 *
	 * @return a list of all ships in the galaxy
	 */
	public List<Ship> getShips()
	{
		return ships;
	}

	/**
	 * Gets the player.
	 *
	 * @return the player
	 */
	public Ship getPlayer()
	{
		return player;
	}

	/**
	 * Gets the number of turns that have passed.
	 *
	 * @return the number of turns that have passed
	 */
	public int getTurn()
	{
		return turn;
	}

	/**
	 * Gets the width of the galaxy in sectors.
	 *
	 * @return the width of the galaxy in sectors
	 */
	public int getWidth()
	{
		return sectors[0].length;
	}

	/**
	 * Gets the height of the galaxy in sectors.
	 *
	 * @return the height of the galaxy in sectors
	 */
	public int getHeight()
	{
		return sectors.length;
	}

	/**
	 * Gets the coordinates of the galactic center.
	 *
	 * @return the coordinates of the galactic center
	 */
	public Coord getCenter()
	{
		return Coord.get(getWidth() / 2, getHeight() / 2);
	}

	/**
	 * Returns the sector at the given coordinates.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the sector at the given coordinates
	 */
	public Sector sectorAt(int x, int y)
	{
		return sectors[y][x];
	}

	/**
	 * Returns the sector at the given coordinates.
	 *
	 * @param p the coordinates
	 * @return the sector at the given coordinates
	 */
	public Sector sectorAt(Coord p)
	{
		return p == null ? null : sectorAt(p.x, p.y);
	}

	/**
	 * Returns true if the specified coordinates are in the galaxy.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return true if the coordinates correspond with coordinates on the map
	 */
	public boolean contains(int x, int y)
	{
		return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
	}

	/**
	 * Returns true if the specified coordinates are in the galaxy.
	 *
	 * @param p the coordinates
	 * @return true if the coordinates correspond with coordinates on the map
	 */
	public boolean contains(Coord p)
	{
		return contains(p.x, p.y);
	}

	/**
	 * Gets the factions in the galaxy.
	 *
	 * @return the factions in the galaxy
	 */
	public Faction[] getFactions()
	{
		return factions;
	}

	/**
	 * Gets the faction with the given name.
	 *
	 * @param name the name of the faction
	 * @return the faction with the given name
	 */
	public Faction getFaction(String name)
	{
		for (Faction faction : factions) {
			if (faction.getName().equals(name)) {
				return faction;
			}
		}
		return null;
	}

	/**
	 * Returns the index of the given faction.
	 *
	 * @param faction the faction to find the index of
	 * @return the index of the given faction in the faction array
	 */
	public int getIndex(Faction faction)
	{
		for (int i = 0; i < factions.length; i++) {
			if (factions[i] == faction) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Returns a random faction in the galaxy.
	 *
	 * @return a random faction in the galaxy
	 */
	public final Faction getRandomFaction()
	{
		return factions[Main.rng.nextInt(factions.length)];
	}

	/**
	 * Returns a random faction that is not the specified one.
	 *
	 * @param faction the faction to exclude from the list of selections
	 * @return a random faction from the list of all factions as long as it is
	 * not the one provided
	 */
	public final Faction getRandomFaction(Faction faction)
	{
		Faction randomFaction;
		do {
			randomFaction = getRandomFaction();
		} while (randomFaction == faction);
		return randomFaction;
	}

	/**
	 * Returns a random relationship between two factions.
	 *
	 * @return a random relationship between two randomly-chosen factions
	 */
	public final Relationship getRandomRelationship()
	{
		Faction faction1 = getRandomFaction();
		Faction faction2 = getRandomFaction(faction1);
		return faction1.getRelationshipObject(faction2);
	}

	/**
	 * Gets the types of ore in the galaxy.
	 *
	 * @return the types of ore in the galaxy
	 */
	public Ore[] getOreTypes()
	{
		return oreTypes;
	}

	/**
	 * Gets a random type of ore in the galaxy.
	 *
	 * @return a random type of ore in the galaxy
	 */
	public Ore getRandomOre()
	{
		return oreTypes[Main.rng.nextInt(oreTypes.length)];
	}

	/**
	 * Returns the names of all star systems.
	 *
	 * @return the names of all star systems
	 */
	public List<String> getStarNames()
	{
		return starNames;
	}

	/**
	 * Returns the names of all ships.
	 *
	 * @return the names of all ships
	 */
	public List<String> getShipNames()
	{
		return shipNames;
	}

	/**
	 * Sets the player to a designated ship.
	 *
	 * @param player the ship to become the player
	 */
	public void setPlayer(Ship player)
	{
		this.player = player;
	}

	/**
	 * Creates the player, the starting sector, and the player's faction.
	 */
	public void createNewPlayer()
	{
		SectorLocation location = new Location(
			this,
			getRandomStationSystem().getLocation().getCoord()
		).enterSector();
		location = location.setOrbit(
				location.getSector().getRandomStationOrbit()
			);

		Faction faction = location.getStation().getFaction();
		player = new Ship(location, faction);
		player.setName("Player");
		player.setAI(null);
	}

	/**
	 * Gets a random station system.
	 *
	 * @return a random station system
	 */
	public Sector getRandomStationSystem()
	{
		List<Sector> stationSystems = new LinkedList<>();
		for (Sector[] row : sectors) {
			for (Sector sector : row) {
				if (sector.hasStations()) {
					stationSystems.add(sector);
				}
			}
		}

		return Main.rng.getRandomElement(stationSystems);
	}

	/**
	 * Gets a random sector on the edge of the galaxy.
	 *
	 * @return a sector on the edge of the galaxy
	 */
	public Sector getRandomEdgeSector()
	{
		int edgeCoord = Main.rng.nextInt(sectors.length);
		if (Main.rng.nextBoolean()) {
			return Main.rng.nextBoolean()
				? sectors[0][edgeCoord]
				: sectors[sectors.length - 1][edgeCoord];
		}

		return Main.rng.nextBoolean()
			? sectors[edgeCoord][0]
			: sectors[edgeCoord][sectors.length - 1];
	}

	/**
	 * Gets the light resistance map for FOV calculations.
	 *
	 * @return the resistance map
	 */
	public double[][] getResistanceMap()
	{
		double[][] resistance = new double[sectors.length][sectors[0].length];
		for (int y = 0; y < sectors.length; y++) {
			for (int x = 0; x < sectors[y].length; x++) {
				resistance[x][y] = sectors[y][x].hasNebula() ? 1.0 : 0.0;
			}
		}

		return resistance;
	}

	/**
	 * Converts the galaxy into a List of ColorStrings for displaying.
	 *
	 * @param showStars if true, will show the star Symbol of sectors rather
	 *                  than their type Symbol
	 * @param cursor    the sector to show as selected
	 * @return the galaxy as a List of ColorStrings
	 */
	public List<ColorString> toColorStrings(boolean showStars, Coord cursor)
	{
		List<ColorString> output = new ArrayList<>(sectors.length);

		for (Sector[] row : sectors) {
			ColorString line = new ColorString();

			for (Sector sector : row) {
				ColorChar symbol = new ColorChar(showStars
					? sector.getStarSymbol()
					: sector.getSymbol()
				);
				if (sector.getLocation().getCoord().equals(cursor)) {
					symbol.setBackground(Main.COLOR_SELECTION_BACKGROUND);
				}
				line.add(symbol);
			}

			output.add(line);
		}

		return output;
	}

	/**
	 * Converts the FOV of a Ship into a List of ColorStrings for displaying.
	 *
	 * @param ship      the ship whose FOV will be used as the rendering range
	 * @param showStars if true, will show the star Symbol of sectors rather
	 *                  than their type Symbol
	 * @param cursor    the sector to show as selected
	 * @return the galaxy as a List of ColorStrings
	 */
	public List<ColorString> toColorStrings(
		Ship ship,
		boolean showStars,
		Coord cursor
	)
	{
		int fovRadius = (int) Math.floor(ship.getFOVRadius());
		LinkedList<ColorString> output = new LinkedList<>();

		for (int y = 0; y < fovRadius * 2 - 1; y++) {
			output.add(new ColorString());
			for (int x = 0; x < fovRadius * 2 - 1; x++) {
				output.getLast().add(Symbol.UNDISCOVERED.get());
			}
		}

		List<Coord> fov = ship.getFOV();

		for (Coord coord : fov) {
			ColorChar symbol = showStars
				? sectorAt(coord).getStarSymbol()
				: sectorAt(coord).getSymbol();

			if (sectorAt(coord).getLocation().getCoord().equals(cursor)) {
				symbol = new ColorChar(symbol);
				symbol.setBackground(Main.COLOR_SELECTION_BACKGROUND);
			}

			int x = coord.x - ship.getLocation().getCoord().x + fovRadius - 1;
			int y = coord.y - ship.getLocation().getCoord().y + fovRadius - 1;
			output.get(y).getCharacters().set(x, symbol);
		}

		return output;
	}

	/**
	 * Processes the next turn.
	 */
	public void nextTurn()
	{
		if (player != null) {
			player.updateContinuousEffects();
		}

		for (Ship ship : ships) {
			Action action = ship.getAI().act();
			if (action != null) {
				action.execute(ship);
			}
		}

		ships.removeIf(Ship::isDestroyed);

		for (Faction faction : factions) {
			faction.cacheAverageReputation();
		}

		for (Ship ship : ships) {
			ship.updateContinuousEffects();
			if (turn % REPUTATION_FADE_FREQ == 0) {
				ship.fadeReputations();
			}
		}

		if (player != null) {
			player.fadeReputations();
		}

		// Respawns ships if there are fewer than the minimum ships in a sector
		for (Sector[] row : sectors) {
			for (Sector sector : row) {
				if (sector.getNShips() < Sector.MIN_SHIPS && sector.hasStations()) {
					Station station = sector.getStationAt(sector.getRandomStationOrbit());

					// if (station.getFaction().changeEconomy(-Ship.BASE_VALUE))
					// {
					Ship newShip = new Ship(
						new SectorLocation(
							sector.getLocation(),
							station.getLocation().getOrbit()),
						station.getFaction()
					);
					new Dock().execute(newShip);
					ships.add(newShip);
					// }
				}

				// Only known way to fix duplicate ship bug
				sector.resetDuplicateShips();
			}
		}

		// Update relationships if there are more than two factions
		if (turn >= (RELATION_UPDATE_FREQ / factions.length) &&
			factions.length > 2 &&
			turn % (RELATION_UPDATE_FREQ / factions.length) == 0) {
			int tries = 0;
			do {
				tries++;
				if (tries > MAX_RELATIONSHIP_UPDATE_TRIES) {
					break;
				}
			} while (!getRandomRelationship().updateRelationship());
		}

		// Update faction leaders periodically, or immediately if destroyed
		// Also update faction leaders immediately before gameplay starts
		if (turn > 0) {
			updateFactionLeaders();
		} else if (turn == -1) {
			updateFactionLeaders();
		}

		turn++;
	}

	/**
	 * Holds scheduled elections for factions based on when their last election
	 * occurred, as well as for factions with destroyed leaders.
	 *
	 * @see #updateDestroyedFactionLeaders() #updateDestroyedFactionLeaders()
	 */
	public void updateFactionLeaders()
	{
		for (Faction faction : factions) {
			if (turn - faction.getLastElection() == ELECTION_FREQ) {
				faction.holdElection();
			}
		}

		updateDestroyedFactionLeaders();
	}

	/**
	 * Elects a new leader of each faction if their previous leader was
	 * destroyed.
	 */
	public void updateDestroyedFactionLeaders()
	{
		for (Faction faction : factions) {
			if (
				faction.getLeader() == null
				|| faction.getLeader().isDestroyed()
			) {
				faction.holdElection();
			}
		}
	}

	/**
	 * Initializes all the sectors in the galaxy.
	 */
	private void init()
	{
		char[][] level = new char[sectors.length + 2][sectors[0].length + 2];
		// Initialize all sectors to passable for nebula generation
		for (int i = 1; i < level.length - 1; i++) {
			for (int j = 1; j < level.length - 1; j++) {
				level[i][j] = '.';
			}
		}
		// Surround the map with walls so that MultiSpill doesn't go out of
		// bounds
		for (int i = 0; i < level.length; i++) {
			level[0][i] = '#';
			level[level.length - 1][0] = '#';
			level[i][0] = '#';
			level[i][level[i].length - 1] = '#';
		}

		MultiSpill nebulaGenerator = new MultiSpill(Main.rng).initialize(level);
		int nNebulae = sectors.length * sectors[0].length / NEBULA_SIZE / 2;

		List<Coord> entries = new ArrayList<Coord>();
		for (int i = 0; i < nNebulae; i++) {
			entries.add(Main.rng.nextCoord(sectors[0].length, sectors.length));
		}

		ArrayList<ArrayList<Coord>> nebulae = nebulaGenerator.start(
				entries,
				NEBULA_SIZE * entries.size(),
				null
			);

		Nebula[] nebulaTypes = new Nebula[nNebulae];
		for (int i = 0; i < nNebulae; i++) {
			nebulaTypes[i] = Main.rng.getRandomElement(Nebula.values());
		}

		for (int y = 0; y < sectors.length; y++) {
			for (int x = 0; x < sectors[y].length; x++) {
				Nebula nebula = null;
				for (int i = 0; i < nNebulae; i++) {
					List<Coord> tempNebula = nebulae.get(i);
					if (
						tempNebula != null &&
						tempNebula.contains(Coord.get(x, y))
					) {
						nebula = nebulaTypes[i];
						break;
					}
				}

				sectors[y][x] = new Sector(
					new Location(this, Coord.get(x, y)),
					nebula
				);
				sectors[y][x].init();
			}
		}
	}

	/**
	 * Creates the factions using constant names, and then generates the
	 * relationships among them.
	 */
	private void createFactions()
	{
		List<String> usedTypes = new LinkedList<>();

		for (int i = 0; i < factions.length; i++) {
			Color color;
			switch (i) {
			case 0:
				color = AsciiPanel.brightRed;
				break;
			case 1:
				color = AsciiPanel.brightCyan;
				break;
			case 2:
				color = AsciiPanel.brightGreen;
				break;
			case 3:
				color = AsciiPanel.brightYellow;
				break;
			case 4:
				color = AsciiPanel.brightMagenta;
				break;
			default:
				color = AsciiPanel.brightWhite;
				break;
			}
			factions[i] = new Faction(this, color);
		}

		// If there are only two factions, they will always be at war
		if (factions.length == 2) {
			new Relationship(
				factions[0],
				factions[1],
				Relationship.RelationshipType.WAR
			).addToFactions();
			return;
		}

		// Will pair up all factions with no duplicates
		for (int i = 0; i < factions.length; i++) {
			for (int j = i + 1; j < factions.length; j++) {
				new Relationship(factions[i], factions[j]).addToFactions();
			}
		}
	}

	/**
	 * Generates the types of ore in the galaxy.
	 *
	 * @return the array of ore types
	 */
	private Ore[] generateOreTypes()
	{
		Ore[] ores = new Ore[Math.min(
				MIN_ORE + Main.rng.nextInt(ORE_RANGE),
				Ore.DENSITY
			)];

		int totalDensity = 0;
		for (int i = 0; i < ores.length; i++) {
			String name;
			do {
				name = Main.rng.getRandomElement(Ore.NAME_PREFIX)
					+ Main.rng.getRandomElement(Ore.NAME_SUFFIX);

				// Ensure unique names
				for (Ore ore : ores) {
					if (name == null ||
						(ore != null && name.equals(ore.getName()))) {
						name = null;
					}
				}
			} while (name == null);

			int density;
			do {
				density = Main.rng.nextInt(Ore.DENSITY) + 1;

				// Ensure unique densities
				for (Ore ore : ores) {
					if (ore != null && density == ore.getDensity()) {
						density = 0;
					}
				}
			} while (density == 0);
			totalDensity += density;
			if (i == ores.length - 1 && totalDensity < Ore.DENSITY) {
				density = Math.min(
						Ore.DENSITY,
						density + Ore.DENSITY - totalDensity
					);
			}
			ores[i] = new Ore(name, density);
		}

		Arrays.sort(ores, Collections.reverseOrder());
		return ores;
	}
}
