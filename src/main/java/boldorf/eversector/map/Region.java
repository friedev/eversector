package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import static boldorf.eversector.Main.rng;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.storage.Symbol;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/** A planetary region with various characteristics. */
public class Region implements ColorStringObject
{
    public enum RegionType
    {
        MAGMA("Magma", false, Symbol.LIQUID_REGION, AsciiPanel.brightRed),
        ROCK ("Rock",  true,  Symbol.FLAT_REGION,   AsciiPanel.brightBlack),
        
        DESERT("Desert", true, Symbol.FLAT_REGION, AsciiPanel.yellow),
        DUNES ("Dunes",  true, Symbol.HILL_REGION, AsciiPanel.yellow),
        
        OCEAN   ("Ocean",       false, Symbol.LIQUID_REGION,   AsciiPanel.brightBlue),
        COAST   ("Coast",       false, Symbol.LIQUID_REGION,   AsciiPanel.brightCyan),
        PLAIN   ("Plains",      true,  Symbol.FLAT_REGION,     AsciiPanel.brightGreen),
        FOREST  ("Forest",      true,  Symbol.FOREST_REGION,   AsciiPanel.green),
        MOUNTAIN("Mountainous", true,  Symbol.MOUNTAIN_REGION, AsciiPanel.brightBlack),
        
        FLATS  ("Flats",   true, Symbol.FLAT_REGION, AsciiPanel.brightCyan),
        GLACIER("Glacier", true, Symbol.HILL_REGION, AsciiPanel.brightCyan);
        
        private String type;
        private char symbol;
        private Color foreground;
        private boolean isLand;
        
        RegionType(String type, boolean isLand, char symbol, Color foreground)
        {
            this.type       = type;
            this.isLand     = isLand;
            this.symbol     = symbol;
            this.foreground = foreground;
        }
        
        RegionType(String type, boolean hasOre, Symbol symbol, Color foreground)
            {this(type, hasOre, symbol.get(), foreground);}
        
        @Override
        public String toString()
            {return type;}
        
        public String getType()
            {return type;}
        
        public ColorChar getSymbol()
            {return new ColorChar(symbol, foreground);}
        
        public boolean isLand()
            {return isLand;}
    }
    
    public static final int MIN_ORE = 50;
    public static final int ORE_RANGE = 451;
    
    private final PlanetLocation location;
    private List<Ship> ships;
    private RegionType type;
    private Faction    faction;
    private Ore        ore;
    private int        nOre;
    
    /**
     * Generates a claimed region of the given faction on the given planet.
     * @param location the location of the region
     * @param type the region's type
     */
    public Region(PlanetLocation location, RegionType type)
    {
        if (location == null)
            throw new NullPointerException();
        
        this.ships    = new LinkedList<>();
        this.location = location;
        this.type     = type;
        this.faction  = null;
        
        if (type.isLand())
        {
            this.ore = location.getPlanet().getRandomOre();
            this.nOre = rng.nextInt(ORE_RANGE) + MIN_ORE;
        }
    }
    
    @Override
    public String toString()
        {return type + " Region";}
    
    @Override
    public ColorString toColorString()
    {
        return isClaimed() ? new ColorString(toString(), faction.getColor()) :
                new ColorString(toString());
    }
    
    public ColorChar toColorChar()
    {
        for (Ship ship: ships)
            if (ship.isPlayer())
                return new ColorChar(Symbol.PLAYER.get(),
                        AsciiPanel.brightWhite);
        
        return type.getSymbol();
    }
    
    public PlanetLocation getLocation() {return location;}
    public RegionType getType()    {return type;           }
    public Faction    getFaction() {return faction;        }
    public Ore        getOre()     {return ore;            }
    public int        getNOre()    {return nOre;           }
    public boolean    isClaimed()  {return faction != null;}
    public boolean    hasOre()     {return ore != null;    }
    public List<Ship> getShips()   {return ships;          }
    
    /**
     * Claims the region for a given faction and updates the faction of the
     * planet it's on to match.
     * @param faction the faction that will claim the region
     */
    public void claim(Faction faction)
    {
        if (this.faction == faction)
            return;
        
        this.faction = faction;
        location.getPlanet().updateFaction();
    }
    
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
     * @param faction the faction to count ships in
     * @return the number of ships docked with the station that belong to the
     * faction, will be non-negative
     */
    public int getNShips(Faction faction)
    {
        int nShips = 0;
        
        for (Ship ship: ships)
            if (ship.getFaction() == faction)
                nShips++;
        
        return nShips;
    }
}