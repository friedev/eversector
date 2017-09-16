package boldorf.eversector.map;

import static boldorf.eversector.Main.rng;

import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.map.Region.RegionType;

import static boldorf.eversector.map.Region.RegionType.*;

import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.locations.SectorLocation;

import java.util.ArrayList;
import java.util.List;

import boldorf.eversector.faction.Faction;
import boldorf.eversector.Symbol;

import java.util.Arrays;
import java.util.LinkedList;

import squidpony.squidmath.Coord;
import squidpony.squidmath.MerlinNoise;

/**
 * A planet in a sector that can be interacted with in different ways.
 */
public class Planet implements ColorStringObject
{
    public enum PlanetType
    {
        /*
        VOLCANIC [------XXXXXXX]
        OCEAN    [---XXXXX-----]
        TERRAN   [----XXX------]
        ARID     [--XXXXXXXX---]
        BARREN   [XXXXXXXXXXX--]
        GLACIAL  [XXX----------]
        */

        VOLCANIC("Volcanic", 6, 12, false, MAGMA, ROCK, MOUNTAIN),
        OCEANIC("Ocean", 3, 7, true, OCEAN, COAST, DESERT),
        TERRAN("Terran", 4, 6, true, OCEAN, PLAIN, FOREST, MOUNTAIN),
        ARID("Arid", 2, 8, true, DESERT, DUNES, MOUNTAIN),
        BARREN("Barren", 0, 10, false, ROCK, MOUNTAIN),
        GLACIAL("Glacial", 0, 2, false, FLATS, GLACIER),

        GAS_GIANT("Gas Giant", Symbol.GAS_GIANT.get(), false, false, false),
        ASTEROID_BELT("Asteroid Belt", Symbol.ASTEROID_BELT.get(), false, true, false);

        private String type;
        private char symbol;
        private boolean canLandOn;
        private boolean canMine;
        private int minTemp;
        private int maxTemp;
        private boolean atmosphere;
        private RegionType[] regions;

        PlanetType(String type, char symbol, boolean canLandOn, boolean canMine, boolean atmosphere)
        {
            this.type = type;
            this.symbol = symbol;
            this.canLandOn = canLandOn;
            this.canMine = canMine;
            this.minTemp = 0;
            this.maxTemp = Integer.MAX_VALUE;
            this.atmosphere = atmosphere;
            this.regions = null;
        }

        PlanetType(String type, int minTemp, int maxTemp, boolean atmosphere, RegionType... regions)
        {
            this.type = type + " Planet";
            this.symbol = Symbol.ROCKY_PLANET.get();
            this.canLandOn = true;
            this.canMine = true;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.atmosphere = atmosphere;
            this.regions = regions;
        }

        @Override
        public String toString()
        {return type;}

        public String getType()
        {return type;}

        public char getSymbol()
        {return symbol;}

        public boolean canLandOn()
        {return canLandOn;}

        public boolean canMine()
        {return canMine;}

        public boolean isRocky()
        {return canLandOn && canMine;}

        public boolean canMineFromOrbit()
        {return !canLandOn && canMine;}

        public int getMinTemp()
        {return minTemp;}

        public int getMaxTemp()
        {return maxTemp;}

        public boolean isInTempRange(int temp)
        {return minTemp <= temp && maxTemp >= temp;}

        public boolean hasAtmosphere()
        {return atmosphere;}

        public RegionType getRegionAtElevation(double elevation)
        {
            if (regions == null || regions.length == 0)
            {
                return null;
            }

            elevation = Math.max(0.0, Math.min(1.0, elevation));
            int index = (int) Math.round(((double) (regions.length - 1)) * elevation);
            return regions[index];
        }
    }

    public static final int MIN_REGION_MULTIPLIER = 2;

    public static final int REGION_MULTIPLIER_RANGE = 4;

    /**
     * The minimum number of ore types on a planet.
     */
    public static final int MIN_ORES = 1;

    /**
     * The maximum increase in ores over the minimum (increased by 1 to include 0).
     */
    public static final int ORE_RANGE = 3;

