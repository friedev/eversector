package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.storage.Symbols;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/** A planetary region with various characteristics. */
public class Region implements ColorStringObject
{
    public enum RegionType
    {
        MAGMA("Magma", false, Symbols.liquidRegion(), AsciiPanel.brightRed),
        ROCK ("Rock",  true,  Symbols.flatRegion(),   AsciiPanel.brightBlack),
        
        DESERT("Desert", true, Symbols.flatRegion(), AsciiPanel.yellow),
        DUNES ("Dunes",  true, Symbols.hillRegion(), AsciiPanel.yellow),
        
        OCEAN   ("Ocean",       false, Symbols.liquidRegion(),   AsciiPanel.brightBlue),
        COAST   ("Coast",       false, Symbols.liquidRegion(),   AsciiPanel.brightCyan),
        PLAIN   ("Plains",      true,  Symbols.flatRegion(),     AsciiPanel.brightGreen),
        FOREST  ("Forest",      true,  Symbols.forestRegion(),   AsciiPanel.green),
        MOUNTAIN("Mountainous", true,  Symbols.mountainRegion(), AsciiPanel.brightBlack),
        
        FLATS  ("Flats",   true, Symbols.flatRegion(), AsciiPanel.brightCyan),
        GLACIER("Glacier", true, Symbols.hillRegion(), AsciiPanel.brightCyan);
        
        private String type;
        private char symbol;
        private Color foreground;
        private boolean hasOre;
        
        RegionType(String type, boolean hasOre, char symbol, Color foreground)
        {
            this.type       = type;
            this.hasOre     = hasOre;
            this.symbol     = symbol;
            this.foreground = foreground;
        }
        
        @Override
        public String toString()
            {return type;}
        
        public String getType()
            {return type;}
        
        public ColorChar getSymbol()
            {return new ColorChar(symbol, foreground);}
        
        public boolean hasOre()
            {return hasOre;}
    }
    
    private final PlanetLocation location;
    private List<Ship> ships;
    private RegionType type;
    private Faction    faction;
    private Ore        ore;
    
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
        
        if (type.hasOre())
            this.ore = location.getPlanet().getRandomOre();
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
                return Symbols.player();
        
        return type.getSymbol();
    }
    
    public PlanetLocation getLocation() {return location;}
    public RegionType getType()    {return type;           }
    public Faction    getFaction() {return faction;        }
    public Ore        getOre()     {return ore;            }
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