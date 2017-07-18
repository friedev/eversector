package boldorf.eversector.entities.locations;

/**
 * 
 */
public class StationLocation extends SectorLocation
{
    public StationLocation(SectorLocation location)
        {super(location);}
    
    public SectorLocation undock()
        {return new SectorLocation(this);}
}