    /**
     * The amount of hull damage done to ships when mining from asteroid belts.
     */
    public static final int ASTEROID_DAMAGE = 1;

    private String name;
    private PlanetType type;
    private final SectorLocation location;
    private Faction faction;
    private List<Ore> ores;
    private Region[][] regions;

    /**
     * Creates a planet with a name, location, and faction.
     *
     * @param name     the name of the planet
     * @param location the location of the planet
     */
    public Planet(String name, SectorLocation location)
    {
        this.name = name;
        this.location = location;
        generateType();

        if (type.canMine())
        {
            generateOre();
        }
        else
        {
            ores = null;
        }
    }

    public void init()
    {
        // Only set the planet's faction if it is a rocky planet
        // Note that unclaim() does not call updateFaction()
        if (type.canLandOn())
        {
            generateRegions();
        }
        else
        {
            unclaim();
        }
    }

    @Override
    public String toString()
    {return type + " " + name;}

    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(), isClaimed() ? getFaction().getColor() : null);
    }

    public String getName()
    {return name;}

    public PlanetType getType()
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
    private void claim(Faction faction)
    {
        if (this.faction == faction)
        {
            return;
        }

        this.faction = faction;
        location.getSector().updateFaction();
    }

    /**
     * Removes claimed status without updating sector factions.
     */
    private void unclaim()
    {faction = null;}

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
        {
            for (int x = 0; x < regions[y].length; x++)
            {
                if (regions[y][x] == region)
                {
                    return Coord.get(x, y);
                }
            }
        }
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
        return containsX(x + getNColumns() / 2) ? x + getNColumns() / 2 : x - getNColumns() / 2;
    }

    /**
     * Calculates the dominant faction on the planet, based on region control.
     */
    public void updateFaction()
    {
        // Note than claim(null) is always used over unclaim() because sector
        // should be refreshed by this calculation as well

        if (type == null || !type.canLandOn())
        {
            // claim(null) must be used instead of unclaim(), so that the
            // sector's faction is updated
            claim(null);
            return;
        }

        int[] control = new int[getLocation().getGalaxy().getFactions().length];

        // Increase the respective counter for each claimed body
        for (Region[] row : regions)
        {
            for (Region region : row)
            {
                if (region != null && region.isClaimed())
                {
                    control[getLocation().getGalaxy().getIndex(region.getFaction())]++;
                }
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

        claim(index == -1 ? null : getLocation().getGalaxy().getFaction(index));
    }

    /**
     * Returns a random region on the planet.
     *
     * @return any of the regions on the planet, chosen at random
     */
    public Region getRandomRegion()
    {
        List<Region> regionList = new ArrayList<>();
        for (Region[] row : regions)
        {
            regionList.addAll(Arrays.asList(row));
        }
        return getRandomRegion(regionList);
    }

    public Coord getRandomCoord()
    {return rng.nextCoord(getNColumns(), getNRows());}

    /**
     * Returns a random region on the planet that is not already controlled by the specified faction.
     *
     * @param faction the faction that regions must not be part of to be selected
     * @return any of the regions on the planet not owned by the specified faction, chosen at random
     */
    public Region getRandomRegion(Faction faction)
    {
        List<Region> unclaimedRegions = new ArrayList<>();

        for (Region[] row : regions)
        {
            for (Region region : row)
            {
                if (region.getType().isLand() && region.getFaction() != faction)
                {
                    unclaimedRegions.add(region);
                }
            }
        }

        return getRandomRegion(unclaimedRegions);
    }

    public Region getRandomOreRegion()
    {
        List<Region> oreRegions = new ArrayList<>();

        for (Region[] row : regions)
        {
            for (Region region : row)
            {
                if (region.hasOre())
                {
                    oreRegions.add(region);
                }
            }
        }

        return getRandomRegion(oreRegions);
    }

    public Coord getRandomOreCoord()
    {
        Region oreRegion = getRandomOreRegion();
        if (oreRegion == null)
        {
            return getRandomCoord();
        }
        return oreRegion.getLocation().getRegionCoord();
    }

    /**
     * Returns a random region selected from a List of regions.
     *
     * @param regions the list of regions from which to select a random one
     * @return any of the regions in the given list, chosen at random
     */
    private static Region getRandomRegion(List<Region> regions)
    {
        return regions == null || regions.isEmpty() ? null : rng.getRandomElement(regions);
    }

    /**
     * Returns the cost to claim a region on the planet.
     *
     * @return the cost of claiming a region on the planet, should be positive
     */
    public int getClaimCost()
    {return Station.CLAIM_COST / getNRegions();}

    /**
     * Returns a symbol for the planet's type.
     *
     * @return the planet's symbol, as a char
     */
    public ColorChar getSymbol()
    {
        return new ColorChar(type.getSymbol(), isClaimed() ? getFaction().getColor() : null);
    }

    /**
     * Returns or generates the ore type of the planet, depending on if it's an asteroid belt.
     *
     * @return the planet's ore type or a randomly generated one
     */
    public Ore getRandomOre()
    {return ores.isEmpty() ? null : ores.get(rng.nextInt(ores.size()));}

    public int getNShips()
    {
        if (!type.canLandOn())
        {
            return 0;
        }

        int nShips = 0;

        for (Region[] row : regions)
        {
            for (Region region : row)
            {
                nShips += region.getShips().size();
            }
        }

        return nShips;
    }

    public int getNShips(Faction faction)
    {
        if (!type.canLandOn())
        {
            return 0;
        }

        int nShips = 0;

        for (Region[] row : regions)
        {
            for (Region region : row)
            {
                nShips += region.getNShips(faction);
            }
        }

        return nShips;
    }

    public List<ColorString> toColorStrings(boolean showFactions)
    {
        List<ColorString> list = new ArrayList<>(getNRows());

        for (Region[] row : regions)
        {
            ColorString rowString = new ColorString();
            for (Region region : row)
            {
                if (showFactions)
                {
                    ColorChar regionChar = new ColorChar(region.toColorChar());
                    regionChar.setForeground(region.isClaimed() ? region.getFaction().getColor() : null);
                    rowString.add(regionChar);
                }
                else
                {
                    rowString.add(region.toColorChar());
                }
            }

            list.add(rowString);
        }

        return list;
    }

    /**
     * Generates a random planet type, including temperature based on distance from the nearest orbit.
     */
    private void generateType()
    {
        Star star = getLocation().getSector().getStar();
        int temp = star.getPowerAt(getLocation().getOrbit());

        List<PlanetType> types = new LinkedList<>();
        for (PlanetType curType : PlanetType.values())
        {
            if (curType.isInTempRange(temp) && !(curType.hasAtmosphere() && star.hasRadiation()))
            {
                types.add(curType);
            }
        }

        type = rng.getRandomElement(types);
    }

    private void generateOre()
    {
        ores = new ArrayList<>();
        int nOres = rng.nextInt(ORE_RANGE) + MIN_ORES;
        for (int i = 0; i < nOres; i++)
        {
            Ore ore = getLocation().getGalaxy().getRandomOre();

            if (!ores.contains(ore))
            {
                ores.add(ore);
            }
        }

        // Add the chance for a region to have no ore
        ores.add(null);
    }

    /**
     * Generates a random amount of regions.
     */
    private void generateRegions()
    {
        int widthMultiplier = rng.nextInt(REGION_MULTIPLIER_RANGE) + MIN_REGION_MULTIPLIER;
        regions = new Region[widthMultiplier + 1][widthMultiplier * 2];

        int[][] heights = MerlinNoise.preCalcNoise2D(regions.length, regions[0].length, rng.nextLong());

        for (int y = 0; y < regions.length; y++)
        {
            for (int x = 0; x < regions[y].length; x++)
            {
                regions[y][x] = new Region(new PlanetLocation(getLocation(), Coord.get(x, y)),
                        type.getRegionAtElevation((double) heights[y][x] / 255.0));
            }
        }

        updateFaction();
    }
}