package boldorf.eversector.faction;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import boldorf.eversector.Main;
import boldorf.eversector.faction.Relationship.RelationshipType;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.map.Sector;
import boldorf.eversector.ships.Ship;

import java.awt.*;

import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.rng;
import static boldorf.eversector.faction.Relationship.RelationshipType.*;

/**
 * A named group that owns ships and territory and has relationships with other factions.
 *
 * @author Boldorf Smokebane
 */
public class Faction implements ColorStringObject
{
    public static final String[] NAME_PREFIX = new String[]{
            "Ari",
            "Axo",
            "Axi",
            "Be",
            "Ceta",
            "Cira",
            "Ciro",
            "Eno",
            "Equa",
            "Equi",
            "Exo",
            "Fe",
            "Gali",
            "Galy",
            "Gani",
            "Hypo",
            "Iglo",
            "Ixa",
            "Mu",
            "Nano",
            "Neo",
            "Neu",
            "Nexo",
            "Nono",
            "Oca",
            "Oxi",
            "Oxy",
            "Psy",
            "Quo",
            "Tera",
            "Thy",
            "Undi",
            "Uxo",
            "Vea",
            "Vi",
            "Viro",
            "Via",
            "Xena",
            "Xeno",
            "Xeo",
            "Xy",
            "Zena",
            "Zeta"
    };

    public static final String[] NAME_SUFFIX = new String[]{
            "con",
            "chon",
            "chron",
            "der",
            "fax",
            "fi",
            "gon",
            "lite",
            "lyte",
            "loi",
            "lon",
            "los",
            "lyx",
            "rax",
            "rani",
            "rano",
            "rea",
            "syn",
            "syth",
            "sino",
            "the",
            "to",
            "tara",
            "tera",
            "tere",
            "tra",
            "tro",
            "var",
            "vax",
            "vea",
            "vyr",
            "vyn",
            "zer",
            "zin",
            "zon"
    };

    /**
     * All the possible "types" of factions that can be generated.
     */
    public static final String[] NAME_TYPES = new String[]{
            "Alliance",
            "Assembly",
            "Association",
            "Coalition",
            "Collective",
            "Commonwealth",
            "Confederacy",
            "Conglomerate",
            "Conspiracy",
            "Corporation",
            "Council",
            "Empire",
            "Federation",
            "Group",
            "Guild",
            "League",
            "Nation",
            "Network",
            "Order",
            "Organization",
            "Republic",
            "State",
            "Union"
    };

    /**
     * The name of the faction.
     */
    private final String name;

    /**
     * The color that represents the faction.
     */
    private final Color color;

    /**
     * The galaxy the faction is in.
     */
    private final Galaxy galaxy;

    /**
     * All relationships this faction has with others.
     */
    private final Relationship[] relationships;

    /**
     * The ship acting as the faction's leader.
     */
    private Ship leader;

    /**
     * The number of credits in the faction's economy.
     */
    private int economy;

    /**
     * The turn on which the last election occurred.
     */
    private int lastElection;

    /**
     * The average reputation of all ships with the faction.
     */
    private int averageReputation;

