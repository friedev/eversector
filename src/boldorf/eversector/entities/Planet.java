package boldorf.eversector.entities;

import boldorf.util.Utility;
import static boldorf.eversector.Main.rng;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.entities.locations.PlanetLocation;
import boldorf.eversector.entities.locations.SectorLocation;
import java.util.ArrayList;
import java.util.List;
import boldorf.eversector.map.Sector;
import boldorf.eversector.map.faction.Faction;
import java.util.Arrays;
import squidpony.squidmath.Coord;

/** A planet in a sector that can be interacted with in different ways. */
public class Planet extends CelestialBody implements ColorStringObject
{
    /**
     * The greatest number that region widths will be multiplied by, along with
     * 2.
     */
    public static final int REGION_MULTIPLIER_RANGE = 3;
    
    /** The minimum number of ore types on a planet. */
    public static final int MIN_ORES = 1;
    
    /**
     * The maximum increase in ores over the minimum (increased by 1 to include 
     * 0).
     */
    public static final int ORE_RANGE = 3;
    
    /**
     * The amount of hull damage done to ships when mining from asteroid belts.
     */
    public static final int ASTEROID_DAMAGE = 1;
    
    private PlanetType type;
    private List<Ore>  ores;
    private Region[][] regions;
    
    /**
     * Creates a planet with a name, location, and faction.
     * @param name the name of the planet
     * @param location the location of the planet
     * @param faction the faction that has claimed the planet
     */
    public Planet(String name, SectorLocation location, Faction faction)
    {
        super(name, location, faction);
        
        generateType();
        
        if (type.hasOre())
            generateOre();
        else
            ores = null;
    }
    
    /**
     * Creates an unclaimed planet with a name and location.
     * @param name the name of the planet
     * @param location the location of the planet
     */
    public Planet(String name, SectorLocation location)
        {this(name, location, null);}
    
    public void init()
    {
        // Only set the planet's faction if it is a rocky planet
        // Note that unclaim() does not call updateFaction()
        if (type.canLandOn())
            generateRegions();
        else
            unclaim();
    }
    
