package boldorf.eversector.map.faction;

import boldorf.eversector.Main;
import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.rng;
import boldorf.apwt.glyphs.ColorString;
import boldorf.util.Nameable;
import boldorf.eversector.entities.Ship;
import static boldorf.eversector.map.faction.Focus.*;
import boldorf.eversector.map.Map;
import static boldorf.eversector.map.faction.RelationshipType.*;
import static boldorf.eversector.storage.Options.NEWS;
import static boldorf.eversector.storage.Options.OPTION_TRUE;
import java.awt.Color;

/** A group of ships with a name and relationships with other factions. */
public class Faction extends Nameable
{
    /** All the possible "types" of factions that can be generated. */
    public static String[] TYPES = new String[]
    {
        "Alliance", "Assembly", "Association", "Coalition", "Collective",
        "Commonwealth", "Conglomerate", "Conspiracy", "Corporation", "Council",
        "Empire", "Federation", "Group", "Guild", "Hivemind", "League",
        "Network", "Order", "Organization", "Republic", "Union"
    };
    
    public static final int ECONOMY_CREDITS = 10000;
    public static final Focus DEFAULT_FOCUS = EXPAND;
    public static final int NEWS_LENGTH = 10;
    
    private Color  color;
    private String type;
    private Map    map;
    private Relationship[] relationships;
    private Ship   leader;
    private int    economy;
    private Focus  focus;
    private int    lastElection;
    
    /**
     * Generates a faction of the given name, type, and on the given map.
     * @param n the name of the faction
     * @param t the type of the faction
     * @param m the map that the faction will be on
     * @param c the faction's color
     */
    public Faction(String n, String t, Map m, Color c)
    {
        super(n);
        color         = c;
        type          = t;
        map           = m;
        relationships = new Relationship[map.getFactions().length -1];
        leader        = null;
        economy       = 0;
        focus         = DEFAULT_FOCUS;
        lastElection  = -Map.SIMULATED_TURNS;
    }
    
    /**
     * Generates a faction of the given name, a random type, and on the given
     * map.
     * @param n the name of the faction
     * @param m the map that the faction will be on
     * @param c the faction's color
     */
    public Faction(String n, Map m, Color c)
        {this(n, (String) rng.getRandomElement(TYPES), m, c);}
    
    @Override
    public String toString()
        {return super.toString() + " " + type;}
    
    public ColorString toColorString()
        {return new ColorString(toString(), color);}
    
    public Color   getColor()           {return color;         }
    public String  getType()            {return type;          }
    public Map     getMap()             {return map;           }
    public Ship    getLeader()          {return leader;        }
    public int     getEconomyCredits()  {return economy;       }
    public Focus   getFocus()           {return focus;         }
    public boolean isLeader(Ship ship)  {return leader == ship;}
    public int     getLastElection()    {return lastElection;  }
    
    public void setLeader(Ship leader)
    {
        lastElection = map.getTurns();
        
        /*
        if (this.leader == leader)
            addNews(leader + " has been reelected.");
        else if (this.leader == null)
            addNews(leader + " has been elected as leader.");
        else
            addNews(leader + " has been elected as leader, succeeding "
                    + this.leader + ".");
        */
        
        this.leader = leader;
    }
    
    public void holdElection()
        {holdElection(leader != null && leader.isDestroyed());}
    
    public void holdElection(boolean emergency)
    {
        Election election = new Election(this, emergency);
        if (map.getPlayer().isInFaction(this) && map.getTurns() >= 0)
        {
            pendingElection = election;
            return;
        }
        setLeader(election.electLeader());
    }
    
    public boolean changeEconomy(int credits)
    {
        if (economy + credits >= 0)
        {
            economy += credits;
            return true;
        }
        
        return false;
    }
    
    public int getSectorsControlled()
        {return map.getSectorsControlledBy(this);}
    
    public int getPlanetsControlled()
        {return map.getPlanetsControlledBy(this);}
    
    public int getNStationsControlled()
        {return map.getNStationsControlledBy(this);}
    
    public String getStationTypes()
        {return map.getStationTypesControlledBy(this);}
    
    public int getNShips()
        {return map.getNShipsIn(this);}
    
    public String getShipTypes()
        {return map.getShipTypesIn(this);}
    
    /**
     * Returns the rank of this faction among any others.
     * @return the amount returned by map.getRank()
     */
    public int getRank()
        {return map.getRank(this);}
    
    /**
     * Returns the faction's relationship with the specified faction.
     * @param faction the faction to find a relationship with
     * @return the String representing the relationship between factions, null
     * if the faction was not found in this faction's relationships
     */
    public RelationshipType getRelationship(Faction faction)
    {
        Relationship relationship = getRelationshipObject(faction);
        return relationship == null ? null : relationship.getType();
    }
    
    /**
     * Returns the faction's relationship with the specified faction as a
     * Relationship object.
     * @param faction the faction to find a relationship with
     * @return the Relationship between the factions, null if the faction was
     * not found in this faction's relationships
     */
    public Relationship getRelationshipObject(Faction faction)
    {
        for (Relationship relationship: relationships)
            if (relationship.getOtherFaction(this) == faction)
                return relationship;
        
        return null;
    }
    
    /**
     * Returns all of this faction's relationships.
     * @return all of this faction's relationships
     */
    public Relationship[] getRelationships()
        {return relationships;}
    
    /**
     * Returns true if the relationship with the specified faction is equal to
     * the specified String.
     * @param relationship the supposed relationship to compare to the actual
     * relationship
     * @param faction the faction to get the actual relationship with
     * @return true if the supposed relationship and the actual relationship
     * match
     */
    public boolean isRelationship(RelationshipType relationship,
            Faction faction)
        {return getRelationship(faction) == relationship;}
    
