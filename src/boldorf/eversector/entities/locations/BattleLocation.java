package boldorf.eversector.entities.locations;

import boldorf.eversector.entities.Battle;

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
        return getMap() == cast.getMap() &&
                getCoords().equals(cast.getCoords()) &&
                getOrbit() == cast.getOrbit();
    }
}