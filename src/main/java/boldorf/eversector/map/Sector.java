package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.Symbol;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.locations.Location;
import boldorf.eversector.locations.SectorLocation;
import boldorf.eversector.ships.Levels;
import boldorf.eversector.ships.Ship;
import boldorf.util.Utility;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.rng;

/**
 * A location on the map, possibly containing a star system.
 *
 * @author Boldorf Smokebane
 */
public class Sector
{
    /**
     * The maximum number of planets that will be generated.
     *
     * @see #generatePlanets()
     */
    private static final int MAX_PLANETS = 10;

    /**
     * The maximum number of stations that will be generated.
     *
     * @see #generateStations()
     */
    private static final int MAX_STATIONS = 3;

    /**
     * The minimum number of ships that will be allowed to exist in station systems. If the amount of ships drops below
     * this number, more will be spawned from a random station.
     *
     * @see Galaxy#nextTurn() Galaxy#nextTurn()
     */
    public static final int MIN_SHIPS = 4;

    /**
     * The location of the sector on the map.
     */
    private final Location location;

    /**
     * The star in the sector. Null if no star is present.
     */
    private Star star;

    /**
     * The nebula in the sector. Null if no nebula is present.
     */
    private Nebula nebula;

    /**
     * The dominant faction in this sector. Manually updated when territory is claimed.
     *
     * @see #updateFaction()
     */
    private Faction faction;

    /**
     * All planets in the sector. Each index represents an orbit in the sector. If an index is null, there is no planet
     * at that orbit. If there is no star, this array will have a length of 0.
     */
    private Planet[] planets;

    /**
     * All stations in the sector. Each index represents an orbit in the sector. If an index is null, there is no
     * station at that orbit. If there is no star, this array will have a length of 0.
     */
    private Station[] stations;

    /**
     * A list of all ships in the sector, excluding those on planets or at stations. Each ship must register when they
     * enter the sector.
     *
     * @see Ship#setLocation(Location)
     */
    private List<Ship> ships;

    /**
     * Creates a sector from a location and nebula.
     *
     * @param location the sector's location
     * @param nebula   the nebula in this sector
     */
    public Sector(Location location, Nebula nebula)
    {
        this.location = location;
        this.nebula = nebula;
        ships = new LinkedList<>();
    }

    /**
     * Generates the star, planets, and stations.
     */
    public void init()
    {
        double chance = 0.2 + Math.min(0.7, 1.0 / location.getCoord().distance(location.getGalaxy().getCenter()));

        if (Utility.getChance(rng, chance))
        {
            star = Star.generate(nebula);

            while (location.getGalaxy().getStarNames().contains(star.getName()))
            {
                star.setName(Star.generateName());
            }

            planets = new Planet[star.getMass()];
            stations = new Station[star.getMass()];

            generatePlanets();

            if (rng.nextBoolean())
            {
                generateStations();
                generateShips(rng.nextInt(MIN_SHIPS * 2) + MIN_SHIPS);
            }
            else
            {
                generateShips(rng.nextInt(MIN_SHIPS + 1));
            }

            updateFaction();
        }
        else
        {
            planets = new Planet[0];
            stations = new Station[0];
        }
    }

    @Override
    public String toString()
    {
        return isEmpty() ? "Empty Sector" : star.getName();
    }

    /**
     * Gets the location of the sector.
     *
     * @return the sector's location
     */
    public Location getLocation()
    {
        return location;
    }

    /**
     * Gets the dominant faction in the sector.
     *
     * @return the sector's faction
     */
    public Faction getFaction()
    {
        return faction;
    }

    /**
     * Gets the star in the sector.
     *
     * @return the sector's star
     */
    public Star getStar()
    {
        return star;
    }

    /**
     * Gets the nebula in the sector.
     *
     * @return the sector's nebula
     */
    public Nebula getNebula()
    {
        return nebula;
    }

    /**
     * Returns true if there is a dominant faction in the sector.
     *
     * @return true if there is a dominant faction in the sector
     */
    public boolean isClaimed()
    {
        return faction != null;
    }

    /**
     * Returns true if there is a nebula in the sector.
     *
     * @return true if there is a nebula in the sector
     */
    public boolean hasNebula()
    {
        return nebula != null;
    }

    /**
     * Gets the number of orbits in the sector.
     *
     * @return the number of orbits in the sectors
     */
    public int getOrbits()
    {
        return star == null ? 0 : star.getMass();
    }

