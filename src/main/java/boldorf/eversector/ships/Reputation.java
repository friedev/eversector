package boldorf.eversector.ships;

import asciiPanel.AsciiPanel;
import boldorf.eversector.faction.Faction;
import java.awt.Color;

/**
 * A container class for an integer representing reputation, and the faction
 * the reputation is with.
 */
public class Reputation implements Comparable<Reputation>
{
    public enum ReputationRange
    {
        HEROIC   ("Heroic",    "Admires",  AsciiPanel.brightYellow,   0.75,  1.0 ),
        RESPECTED("Respected", "Respects", AsciiPanel.brightGreen,    0.5,   0.75),
        POSITIVE ("Positive",  "Likes",    AsciiPanel.green,          0.25,  0.5 ),
        NEGATIVE ("Negative",  "Dislikes", AsciiPanel.red,           -0.25, -0.5 ),
        DESPISED ("Despised",  "Despises", AsciiPanel.brightRed,     -0.5,  -0.75),
        INFAMOUS ("Infamous",  "Loathes",  AsciiPanel.brightMagenta, -0.75, -1.0 ),
        NEUTRAL  ("Neutral",   "Ignores",  null,                     -0.25,  0.25);

        public static final ReputationRange DEFAULT = NEUTRAL;

        private String adjective;
        private String verb;
        private Color  color;
        private double min;
        private double max;

        ReputationRange(String adjective, String verb, Color color, double min,
                double max)
        {
            this.adjective = adjective;
            this.verb      = verb;
            this.color     = color;
            this.min       = min;
            this.max       = max;
        }

        public String getAdjective()
            {return adjective;}

        public String getVerb()
            {return verb;}

        public Color getColor()
            {return color;}

        public double getMin()
            {return min;}

        public double getMax()
            {return max;}

        public double getMin(double range)
            {return min * Math.abs(range);}

        public double getMax(double range)
            {return max * Math.abs(range);}

        public boolean isInRange(int value, double range)
            {return value >= getMin(range) && value <= getMax(range);}
        
        public static ReputationRange getHighestRange()
        {
            ReputationRange highestRange = DEFAULT;
            for (ReputationRange range: values())
                if (range.getMax() > highestRange.getMax())
                    highestRange = range;
            return highestRange;
        }
        
        public static ReputationRange getLowestRange()
        {
            ReputationRange lowestRange = DEFAULT;
            for (ReputationRange range: values())
                if (range.getMax() < lowestRange.getMax())
                    lowestRange = range;
            return lowestRange;
        }
    }
    
    /**
     * Returns the modifier that reputation will be divided by when adjusting
     * reputation towards zero.
     */
    public static final int FADE_MODIFIER = 300;
    
    private int reputation;
    private Faction faction;
    
    public Reputation(int reputation, Faction faction)
    {
        this.reputation = reputation;
        this.faction = faction;
    }
    
    public Reputation(Faction faction)
        {this(0, faction);}
    
    public int get()
        {return reputation;}
    
    public void change(int reputation)
        {this.reputation += reputation;}
    
    public Faction getFaction()
        {return faction;}
    
    @Override
    public int compareTo(Reputation other)
        {return Integer.compare(reputation, other.reputation);}
    
    public ReputationRange getRange()
    {
        double range = reputation > 0 ?
                faction.getMaxReputation() : faction.getMinReputation();
        
        if (reputation > 0 && reputation > range)
            return ReputationRange.getHighestRange();
        else if (reputation < 0 && reputation < range)
            return ReputationRange.getLowestRange();
        
        for (ReputationRange rangeLevel: ReputationRange.values())
            if (rangeLevel.isInRange(reputation, range))
                return rangeLevel;
        
        return ReputationRange.DEFAULT;
    }
}