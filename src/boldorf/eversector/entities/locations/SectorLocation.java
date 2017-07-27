package boldorf.eversector.entities.locations;

import boldorf.eversector.entities.Battle;
import boldorf.eversector.entities.Planet;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Station;
import java.util.List;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class SectorLocation extends Location
{
    private final int orbit;
    
    public SectorLocation(Location location, int orbit)
    {
        super(location);
        
        if (!getSector().isValidOrbit(orbit))
        {
            throw new IndexOutOfBoundsException(
                    "Given orbit not found in sector (" + orbit + ")");
        }
        
        this.orbit = orbit;
    }
    
    public SectorLocation(SectorLocation copying)
        {this(copying, copying.getOrbit());}
    
    public int getOrbit()
        {return orbit;}
    
    public Planet getPlanet()
        {return getSector().getPlanetAt(orbit);}
    
    public Station getStation()
        {return getSector().getStationAt(orbit);}
    
    public boolean isPlanet()
        {return getSector().isPlanetAt(orbit);}
    
    public boolean isStation()
        {return getSector().isStationAt(orbit);}
    
    public List<Ship> getShips()
        {return getSector().getShipsAt(orbit);}
    
    public SectorLocation setOrbit(int orbit)
    {
        return getSector().isValidOrbit(orbit) ?
                new SectorLocation(this, orbit) : null;
    }
    
    public SectorLocation raiseOrbit()
    {
        return orbit == getSector().getOrbits() ?
                null : new SectorLocation(this, orbit + 1);
    }
    
    public SectorLocation lowerOrbit()
        {return orbit == 1 ? null : new SectorLocation(this, orbit - 1);}
    
    public Location escapeSector()
        {return orbit == getSector().getOrbits() ? new Location(this) : null;}
    
    public PlanetLocation land(Coord regionCoords)
        {return new PlanetLocation(this, regionCoords);}
    
    public StationLocation dock()
        {return new StationLocation(this);}
    
    public BattleLocation joinBattle(Battle battle)
        {return new BattleLocation(this, battle);}
    
    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof SectorLocation) || o instanceof PlanetLocation ||
                o instanceof StationLocation)
            return false;
        
        SectorLocation cast = (SectorLocation) o;
        return getMap() == cast.getMap() &&
                getCoords().equals(cast.getCoords()) && orbit == cast.orbit;
    }
}