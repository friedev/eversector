package boldorf.eversector.locations;

import boldorf.eversector.map.Region;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class PlanetLocation extends SectorLocation
{
    private final Coord regionCoord;
    
    public PlanetLocation(SectorLocation location, Coord regionCoord)
    {
        super(location);
        
        if (!getPlanet().contains(regionCoord))
        {
            throw new IndexOutOfBoundsException(
                    "Given coord not found on planet");
        }
        
        this.regionCoord = regionCoord;
    }
    
    public PlanetLocation(PlanetLocation copying)
        {this(copying, copying.regionCoord);}
    
    public Coord getRegionCoord()
        {return regionCoord;}
    
    public Region getRegion()
        {return getPlanet().regionAt(regionCoord);}
    
    public SectorLocation takeoff()
        {return new SectorLocation(this);}
    
    public PlanetLocation moveRegion(Direction direction)
    {
        if (direction.isDiagonal())
            return null;
        
        Coord destination = regionCoord.translate(direction);
        if (getPlanet().contains(destination))
            return new PlanetLocation(this, destination);
        
        if (direction.hasUp() || direction.hasDown())
        {
            return new PlanetLocation(this, regionCoord.setX(getPlanet()
                    .getOppositeSide(regionCoord.x)));
        }
        
        return direction.hasRight() ?
                new PlanetLocation(this, regionCoord.setX(0)) :
                new PlanetLocation(this,
                        regionCoord.setX(getPlanet().getNColumns() - 1));
    }
    
    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof PlanetLocation))
            return false;
        
        PlanetLocation cast = (PlanetLocation) o;
        return getMap() == cast.getMap() &&
                getCoord().equals(cast.getCoord()) &&
                getOrbit() == cast.getOrbit() &&
                regionCoord.equals(cast.regionCoord);
    }
}