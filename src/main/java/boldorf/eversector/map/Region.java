package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.Symbol;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.rng;

/**
 * A planetary region with various characteristics.
 *
 * @author Boldorf Smokebane
 */
public class Region implements ColorStringObject
{
    /**
     * A type of region.
     */
    public enum RegionType
    {
        /**
         * A region filled with a sea of magma.
         */
        MAGMA("Magma", false, Symbol.LIQUID_REGION, AsciiPanel.brightRed),

        /**
         * A flat rocky region.
         */
        ROCK("Rock", true, Symbol.FLAT_REGION, AsciiPanel.brightBlack),

        /**
         * A dry, sandy region.
         */
        DESERT("Desert", true, Symbol.FLAT_REGION, AsciiPanel.yellow),

        /**
         * A region of elevated, sandy dunes.
         */
        DUNES("Dunes", true, Symbol.HILL_REGION, AsciiPanel.yellow),

        /**
         * A deep ocean, probably of water.
         */
        OCEAN("Ocean", false, Symbol.LIQUID_REGION, AsciiPanel.brightBlue),

        /**
         * A shallow section of ocean, probably of water.
         */
        COAST("Coast", false, Symbol.LIQUID_REGION, AsciiPanel.brightCyan),

        /**
         * A grassy plain.
         */
        PLAIN("Plains", true, Symbol.FLAT_REGION, AsciiPanel.brightGreen),

        /**
         * A forest of trees.
         */
        FOREST("Forest", true, Symbol.FOREST_REGION, AsciiPanel.green),

        /**
         * A region of tall, rocky mountains.
         */
        MOUNTAIN("Mountainous", true, Symbol.MOUNTAIN_REGION, AsciiPanel.brightBlack),

        /**
         * A flat ice sheet.
         */
        FLATS("Flats", true, Symbol.FLAT_REGION, AsciiPanel.brightCyan),

        /**
         * A large mass of ice.
         */
        GLACIER("Glacier", true, Symbol.HILL_REGION, AsciiPanel.brightCyan);

        /**
         * The name of the region type.
         */
        private final String type;

        /**
         * The symbol representing the region type.
         */
        private final char symbol;

        /**
         * The color of the symbol's foreground.
         */
        private final Color foreground;

        /**
         * True if the region type is land, meaning it can contain ore and be claimed.
         */
        private final boolean isLand;

        /**
         * Creates a new region type with all fields defined.
         *
         * @param type       the name of the region type
         * @param isLand     true if the region is land
         * @param symbol     the symbol representing the region
         * @param foreground the color of the symbol's foreground
         */
        RegionType(String type, boolean isLand, char symbol, Color foreground)
        {
            this.type = type;
            this.isLand = isLand;
            this.symbol = symbol;
            this.foreground = foreground;
        }

        /**
         * Creates a new region type with all fields defined. Uses a Symbol rather than a char.
         *
         * @param type       the name of the region type
         * @param isLand     true if the region is land
         * @param symbol     the symbol representing the region
         * @param foreground the color of the symbol's foreground
         */
        RegionType(String type, boolean isLand, Symbol symbol, Color foreground)
        {
            this(type, isLand, symbol.get(), foreground);
        }

        @Override
        public String toString()
        {
            return type;
        }

        /**
         * Gets the name of the region type.
         *
         * @return the name of the region type
         */
        public String getType()
        {
            return type;
        }

        /**
         * Gets the symbol representing the region type.
         *
         * @return the symbol representing the region type
         */
        public ColorChar getSymbol()
        {
            return new ColorChar(symbol, foreground);
        }

        /**
         * Returns true if the region is land, meaning it can contain ore and be claimed.
         *
         * @return true if the region is land
         */
        public boolean isLand()
        {
            return isLand;
        }
    }

    /**
     * The lowest amount of ore that can be generated in a region. This refers to the ore, not the units of the ore
     * resource gained by mining it.
     */
    private static final int MIN_ORE = 50;