    /**
     * Returns true if there is no star in the sector.
     *
     * @return true if there is no star in the sector
     */
    public boolean isEmpty()
    {
        return star == null;
    }

    /**
     * Returns true if there is at least one planet in the sector.
     *
     * @return true if there is at least one planet in the sector
     */
    public boolean hasPlanets()
    {
        if (isEmpty())
        {
            return false;
        }

        for (Planet planet : planets)
        {
            if (planet != null)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if there is at least one station in the sector.
     *
     * @return true if there is at least one station in the sector
     */
    public boolean hasStations()
    {
        if (isEmpty())
        {
            return false;
        }

        for (Station station : stations)
        {
            if (station != null)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the dominant faction in the sector, based on their control of claimable bodies.
     */
    public final void updateFaction()
    {
        if (isEmpty())
        {
            return;
        }

        Galaxy galaxy = location.getGalaxy();
        int[] control = new int[galaxy.getFactions().length];

        // Increase the respective counter for each claimed body
        for (Planet planet : planets)
        {
            if (planet != null && planet.isClaimed())
            {
                control[galaxy.getIndex(planet.getFaction())]++;
            }
        }

        for (Station station : stations)
        {
            if (station != null && station.isClaimed())
            {
                control[galaxy.getIndex(station.getFaction())]++;
            }
        }

        int index = -1;
        int maxBodies = 0; // The most owned bodies in a faction

        for (int i = 0; i < control.length; i++)
        {
            if (control[i] > maxBodies)
            {
                maxBodies = control[i];
                index = i;
            }
            else if (control[i] == maxBodies)
            {
                // Set the index to an invalid value so that it is known that
                // there is a tie, but so that it can also be easily overwritten
                index = -1;
            }
        }

        // There was a tie in control, so no faction rules this sector
        if (index == -1)
        {
            faction = null;
            return;
        }

        faction = galaxy.getFactions()[index];
    }

    /**
     * Gets the number of planets controlled by the given faction.
     *
     * @param faction the faction to check
     * @return the number of planets controlled by the given faction
     */
    public int getPlanetsControlledBy(Faction faction)
    {
        int planetsClaimed = 0;

        for (Planet planet : planets)
        {
            if (planet != null && planet.getFaction() == faction)
            {
                planetsClaimed++;
            }
        }

        return planetsClaimed;
    }

    /**
     * Gets the number of stations controlled by the given faction.
     *
     * @param faction the faction to check
     * @return the number of stations controlled by the given faction
     */
    public int getStationsControlledBy(Faction faction)
    {
        int stationsClaimed = 0;

        for (Station station : stations)
        {
            if (station != null && station.getFaction() == faction)
            {
                stationsClaimed++;
            }
        }

        return stationsClaimed;
    }

    /**
     * Gets the number of stations of the given type controlled by the given faction.
     *
     * @param faction the faction to check
     * @param battle  true if the type of station to check for is battle
     * @return the number of stations of the given type controlled by the given faction
     */
    public int getStationTypesControlledBy(Faction faction, boolean battle)
    {
        int stationsClaimed = 0;

        for (Station station : stations)
        {
            if (station != null && station.getFaction() == faction && station.isBattle() == battle)
            {
                stationsClaimed++;
            }
        }

        return stationsClaimed;
    }

    /**
     * Returns the symbol that represents the sector's type.
     *
     * @return the symbol that represents the sector's type
     */
    public ColorChar getTypeSymbol()
    {
        if (isEmpty())
        {
            return Symbol.empty();
        }

        if (hasStations())
        {
            for (Station station : stations)
            {
                if (station != null && station.isBattle())
                {
                    return new ColorChar(Symbol.BATTLE_STATION.get());
                }
            }

            return new ColorChar(Symbol.TRADE_STATION.get());
        }

        if (hasPlanets())
        {
            return new ColorChar(star.getSymbol());
        }

        return new ColorChar(Symbol.UNDISCOVERED.get());
    }

    /**
     * Gets the sector's symbol, based on the player's presence or its contents.
     *
     * @return the sector's symbol as a ColorChar
     */
    public ColorChar getSymbol()
    {
        boolean playerHere = location.getGalaxy().getPlayer().getLocation().getSector() == this;

        char symbol;
        if (playerHere)
        {
            symbol = Symbol.player().getChar();
        }
        else
        {
            symbol = getTypeSymbol().getChar();
        }

        Color foreground;
        if (playerHere)
        {
            foreground = Symbol.player().getForeground();
        }
        else if (isClaimed())
        {
            foreground = faction.getColor();
        }
        else if (isEmpty())
        {
            foreground = Symbol.empty().getForeground();
        }
        else
        {
            foreground = null;
        }

        Color background;
        if (hasNebula())
        {
            background = nebula.getColor();
        }
        else
        {
            background = null;
        }

        return new ColorChar(symbol, foreground, background);
    }

    /**
     * Gets the symbol of the sector's star, or the player's symbol if they are in the sector.
     *
     * @return the sector's star symbol
     */
    public ColorChar getStarSymbol()
    {
        ColorChar symbol;

        if (location.getGalaxy().getPlayer().getLocation().getSector() == this)
        {
            symbol = Symbol.player();
        }
        else if (star == null)
        {
            symbol = Symbol.empty();
        }
        else
        {
            symbol = star.getSymbol();
        }

        ColorChar copy = new ColorChar(symbol);
        copy.setBackground(hasNebula() ? nebula.getColor() : null);
        return copy;
    }

    /**
     * Returns the number of ships in the sector, including those on its planets and in its stations.
     *
     * @return the total number of ships in the sector
     */
    public int getNShips()
    {
        int nShips = ships.size();

        for (Planet planet : planets)
        {
            if (planet != null)
            {
                nShips += planet.getNShips();
            }
        }

        for (Station station : stations)
        {
            if (station != null)
            {
                nShips += station.getShips().size();
            }
        }

        return nShips;
    }

    /**
     * Returns the number of ships in the sector that belong to a specified faction.
     *
     * @param faction the faction that ships will be counted in
     * @return the total number of ships in the sector that belong to the specified faction
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

        for (Planet planet : planets)
        {
            if (planet != null)
            {
                nShips += planet.getNShips(faction);
            }
        }

        for (Station station : stations)
        {
            if (station != null)
            {
                nShips += station.getNShips(faction);
            }
        }

        return nShips;
    }

    /**
     * Gets planets.
     *
     * @return the planets
     */
    public List<Planet> getPlanets()
    {
        List<Planet> planetList = new ArrayList<>();
        for (Planet planet : planets)
        {
            if (planet != null)
            {
                planetList.add(planet);
            }
        }
        return planetList;
    }

    /**
     * Gets stations.
     *
     * @return the stations
     */
    public List<Station> getStations()
    {
        List<Station> stationList = new ArrayList<>();
        for (Station station : stations)
        {
            if (station != null)
            {
                stationList.add(station);
            }
        }
        return stationList;
    }

    /**
     * Returns the planet at the specified orbit.
     *
     * @param orbit the orbit of the planet, must be a valid orbit (between 1 and 10)
     * @return the planet at the specified orbit, null if invalid orbit
     */
    public Planet getPlanetAt(int orbit)
    {
        return isValidOrbit(orbit) ? planets[orbit - 1] : null;
    }

    /**
     * Returns the station at the specified orbit.
     *
     * @param orbit the orbit of the station, must be a valid orbit (between 1 and 10)
     * @return the planet at the specified orbit, null if invalid orbit
     */
    public Station getStationAt(int orbit)
    {
        return isValidOrbit(orbit) ? stations[orbit - 1] : null;
    }

    /**
     * Returns true if there is a planet at the specified orbit.
     *
     * @param orbit the orbit to check for planets
     * @return true if a search for a planet in the orbit does not return null
     */
    public boolean isPlanetAt(int orbit)
    {
        return getPlanetAt(orbit) != null;
    }

    /**
     * Returns true if there is a station at the specified orbit.
     *
     * @param orbit the orbit to check for stations
     * @return true if a search for a station in the orbit does not return null
     */
    public boolean isStationAt(int orbit)
    {
        return getStationAt(orbit) != null;
    }

    /**
     * Returns the station in the sector with the given name, null if not found.
     *
     * @param name the name to search for
     * @return the station found in the sector with the given name, null if not found
     */
    public Station getStation(String name)
    {
        for (Station station : stations)
        {
            if (station != null && station.getName().equals(name))
            {
                return station;
            }
        }
        return null;
    }

    /**
     * Randomly generates the planets and their number.
     */
    private void generatePlanets()
    {
        if (isEmpty())
        {
            planets = new Planet[0];
            return;
        }

        // The (... - 1) + 2 is to ensure at least one planet
        for (int i = 0; i < rng.nextInt(Math.min(MAX_PLANETS, star.getMass()) - 1) + 2; i++)
        {
            int j;
            do
            {
                j = rng.nextInt(star.getMass());
            } while (planets[j] != null);

            planets[j] = new Planet(i + 1, new SectorLocation(getLocation(), j + 1));
            planets[j].init();
        }
    }

    /**
     * Randomly generates the stations and their number.
     */
    private void generateStations()
    {
        if (isEmpty())
        {
            stations = new Station[0];
            return;
        }

        // The (...) + 1  is to ensure at least 1 station
        // Power is divided by 2 to avoid overpopulating small sectors
        for (int i = 0; i < rng.nextInt(Math.min(MAX_STATIONS, star.getMass() / 2)) + 1; i++)
        {
            int j;
            do
            {
                j = rng.nextInt(star.getMass());
            } while (stations[j] != null);

            // There is no need to do a check for if this is a station system,
            // because stations would not otherwise be generated
            stations[j] = new Station(new SectorLocation(getLocation(), j + 1),
                    location.getGalaxy().getRandomFaction());
        }
    }

    /**
     * Randomly generates any ships and their number.
     *
     * @param nShips the number of ships to generate
     */
    private void generateShips(int nShips)
    {
        ships = new LinkedList<>();

        for (int i = 0; i < nShips; i++)
        {
            Ship ship = new Ship(new SectorLocation(location, rng.nextInt(star.getMass()) + 1),
                    location.getGalaxy().getRandomFaction());
            ships.add(ship);
            location.getGalaxy().getShips().add(ship);
        }
    }

    /**
     * Returns the first ship found with the given name.
     *
     * @param name the name of the ship to find
     * @return the first ship found with the given name, null if not found
     */
    public Ship getShip(String name)
    {
        for (Ship ship : ships)
        {
            if (ship != null && (name.equalsIgnoreCase(ship.getName()) || name.equalsIgnoreCase(ship.toString())))
            {
                return ship;
            }
        }

        return null;
    }

    /**
     * Gets ships.
     *
     * @return the ships
     */
    public List<Ship> getShips()
    {
        return ships;
    }

    /**
     * Returns an array of all ships at a given orbit.
     *
     * @param orbit the orbit to find ships at
     * @return an array of ships that are at the orbit
     */
    public List<Ship> getShipsAt(int orbit)
    {
        List<Ship> shipsAtOrbit = new LinkedList<>();

        for (Ship ship : ships)
        {
            if (ship != null && ship.getSectorLocation().getOrbit() == orbit)
            {
                shipsAtOrbit.add(ship);
            }
        }

        return shipsAtOrbit;
    }

    /**
     * Returns the orbit of a randomly-picked station.
     *
     * @return the orbit of a random station, -1 if no stations were found
     */
    public int getRandomStationOrbit()
    {
        int nStations = 0;

        for (Station station : stations)
        {
            if (station != null)
            {
                nStations++;
            }
        }

        if (nStations == 0)
        {
            return -1;
        }

        int[] stationOrbits = new int[nStations];

        int nextSlot = 0;
        for (int i = 0; i < stations.length; i++)
        {
            if (stations[i] != null)
            {
                stationOrbits[nextSlot] = i + 1;
                nextSlot++;
            }
        }

        return stationOrbits[rng.nextInt(stationOrbits.length)];
    }

    /**
     * Gets symbols for orbit.
     *
     * @param orbit the orbit
     * @return the symbols for orbit
     */
    public ColorString getSymbolsForOrbit(int orbit)
    {
        ColorString symbols = new ColorString();
        int orbitIndex = orbit - 1;
        int nShips = getShipsAt(orbit).size();

        boolean playerIsHere;
        Location playerLocation = location.getGalaxy().getPlayer().getLocation();
        if (playerLocation instanceof SectorLocation)
        {
            SectorLocation sectorLocation = (SectorLocation) playerLocation;
            playerIsHere = sectorLocation.getOrbit() == orbit;
        }
        else
        {
            playerIsHere = false;
        }

        if (stations[orbitIndex] != null)
        {
            symbols.add(stations[orbitIndex].getSymbol());
        }
        else
        {
            symbols.add(Symbol.empty());
        }

        if (playerIsHere)
        {
            symbols.add(Symbol.player());
        }
        else if (nShips > 0)
        {
            // Calculate the highest level of ship at this orbit
            int highestLevel = 0;
            boolean isCommonFaction = true;
            Faction commonFaction = null;
            for (Ship ship : ships)
            {
                if (ship.getSectorLocation().getOrbit() == orbit && !ship.isPlayer())
                {
                    if (ship.getHighestLevel() > highestLevel)
                    {
                        highestLevel = ship.getHighestLevel();
                    }

                    if (commonFaction == null)
                    {
                        commonFaction = ship.getFaction();
                    }
                    else
                    {
                        isCommonFaction = ship.getFaction() == commonFaction;
                    }
                }
            }

            Symbol symbol;
            // Print the corresponding symbol to the highest level
            switch (highestLevel / Levels.LEVEL_AMOUNT)
            {
                case 0:
                case 1:
                    symbol = Symbol.WEAK_SHIP;
                    break;
                case 2:
                    symbol = Symbol.MEDIUM_SHIP;
                    break;
                default:
                    symbol = Symbol.STRONG_SHIP;
                    break;
            }

            if (isCommonFaction && commonFaction != null)
            {
                symbols.add(new ColorChar(symbol.get(), commonFaction.getColor()));
            }
            else
            {
                symbols.add(new ColorChar(symbol.get()));
            }
        }
        else
        {
            symbols.add(Symbol.empty());
        }

        if (planets[orbitIndex] != null)
        {
            symbols.add(planets[orbitIndex].getSymbol());
        }
        else
        {
            symbols.add(Symbol.empty());
        }

        return symbols;
    }

    /**
     * Gets orbit contents.
     *
     * @param orbit the orbit
     * @return the orbit contents
     */
    public List<ColorString> getOrbitContents(int orbit)
    {
        List<ColorString> contents = new LinkedList<>();
        if (planets[orbit - 1] != null)
        {
            contents.add(planets[orbit - 1].toColorString());
        }

        if (stations[orbit - 1] != null)
        {
            contents.add(stations[orbit - 1].toColorString());
        }

        int notShown = 0;
        for (Ship ship : ships)
        {
            if (ship != null && ship.getSectorLocation().getOrbit() == orbit && !ship.isPlayer())
            {
                if (contents.size() >= Star.StarMass.getLargest().getMass())
                {
                    notShown++;
                    break;
                }

                ColorString shipString = ship.toColorString();
                if (ship.isLeader())
                {
                    shipString.add(new ColorString(" (Leader)", COLOR_FIELD));
                }
                contents.add(shipString);
            }
        }

        if (notShown > 0)
        {
            // (notShown + 1) accounts for the replaced line
            contents.set(11, new ColorString("(" + (notShown + 1) + " more)", AsciiPanel.brightBlack));
        }

        return contents;
    }

    /**
     * Returns true if a specified orbit is valid, meaning it ranges between 1 and the constant number of orbits.
     *
     * @param orbit the orbit to validate
     * @return true if the orbit is between 1 and the constant number of orbits
     */
    public boolean isValidOrbit(int orbit)
    {
        return !isEmpty() && orbit >= 1 && orbit <= star.getMass();
    }

    /**
     * Returns true if there are any planets or stations in this sector that can be claimed.
     *
     * @return true if there are any rocky planets or stations in the sector
     */
    public boolean hasClaimableTerritory()
    {
        if (hasStations())
        {
            return true;
        }

        for (Planet planet : planets)
        {
            if (planet != null && planet.getType().canLandOn())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a random name to be used in sectors, and it must be final so that it cannot modify sector's
     * constructor. This allows for 67,600 possible designations, enough for a grid of 260x260 sectors.
     *
     * @return a String consisting of two characters, a hyphen, and two numbers
     */
    public static String generateName()
    {
        char c = (char) (rng.nextInt(25) + 65);
        char d = (char) (rng.nextInt(25) + 65);
        int i = rng.nextInt(10);
        int j = rng.nextInt(10);
        return c + "" + d + "-" + i + "" + j;
    }

    /**
     * Scans through the list of ships, removing ones that are on planets and stations.
     */
    public void resetDuplicateShips()
    {
        boolean shipReset;

        do
        {
            shipReset = false;

            for (Ship ship : ships)
            {
                if (ship.isLanded() || ship.isDocked())
                {
                    ships.remove(ship);
                    shipReset = true;
                    break;
                }
            }
        } while (shipReset);
    }
}