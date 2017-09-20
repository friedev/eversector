package boldorf.eversector.map;

import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Symbol;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.locations.SectorLocation;
import boldorf.eversector.map.Region.RegionType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.MerlinNoise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.rng;
import static boldorf.eversector.map.Region.RegionType.*;

/**
 * A planet in a sector that can be interacted with in different ways.
 *
 * @author Boldorf Smokebane
 */
public class Planet implements ColorStringObject
{
    /**
     * The enum Planet type.
     */
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

        /**
         * A planet with extreme volcanic activity.
         */
        VOLCANIC("Volcanic", 6, 12, false, MAGMA, MAGMA, ROCK, MOUNTAIN, MOUNTAIN),

        /**
         * A planet primarily covered in oceans.
         */
        OCEANIC("Ocean", 3, 7, true, OCEAN, OCEAN, COAST, DESERT),

        /**
         * A planet shrouded in dense forests.
         */
        JUNGLE("Jungle", 5, 7, true, OCEAN, COAST, FOREST, FOREST, MOUNTAIN),

        /**
         * An Earth-like planet with water and vegetation.
         */
        TERRAN("Terran", 4, 6, true, OCEAN, OCEAN, PLAIN, FOREST, MOUNTAIN),

        /**
         * A dry, Mars-like planet.
         */
        ARID("Arid", 2, 8, true, DESERT, DESERT, DUNES, MOUNTAIN, MOUNTAIN),

        /**
         * A barren, rocky planet with no atmosphere.
         */
        BARREN("Barren", 0, 10, false, ROCK, MOUNTAIN),

        /**
         * A cold planet covered in ice.
         */
        GLACIAL("Glacial", 0, 2, false, FLATS, GLACIER),

        /**
         * A massive planet consisting mostly of gas.
         */
        GAS_GIANT("Gas Giant", Symbol.GAS_GIANT.get(), false, false),

        /**
         * A belt of asteroids.
         */
        ASTEROID_BELT("Asteroid Belt", Symbol.ASTEROID_BELT.get(), true, false);

        /**
         * The name of the planet type.
         */
        private final String type;

        /**
         * The character representing the planet type.
         */
        private final char symbol;

        /**
         * True if the planet can be landed on.
         */
        private final boolean canLandOn;

        /**
         * True if the planet can be mined.
         */
        private final boolean canMine;

        /**
         * The lowest temperature at which the planet can generate.
         *
         * @see Star#getPowerAt(int)
         */
        private final int minTemp;

        /**
         * The highest temperature at which the planet can generate.
         *
         * @see Star#getPowerAt(int)
         */
        private final int maxTemp;

        /**
         * True if the planet has an atmosphere. Influences generation around high-radiation stars.
         */
        private final boolean atmosphere;

        /**
         * The types of regions that can be generated on the planet, in ascending order of altitude.
         */
        private final RegionType[] regions;

        /**
         * Creates a rocky planet.
         *
         * @param type       the name of the planet type
         * @param minTemp    the lowest temperature the planet can generate at
         * @param maxTemp    the highest temperature the planet can generate at
         * @param atmosphere true if the planet has an atmosphere
         * @param regions    the types of regions that can generate on the planet, in ascending order of altitude
         */
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

        /**
         * Creates a planet that cannot be landed on.
         *
         * @param type       the name of the planet type
         * @param symbol     the symbol representing the planet type
         * @param canMine    true if the planet can be mined
         * @param atmosphere true if the planet has an atmosphere
         */
        PlanetType(String type, char symbol, boolean canMine, boolean atmosphere)
        {
            this.type = type;
            this.symbol = symbol;
            this.canLandOn = false;
            this.canMine = canMine;
            this.minTemp = 0;
            this.maxTemp = Integer.MAX_VALUE;
            this.atmosphere = atmosphere;
            this.regions = null;
        }

        @Override
        public String toString()
        {
            return type;
        }

