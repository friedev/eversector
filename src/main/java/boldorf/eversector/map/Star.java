package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.util.Utility;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Main;
import boldorf.eversector.storage.Symbol;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/** A star that possesses a type and a power level. */
public class Star implements ColorStringObject
{
    public static final double SPECIAL_CHANCE = 0.1;
    
    private enum StarMass
    {
        SUBDWARF  ("Subdwarf",   Symbol.SUBDWARF,   0.2,   4,  true ),
        DWARF     ("Dwarf",      Symbol.SUBDWARF,      0.6,   5,  true ),
        SUBGIANT  ("Subgiant",   Symbol.SUBGIANT,   0.1,   7,  false),
        GIANT     ("Giant",      Symbol.GIANT,      0.05,  8,  false),
        SUPERGIANT("Supergiant", Symbol.SUPERGIANT, 0.025, 10, false),
        HYPERGIANT("Hypergiant", Symbol.HYPERGIANT, 0.025, 12, false);
        
        private String  name;
        private char    symbol;
        private double  probability;
        private int     mass;
        private boolean inNebula;
        
        StarMass(String name, char symbol, double probability, int mass,
                boolean inNebula)
        {
            this.name        = name;
            this.symbol      = symbol;
            this.probability = probability;
            this.mass        = mass;
            this.inNebula    = inNebula;
        }
        
        StarMass(String name, Symbol symbol, double probability, int mass,
                boolean inNebula)
            {this(name, symbol.get(), probability, mass, inNebula);}
        
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
        
        public boolean inNebula()
            {return inNebula;}
        
        public static StarMass select()
        {
            double[] probabilities = new double[StarMass.values().length];
            for (int i = 0; i < StarMass.values().length; i++)
                probabilities[i] = StarMass.values()[i].probability;
            return (StarMass) Utility.select(Main.rng, StarMass.values(),
                    probabilities);
        }
        
        public static StarMass select(Nebula nebula)
        {
            if (nebula == null)
                return select();
            
            List<StarMass> masses = new LinkedList<>();
            for (StarMass mass: values())
                if (mass.inNebula)
                    masses.add(mass);
            
            double[] probabilities = new double[masses.size()];
            double totalProbability = 0.0;
            for (int i = 0; i < masses.size(); i++)
            {
                probabilities[i] = masses.get(i).probability;
                totalProbability += masses.get(i).probability;
            }
            
            probabilities[0] += 1.0 - totalProbability;
            
            return (StarMass) Utility.select(Main.rng, masses.toArray(),
                    probabilities);
        }
    }
    
    private enum StarTemperature
    {
        RED   ("Red",    AsciiPanel.brightRed,    false, StarMass.SUBDWARF.mass, StarMass.HYPERGIANT.mass),
        YELLOW("Yellow", AsciiPanel.brightYellow, false, StarMass.DWARF.mass,    StarMass.HYPERGIANT.mass),
        BLUE  ("Blue",   AsciiPanel.brightCyan,   true,  StarMass.GIANT.mass,    StarMass.HYPERGIANT.mass);
        
        private String  name;
        private Color   color;
        private boolean radiation;
        private int     minSize;
        private int     maxSize;
        
        StarTemperature(String name, Color color, boolean radiation, int minSize,
                int maxSize)
        {
            this.name      = name;
            this.color     = color;
            this.radiation = radiation;
            this.minSize   = minSize;
            this.maxSize   = maxSize;
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
        
        public int getMinSize()
            {return minSize;}
        
        public int getMaxSize()
            {return maxSize;}
        
        public boolean isInMassRange(int mass)
            {return minSize <= mass && maxSize >= mass;}
        
        public static StarTemperature select()
            {return Main.rng.getRandomElement(StarTemperature.values());}
        
        public static StarTemperature select(StarMass mass)
        {
            List<StarTemperature> temperatures = new LinkedList<>();
            for (StarTemperature temperature: StarTemperature.values())
                if (temperature.isInMassRange(mass.mass))
                    temperatures.add(temperature);
            
            return Main.rng.getRandomElement(temperatures);
        }
    }
    
    private enum SpecialStar
    {
        BROWN_DWARF(new Star("Brown Dwarf", AsciiPanel.yellow,
                StarMass.SUBDWARF.getSymbol(), StarMass.SUBDWARF.getMass(),
                false)),
        WHITE_DWARF(new Star("White Dwarf", AsciiPanel.brightWhite,
                StarMass.SUBDWARF.getSymbol(), StarMass.SUBDWARF.getMass(),
                false)),
        BINARY_STAR(new Star("Binary Star", AsciiPanel.brightWhite,
                Symbol.BINARY_STAR.get(), StarMass.SUBGIANT.getMass(), false)),
        NEUTRON_STAR(new Star("Neutron Star", AsciiPanel.brightWhite,
                Symbol.NEUTRON_STAR.get(), StarMass.GIANT.getMass(), true)),
        PULSAR(new Star("Pulsar", NEUTRON_STAR.star.color,
                Symbol.PULSAR.get(), NEUTRON_STAR.star.mass, true));
        
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
    
    private Star(StarMass mass, StarTemperature temperature)
    {
        this.name      = temperature.getName() + " " + mass.getName();
        this.color     = temperature.getColor();
        this.symbol    = mass.getSymbol();
        this.mass      = mass.getMass();
        this.radiation = temperature.hasRadiation();
    }
    
    public Star(Nebula nebula)
        {this(generate(nebula));}
    
    public Star()
        {this(generate());}
    
    public static Star generate(Nebula nebula)
    {
        if (nebula == null && Utility.getChance(Main.rng, SPECIAL_CHANCE))
            return Main.rng.getRandomElement(SpecialStar.values()).star;
        
        StarMass mass = StarMass.select(nebula);
        StarTemperature temperature = StarTemperature.select(mass);
        return new Star(mass, temperature);
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
        {return getPowerAt(orbit) / (StarMass.SUBDWARF.getMass()) + 1;}
}