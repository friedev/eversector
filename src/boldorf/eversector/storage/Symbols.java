package boldorf.eversector.storage;

import asciiPanel.AsciiPanel;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import static boldorf.eversector.Main.tiles;

public abstract class Symbols
{
    private static final ColorChar
    EMPTY = new ColorChar(ExtChars.DOT, AsciiPanel.brightBlack);
    
    private static final char
    COPYRIGHT    = 0,
    UNDISCOVERED = ' ';
    
    private abstract class Tiles
    {
        public static final char
        PLAYER          = 1,
        WEAK_SHIP       = 2,
        MEDIUM_SHIP     = 3,
        STRONG_SHIP     = 4,
        ROCKY_PLANET    = 5,
        GAS_GIANT       = 6,
        ASTEROID_BELT   = 7,
        TRADE_STATION   = 8,
        BATTLE_STATION  = 9,
        SUBDWARF        = 10,
        DWARF           = 11,
        SUBGIANT        = 12,
        GIANT           = SUBGIANT,
        SUPERGIANT      = 13,
        HYPERGIANT      = SUPERGIANT,
        BINARY_STAR     = 14,
        NEUTRON_STAR    = SUBDWARF,
        PULSAR          = 15,
        STAR_SYSTEM     = DWARF,
        STATION_SYSTEM  = TRADE_STATION,
        LIQUID_REGION   = 19,
        FLAT_REGION     = 20,
        HILL_REGION     = 21,
        MOUNTAIN_REGION = 22,
        FOREST_REGION   = 28,
        CREDITS         = '$';
    }
    
    private abstract class Ascii
    {
        public static final char
        PLAYER          = '@',
        WEAK_SHIP       = '>',
        MEDIUM_SHIP     = ExtChars.ARROW2_R,
        STRONG_SHIP     = ExtChars.SIGMA,
        ROCKY_PLANET    = ExtChars.THETA,
        GAS_GIANT       = ExtChars.CIRCLE,
        ASTEROID_BELT   = ExtChars.INFINITY,
        TRADE_STATION   = '#',
        BATTLE_STATION  = '%',
        SUBDWARF        = '+',
        DWARF           = '*',
        SUBGIANT        = ExtChars.STAR,
        GIANT           = SUBGIANT,
        SUPERGIANT      = ExtChars.CIRCLE,
        HYPERGIANT      = SUPERGIANT,
        BINARY_STAR     = ExtChars.INFINITY,
        NEUTRON_STAR    = SUBDWARF,
        PULSAR          = GIANT,
        STAR_SYSTEM     = ExtChars.STAR,
        STATION_SYSTEM  = TRADE_STATION,
        LIQUID_REGION   = ExtChars.APPROX_EQUAL,
        FLAT_REGION     = '+',
        HILL_REGION     = ExtChars.BUMP,
        MOUNTAIN_REGION = ExtChars.TRIANGLE_U,
        FOREST_REGION   = ExtChars.SPADE,
        CREDITS         = ExtChars.STAR;
    }
    
    public static ColorChar empty()
        {return EMPTY;}
    
    public static char copyright()
        {return COPYRIGHT;}
    
    public static char undiscovered()
        {return UNDISCOVERED;}
    
    public static ColorChar player()
    {
        return new ColorChar(tiles ? Tiles.PLAYER : Ascii.PLAYER,
                AsciiPanel.brightWhite);
    }
    
    public static char weakShip()
        {return tiles ? Tiles.WEAK_SHIP : Ascii.WEAK_SHIP;}
    
    public static char mediumShip()
        {return tiles ? Tiles.MEDIUM_SHIP : Ascii.MEDIUM_SHIP;}
    
    public static char strongShip()
        {return tiles ? Tiles.STRONG_SHIP : Ascii.STRONG_SHIP;}
    
    public static char rockyPlanet()
        {return tiles ? Tiles.ROCKY_PLANET : Ascii.ROCKY_PLANET;}
    
    public static char gasGiant()
        {return tiles ? Tiles.GAS_GIANT : Ascii.GAS_GIANT;}
    
    public static char asteroidBelt()
        {return tiles ? Tiles.ASTEROID_BELT : Ascii.ASTEROID_BELT;}
    
    public static char tradeStation()
        {return tiles ? Tiles.TRADE_STATION : Ascii.TRADE_STATION;}
    
    public static char battleStation()
        {return tiles ? Tiles.BATTLE_STATION : Ascii.BATTLE_STATION;}
    
    public static char subdwarf()
        {return tiles ? Tiles.SUBDWARF : Ascii.SUBDWARF;}
    
    public static char dwarf()
        {return tiles ? Tiles.DWARF : Ascii.DWARF;}
    
    public static char subgiant()
        {return tiles ? Tiles.SUBGIANT : Ascii.SUBGIANT;}
    
    public static char giant()
        {return tiles ? Tiles.GIANT : Ascii.GIANT;}
    
    public static char supergiant()
        {return tiles ? Tiles.SUPERGIANT : Ascii.SUPERGIANT;}
    
    public static char hypergiant()
        {return tiles ? Tiles.HYPERGIANT : Ascii.HYPERGIANT;}
    
    public static char binaryStar()
        {return tiles ? Tiles.BINARY_STAR : Ascii.BINARY_STAR;}
    
    public static char neutronStar()
        {return tiles ? Tiles.NEUTRON_STAR : Ascii.NEUTRON_STAR;}
    
    public static char pulsar()
        {return tiles ? Tiles.PULSAR : Ascii.PULSAR;}
    
    public static char starSystem()
        {return tiles ? Tiles.STAR_SYSTEM : Ascii.STAR_SYSTEM;}
    
    public static char stationSystem()
        {return tiles ? Tiles.STATION_SYSTEM : Ascii.STATION_SYSTEM;}
    
    public static char liquidRegion()
        {return tiles ? Tiles.LIQUID_REGION : Ascii.LIQUID_REGION;}
    
    public static char flatRegion()
        {return tiles ? Tiles.FLAT_REGION : Ascii.FLAT_REGION;}
    
    public static char hillRegion()
        {return tiles ? Tiles.HILL_REGION : Ascii.HILL_REGION;}
    
    public static char mountainRegion()
        {return tiles ? Tiles.MOUNTAIN_REGION : Ascii.MOUNTAIN_REGION;}
    
    public static char forestRegion()
        {return tiles ? Tiles.FOREST_REGION : Ascii.FOREST_REGION;}
    
    public static char credits()
        {return tiles ? Tiles.CREDITS : Ascii.CREDITS;}
}