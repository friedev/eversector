package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.util.Utility;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Main;
import java.awt.Color;

/** A star that possesses a type and a power level. */
public class Star implements ColorStringObject
{
    public static final double SPECIAL_CHANCE = 0.1;
    
    private enum StarSize
    {
        SUBDWARF  ("Subdwarf",   '+',             0.2,   4 ),
        DWARF     ("Dwarf",      '*',             0.6,   5 ),
        SUBGIANT  ("Subgiant",   ExtChars.STAR,   0.1,   7 ),
        GIANT     ("Giant",      ExtChars.STAR,   0.05,  8 ),
        SUPERGIANT("Supergiant", ExtChars.CIRCLE, 0.025, 10),
        HYPERGIANT("Hypergiant", ExtChars.CIRCLE, 0.025, 12);
        
        private String name;
        private char symbol;
        private double probability;
        private int mass;
        
        StarSize(String name, char symbol, double probability, int mass)
        {
            this.name = name;
            this.symbol = symbol;
            this.probability = probability;
            this.mass = mass;
        }
        
        @Override
        public String toString()
            {return name;}
        
        public String getName()
            {return name;}
        
        public char getSymbol()
            {return symbol;}
        
        public double getProbability()
            {return probability;}
        
        public int getMass()
            {return mass;}
        
        public static StarSize select()
        {
            double[] probabilities = new double[StarSize.values().length];
            for (int i = 0; i < StarSize.values().length; i++)
                probabilities[i] = StarSize.values()[i].probability;
            return (StarSize) Utility.select(Main.rng, StarSize.values(),
                    probabilities);
        }
    }
    
    private enum StarColor
    {
        BLUE  ("Blue",   AsciiPanel.brightCyan,   true),
        YELLOW("Yellow", AsciiPanel.brightYellow, false),
        RED   ("Red",    AsciiPanel.brightRed,    false);
        
        private String  name;
        private Color   color;
        private boolean radiation;
        
        StarColor(String name, Color color, boolean radiation)
        {
            this.name      = name;
            this.color     = color;
            this.radiation = radiation;
        }
        
        @Override
        public String toString()
            {return name;}
        
        public String getName()
            {return name;}
        
        public Color getColor()
            {return color;}
        
        public boolean hasRadiation()
            {return radiation;}
        
        public static StarColor select()
            {return Main.rng.getRandomElement(StarColor.values());}
    }
    
    private enum SpecialStar
    {
        BROWN_DWARF(new Star("Brown Dwarf", AsciiPanel.yellow,
                StarSize.SUBDWARF.getSymbol(), StarSize.SUBDWARF.getMass(),
                false)),
        WHITE_DWARF(new Star("White Dwarf", AsciiPanel.brightWhite,
                StarSize.SUBDWARF.getSymbol(), StarSize.SUBDWARF.getMass(),
                false)),
        BINARY_STAR(new Star("Binary Star", AsciiPanel.brightWhite,
                ExtChars.INFINITY, StarSize.SUBGIANT.getMass(), false)),
        NEUTRON_STAR(new Star("Neutron Star", AsciiPanel.brightCyan,
                StarSize.SUBDWARF.getSymbol(), StarSize.GIANT.getMass(), true)),
        PULSAR(new Star("Pulsar", NEUTRON_STAR.star.color,
                NEUTRON_STAR.star.symbol, NEUTRON_STAR.star.mass, true));
        
        private Star star;
        
        SpecialStar(Star star)
            {this.star = star;}
    }
    
    private String  name;
    private Color   color;
    private char    symbol;
    private int     mass;
    private boolean radiation;
    
    public Star(String name, Color color, char symbol, int mass,
            boolean radiation)
    {
        this.name      = name;
        this.color     = color;
        this.symbol    = symbol;
        this.mass      = mass;
        this.radiation = radiation;
    }
    
    public Star(Star copying)
    {
        this(copying.name, copying.color, copying.symbol, copying.mass,
                copying.radiation);
    }
    
    private Star(StarSize size, StarColor color)
    {
        this.name      = color.getName() + " " + size.getName();
        this.color     = color.getColor();
        this.symbol    = size.getSymbol();
        this.mass      = size.getMass();
        this.radiation = color.hasRadiation();
    }
    
    public Star(Nebula nebula)
        {this(generate(nebula));}
    
    public Star()
        {this(generate());}
    
    public static Star generate(Nebula nebula)
    {
        if (Utility.getChance(Main.rng, SPECIAL_CHANCE))
            return Main.rng.getRandomElement(SpecialStar.values()).star;
        return new Star(StarSize.select(), StarColor.select());
    }
    
    public static Star generate()
        {return generate(null);}
    
    @Override
    public String toString()
        {return name;}
    
    @Override
    public ColorString toColorString()
        {return new ColorString(name, color);}
    
    public String getName()
        {return name;}
    
    public ColorChar getSymbol()
        {return new ColorChar(symbol, color);}
    
    public int getMass()
        {return mass;}
    
    public boolean hasRadiation()
        {return radiation;}
    
    /**
     * Calculates the power level of the star at a certain orbit.
     * @param orbit the orbit at which to calculate the star's power, must be a
     * valid orbit
     * @return the reduced power of the star at the orbit, -1 if the orbit is
     * invalid
     */
    public int getPowerAt(int orbit)
        {return orbit > 0 ? Math.max(0, mass - (orbit - 1)) : -1;}
    
    /**
     * Calculates the amount of energy generated by a solar array at the given
     * orbit.
     * @param orbit the orbit at which to calculate the star's solar power, must
     * be a valid orbit
     * @return the power generated by a solar array at the given orbit of the
     * star, -1 if the orbit is invalid
     */
    public int getSolarPowerAt(int orbit)
        {return getPowerAt(orbit) / (StarSize.SUBDWARF.getMass()) + 1;}
}