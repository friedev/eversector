package boldorf.eversector.entities;

import boldorf.eversector.map.faction.Faction;

/**
 * A container class for an integer representing reputation, and the faction
 * the reputation is with.
 */
public class Reputation implements Comparable<Reputation>
{
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
        double maxReputation = faction.getMaxReputation();
        
        for (ReputationRange range: ReputationRange.values())
            if (range.isInRange(reputation, maxReputation))
                return range;
        
        return ReputationRange.DEFAULT;
    }
}