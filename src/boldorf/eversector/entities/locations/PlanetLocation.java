package boldorf.eversector.entities.locations;

import boldorf.eversector.entities.Region;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class PlanetLocation extends SectorLocation
{
    private final Coord regionCoords;
    
    public PlanetLocation(SectorLocation location, Coord regionCoords)
    {
        super(location);
        
        if (!getPlanet().contains(regionCoords))
        {
            throw new IndexOutOfBoundsException(
                    "Given coords not found on planet");
        }
        
        this.regionCoords = regionCoords;
    }
    
    public PlanetLocation(PlanetLocation copying)
        {this(copying, copying.regionCoords);}
    
    public Coord getRegionCoords()
        {return regionCoords;}
    
    public Region getRegion()
        {return getPlanet().regionAt(regionCoords);}
    
    public SectorLocation takeoff()
        {return new SectorLocation(this);}
    
    public PlanetLocation moveRegion(Direction direction)
    {
        if (direction.isDiagonal())
            return null;
        
        Coord destination = regionCoords.translate(direction);
        if (getPlanet().contains(destination))
            return new PlanetLocation(this, destination);
        
        if (direction.hasUp() || direction.hasDown())
        {
            return new PlanetLocation(this, regionCoords.setX(getPlanet()
                    .getOppositeSide(regionCoords.x)));
        }
        
        return direction.hasRight() ?
                new PlanetLocation(this, regionCoords.setX(0)) :
                new PlanetLocation(this,
                        regionCoords.setX(getPlanet().getNColumns() - 1));
    }
    
    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof PlanetLocation))
            return false;
        
        PlanetLocation cast = (PlanetLocation) o;
        return getMap() == cast.getMap() &&
                getCoords().equals(cast.getCoords()) &&
                getOrbit() == cast.getOrbit() &&
                regionCoords.equals(cast.regionCoords);
    }
}