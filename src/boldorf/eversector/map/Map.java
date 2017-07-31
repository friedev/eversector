package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.rng;
import boldorf.util.NameGenerator;
import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import boldorf.eversector.entities.Ore;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Station;
import boldorf.eversector.entities.locations.Location;
import boldorf.eversector.entities.locations.SectorLocation;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import boldorf.eversector.map.faction.Faction;
import boldorf.eversector.map.faction.Relationship;
import static boldorf.eversector.map.faction.RelationshipType.WAR;
import static boldorf.eversector.storage.Names.ORE;
import java.awt.Color;
import squidpony.squidmath.Coord;

/** The Map class manages a 2D array of sectors and everything in them. */
public class Map
{
    /**
     * The default number of sectors on each side of the origin (central
     * sector).
     */
    public static final int SIZE = 8;
    
    /** The minimum number of faction that will be present in the game. */
    public static final int MIN_FACTIONS = 2;
    
    /**
     * The maximum increase in factions over the minimum (adjusted by 1 to 
     * include 0).
     */
    public static final int FACTION_RANGE = 4;
    
    /** The number of turns that are simulated before the map is used. */
    public static final int SIMULATED_TURNS = 50;
    
    /**
     * The frequency at which to update a relationship between factions, to be
     * divided by the number of factions.
     */
    public static final int RELATION_UPDATE_FREQ = 120;
    
    /**
     * The amount of tries that can be made to update a relationship before the
     * update is skipped.
     */
    public static final int MAX_RELATIONSHIP_UPDATE_TRIES = 5;
    
    /** The frequency at which to elect for new faction leaders, in turns. */
    public static final int ELECTION_FREQ = 150;
    
    /** The number of candidates that are selected for an election. */
    public static final int CANDIDATES = 4;
    
    /**
     * The amount by which the player's reputation will be multiplied when
     * compared to other ships while checking for leaders.
     */
    public static final double PLAYER_REP_MODIFIER = 1.25;
    
    /**
     * The number of election cycles before the player receives their reputation
     * boost.
     */
    public static final int PLAYER_REP_TIME = 5;
    
    /** The fewest types of ore that can exist. */
    public static final int MIN_ORE = 4;
    
    /** The range of possible amounts of ore types over the minimum. */
    public static final int ORE_RANGE = 3;
    
    private Sector[][]   map;
    private final int    offset;
    private Ship         player;
    private List<Ship>   ships;
    private Faction[]    factions;
    private List<String> designations;
    private Ore[]        oreTypes;   
    private int          turn;
    
    /**
     * Generates a map with the default size.
     * @throws java.io.FileNotFoundException if ore types cannot be loaded
     */
    public Map() throws FileNotFoundException
        {this(SIZE);}
    
    /**
     * Generates a map of a specified size.
     * @param size the side length of the map in sectors
     * @throws java.io.FileNotFoundException if ore types cannot be loaded
     */
    public Map(int size) throws FileNotFoundException
    {
        map      = new Sector[size * 2 + 1][size * 2 + 1];
        offset   = (int) Math.floor((double) map.length / 2.0);
        ships    = new LinkedList<>();
        factions = new Faction[rng.nextInt(FACTION_RANGE)
                 + MIN_FACTIONS];
        designations = new LinkedList<>();
        oreTypes = generateOreTypes();
        turn = -SIMULATED_TURNS;
        
        // Factions must be created first so they can be assigned to ships
        createFactions();
        init();
        sectorAt(0, 0).setType(Sector.STATION_SYSTEM);
    }
    
    public Sector[][] toArray()
        {return map;}
    
    public List<Ship> getShips()
        {return ships;}
    
    public Ship getPlayer()
        {return player;}
    
    public int getTurn()
        {return turn;}
    
    public int getMinY()
        {return -offset;}
    
    public int getMaxY()
        {return (map.length - 1) - offset;}
    
    public int getMinX()
        {return -offset;}
    
    public int getMaxX()
        {return (map[0].length - 1) - offset;}
    