        /**
         * Gets the name of the planet type.
         *
         * @return the name of the planet type
         */
        public String getType()
        {
            return type;
        }

        /**
         * Gets the symbol representing the planet type.
         *
         * @return the symbol representing the planet type
         */
        public char getSymbol()
        {
            return symbol;
        }

        /**
         * Returns true if the planet can be landed on.
         *
         * @return true if the planet can be landed on
         */
        public boolean canLandOn()
        {
            return canLandOn;
        }

        /**
         * Return true if the planet can be mined.
         *
         * @return true if the planet can be mined
         */
        public boolean canMine()
        {
            return canMine;
        }

        /**
         * Returns true if the planet is rocky, meaning it can be landed on and mined.
         *
         * @return true if the planet is rocky
         */
        public boolean isRocky()
        {
            return canLandOn && canMine;
        }

        /**
         * Returns true if the planet cannot be landed on, but can be mined for orbit.
         *
         * @return true if the planet can be mined from orbit
         */
        public boolean canMineFromOrbit()
        {
            return !canLandOn && canMine;
        }

        /**
         * Gets the minimum temperature of the planet.
         *
         * @return the mininum temperature of the planet
         */
        public int getMinTemp()
        {
            return minTemp;
        }

        /**
         * Gets the maximum temperature of the planet.
         *
         * @return the maximum temperature of the planet
         */
        public int getMaxTemp()
        {
            return maxTemp;
        }

        /**
         * Returns true if the given temperature is in the planet's accepted range.
         *
         * @param temp the temperature to check
         * @return true if the given temperature is in the planet's accepted range
         * @see Star#getPowerAt(int)
         */
        public boolean isInTempRange(int temp)
        {
            return minTemp <= temp && maxTemp >= temp;
        }

        /**
         * Returns true if the planet has an atmosphere.
         *
         * @return true if the planet has an atmosphere
         */
        public boolean hasAtmosphere()
        {
            return atmosphere;
        }

        /**
         * Gets the region type that would generate at the given elevation.
         *
         * @param elevation the elevation to find a region for
         * @return the region type that would generate at the given elevation
         */
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

    /**
     * The amount of hull damage done to ships when mining from asteroid belts.
     */
    public static final int ASTEROID_DAMAGE = 1;

    /**
     * The minimum region multiplier.
     *
     * @see #generateRegions() for an explanation of the multiplier formula
     */
    private static final int MIN_REGION_MULTIPLIER = 2;

    /**
     * The range of region multipliers.
     *
     * @see #generateRegions() for an explanation of the multiplier formula
     */
    private static final int REGION_MULTIPLIER_RANGE = 4;

    /**
     * The minimum number of ore types on a planet.
     *
     * @see #generateOre()
     */
    private static final int MIN_ORES = 1;

    /**
     * The maximum increase in ores over the minimum.
     *
     * @see #generateOre()
     */
    private static final int ORE_RANGE = 3;

    /**
     * The name of the planet.
     */
    private String name;

    /**
     * The type of planet.
     */
    private PlanetType type;

    /**
     * The location of the planet in its sector.
     */
    private final SectorLocation location;

    /**
     * The dominant faction on the planet.
     */
    private Faction faction;

    /**
     * The possible ores on the planet.
     */
    private List<Ore> ores;

    /**
     * The regions on the planet.
     */
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

