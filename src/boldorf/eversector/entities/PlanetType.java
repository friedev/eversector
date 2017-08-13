package boldorf.eversector.entities;

import boldorf.apwt.ExtChars;

public enum PlanetType
{
    BARREN_PLANET ("Barren Planet"),
    TERRAN_PLANET ("Terran Planet"),
    GLACIAL_PLANET("Glacial Planet"),
    GAS_GIANT     ("Gas Giant",     ExtChars.CIRCLE,   false, false, false),
    ASTEROID_BELT ("Asteroid Belt", ExtChars.INFINITY, false, true,  false);
    
    private String type;
    private char symbol;
    private boolean canLandOn;
    private boolean canMine;
    private boolean hasOre;
    
    PlanetType(String type, char symbol, boolean landable, boolean mineable,
            boolean ore)
    {
        this.type = type;
        this.symbol = symbol;
        this.canLandOn = landable;
        this.canMine = mineable;
        this.hasOre = ore;
    }
    
    PlanetType(String type)
        {this(type, ExtChars.THETA, true, true, true);}
    
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
    
    public boolean hasOre()
        {return hasOre;}
    
    public boolean canMineFromOrbit()
        {return !canLandOn && canMine;}
    
    public static PlanetType getRockyType(int starPower)
    {
        if (starPower >= 6)
            return PlanetType.BARREN_PLANET;
        
        if (starPower <= 3)
            return PlanetType.TERRAN_PLANET;
        
        return PlanetType.GLACIAL_PLANET;
    }
}