    public Sector sectorAt(int x, int y)
        {return map[-y + offset][x + offset];}
    
    public Sector sectorAt(Coord p)
        {return p == null ? null : sectorAt(p.x, p.y);}
    
    /**
     * Returns true if the specified coordinates are on the map.
     * @param x the x coordinate of the Coord to check
     * @param y the y coordinate of the Coord to check
     * @return true if the coordinates correspond with a Coord on the map
     */
    public boolean contains(int x, int y)
    {
        return (x >= getMinX() && x <= getMaxX()) &&
               (y >= getMinY() && y <= getMaxY());
    }
    
    /**
     * Performs the same function as contains(int, int), except that it uses a
     * predefined Coord's coordinates.
     * @param p the Coord to use coordinates from
     * @return true if the Coord is on the map
     */
    public boolean contains(Coord p)
        {return contains(p.x, p.y);}
    
    /**
     * Sets the player to a designated ship.
     * @param p the ship to become the player
     */
    public void setPlayer(Ship p)
        {player = p;}
    
    /** Creates the player, the starting sector, and the player's faction. */
    public void createNewPlayer()
    {
        SectorLocation location = new Location(this, Coord.get(0, 0))
                .enterSector();
        location =
                location.setOrbit(location.getSector().getRandomStationOrbit());
        Faction faction = location.getStation().getFaction();
        player = new Ship("Player", location, faction);
        player.setAI(null);
    }
    
    /** Plays through the next turn on the map. */
    public void nextTurn()
    {
        player.updateContinuousEffects();
        
        for (Ship ship: ships)
            ship.getAI().act();
        
        for (Iterator<Ship> it = ships.iterator(); it.hasNext();)
            if (it.next().isDestroyed())
                it.remove();
        
        for (Ship ship: ships)
        {
            ship.updateContinuousEffects();
            ship.fadeReputations();
        }
        
        player.fadeReputations();
        
        // Respawns ships if there are fewer than the minimum ships in a sector
        for (int y = 0; y < map.length; y++)
        {
            for (int x = 0; x < map[y].length; x++)
            {
                Sector sector = map[y][x];
                
                if (sector.getNShips() < Sector.MIN_SHIPS &&
                    Sector.STATION_SYSTEM.equals(sector.getType()))
                {
                    Station station =
                            sector.getStationAt(sector.getRandomStationOrbit());
                    
                    if (station.getFaction().changeEconomy(-Ship.BASE_VALUE))
                    {
                        Ship newShip = new Ship(sector.generateNameFor(
                                rng.nextInt(26)), new SectorLocation(
                                        sector.getLocation(),
                                        station.getLocation().getOrbit()),
                                station.getFaction());
                        newShip.dock();
                        ships.add(newShip);
                    }
                }
                
                // Only known way to fix duplicate ship bug
                sector.resetDuplicateShips();
            }
        }
        
        // Update relationships if there are more than two factions
        if (turn >= (RELATION_UPDATE_FREQ / factions.length) &&
                factions.length > 2 &&
                turn % (RELATION_UPDATE_FREQ / factions.length) == 0)
        {
            int tries = 0;
            do
            {
                tries++;
                if (tries > MAX_RELATIONSHIP_UPDATE_TRIES)
                    break;
            }
            while (!getRandomRelationship().updateRelationship());
        }
        
        // Update faction leaders periodically, or immediately if destroyed
        // Also update faction leaders immediately before gameplay starts
        if (turn > 0)
            updateFactionLeaders();
        else if (turn == -1)
            updateFactionLeaders();
        
        turn++;
    }
    
    /** Updates the leader of each faction. */
    public void updateFactionLeaders()
    {
        for (Faction faction: factions)
            if (turn - faction.getLastElection() == ELECTION_FREQ)
                faction.holdElection();
        
        updateDestroyedFactionLeaders();
    }
    
    public void updateDestroyedFactionLeaders()
    {
        for (Faction faction: factions)
        {
            if (faction.getLeader() == null ||
                    faction.getLeader().isDestroyed())
            {
                faction.holdElection();
            }
        }
    }
    