    /**
     * Initializes regions if applicable.
     */
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
    {
        return type + " " + name;
    }

    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(), isClaimed() ? getFaction().getColor() : null);
    }

    /**
     * Gets the name of the planet.
     *
     * @return the name of the planet
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the type of planet.
     *
     * @return the type of planet
     */
    public PlanetType getType()
    {
        return type;
    }

    /**
     * Gets the location of the planet.
     *
     * @return the location of the planet
     */
    public SectorLocation getLocation()
    {
        return location;
    }

    /**
     * Gets the dominant faction on the planet.
     *
     * @return the dominant faction on the planet
     */
    public Faction getFaction()
    {
        return faction;
    }

    /**
     * Returns true if the planet has a dominant faction.
     *
     * @return true if the planet has a dominant faction
     */
    public boolean isClaimed()
    {
        return faction != null;
    }

    /**
     * Claims the planet for the specified faction.
     *
     * @param faction the faction that will claim the planet
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
    {
        faction = null;
    }

    /**
     * Gets the regions on the planet.
     *
     * @return the regions on the planet
     */
    public Region[][] getRegions()
    {
        return regions;
    }

    /**
     * Gets the number of regions on the planet.
     *
     * @return the number of regions on the planet
     */
    public int getNRegions()
    {
        return getNRows() * getNColumns();
    }

    /**
     * Gets the number of region rows on the planet.
     *
     * @return the number of region rows on the planet
     */
    public int getNRows()
    {
        return regions.length;
    }

    /**
     * Gets the number of region columns on the planet.
     *
     * @return the number of region columns on the planet
     */
    public int getNColumns()
    {
        return regions[0].length;
    }

    /**
     * Gets the region at the given coordinates.
     *
     * @param location the coordinates to get a region from
     * @return the region at the given coordinates
     */
    public Region regionAt(Coord location)
    {
        return regions[location.y][location.x];
    }

    /**
     * Gets the index of the given region.
     *
     * @param region the region to find an index for
     * @return the index of the given region as a Coord
     */
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

    /**
     * Return true if the planet contains a region at the given coordinates.
     *
     * @param location the location to check
     * @return true if the planet contains a region at the given coordinates
     */
    public boolean contains(Coord location)
    {
        return containsX(location.x) && containsY(location.y);
    }

    /**
     * Returns true if the given x value is contained in the region array.
     *
     * @param x the x value to check
     * @return true if the given x value is contained in the region array
     */
    public boolean containsX(int x)
    {
        return x >= 0 && getNColumns() >= x + 1;
    }

    /**
     * Returns true if the given y value is contained in the region array.
     *
     * @param y the y value to check
     * @return true if the given y value is contained in the region array
     */
    public boolean containsY(int y)
    {
        return y >= 0 && getNRows() >= y + 1;
    }

    /**
     * Gets the central region of the planet.
     *
     * @return the central region of the planet
     */
    public Coord getCenter()
    {
        return Coord.get(getNColumns() / 2, getNRows() / 2);
    }

    /**
     * Gets the x value on the opposite side of the planet. Used when ships travel over the planet's poles.
     *
     * @param x the initial x value
     * @return the x value on the opposite side of the planet
     */
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

        claim(index == -1 ? null : getLocation().getGalaxy().getFactions()[index]);
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

    /**
     * Gets random coordinates on the planet.
     *
     * @return random coordinates on the planet
     */
    public Coord getRandomCoord()
    {
        return rng.nextCoord(getNColumns(), getNRows());
    }

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

    /**
     * Gets a random region on the planet with ore.
     *
     * @return a random region on the planet with ore
     */
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

    /**
     * Gets the coordinates of a random region on the planet with ore.
     *
     * @return the coordinates of a random region on the planet with ore
     */
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
    {
        return Station.CLAIM_COST / getNRegions();
    }

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
    {
        return ores.isEmpty() ? null : ores.get(rng.nextInt(ores.size()));
    }

    /**
     * Gets the number of ships on the planet.
     *
     * @return the number of ships on the planet
     */
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

    /**
     * Gets the number of ships on the planet that belong to the given faction.
     *
     * @param faction the faction that ships must be in
     * @return the number of ships on the planet that belong to the given faction
     */
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

    /**
     * Returns the planet represented as a list of ColorStrings. Each symbol represents a region on the planet.
     *
     * @param showFactions if true, will show faction colors instead of region colors
     * @return the list of ColorStrings representing the planet
     */
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

    /**
     * Chooses ore for each region on the planet.
     */
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