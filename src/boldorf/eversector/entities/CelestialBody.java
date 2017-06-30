package boldorf.eversector.entities;

import boldorf.eversector.map.faction.Faction;
import boldorf.eversector.map.Sector;

/** A stationary satellite that can have ships land on it. */
public abstract class CelestialBody extends Satellite
{
    /** The base cost in credits to claim any celestial body. */
    public static final int CLAIM_COST = 250;
    
    /** The sector this body belongs to. */
    private Sector sector;
    
    /** The faction that has claimed this body. */
    private Faction faction;
    
    /**
     * Creates a CelestialBody from a name, orbit, faction, and sector.
     * @param name the name of the body
     * @param orbit the orbit of the body
     * @param faction the faction that owns the body
     * @param sector the sector that the body is in
     */
    public CelestialBody(String name, int orbit, Faction faction, Sector sector)
    {
        super(name, orbit);
        this.sector = sector;
        this.faction = faction;
    }
    
    public CelestialBody(String name, int orbit, Sector sector)
        {this(name, orbit, null, sector);}
    
    /**
     * Returns the cost of claiming territory on the celestial body.
     * @return the cost in credits of claiming one piece of territory on the
     * celestial body
     */
    public abstract int getClaimCost();
    
    public Sector getSector()
        {return sector;}
    
    public Faction getFaction()
        {return faction;}
    
    public boolean isClaimed()
        {return faction != null;}
    
    /**
     * Claims the celestial body for the specified faction.
     * @param f the faction that will claim the celestial body
     */
    public void claim(Faction f)
    {
        if (faction == f)
            return;
        
        faction = f;
        sector.updateFaction();
    }
    
    /** Removes claimed status without updating sector factions. */
    public final void unclaim()
        {faction = null;}
}