    /**
     * Reveals a given location, meaning all adjacent sectors will be marked as
     * discovered.
     * @param location the Coord to reveal
     */
    public void reveal(Coord location)
    {
        int x = location.x;
        int y = location.y;
        
        if (contains(location))
            sectorAt(location).discover();
        if (contains(Coord.get(x - 1, y)))
            sectorAt(Coord.get(x - 1, y)).discover();
        if (contains(Coord.get(x + 1, y)))
            sectorAt(Coord.get(x + 1, y)).discover();
        if (contains(Coord.get(x, y - 1)))
            sectorAt(Coord.get(x, y - 1)).discover();
        if (contains(Coord.get(x, y + 1)))
            sectorAt(Coord.get(x, y + 1)).discover();
    }
    
    /**
     * Scans a given location by revealing its surroundings..
     * @param location the Coord to scan
     */
    public void scan(Coord location)
    {
        int x = location.x;
        int y = location.y;
        
        reveal(Coord.get(x, y));
        reveal(Coord.get(x - 1, y));
        reveal(Coord.get(x + 1, y));
        reveal(Coord.get(x, y - 1));
        reveal(Coord.get(x, y + 1));
    }
    
    /**
     * Reveals the sectors surrounding the specified sector.
     * @param sector the sector whose location will be used in the reveal
     */
    public void reveal(Sector sector)
        {reveal(sector.getLocation().getCoords());}
    
    /**
     * Scans the sector by revealing its surroundings
     * @param sector the sector whose location will be used in the scan
     */
    public void scan(Sector sector)
        {scan(sector.getLocation().getCoords());}
    
    /** Discovers all sectors. */
    public void discoverAll()
    {
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                sectorAt(map[y][x].getLocation().getCoords()).discover();
    }
    
    /**
     * Returns the number of discovered sectors on the map.
     * @return the number of sectors marked as discovered
     */
    public int getNDiscoveredSectors()
    {
        int discoveredSectors = 0;
        
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                if (map[y][x].isDiscovered())
                    discoveredSectors++;
        
        return discoveredSectors;
    }
    
    /**
     * Adds the given ship to the ships list.
     * @param ship the ship to add to the ships list
     * @return true if the addition was successful
     */
    public boolean addShip(Ship ship)
        {return ships.add(ship);}
    
    /**
     * Removes the given ship from the ships list.
     * @param ship the ship to remove from the ships list
     * @return true if the removal was successful
     */
    public boolean removeShip(Ship ship)
        {return ships.remove(ship);}
    
    /**
     * Finds the first ship with the given name.
     * @param name the name of the ship to find
     * @return the first ship found with the given name
     */
    public Ship findShip(String name)
    {
        for (Ship ship: ships)
            if (name.equalsIgnoreCase(ship.getName()))
                return ship;
        
        return null;
    }
    
    /**
     * Returns the faction at the given index. 
     * @param index the index of the faction to find
     * @return the faction with the given position in the faction array
     */
    public Faction getFaction(int index)
        {return factions[index];}
    
    public Faction getFaction(String name)
    {
        for (Faction faction: factions)
            if (faction.getName().equals(name))
                return faction;
        return null;
    }
    
    /**
     * Returns the index of the given faction. This method is public so as to 
     * ease iteration over factions.
     * @param faction the faction to find the index of
     * @return the index of the given faction in the faction array
     */
    public int getIndex(Faction faction)
    {
        for (int i = 0; i < factions.length; i++)
            if (factions[i] == faction)
                return i;
        
        return -1;
    }
    
    /**
     * Converts the map into a List of ColorStrings for displaying.
     * @param showStars if true, will show the star symbols of sectors rather
     * than their type symbols
     * @param cursor the sector to show as selected
     * @return the map as a List of ColorStrings
     */
    public List<ColorString> toColorStrings(boolean showStars, Coord cursor)
    {
        List<ColorString> output = new ArrayList<>(map.length);
        
        for (int y = 0; y < map.length; y++)
        {
            ColorString line = new ColorString();
            
            for (int x = 0; x < map[y].length; x++)
            {
                ColorChar symbol = new ColorChar(showStars ?
                        map[y][x].getStarSymbol() : map[y][x].getSymbol());
                if (map[y][x].getLocation().getCoords().equals(cursor))
                    symbol.setBackground(COLOR_SELECTION_BACKGROUND);
                line.add(symbol);
            }
            
            output.add(line);
        }
        
        return output;
    }
    