    /**
     * Generates a faction in the galaxy with the given color.
     *
     * @param galaxy the galaxy that the faction will be in
     * @param color  the faction's color
     */
    public Faction(Galaxy galaxy, Color color)
    {
        this.name = rng.getRandomElement(NAME_PREFIX) + rng.getRandomElement(NAME_SUFFIX) + " " + rng.getRandomElement(
                NAME_TYPES);
        this.color = color;
        this.galaxy = galaxy;
        relationships = new Relationship[galaxy.getFactions().length - 1];
        leader = null;
        economy = 0;
        lastElection = -Galaxy.SIMULATED_TURNS;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(), color);
    }

    /**
     * Gets the name of the faction.
     *
     * @return the faction's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the color of the faction.
     *
     * @return the faction's color
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * Gets the galaxy the faction is in.
     *
     * @return the faction's galaxy
     */
    public Galaxy getGalaxy()
    {
        return galaxy;
    }

    /**
     * Gets the leader of the faction.
     *
     * @return the faction's leader
     */
    public Ship getLeader()
    {
        return leader;
    }

    /**
     * Gets the number of credits in the faction's economy.
     *
     * @return the faction's economy credits
     */
    public int getEconomyCredits()
    {
        return economy;
    }

    /**
     * Gets the turn on which the last election took place.
     *
     * @return the turn of the faction's last election
     */
    public int getLastElection()
    {
        return lastElection;
    }

    /**
     * Sets the leader of the faction.
     *
     * @param leader the new leader of the faction
     */
    public void setLeader(Ship leader)
    {
        lastElection = galaxy.getTurn();
        this.leader = leader;
    }

    /**
     * Holds an election for the leader of the faction.
     */
    public void holdElection()
    {
        holdElection(leader != null && leader.isDestroyed());
    }

    /**
     * Holds an election for the leader of the faction.
     *
     * @param emergency true if the election is an emergency
     */
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

    /**
     * Changes the number of credits in the faction's economy.
     *
     * @param credits the number of credits to add to the economy
     * @return true if the credit change occurs
     */
    public boolean changeEconomy(int credits)
    {
        if (economy + credits >= 0)
        {
            economy += credits;
            return true;
        }

        return false;
    }

    /**
     * Returns the rank of the faction by sectors controlled.
     *
     * @return the faction's rank among other factions based on the number of sectors controlled by each
     */
    public int getRank()
    {
        int rank = 1;

        for (Faction otherFaction : galaxy.getFactions())
        {
            if (otherFaction != this && getSectorsControlled() < otherFaction.getSectorsControlled())
            {
                rank++;
            }
        }

        return rank;
    }

    /**
     * Returns the number of sectors controlled by the faction.
     *
     * @return the number of sectors in which the given faction is the dominant one
     */
    public int getSectorsControlled()
    {
        int sectorsClaimed = 0;

        for (Sector[] row : galaxy.getSectors())
        {
            for (Sector sector : row)
            {
                if (sector.getFaction() == this)
                {
                    sectorsClaimed++;
                }
            }
        }

        return sectorsClaimed;
    }

    /**
     * Gets the number of planets controlled by the faction.
     *
     * @return the number of planets controlled by the faction
     */
    public int getPlanetsControlled()
    {
        int planetsClaimed = 0;

        for (Sector[] row : galaxy.getSectors())
        {
            for (Sector sector : row)
            {
                planetsClaimed += sector.getPlanetsControlledBy(this);
            }
        }

        return planetsClaimed;
    }

    /**
     * Gets the number of stations controlled by the faction.
     *
     * @return the number of stations controlled by the faction
     */
    public int getStationsControlled()
    {
        int stationsClaimed = 0;

        for (Sector[] row : galaxy.getSectors())
        {
            for (Sector sector : row)
            {
                stationsClaimed += sector.getStationsControlledBy(this);
            }
        }

        return stationsClaimed;
    }

    /**
     * Gets a String representation of the types of stations controlled by the faction.
     *
     * @return the station types of the faction as a String
     */
    public String getStationTypes()
    {
        int trade = 0;
        int battle = 0;

        for (Sector[] row : galaxy.getSectors())
        {
            for (Sector sector : row)
            {
                trade += sector.getStationTypesControlledBy(this, false);
                battle += sector.getStationTypesControlledBy(this, true);
            }
        }

        return (trade + battle) + " (" + trade + " Trade, " + battle + " Battle)";
    }

    /**
     * Gets the number of ships in the faction.
     *
     * @return the number of ships in the faction
     */
    public int getShips()
    {
        int nShips = 0;

        for (Ship ship : galaxy.getShips())
        {
            if (ship.getFaction() == this)
            {
                nShips++;
            }
        }

        return nShips;
    }

    /**
     * Gets a String representation of the types of ships controlled by the faction.
     *
     * @return the ship types of the faction as a String
     */
    public String getShipTypes()
    {
        int total = 0;
        int mining = 0;
        int battle = 0;

        for (Ship ship : galaxy.getShips())
        {
            if (ship.getFaction() == this)
            {
                total++;

                if ("mining".equals(ship.getHigherLevel()))
                {
                    mining++;
                }
                else if ("battle".equals(ship.getHigherLevel()))
                {
                    battle++;
                }
            }
        }

        return total + " (" + mining + " Mining, " + battle + " Battle)";
    }

    /**
     * Gets the highest reputation with the faction of any ship.
     *
     * @return the highest reputation with the faction of any ship
     */
    public int getMaxReputation()
    {
        int maxReputation = Integer.MIN_VALUE;
        for (Ship ship : galaxy.getShips())
        {
            maxReputation = Math.max(maxReputation, ship.getReputation(this).get());
        }
        return maxReputation;
    }

    /**
     * Gets the lowest reputation with the faction of any ship.
     *
     * @return the lowest reputation with the faction of any ship
     */
    public int getMinReputation()
    {
        int minReputation = Integer.MAX_VALUE;
        for (Ship ship : galaxy.getShips())
        {
            minReputation = Math.min(minReputation, ship.getReputation(this).get());
        }
        return minReputation;
    }

    /**
     * Returns the cached average reputation of all ships with the faction.
     *
     * @return the average reputation of all ships with the faction
     * @see #cacheAverageReputation()
     */
    public int getAverageReputation()
    {
        return averageReputation;
    }

    /**
     * Caches the average reputation of all ships with the faction.
     */
    public void cacheAverageReputation()
    {
        int totalReputation = 0;
        int shipsWithReputation = 0;
        for (Ship ship : galaxy.getShips())
        {
            int reputation = ship.getReputation(this).get();
            if (reputation != 0)
            {
                totalReputation += reputation;
                shipsWithReputation++;
            }
        }
        averageReputation = totalReputation / Math.max(1, shipsWithReputation);
    }

    /**
     * Returns the faction's relationship with the specified faction.
     *
     * @param faction the faction to find a relationship with
     * @return the String representing the relationship between factions, null if the faction was not found in this
     * faction's relationships
     */
    public RelationshipType getRelationship(Faction faction)
    {
        Relationship relationship = getRelationshipObject(faction);
        return relationship == null ? null : relationship.getType();
    }

    /**
     * Returns the faction's relationship with the specified faction as a Relationship object.
     *
     * @param faction the faction to find a relationship with
     * @return the Relationship between the factions, null if the faction was not found in this faction's relationships
     */
    public Relationship getRelationshipObject(Faction faction)
    {
        for (Relationship relationship : relationships)
        {
            if (relationship.getOtherFaction(this) == faction)
            {
                return relationship;
            }
        }

        return null;
    }

    /**
     * Returns all of this faction's relationships.
     *
     * @return all of this faction's relationships
     */
    public Relationship[] getRelationships()
    {
        return relationships;
    }

    /**
     * Returns true if the relationship with the specified faction is equal to the specified String.
     *
     * @param relationship the supposed relationship to compare to the actual relationship
     * @param faction      the faction to get the actual relationship with
     * @return true if the supposed relationship and the actual relationship match
     */
    public boolean isRelationship(RelationshipType relationship, Faction faction)
    {
        return getRelationship(faction) == relationship;
    }

    /**
     * Adds a relationship to the list of relationships, and thus a faction.
     *
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
     *
     * @param faction         the faction to change a relationship with
     * @param newRelationship the new relationship to change to
     */
    public void setRelationship(Faction faction, RelationshipType newRelationship)
    {
        for (Relationship relationship : relationships)
        {
            if (relationship.hasFaction(faction))
            {
                relationship.setRelationship(newRelationship);
                return;
            }
        }
    }

    /**
     * Makes this faction request that the relationship with the given faction be set to the given relationship.
     *
     * @param faction         the faction with which to request a relationship change, must be a faction that this
     *                        faction has a relationship with currently
     * @param newRelationship the new relationship to change to, must be a valid relationship
     * @return true if the relationship was changed to the requested one
     */
    public boolean requestRelationship(Faction faction, RelationshipType newRelationship)
    {
        Relationship relationship = getRelationshipObject(faction);

        if (relationship == null)
        {
            return false;
        }

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

            if (relationship.getType() == WAR && faction.getSectorsControlled() <= getSectorsControlled())
            {
                relationship.setRelationship(newRelationship);
                return true;
            }

            return false;
        }

        return false;
    }

    /**
     * Returns the relationship that this faction would like to have with the given one.
     *
     * @param faction the faction that this faction will choose a relationship with
     * @return the relationship that this faction would like to have with the given one
     */
    public RelationshipType chooseRelationship(Faction faction)
    {
        Relationship relationship = getRelationshipObject(faction);

        if (relationship == null)
        {
            return PEACE;
        }

        if (getSectorsControlled() > faction.getSectorsControlled())
        {
            return relationship.getType() == PEACE || relationship.getType() == WAR ? WAR : PEACE;
        }
        else
        {
            return relationship.getType() == PEACE || relationship.getType() == ALLIANCE ? ALLIANCE : PEACE;
        }
    }

    /**
     * Adds a String to the display as news.
     *
     * @param news the news to add
     */
    public void addNews(String news)
    {
        addNews(new ColorString(news));
    }

    /**
     * Adds a ColorString to the display as news.
     *
     * @param news the news to add
     */
    public void addNews(ColorString news)
    {
        if (galaxy.getPlayer() != null && galaxy.getPlayer().getFaction() == this)
        {
            Main.addColorMessage(news);
        }
    }
}