    /**
     * Adds a relationship to the list of relationships, and thus a faction.
     * @param relationship the relationship to add
     */
    public void addRelationship(Relationship relationship)
    {
        for (int i = 0; i < relationships.length; i++)
        {
            if (relationships[i] == null)
            {
                relationships[i] = relationship;
                return;
                // Note that this return is necessary to stop the loop from
                // replacing all null slots
            }
        }
    }
    
    /**
     * Changes a relationship with a faction to a specified String.
     * @param faction the faction to change a relationship with
     * @param newRelationship the new relationship to change to
     */
    public void setRelationship(Faction faction,
            RelationshipType newRelationship)
    {
        for (Relationship relationship: relationships)
        {
            if (relationship.hasFaction(faction))
            {
                relationship.setRelationship(newRelationship);
                return;
            }
        }
    }
    
    /**
     * Makes this faction request that the relationship with the given faction
     * be set to the given relationship.
     * @param faction the faction with which to request a relationship change,
     * must be a faction that this faction has a relationship with currently
     * @param newRelationship the new relationship to change to, must be a
     * valid relationship
     * @return true if the relationship was changed to the requested one
     */
    public boolean requestRelationship(Faction faction,
            RelationshipType newRelationship)
    {
        Relationship relationship = getRelationshipObject(faction);
        
        if (relationship == null)
            return false;
        
        // Declaring war does not require a request
        if (WAR.equals(newRelationship))
        {
            relationship.setRelationship(newRelationship);
            return true;
        }
        
        // The other faction will ally if it will benefit them
        if (ALLIANCE.equals(newRelationship))
        {
            if (faction.getSectorsControlled() <= getSectorsControlled())
            {
                relationship.setRelationship(newRelationship);
                return true;
            }
            
            return false;
        }
        
        // Alliances can always be broken, but peace treaties will only be made
        // if it benefits the other faction
        if (PEACE.equals(newRelationship))
        {
            if (relationship.getType() == ALLIANCE)
            {
                relationship.setRelationship(newRelationship);
                return true;
            }
            
            if (relationship.getType() == WAR &&
                    faction.getSectorsControlled() <= getSectorsControlled())
            {
                relationship.setRelationship(newRelationship);
                return true;
            }
            
            return false;
        }
        
        return false;
    }
    
    /**
     * Returns the relationship that this faction would like to have with the
     * given one.
     * @param faction the faction that this faction will choose a relationship
     * with
     * @return the relationship that this faction would like to have with the
     * given one
     */
    public RelationshipType chooseRelationship(Faction faction)
    {
        Relationship relationship = getRelationshipObject(faction);
        
        if (relationship == null)
            return PEACE;
        
        if (getSectorsControlled() > faction.getSectorsControlled())
        {
            return relationship.getType() == PEACE ||
                   relationship.getType() == WAR ? WAR : PEACE;
        }
        else
        {
            return relationship.getType() == PEACE ||
                   relationship.getType() == ALLIANCE ? ALLIANCE : PEACE;
        }
    }
    
    /**
     * Returns a comma-separated list of every focus keyword, for informing the
     * player of their command choices.
     * @return a comma-separated list of every focus keyword
     */
    public static String getFocusKeywords()
    {
        StringBuilder builder = new StringBuilder();
        for (Focus f: Focus.values())
            builder.append(f.getName()).append(", ");
        builder.delete(builder.length() - 2, builder.length());
        return builder.toString();
    }
    
    public void cycleFocus()
    {
        switch (focus)
        {
            case INVADE:
                focus = DEFEND;
                return;
            case DEFEND:
                focus = EXPAND;
                return;
            case EXPAND:
                focus = INVADE;
                return;
        }
    }
    
    public boolean setFocus(String newFocus)
    {
        if (newFocus == null)
            return false;
        
        for (Focus f: Focus.values())
        {
            if (newFocus.equalsIgnoreCase(f.getName()))
            {
                focus = f;
                return true;
            }
        }
        
        return false;
    }
    
    /** Updates the focus of the faction depending on its circumstances. */
    public void updateFocus()
    {
        if (relationships.length == 2)
        {
            // If stronger than the enemy
            if (getRank() == 1)
                focus = INVADE;
            // If tied with the enemy
            else if (relationships[0].getOtherFaction(this).getRank() == 2)
                focus = EXPAND;
            // If weaker than the enemy
            else
                focus = DEFEND;
            return;
        }
        
        // The amount of wars the faction is in, higher ratings mean more wars
        // Alliances subtract from this count
        int warRating = 0;
        for (Relationship relationship: relationships)
        {
            switch (relationship.getType())
            {
                case WAR:
                    warRating++;
                    break;
                case ALLIANCE:
                    warRating--;
                    break;
            }
        }
        
        // True if this faction is in the top half of factions by control
        boolean topFaction = getRank() <= map.getFactions().length / 2;
        
        // If partaking in many wars
        if (warRating > 0)
        {
            if (topFaction)
            {
                // Attack factions at war if strong
                focus = INVADE;
                return;
            }
            
            // Defend from stronger factions
            focus = DEFEND;
            return;
        }
        
        // If allied with many factions
        if (warRating < 0)
        {
            if (topFaction)
            {
                // Secure current sectors while at peace
                focus = DEFEND;
                return;
            }
            
            // Expand to new sectors to raise ranking
            focus = EXPAND;
            return;
        }
        
        // If at peace (or a balance of alliances and war), expand the faction
        focus = EXPAND;
    }
    
    public void addNews(String news)
        {addNews(new ColorString(news));}
    
    public void addNews(ColorString news)
    {
        if (Main.optionIs(OPTION_TRUE, NEWS) && map.getPlayer() != null &&
                map.getPlayer().isInFaction(this))
        {
            Main.addColorMessage(news);
        }
    }
}