    /**
     * Returns the rank of the given faction by sectors controlled.
     * @param faction the faction of which to return the rank of
     * @return the faction's rank among other factions based on the number of
     * sectors controlled by each
     */
    public int getRank(Faction faction)
    {
        int rank = 1;
        
        for (Faction otherFaction: factions)
            if (otherFaction != faction && getSectorsControlledBy(otherFaction)
                    >= getSectorsControlledBy(faction))
                rank++;
        
        return rank;
    }
    
    /**
     * Returns the number of sectors controlled by the given faction.
     * @param faction the faction to count claimed sectors of
     * @return the number of sectors in which the given faction is the dominant
     * one
     */
    public int getSectorsControlledBy(Faction faction)
    {
        int sectorsClaimed = 0;
        
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                if (map[y][x].getFaction() == faction)
                    sectorsClaimed++;
        
        return sectorsClaimed;
    }
    
    public int getPlanetsControlledBy(Faction faction)
    {
        int planetsClaimed = 0;
        
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                planetsClaimed += map[y][x].getPlanetsControlledBy(faction);
        
        return planetsClaimed;
    }
    
    public int getNStationsControlledBy(Faction faction)
    {
        int stationsClaimed = 0;
        
        for (int y = 0; y < map.length; y++)
            for (int x = 0; x < map[y].length; x++)
                stationsClaimed += map[y][x].getStationsControlledBy(faction);
        
        return stationsClaimed;
    }
    
    public String getStationTypesControlledBy(Faction faction)
    {
        int trade  = 0;
        int battle = 0;
        
        for (int y = 0; y < map.length; y++)
        {
            for (int x = 0; x < map[y].length; x++)
            {
                trade += map[y][x].getStationTypesControlledBy(faction,
                        Station.TRADE);
                battle += map[y][x].getStationTypesControlledBy(faction,
                        Station.BATTLE);
            }
        }
        
        return new StringBuilder().append(trade + battle).append(" (")
                .append(trade).append(" Trade, ").append(battle)
                .append(" Battle)").toString();
    }
    
    public int getNShipsIn(Faction faction)
    {
        int nShips = 0;
        
        for (Ship ship: ships)
            if (ship.isInFaction(faction))
                nShips++;
        
        return nShips;
    }
    
    public String getShipTypesIn(Faction faction)
    {
        int total  = 0;
        int mining = 0;
        int battle = 0;
        
        for (Ship ship: ships)
        {
            if (ship.isInFaction(faction))
            {
                total++;
                
                if ("mining".equals(ship.getHigherLevel()))
                    mining++;
                else if ("battle".equals(ship.getHigherLevel()))
                    battle++;
            }
        }
        
        return new StringBuilder().append(total).append(" (").append(mining)
                .append(" Mining, ").append(battle).append(" Battle)")
                .toString();
    }
    
    public Faction[] getFactions()
        {return factions;}
    
    /**
     * Returns a random faction from the list of factions.
     * @return a randomly selected faction from the list of all factions
     */
    public final Faction getRandomFaction()
        {return factions[rng.nextInt(factions.length)];}
    
    /**
     * Returns a random faction that is not the specified one.
     * @param f the faction to exclude from the list of selections
     * @return a random faction from the list of all factions as long as it is
     * not the one provided
     */
    public final Faction getRandomFaction(Faction f)
    {
        Faction randomFaction;
        do
        {
            randomFaction = getRandomFaction();
        } while (randomFaction == f);
        return randomFaction;
    }
    
