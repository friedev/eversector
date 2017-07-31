package boldorf.eversector.entities.locations;

import boldorf.eversector.map.Map;
import boldorf.eversector.map.Sector;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 *
 */
public class Location
{
    public static final String LOCATION         = "interstellar";
    public static final String SECTOR_LOCATION  = "orbital";
    public static final String PLANET_LOCATION  = "landed";
    public static final String STATION_LOCATION = "docked";
    
    private final Map map;
    private final Coord coords;
    
    public Location(Map map, Coord coords)
    {
        if (!map.contains(coords))
        {
            throw new IndexOutOfBoundsException(
                    "Given coords not found on the map");
        }
        
        this.map = map;
        this.coords = coords;
    }
    
    public Location(Location copying)
        {this(copying.map, copying.coords);}
    
    public Map getMap()
        {return map;}
    
    public Coord getCoords()
        {return coords;}
    
    public Sector getSector()
        {return map.sectorAt(coords);}
    
    public Location move(Direction direction)
    {
        return moveTo(coords.translate(Direction.getDirection(direction.deltaX,
                -direction.deltaY)));
    }
    
    public Location moveTo(Coord destination)
    {
        return map.contains(destination) ?
                new Location(map, destination) : null;
    }
    
    public SectorLocation enterSector()
        {return new SectorLocation(this, getSector().getOrbits());}
    
    @Override
    public String toString()
    {
        StringBuilder params = new StringBuilder();
        
        if (this instanceof StationLocation)
            params.append(STATION_LOCATION);
        else if (this instanceof PlanetLocation)
            params.append(PLANET_LOCATION);
        else if (this instanceof SectorLocation)
            params.append(SECTOR_LOCATION);
        else
            params.append(LOCATION);
        
        params.append("; ").append(Utility.coordToOrderedPair(coords));
        
        if (!(this instanceof SectorLocation))
            return params.toString();
        
        params.append("; ").append(((SectorLocation) this).getOrbit());
        
        if (this instanceof PlanetLocation)
        {
            params.append("; ").append(((PlanetLocation) this)
                    .getRegionCoords());
        }
        
        return params.toString();
    }
    
    public static Location parseLocation(Map map, String value)
    {
        String[] params = value.split("; ");
        
        Coord coords = Utility.parseCoord(params[1]);
        Location location = new Location(map, coords);
        
        if (LOCATION.equals(params[0]))
            return location;
        
        int orbit = Utility.parseInt(params[2]);
        SectorLocation sectorLocation = new SectorLocation(location, orbit);
        
        if (PLANET_LOCATION.equals(params[0]))
        {
            return new PlanetLocation(sectorLocation,
                    Utility.parseCoord(params[3]));
        }
        
        if (STATION_LOCATION.equals(params[0]))
            return new StationLocation(sectorLocation);
        
        return sectorLocation;
    }
    
    public boolean equals(Location o)
    {
        if (o instanceof SectorLocation)
            return false;
        
        return map == o.map && coords.equals(o.coords);
    }
}
