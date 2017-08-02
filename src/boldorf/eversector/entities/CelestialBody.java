package boldorf.eversector.entities;

import boldorf.eversector.entities.locations.SectorLocation;
import boldorf.eversector.map.faction.Faction;
import boldorf.util.Nameable;

/** A stationary satellite that can have ships land on it. */
public abstract class CelestialBody extends Nameable
{
    /** The sector this body belongs to. */
    private final SectorLocation location;
    
    /** The faction that has claimed this body. */
    private Faction faction;
    
    /**
     * Creates a CelestialBody from a name, location, and faction.
     * @param name the name of the body
     * @param location the location of the body
     * @param faction the faction that owns the body
     */
    public CelestialBody(String name, SectorLocation location, Faction faction)
    {
        super(name);
        this.location = location;
        this.faction = faction;
    }
    
    /**
     * Creates an unclaimed CelestialBody from a name and location.
     * @param name the name of the body
     * @param location the location of the body
     */
    public CelestialBody(String name, SectorLocation location)
        {this(name, location, null);}
    
    /**
     * Returns the cost of claiming territory on the celestial body.
     * @return the cost in credits of claiming one piece of territory on the
     * celestial body
     */
    public abstract int getClaimCost();
    
    public SectorLocation getLocation()
        {return location;}
    
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
        location.getSector().updateFaction();
    }
    
    /** Removes claimed status without updating sector factions. */
    public final void unclaim()
        {faction = null;}
}