    /**
     * The range of ore above the minimum that can be generated in a region.
     */
    private static final int ORE_RANGE = 451;

    /**
     * The location of the region.
     */
    private final PlanetLocation location;

    /**
     * The ships in the region.
     */
    private List<Ship> ships;

    /**
     * The type of region.
     */
    private RegionType type;

    /**
     * The faction that controls the region.
     */
    private Faction faction;

    /**
     * The type of ore in the region, null if none.
     */
    private Ore ore;

    /**
     * The amount of ore in the region.
     */
    private int nOre;

    /**
     * Generates a claimed region of the given faction on the given planet.
     *
     * @param location the location of the region
     * @param type     the region's type
     */
    public Region(PlanetLocation location, RegionType type)
    {
        if (location == null)
        {
            throw new NullPointerException();
        }

        this.ships = new LinkedList<>();
        this.location = location;
        this.type = type;
        this.faction = null;

        if (type.isLand())
        {
            this.ore = location.getPlanet().getRandomOre();
            this.nOre = rng.nextInt(ORE_RANGE) + MIN_ORE;
        }
    }

    @Override
    public String toString()
    {
        return type + " Region";
    }

    @Override
    public ColorString toColorString()
    {
        return isClaimed() ? new ColorString(toString(), faction.getColor()) : new ColorString(toString());
    }

    /**
     * Returns a ColorChar representation of the region, overwritten by the player's sybmol if they're present in the
     * region.
     *
     * @return a ColorChar representing the region
     */
    public ColorChar toColorChar()
    {
        for (Ship ship : ships)
        {
            if (ship.isPlayer())
            {
                return new ColorChar(Symbol.PLAYER.get(), AsciiPanel.brightWhite);
            }
        }

        return type.getSymbol();
    }

    /**
     * Gets the location of the region.
     *
     * @return the location of the region
     */
    public PlanetLocation getLocation()
    {
        return location;
    }

    /**
     * Gets the type of region.
     *
     * @return the type of region
     */
    public RegionType getType()
    {
        return type;
    }

    /**
     * Gets the faction in control of the region.
     *
     * @return the faction in control of the region
     */
    public Faction getFaction()
    {
        return faction;
    }

    /**
     * Gets the type of ore in the region.
     *
     * @return the type of ore in the region
     */
    public Ore getOre()
    {
        return ore;
    }

    /**
     * Gets the amount of ore in the region.
     *
     * @return the amount of ore in the region
     */
    public int getNOre()
    {
        return nOre;
    }

    /**
     * Returns true if a faction has claimed the region.
     *
     * @return true if the region is claimed
     */
    public boolean isClaimed()
    {
        return faction != null;
    }

    /**
     * Returns true if the region contains ore.
     *
     * @return true if the region contains ore
     */
    public boolean hasOre()
    {
        return ore != null;
    }

    /**
     * Gets the ships in the region.
     *
     * @return the ships in the region
     */
    public List<Ship> getShips()
    {
        return ships;
    }

    /**
     * Claims the region for a given faction and updates the faction of the planet it's on to match.
     *
     * @param faction the faction that will claim the region
     */
    public void claim(Faction faction)
    {
        if (this.faction == faction)
        {
            return;
        }

        this.faction = faction;
        location.getPlanet().updateFaction();
    }

    /**
     * Extracts ore from the region. <b>Currently not in use.</b>
     *
     * @param extracted the amount of ore to extract
     * @return the amount of ore actually extracted
     */
    public int extractOre(int extracted)
    {
        return extracted;
        
        /*
        int actualExtracted = Math.min(nOre, extracted);
        
        nOre -= extracted;
        if (nOre <= 0)
            ore = null;
        
        return actualExtracted;
        */
    }

    /**
     * Returns the number of ships that belong to a specified faction.
     *
     * @param faction the faction to count ships in
     * @return the number of ships in the region that belong to the faction
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
}