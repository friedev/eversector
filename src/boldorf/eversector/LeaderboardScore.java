package boldorf.eversector;

import boldorf.util.Utility;
import java.util.Properties;

/**
 * A score on the leaderboard, consisting of a ship name, credits, and
 * reputation.
 */
public class LeaderboardScore implements Comparable<LeaderboardScore>
{
    public static final String INVALID = "INVALID";
    
    private String  shipName;
    private Integer score;
    private Integer turns;
    private Integer sectors;
    private String  reputation;
    private boolean leader;
    
    public LeaderboardScore(Properties properties)
    {
        if (properties == null || properties.isEmpty())
            throw new IllegalArgumentException();
        
        shipName   = properties.getProperty("shipName");
        score      = Utility.parseInt(properties.getProperty("score"));
        turns      = Utility.parseInt(properties.getProperty("turns"));
        sectors    = Utility.parseInt(properties.getProperty("sectors"));
        reputation = properties.getProperty("reputation");
        leader     = "true".equals(properties.getProperty("leader"));
    }
    
    /**
     * Creates a new LeaderboardScore from its three components.
     * @param shipName the name of the ship to attribute the score to
     * @param score the score
     * @param turns the number of turns played
     * @param sectors the number of sectors discovered
     * @param reputation the reputation
     * @param leader true if the player was a leader
     */
    public LeaderboardScore(String shipName, int score, int turns, int sectors,
            String reputation, boolean leader)
    {
        this.shipName   = shipName;
        this.score      = score;
        this.turns      = turns;
        this.sectors    = sectors;
        this.reputation = reputation;
        this.leader     = leader;
    }
    
    /**
     * Creates a new LeaderboardScore without a ship name accompanying it.
     * @param score the score
     * @param turns the number of turns played
     * @param sectors the number of sectors discovered
     * @param reputation the reputation of the score
     * @param leader true if the player was a leader
     */
    public LeaderboardScore(int score, int turns, int sectors,
            String reputation, boolean leader)
        {this(null, score, turns, sectors, reputation, leader);}
    
    @Override
    public String toString()
    {
        if (!isValid())
            return INVALID;
        
        StringBuilder builder = new StringBuilder();
        
        if (shipName != null)
            builder.append(shipName).append(": ");
        
        builder.append(score).append(" Credits, ")
                .append(turns).append(" Turns, ")
                .append(sectors).append(" Sectors, ");
        
        builder.append(reputation);
        if (leader)
            builder.append(" (Leader)");
        
        return builder.toString();
    }
    
    /**
     * Returns a Properties object representing the score.
     * @return a Properties object representing the score
     */
    public Properties toProperties()
    {
        Properties properties = new Properties();
        
        if (shipName != null)
            properties.setProperty("shipName", shipName);
        
        properties.setProperty("score",      score.toString());
        properties.setProperty("turns",      turns.toString());
        properties.setProperty("sectors",    sectors.toString());
        properties.setProperty("reputation", reputation);
        properties.setProperty("leader",     Boolean.toString(leader));
        return properties;
    }

    /**
     * Returns true if the two required fields (score and reputation) are set.
     * @return true if the score and reputation of the object are not their
     * default values
     */
    public boolean isValid()
    {
        return score != null && turns != null && sectors != null &&
                reputation != null;
    }
    
    @Override
    public int compareTo(LeaderboardScore other)
        {return Integer.compare(score, other.score);}
}