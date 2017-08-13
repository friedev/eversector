package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.eversector.entities.Planet;
import boldorf.eversector.entities.Station;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Levels;
import boldorf.eversector.Main;
import boldorf.eversector.map.faction.Faction;
import static boldorf.eversector.Main.rng;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.SYMBOL_EMPTY;
import static boldorf.eversector.Main.SYMBOL_PLAYER;
import boldorf.eversector.entities.locations.Location;
import boldorf.eversector.entities.locations.SectorLocation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/** A location on the map, possibly containing a star or station. */
public class Sector
{
    public static final ColorChar
    SYMBOL_UNDISCOVERED   = new ColorChar(ExtChars.BLOCK_SHADE_1,
            AsciiPanel.brightBlack),
    SYMBOL_STAR_SYSTEM    = new ColorChar(ExtChars.STAR),
    SYMBOL_STATION_SYSTEM = new ColorChar('#'),
    SYMBOL_MANY_SHIPS     = new ColorChar('+');
    
    public static final char
    SYMBOL_WEAK_SHIP   = '>',
    SYMBOL_MEDIUM_SHIP = ExtChars.ARROW2_R,
    SYMBOL_STRONG_SHIP = ExtChars.SIGMA,
    SYMBOL_LEADER      = ExtChars.PHI_UPPER;
    
    /** The maximum number of planets that will be generated. */
    public static final int MAX_PLANETS = 10;
    
    /** The maximum number of stations that will be generated. */
    public static final int MAX_STATIONS = 3;
    
    /**
     * The minimum number of ships that will be allowed to exist in station
     * systems.
     */
    public static final int MIN_SHIPS = 4;
    
    private String        name;
    private String        nickname;
    private Star          star;
    private Location      location;
    private Faction       faction;
    private Planet[]      planets;
    private Station[]     stations;
    private List<Ship>    ships;
    private List<Integer> usedLetters;
    
    /**
     * Creates a sector from a location.
     * @param location the sector's location
     */
    public Sector(Location location)
    {
        // name generated below with generateName()
        nickname = Main.nameGenerator.generateName(2);
        this.location = location;
        ships = new LinkedList<>();
        usedLetters = new LinkedList<>();
        
        // Generate a name that isn't used
        do
        {
            name = generateName();
        } while (location.getMap().isUsed(name));
        location.getMap().addDesignation(name);
    }
    
