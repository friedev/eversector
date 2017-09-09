package boldorf.eversector.locations;

import boldorf.eversector.ships.Battle;

/**
 * 
 */
public class BattleLocation extends SectorLocation
{
    private Battle battle;
    
    public BattleLocation(SectorLocation location, Battle battle)
    {
        super(location);
        this.battle = battle;
    }
    
    public Battle getBattle()
        {return battle;}
    
    public SectorLocation leaveBattle()
        {return new SectorLocation(this);}
    
    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof BattleLocation))
            return false;
        
        BattleLocation cast = (BattleLocation) o;
        return getGalaxy() == cast.getGalaxy() &&
                getCoord().equals(cast.getCoord()) &&
                getOrbit() == cast.getOrbit();
    }
}