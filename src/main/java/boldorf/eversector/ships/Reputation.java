package boldorf.eversector.ships;

import asciiPanel.AsciiPanel;
import boldorf.eversector.faction.Faction;

import java.awt.Color;

/**
 * A container class for an integer representing reputation, and the faction the reputation is with.
 */
public class Reputation implements Comparable<Reputation>
{
    public static int MINE = 3;
    public static int MINE_DRY = -30;
    public static int JOIN = 20;
    public static int LEAVE = -50;
    public static int KILL_ENEMY = 75;
    public static int KILL_ALLY = -200;
    public static int CONVERT = 100;
    public static int CLAIM = 175;
    public static int CLAIM_ALLY = 100;
    public static int DISTRESS_ATTEMPT = -10;
    public static int DISTRESS = -100;
    public static int REELECTION = -100;
    public static int REQ_REJECTION = -100;

    public enum ReputationRange
    {
        HEROIC("Heroic", "Admires", AsciiPanel.brightYellow, 0.75, 1.0),
        RESPECTED("Respected", "Respects", AsciiPanel.brightGreen, 0.5, 0.75),
        POSITIVE("Positive", "Likes", AsciiPanel.green, 0.25, 0.5),
        NEGATIVE("Negative", "Dislikes", AsciiPanel.red, -0.25, -0.5),
        DESPISED("Despised", "Despises", AsciiPanel.brightRed, -0.5, -0.75),
        INFAMOUS("Infamous", "Loathes", AsciiPanel.brightMagenta, -0.75, -1.0),
        NEUTRAL("Neutral", "Ignores", null, -0.25, 0.25);

        public static final ReputationRange DEFAULT = NEUTRAL;

        private String adjective;
        private String verb;
        private Color color;
        private double min;
        private double max;

        ReputationRange(String adjective, String verb, Color color, double min, double max)
        {
            this.adjective = adjective;
            this.verb = verb;
            this.color = color;
            this.min = min;
            this.max = max;
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

        public boolean isInRange(double value, double range)
        {return value >= getMin(range) && value <= getMax(range);}

        public static ReputationRange getHighestRange()
        {
            ReputationRange highestRange = DEFAULT;
            for (ReputationRange range : values())
            {
                if (range.getMax() > highestRange.getMax())
                {
                    highestRange = range;
                }
            }
            return highestRange;
        }

        public static ReputationRange getLowestRange()
        {
            ReputationRange lowestRange = DEFAULT;
            for (ReputationRange range : values())
            {
                if (range.getMin() < lowestRange.getMin())
                {
                    lowestRange = range;
                }
            }
            return lowestRange;
        }
    }

    /**
     * Returns the modifier that reputation will be divided by when adjusting reputation towards zero.
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
        double range = reputation > 0 ? faction.getMaxReputation() : faction.getMinReputation();

        if (reputation > 0 && reputation >= range)
        {
            return ReputationRange.getHighestRange();
        }
        if (reputation < 0 && reputation <= range)
        {
            return ReputationRange.getLowestRange();
        }
        if (ReputationRange.DEFAULT.isInRange(reputation, range))
        {
            return ReputationRange.DEFAULT;
        }

        for (ReputationRange rangeLevel : ReputationRange.values())
        {
            if (rangeLevel.isInRange(reputation, range))
            {
                return rangeLevel;
            }
        }

        return ReputationRange.DEFAULT;
    }
}