    /**
     * Returns a random relationship between two factions.
     * @return a random relationship between two randomly-chosen factions
     */
    public final Relationship getRandomRelationship()
    {
        Faction faction1 = getRandomFaction();
        Faction faction2 = getRandomFaction(faction1);
        return faction1.getRelationshipObject(faction2);
    }
    
    public Ore[] getOreTypes()
        {return oreTypes;}
    
    public Ore getRandomOre()
        {return oreTypes[rng.nextInt(oreTypes.length)];}
    
    /** Initializes all the sectors on the map. */
    private void init()
    {
        for (int y = 0; y < map.length; y++)
        {
            for (int x = 0; x < map[y].length; x++)
            {
                map[y][x] = new Sector(new Location(this,
                        Coord.get(x - offset, -y + offset)));
                map[y][x].init();
            }
        }
    }
    
    /**
     * Returns true if the given designation is in use.
     * @param designation the designation to check
     * @return true if there is already a sector using the given designation
     */
    public boolean isUsed(String designation)
        {return designations.contains(designation);}
    
    /**
     * Adds the given designation to the list of designations.
     * @param designation the designation to add to the list
     */
    public void addDesignation(String designation)
        {designations.add(designation);}
    
    /**
     * Removes the given designation from the list of designations.
     * @param designation the designation to remove from the list
     */
    public void removeDesignation(String designation)
        {designations.remove(designation);}
    
    /**
     * Creates the factions using constant names, and then generates the
     * relationships among them.
     */
    private void createFactions()
    {
        List<String> usedTypes = new LinkedList<>();
        
        for (int i = 0; i < factions.length; i++)
        {
            String name = Main.nameGenerator.generateName(2);

            // Ensure no factions are of the same type
            String type = (String) rng.getRandomElement(Faction.TYPES);
            while (usedTypes.contains(type))
                type = (String) rng.getRandomElement(Faction.TYPES);

            usedTypes.add(type);
            Color color;
            switch (i)
            {
                case 0:  color = AsciiPanel.brightRed;     break;
                case 1:  color = AsciiPanel.brightCyan;    break;
                case 2:  color = AsciiPanel.brightGreen;   break;
                case 3:  color = AsciiPanel.brightYellow;  break;
                case 4:  color = AsciiPanel.brightMagenta; break;
                default: color = AsciiPanel.brightWhite;   break;
            }
            factions[i] = new Faction(name, type, this, color);
        }
        
        // If there are only two factions, they will always be at war
        if (factions.length == 2)
        {
            new Relationship(factions[0], factions[1], WAR).addToFactions();
            return;
        }
        
        // Will pair up all factions with no duplicates
        for (int i = 0; i < factions.length; i++)
            for (int j = i + 1; j < factions.length; j++)
                new Relationship(factions[i], factions[j]).addToFactions();
    }
    
    private Ore[] generateOreTypes()
    {
        Ore[] ores = new Ore[Math.min(
                MIN_ORE + rng.nextInt(ORE_RANGE), Ore.DENSITY)];
        
        NameGenerator oreNames = new NameGenerator(ORE, rng);
        
        for (int i = 0; i < ores.length; i++)
        {
            String name;
            do
            {
                name = oreNames.generateName(2);
                
                // Ensure unique names
                for (Ore ore: ores)
                    if (name == null ||
                            (ore != null && name.equals(ore.getName())))
                        name = null;
            } while (name == null);
            
            int density;
            do
            {
                density = rng.nextInt(Ore.DENSITY) + 1;
                
                // Ensure unique densities
                for (Ore ore: ores)
                    if (ore != null && density == ore.getDensity())
                        density = 0;
            } while (density == 0);
            ores[i] = new Ore(name, density);
        }
        
        int totalDensity = 0;
        for (Ore ore: ores)
            totalDensity += ore.getDensity();
        
        // If there are not enough ores with high enough density, set one higher
        // to compensate
        if (totalDensity < Ore.DENSITY)
            ores[0].setDensity(ores[0].getDensity() +
                    (Ore.DENSITY - totalDensity));
        
        Arrays.sort(ores, Collections.reverseOrder());
        return ores;
    }
}