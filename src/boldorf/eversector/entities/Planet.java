package boldorf.eversector.entities;

import boldorf.util.Utility;
import static boldorf.eversector.Main.rng;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import java.util.ArrayList;
import java.util.List;
import boldorf.eversector.map.Map;
import boldorf.eversector.map.Sector;
import boldorf.eversector.map.faction.Faction;

/** A planet in a sector that can be interacted with in different ways. */
public class Planet extends CelestialBody implements ColorStringObject
{
    /** The minimum number of regions a planet can have. */
    public static final int MIN_REGIONS = 3;
    
    /**
     * The maximum increase in factions over the minimum (increased by 1 to 
     * include 0).
     */
    public static final int REGION_RANGE = 5;
    
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
    
    private PlanetType   type;
    private List<Ore>    ores;
    private List<Region> regions;
    
    /**
     * Creates a planet with a name, orbit, faction, and sector.
     * @param name the name of the planet
     * @param orbit the orbit of the planet
     * @param faction the faction that has claimed the planet
     * @param sector the sector the planet belongs to
     */
    public Planet(String name, int orbit, Faction faction, Sector sector)
    {
        super(name, orbit, faction, sector);
        
        generateType();
        
        if (type.hasOre())
            generateOre();
        else
            ores = null;
        
        // Only set the planet's faction if it is a rocky planet
        // Note that unclaim() does not call updateFaction()
        if (type.canLandOn())
            generateRegions();
        else
            unclaim();
    }
    
    public Planet(String name, int orbit, Sector sector)
        {this(name, orbit, null, sector);}
    
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
    
    public List<Region> getRegions()
        {return regions;}
    
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
        
        Map map = getSector().getMap();
        
        int[] control = new int[map.getFactions().length];
        
        // Increase the respective counter for each claimed body
        for (Region region: regions)
            if (region != null && region.isClaimed())
                control[map.getIndex(region.getFaction())]++;
        
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
        
        claim(index == -1 ? null : map.getFaction(index));
    }
    
    /**
     * Returns true if there is a region with the given type on this planet.
     * @param type the type of region to look for
     * @return true if a search for the region does not return null
     */
    public boolean hasRegion(String type)
    {
        for (Region region: regions)
            if (region.getType().equalsIgnoreCase(type))
                return true;
        return false;
    }
    
    /**
     * Returns a random region on the planet.
     * @return any of the regions on the planet, chosen at random
     */
    public Region getRandomRegion()
        {return getRandomRegion(regions);}
    
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
        
        for (Region region: regions)
            if (region.getFaction() != faction)
                unclaimedRegions.add(region);
        
        return getRandomRegion(unclaimedRegions);
    }
    
    public Region getRandomOreRegion()
    {
        ArrayList<Region> oreRegions = new ArrayList<>();
        
        for (Region region: regions)
            if (region.hasOre())
                oreRegions.add(region);
        
        return getRandomRegion(oreRegions);
    }
    
    /**
     * Returns a random region selected from a List of regions.
     * @param regions the list of regions from which to select a random one
     * @return any of the regions in the given list, chosen at random
     */
    private static Region getRandomRegion(List<Region> regions)
    {
        return regions == null || regions.isEmpty() ?
                null : regions.get(rng.nextInt(regions.size()));
    }
    
    /**
     * Returns the cost to claim a region on the planet.
     * @return the cost of claiming a region on the planet, should be positive
     */
    @Override
    public int getClaimCost()
        {return CLAIM_COST / regions.size();}
    
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
        
        for (Region region: regions)
            nShips += region.getShips().size();
        
        return nShips;
    }
    
    public int getNShips(Faction faction)
    {
        if (!type.canLandOn())
            return 0;
        
        int nShips = 0;
        
        for (Region region: regions)
            nShips += region.getNShips(faction);
        
        return nShips;
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
        if (getSector().getStar().isNebular() && rng.nextBoolean())
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
        return PlanetType.getRockyType(
                getSector().getStar().getPowerAt(getOrbit()));
    }
    
    private void generateOre()
    {
        ores = new ArrayList<>();
        int nOres = rng.nextInt(ORE_RANGE) + MIN_ORES;
        for (int i = 0; i < nOres; i++)
        {
            Ore ore = getSector().getMap().getRandomOre();
            
            if (!ores.contains(ore))
                ores.add(ore);
        }
        
        // Add the chance for a region to have no ore
        ores.add(null);
    }
    
    /** Generates a random amount of regions. */
    private void generateRegions()
    {
        int nRegions = rng.nextInt(REGION_RANGE) + MIN_REGIONS + 1;
        regions = new ArrayList<>();
        for (int i = 0; i < nRegions; i++)
        {
            Faction ruler = Sector.STATION_SYSTEM.equals(getSector().getType())
                    ? getSector().getMap().getRandomFaction() : null;
            regions.add(new Region(this, ruler));
        }
        
        updateFaction();
    }
}