    @Override
    public String toString()
        {return type + " " + super.toString();}
    
    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(),
                isClaimed() ? getFaction().getColor() : null);
    }
    
    public PlanetType getType()
        {return type;}
    
    public Region[][] getRegions()
        {return regions;}
    
    public int getNRegions()
        {return getNRows() * getNColumns();}
    
    public int getNRows()
        {return regions.length;}
    
    public int getNColumns()
        {return regions[0].length;}
    
    public Region regionAt(Coord location)
        {return regions[location.y][location.x];}
    
    public Coord indexOf(Region region)
    {
        for (int y = 0; y < regions.length; y++)
            for (int x = 0; x < regions[y].length; x++)
                if (regions[y][x] == region)
                    return Coord.get(x, y);
        return null;
    }
    
    public boolean contains(Coord location)
        {return containsX(location.x) && containsY(location.y);}
    
    public boolean containsX(int x)
        {return x >= 0 && getNColumns() >= x + 1;}
    
    public boolean containsY(int y)
        {return y >= 0 && getNRows() >= y + 1;}
    
    public Coord getCenter()
        {return Coord.get(getNColumns() / 2, getNRows() / 2);}
    
    public int getOppositeSide(int x)
    {
        return containsX(x + getNColumns() / 2) ?
                x + getNColumns() / 2 : x - getNColumns() / 2;
    }
    
    // TODO change CelestialBody/Station regarding claiming changes to planet
    
    /**
     * Calculates the dominant faction on the planet, based on region control.
     */
    public void updateFaction()
    {
        // Note than claim(null) is always used over unclaim() because sector
        // should be refreshed by this calculation as well
        
        if (type == null || !type.canLandOn())
        {
            claim(null);
            return;
        }
        
        int[] control = new int[getLocation().getMap().getFactions().length];
        
        // Increase the respective counter for each claimed body
        for (Region[] row: regions)
            for (Region region: row)
                if (region != null && region.isClaimed())
                    control[getLocation().getMap().getIndex(
                            region.getFaction())]++;
        
        int index     = -1;
        int maxBodies = 0; // The most owned bodies in a faction
        
        for (int i = 0; i < control.length; i++)
        {
            if (control[i] > maxBodies)
            {
                maxBodies = control[i];
                index     = i;
            }
            else if (control[i] == maxBodies)
            {
                // Set the index to an invalid value so that it is known that
                // there is a tie, but so that it can also be easily overwritten
                index = -1;
            }
        }
        
        claim(index == -1 ? null : getLocation().getMap().getFaction(index));
    }
    
    /**
     * Returns true if there is a region with the given type on this planet.
     * @param type the type of region to look for
     * @return true if a search for the region does not return null
     */
    public boolean hasRegion(String type)
    {
        for (Region[] row: regions)
            for (Region region: row)
                if (region.getType().equalsIgnoreCase(type))
                    return true;
        return false;
    }
    
    /**
     * Returns a random region on the planet.
     * @return any of the regions on the planet, chosen at random
     */
    public Region getRandomRegion()
    {
        List<Region> regionList = new ArrayList<>();
        for (Region[] row: regions)
            regionList.addAll(Arrays.asList(row));
        return getRandomRegion(regionList);
    }
    
    public Coord getRandomCoord()
        {return rng.nextCoord(getNColumns(), getNRows());}
    
    /**
     * Returns a random region on the planet that is not already controlled by
     * the specified faction.
     * @param faction the faction that regions must not be part of to be
     * selected
     * @return any of the regions on the planet not owned by the specified
     * faction, chosen at random
     */
    public Region getRandomRegion(Faction faction)
    {
        ArrayList<Region> unclaimedRegions = new ArrayList<>();
        
        for (Region[] row: regions)
            for (Region region: row)
                if (region.getFaction() != faction)
                    unclaimedRegions.add(region);
        
        return getRandomRegion(unclaimedRegions);
    }
    
    public Region getRandomOreRegion()
    {
        ArrayList<Region> oreRegions = new ArrayList<>();
        
        for (Region[] row: regions)
            for (Region region: row)
                if (region.hasOre())
                    oreRegions.add(region);
        
        return getRandomRegion(oreRegions);
    }
    
    public Coord getRandomOreCoord()
    {
        Region oreRegion = getRandomOreRegion();
        if (oreRegion == null)
            return getRandomCoord();
        return oreRegion.getLocation().getRegionCoords();
    }
    
    /**
     * Returns a random region selected from a List of regions.
     * @param regions the list of regions from which to select a random one
     * @return any of the regions in the given list, chosen at random
     */
    private static Region getRandomRegion(List<Region> regions)
    {
        return regions == null || regions.isEmpty() ?
                null : rng.getRandomElement(regions);
    }
    
    /**
     * Returns the cost to claim a region on the planet.
     * @return the cost of claiming a region on the planet, should be positive
     */
    @Override
    public int getClaimCost()
        {return CLAIM_COST / getNRegions();}
    
    /**
     * Returns a symbol for the planet's type.
     * @return the planet's symbol, as a char
     */
    public ColorChar getSymbol()
    {
        return new ColorChar(type.getSymbol(),
                isClaimed() ? getFaction().getColor() : null);
    }
    
    /**
     * Returns or generates the ore type of the planet, depending on if it's an
     * asteroid belt.
     * @return the planet's ore type or a randomly generated one
     */
    public Ore getRandomOre()
    {
        return type.hasOre() ? ores.get(rng.nextInt(ores.size())) : null;
    }
    
    public int getNShips()
    {
        if (!type.canLandOn())
            return 0;
        
        int nShips = 0;
        
        for (Region[] row: regions)
            for (Region region: row)
                nShips += region.getShips().size();
        
        return nShips;
    }
    
    public int getNShips(Faction faction)
    {
        if (!type.canLandOn())
            return 0;
        
        int nShips = 0;
        
        for (Region[] row: regions)
            for (Region region: row)
                nShips += region.getNShips(faction);
        
        return nShips;
    }
    
    public List<ColorString> toColorStrings()
    {
        List<ColorString> list = new ArrayList<>(getNRows());
        
        for (Region[] row: regions)
        {
            ColorString rowString = new ColorString();
            for (Region region: row)
                rowString.add(region.toColorChar());
            list.add(rowString);
        }
        
        return list;
    }
    
    /**
     * Generates a random planet type, including temperature based on distance
     * from the nearest orbit.
     * @return a String for the planet type
     */
    private void generateType()
    {
        // If the star is nebular, there is a 1/2 chance that a nebula is
        // generated instead of a planet
        if (getLocation().getSector().getStar().isNebular() &&
                rng.nextBoolean())
        {
            type = PlanetType.NEBULA;
            return;
        }
        
        PlanetType rocky = getRockyType();
        type = (PlanetType) Utility.select(rng, new PlanetType[]
                {rocky, PlanetType.GAS_GIANT, PlanetType.ASTEROID_BELT},
                new double[] {0.6, 0.3, 0.1});
    }
    
    private PlanetType getRockyType()
    {
        return PlanetType.getRockyType(getLocation().getSector().getStar()
                .getPowerAt(getLocation().getOrbit()));
    }
    
    private void generateOre()
    {
        ores = new ArrayList<>();
        int nOres = rng.nextInt(ORE_RANGE) + MIN_ORES;
        for (int i = 0; i < nOres; i++)
        {
            Ore ore = getLocation().getMap().getRandomOre();
            
            if (!ores.contains(ore))
                ores.add(ore);
        }
        
        // Add the chance for a region to have no ore
        ores.add(null);
    }
    
    /** Generates a random amount of regions. */
    private void generateRegions()
    {
        int widthMultiplier = rng.nextInt(REGION_MULTIPLIER_RANGE) + 1;
        regions = new Region[widthMultiplier + 1][widthMultiplier * 2];
        for (int y = 0; y < regions.length; y++)
        {
            for (int x = 0; x < regions[y].length; x++)
            {
                regions[y][x] = new Region(new PlanetLocation(getLocation(),
                        Coord.get(x, y)), Sector.STATION_SYSTEM.equals(
                        getLocation().getSector().getType()) ?
                        getLocation().getMap().getRandomFaction() : null);
            }
        }
        
        updateFaction();
    }
}