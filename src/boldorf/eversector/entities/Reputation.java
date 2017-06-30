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
    
    public Reputation(int r, Faction f)
    {
        reputation = r;
        faction = f;
    }
    
    public Reputation(Faction f)
        {this(0, f);}
    
    public int get()
        {return reputation;}
    
    public void change(int r)
        {reputation += r;}
    
    public Faction getFaction()
        {return faction;}
    
    @Override
    public int compareTo(Reputation other)
        {return Integer.compare(reputation, other.reputation);}
    
    public ReputationRange getRange()
        {return ReputationRange.getRange(reputation);}
}