    public void init()
    {
        if (rng.nextBoolean())
        {
            star = Star.generate();
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
        {return "Sector " + name;}
    
    public Location getLocation() {return location;       }
    public Faction  getFaction()  {return faction;        }
    public Star     getStar()     {return star;           }
    public boolean  isClaimed()   {return faction != null;}
    
    public int getOrbits()
        {return star == null ? 0 : star.getMass();}
    
    public boolean isEmpty()
        {return star == null;} 
    
    public boolean hasPlanets()
    {
        if (isEmpty())
            return false;
        
        for (Planet planet: planets)
            if (planet != null)
                return true;
        
        return false;
    }
    
    public boolean hasStations()
    {
        if (isEmpty())
            return false;
        
        for (Station station: stations)
            if (station != null)
                return true;
        
        return false;
    }
    
    public boolean isDiscovered()
        {return Main.sectorsDiscovered.contains(this);}
    
    /** Changes the sector's discovered status to true. */
    public void discover()
    {
        if (!isDiscovered())
            Main.sectorsDiscovered.add(this);
    }
    
    /**
     * Calculates the dominant faction in the sector, based on their control of
     * claimable bodies.
     */
    public final void updateFaction()
    {
        if (isEmpty())
            return;
        
        Map map = location.getMap();
        int[] control = new int[map.getFactions().length];
        
        // Increase the respective counter for each claimed body
        for (Planet planet: planets)
            if (planet != null && planet.isClaimed())
                control[map.getIndex(planet.getFaction())]++;
        
        for (Station station: stations)
            if (station != null && station.isClaimed())
                control[map.getIndex(station.getFaction())]++;
        
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
        
        String locationString = "(" + location.getCoord().x + ","
                + location.getCoord().y + ")";
        
        // There was a tie in control, so no faction rules this sector
        if (index == -1)
        {
            if (faction != null)
                faction.addNews(toString() + " " + locationString
                        + " has been lost.");
            
            faction = null;
            return;
        }
        
        Faction ruler = map.getFaction(index);
        
        if (faction != ruler)
        {
            if (map.getTurn() > -Map.SIMULATED_TURNS)
            {
                if (faction == null)
                {
                    ruler.addNews(toString() + " " + locationString
                            + " has been claimed.");
                }
                else
                {
                    ruler.addNews(new ColorString(toString() + " "
                            + locationString + " has been taken from the ")
                            .add(faction).add("."));
                }
            }
            
            if (faction != null)
            {
                faction.addNews(new ColorString(toString() + " "
                        + locationString + " has been lost to the ").add(ruler)
                        .add("."));
            }
        }
        
        faction = ruler;
    }
    
    public int getPlanetsControlledBy(Faction f)
    {
        int planetsClaimed = 0;
        
        for (Planet planet: planets)
            if (planet != null && planet.getFaction() == f)
                planetsClaimed++;
        
        return planetsClaimed;
    }
    
    public int getStationsControlledBy(Faction f)
    {
        int stationsClaimed = 0;
        
        for (Station station: stations)
            if (station != null && station.getFaction() == f)
                stationsClaimed++;
        
        return stationsClaimed;
    }
    
    public int getStationTypesControlledBy(Faction f, String type)
    {
        int stationsClaimed = 0;
        
        for (Station station: stations)
            if (station != null && station.getFaction() == f &&
                    station.getType().equals(type))
                stationsClaimed++;
        
        return stationsClaimed;
    }
    
    /**
     * Returns corresponding symbol that represents the sector's type.
     * @return the char from Symbols that represents the sector type
     */
    public ColorChar getTypeSymbol()
    {
        if (isEmpty())
            return SYMBOL_EMPTY;
        if (hasStations())
            return SYMBOL_STATION_SYSTEM;
        if (hasPlanets())
            return SYMBOL_STAR_SYSTEM;
        return SYMBOL_UNDISCOVERED;
    }
    
    /**
     * Gets the sector's symbol, based on its discovered status, the player's
     * presence, or its contents.
     * @return the sector's symbol as a character
     */
    public ColorChar getSymbol()
    {
        if (!isDiscovered())
            return SYMBOL_UNDISCOVERED;
        
        char symbol;
        if (location.getMap().getPlayer().getLocation().getSector() == this)
            symbol = SYMBOL_PLAYER.getChar();
        else
            symbol = getTypeSymbol().getChar();
        
        return isClaimed() ? new ColorChar(symbol, faction.getColor()) :
                new ColorChar(symbol);
    }
    
    public ColorChar getStarSymbol()
    {
        if (!isDiscovered())
            return SYMBOL_UNDISCOVERED;
        
        if (location.getMap().getPlayer().getLocation().getSector() == this)
            return SYMBOL_PLAYER;
        
        if (star == null)
            return SYMBOL_EMPTY;
        
        return star.getSymbol();
    }
    
    /**
     * Returns the number of ships in the sector, including those on its planets
     * and in its stations.
     * @return the total number of ships in the sector, will be non-negative
     */
    public int getNShips() 
    {
        int nShips = ships.size();
        
        for (Planet planet: planets)
            if (planet != null)
                nShips += planet.getNShips();
        
        for (Station station: stations)
            if (station != null)
                nShips += station.getShips().size();
        
        return nShips;
    }
    
    /**
     * Returns the number of ships in the sector that belong to a specified
     * faction.
     * @param faction the faction that ships will be counted in
     * @return the total number of ships in the sector that belong to the
     * specified faction, will be non-negative
     */
    public int getNShips(Faction faction)
    {
        int nShips = 0;
        
        for (Ship ship: ships)
            if (ship.getFaction() == faction)
                nShips++;
        
        for (Planet planet: planets)
            if (planet != null)
                nShips += planet.getNShips(faction);
        
        for (Station station: stations)
            if (station != null)
                nShips += station.getNShips(faction);
        
        return nShips;
    }
    
    public List<Planet> getPlanets()
    {
        List<Planet> planetList = new ArrayList<>();
        for (Planet planet: planets)
            if (planet != null)
                planetList.add(planet);
        return planetList;
    }
    
    public List<Station> getStations()
    {
        List<Station> stationList = new ArrayList<>();
        for (Station station: stations)
            if (station != null)
                stationList.add(station);
        return stationList;
    }
    
    /**
     * Returns the planet at the specified orbit.
     * @param orbit the orbit of the planet, must be a valid orbit (between 1
     * and 10)
     * @return the planet at the specified orbit, null if invalid orbit
     */
    public Planet getPlanetAt(int orbit)
        {return isValidOrbit(orbit) ? planets[orbit - 1] : null;}
    
    /**
     * Returns the station at the specified orbit.
     * @param orbit the orbit of the station, must be a valid orbit (between 1
     * and 10)
     * @return the planet at the specified orbit, null if invalid orbit 
     */
    public Station getStationAt(int orbit)
        {return isValidOrbit(orbit) ? stations[orbit - 1] : null;}
    
    /**
     * Returns true if there is a planet at the specified orbit.
     * @param orbit the orbit to check for planets
     * @return true if a search for a planet in the orbit does not return null
     */
    public boolean isPlanetAt(int orbit)
        {return getPlanetAt(orbit) != null;}
    
    /**
     * Returns true if there is a station at the specified orbit.
     * @param orbit the orbit to check for stations
     * @return true if a search for a station in the orbit does not return null
     */
    public boolean isStationAt(int orbit)
        {return getStationAt(orbit) != null;}
    
    /** Randomly generates the planets and their number. */
    private void generatePlanets()
    {
        if (isEmpty())
        {
            planets = new Planet[0];
            return;
        }
        
        // The (... - 1) + 2 is to ensure at least one planet
        for (int i = 0; i < rng.nextInt(Math.min(MAX_PLANETS,
                star.getMass()) - 1) + 2; i++)
        {
            int j;
            do
            {
                j = rng.nextInt(star.getMass());
            } while (planets[j] != null);
            
            // Make a new planet with the sector's name and i's corresponding
            // letter, this sector, and j adjusted from a whole number to a
            // natural number
            planets[j] = new Planet(generateNameFor(i),
                    new SectorLocation(getLocation(), j + 1));
            planets[j].init();
        }
    }
    
    /** Randomly generates the stations and their number. */
    private void generateStations()
    {
        if (isEmpty())
        {
            stations = new Station[0];
            return;
        }
        
        // The (...) + 1  is to ensure at least 1 station
        // Power is divided by 2 to avoid overpopulating small sectors
        for (int i = 0; i < rng.nextInt(Math.min(MAX_STATIONS,
                star.getMass() / 2)) + 1; i++)
        {
            int j;
            do
            {
                j = rng.nextInt(star.getMass());
            } while (stations[j] != null);
            
            // There is no need to do a check for if this is a station system,
            // because stations would not otherwise be generated
            
            // Make a new station with the sector's name and i's corresponding
            // letter, this sector, and j adjusted from a whole number to a 
            // natural number
            stations[j] = new Station(generateNameFor(i),
                    new SectorLocation(getLocation(), j + 1),
                    location.getMap().getRandomFaction());
        }
    }
    
    /** Randomly generates any ships and their number. */
    private void generateShips(int nShips)
    {
        ships = new LinkedList<>();
        
        for (int i = 0; i < nShips; i++)
        {
            // Make a new ship with the sector's name and i's corresponding
            // letter, the sector's location, and its map
            Ship ship = new Ship(generateShipName(),
                    new SectorLocation(location,
                            rng.nextInt(star.getMass()) + 1),
                    location.getMap().getRandomFaction());
            ships.add(ship);
            location.getMap().addShip(ship);
        }
    }
    
    /**
     * Generates a name consisting of the sector's name and a letter, whose
     * place is represented by the specified number.
     * @param i the place of the letter in the alphabet to use
     * @return a String containing the sector's name and the character form of i
     */
    public String generateNameFor(int i)
        {return name + (char) (i + 65);}
    
    /**
     * Generates a name for a ship consisting of the sector's name and a random
     * letter not already in use by another ship.
     * @return a String containing the sector's name and a character that has
     * not yet been used for ship names, if possible
     */
    public String generateShipName()
    {
        // TODO fix possible duplicate ship name bug
        
        if (usedLetters.size() >= 26)
            return generateNameFor(rng.nextInt(26));
        
        int letterPlace;
        do
        {
            letterPlace = rng.nextInt(26);
        } while (usedLetters.contains(letterPlace));
        usedLetters.add(letterPlace);
        return generateNameFor(letterPlace);
    }
    
    /**
     * Removes the given letter from the sector's list of used ship letters.
     * @param letter the letter to remove
     * @return true if the letter was removed
     */
    public boolean removeLetter(Integer letter)
        {return usedLetters.remove(letter);}
    
    /**
     * Returns the first ship found with the given name.
     * @param name the name of the ship to find
     * @return the first ship found with the given name, null if not found
     */
    public Ship getShip(String name)
    {
        for (Ship ship: ships)
            if (ship != null && (name.equalsIgnoreCase(ship.getName()) ||
                    name.equalsIgnoreCase(ship.toString())))
                return ship;
        
        return null;
    }
    
    /**
     * Returns the first ship found at a given orbit.
     * @param orbit the orbit to find a ship at
     * @return the first ship found at the orbit, null if none
     */
    public Ship getFirstShipAt(int orbit)
    {
        for (Ship ship: ships)
            if (ship != null)
                return ship;
        
        return null;
    }
    
    /**
     * Returns the first ship found at a given orbit that is not the entered
     * one.
     * @param ship the disallowed ship that will not be found at the orbit
     * @return the first ship that is not the specified ship found at the orbit,
     * null if no others found
     */
    public Ship getFirstOtherShip(Ship ship)
    {
        int orbit = ship.getSectorLocation().getOrbit();
        for (Ship otherShip: ships)
            if (otherShip != null && otherShip != ship &&
                    otherShip.getSectorLocation().getOrbit() == orbit)
                return otherShip;
        
        return null;
    }
    
    /**
     * Returns the first ship found at a given orbit that is not aligned to the
     * entered faction.
     * @param orbit the orbit to find an unaligned ship at
     * @param faction the faction that the ship must be at war with
     * @return the first ship that is at the orbit and is part of a faction that
     * is at war with the specified faction (or is not part of a faction), null
     * if not found
     */
    public Ship getFirstHostileShip(int orbit, Faction faction)
    {
        // The if checks if the ship is at the same orbit, is of a different
        // faction, and is either unaligned or at war
        for (Ship otherShip: ships)
            if (((SectorLocation) otherShip.getLocation()).getOrbit() == orbit
                    && otherShip.isHostile(faction))
                return otherShip;
        
        return null;
    }
    
    /**
     * Performs the same function as getFirstHostileShip, using a ship's orbit
     * and faction.
     * @param ship the ship that orbit and faction with be collected from
     * @return the first ship hostile to the entered ship in the same orbit
     */
    public Ship getFirstHostileShip(Ship ship)
    {
        if (!ship.isInSector())
            return null;
        
        return getFirstHostileShip(ship.getSectorLocation().getOrbit(),
                ship.getFaction());
    }
    
    public List<Ship> getShips()
        {return ships;}
    
    /**
     * Returns an array of all ships at a given orbit.
     * @param orbit the orbit to find ships at
     * @return an array of ships that are at the orbit
     */
    public List<Ship> getShipsAt(int orbit)
    {
        List<Ship> shipsAtOrbit = new LinkedList<>();
        
        for (Ship ship: ships)
            if (ship != null && ship.getSectorLocation().getOrbit() == orbit)
                shipsAtOrbit.add(ship);
        
        return shipsAtOrbit;
    }
    
    /**
     * Returns the orbit of a randomly-picked station.
     * @return the orbit of a random station, -1 if no stations were found
     */
    public int getRandomStationOrbit()
    {
        int nStations = 0;
        
        for (Station station: stations)
            if (station != null)
                nStations++;
        
        if (nStations == 0)
            return -1;
        
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
    
    public ColorString getSymbolsForOrbit(int orbit)
    {
        ColorString symbols = new ColorString();
        int orbitIndex = orbit - 1;
        int nShips = getShipsAt(orbit).size();
        
        boolean playerIsHere;
        Location playerLocation = location.getMap().getPlayer().getLocation();
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
            symbols.add(stations[orbitIndex].getSymbol());
        else
            symbols.add(SYMBOL_EMPTY);
        
        if (playerIsHere)
        {
            symbols.add(SYMBOL_PLAYER);
        }
        else if (nShips > 0)
        {
            // Calculate the highest level of ship at this orbit
            int highestLevel = 0;
            boolean isLeader = false;
            boolean isCommonFaction = true;
            Faction commonFaction = null;
            for (Ship ship: ships)
            {
                if (ship.getSectorLocation().getOrbit() == orbit &&
                        !ship.isPlayer())
                {
                    if (ship.getHighestLevel() > highestLevel)
                        highestLevel = ship.getHighestLevel();
                    
                    if (ship.isLeader())
                        isLeader = true;
                    
                    if (commonFaction == null)
                        commonFaction = ship.getFaction();
                    else
                        isCommonFaction = ship.getFaction() == commonFaction;
                }
            }
            
            char symbol;
            // Append either the leader symbol or the symbol of the highest ship
            if (isLeader)
            {
                symbol = SYMBOL_LEADER;
            }
            else
            {
                // Print the corresponding symbol to the highest level
                switch (highestLevel / Levels.LEVEL_AMT)
                {
                    case 0: case 1: symbol = SYMBOL_WEAK_SHIP;   break;
                    case 2:         symbol = SYMBOL_MEDIUM_SHIP; break;
                    default:        symbol = SYMBOL_STRONG_SHIP; break;
                }
            }
            
            if (isCommonFaction && commonFaction != null)
                symbols.add(new ColorChar(symbol, commonFaction.getColor()));
            else
                symbols.add(new ColorChar(symbol));
        }
        else
        {
            symbols.add(SYMBOL_EMPTY);
        }
        
        if (planets[orbitIndex] != null)
            symbols.add(planets[orbitIndex].getSymbol());
        else
            symbols.add(SYMBOL_EMPTY);
        
        return symbols;
    }
    
    public List<ColorString> getOrbitContents(int orbit)
    {
        List<ColorString> contents = new LinkedList<>();
        if (planets[orbit - 1] != null)
            contents.add(planets[orbit - 1].toColorString());
        
        if (stations[orbit - 1] != null)
            contents.add(stations[orbit - 1].toColorString());
        
        for (Ship ship: ships)
        {
            if (ship != null && ship.getSectorLocation().getOrbit() == orbit &&
                    !ship.isPlayer())
            {
                ColorString shipString = ship.toColorString();
                if (ship.isLeader())
                    shipString.add(new ColorString(" (Leader)", COLOR_FIELD));
                contents.add(shipString);
            }
        }
        
        return contents;
    }
    
    /**
     * Returns true if a specified orbit is valid, meaning it ranges between 1
     * and the constant number of orbits.
     * @param orbit the orbit to validate
     * @return true if the orbit is between 1 and the constant number of orbits
     */
    public boolean isValidOrbit(int orbit)
        {return !isEmpty() && orbit >= 1 && orbit <= star.getMass();}
    
    /**
     * Returns true if there are any planets or stations in this sector that can
     * be claimed.
     * @return true if there are any landable planets or stations in the sector
     */
    public boolean hasClaimableTerritory()
    {
        if (hasStations())
            return true;
        
        for (Planet planet: planets)
            if (planet != null && planet.getType().canLandOn())
                return true;
        
        return false;
    }
    
    /**
     * Generates a random name to be used in sectors, and it must be final so
     * that it cannot modify sector's constructor. This allows for 67,600
     * possible designations, enough for a grid of 260x260 sectors.
     * @return a String consisting of two characters, a hyphen, and two numbers
     */
    public static final String generateName()
    {
        char c = (char) (rng.nextInt(25) + 65);
        char d = (char) (rng.nextInt(25) + 65);
        int i  = rng.nextInt(10);
        int j  = rng.nextInt(10);
        return c + "" + d + "-" + i + "" + j;
    }
    
    /**
     * Scans through the list of ships, removing ones that are on planets and
     * stations.
     */
    public void resetDuplicateShips()
    {
        boolean shipReset;
        
        do
        {
            shipReset = false;
            
            for (Ship ship: ships)
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