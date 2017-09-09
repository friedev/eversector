package boldorf.eversector.faction;

import boldorf.eversector.Main;
import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.rng;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.faction.Relationship.RelationshipType;
import static boldorf.eversector.faction.Relationship.RelationshipType.*;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.map.Galaxy;
import java.awt.Color;

/** A group of ships with a name and relationships with other factions. */
public class Faction implements ColorStringObject
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
    public static final int NEWS_LENGTH = 10;
    
    private String name;
    private Color  color;
    private String type;
    private Galaxy galaxy;
    private Relationship[] relationships;
    private Ship   leader;
    private int    economy;
    private int    lastElection;
    
    /**
     * Generates a faction of the given name, type, and on the given map.
     * @param name the name of the faction
     * @param type the type of the faction
     * @param galaxy the galaxy that the faction will be in
     * @param color the faction's color
     */
    public Faction(String name, String type, Galaxy galaxy, Color color)
    {
        this.name     = name;
        this.color    = color;
        this.type     = type;
        this.galaxy   = galaxy;
        relationships = new Relationship[galaxy.getFactions().length -1];
        leader        = null;
        economy       = 0;
        lastElection  = -Galaxy.SIMULATED_TURNS;
    }
    
    /**
     * Generates a faction of the given name, a random type, and on the given
     * galaxy.
     * @param name the name of the faction
     * @param galaxy the galaxy that the faction will be in
     * @param color the faction's color
     */
    public Faction(String name, Galaxy galaxy, Color color)
        {this(name, (String) rng.getRandomElement(TYPES), galaxy, color);}
    
    @Override
    public String toString()
        {return name + " " + type;}
    
    @Override
    public ColorString toColorString()
        {return new ColorString(toString(), color);}
    
    public String  getName()            {return name;          }
    public Color   getColor()           {return color;         }
    public String  getType()            {return type;          }
    public Galaxy  getGalaxy()          {return galaxy;           }
    public Ship    getLeader()          {return leader;        }
    public int     getEconomyCredits()  {return economy;       }
    public boolean isLeader(Ship ship)  {return leader == ship;}
    public int     getLastElection()    {return lastElection;  }
    
    public void setLeader(Ship leader)
    {
        lastElection = galaxy.getTurn();
        
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
        if (galaxy.getTurn() >= 0 && galaxy.getPlayer().getFaction() == this)
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
        {return galaxy.getSectorsControlledBy(this);}
    
    public int getPlanetsControlled()
        {return galaxy.getPlanetsControlledBy(this);}
    
    public int getNStationsControlled()
        {return galaxy.getNStationsControlledBy(this);}
    
    public String getStationTypes()
        {return galaxy.getStationTypesControlledBy(this);}
    
    public int getNShips()
        {return galaxy.getNShipsIn(this);}
    
    public String getShipTypes()
        {return galaxy.getShipTypesIn(this);}
    
    /**
     * Returns the rank of this faction among any others.
     * @return the amount returned by galaxy.getRank()
     */
    public int getRank()
        {return galaxy.getRank(this);}
    
    public int getMaxReputation()
    {
        int maxReputation = Integer.MIN_VALUE;
        for (Ship ship: galaxy.getShips())
            if (ship.getFaction() == this)
                maxReputation = Math.max(maxReputation,
                        ship.getReputation(this).get());
        return maxReputation;
    }
    
    public int getMinReputation()
    {
        int minReputation = Integer.MAX_VALUE;
        for (Ship ship: galaxy.getShips())
            if (ship.getFaction() == this)
                minReputation = Math.min(minReputation,
                        ship.getReputation(this).get());
        return minReputation;
    }
    
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
    
    public void addNews(String news)
        {addNews(new ColorString(news));}
    
    public void addNews(ColorString news)
    {
        if (galaxy.getPlayer() != null && galaxy.getPlayer().getFaction() == this)
            Main.addColorMessage(news);
    }
}