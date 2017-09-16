package boldorf.eversector.locations;

/**
 *
 */
public class StationLocation extends SectorLocation
{
    public StationLocation(SectorLocation location)
    {super(location);}

    public SectorLocation undock()
    {return new SectorLocation(this);}

    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof StationLocation))
        {
            return false;
        }

        StationLocation cast = (StationLocation) o;
        return getGalaxy() == cast.getGalaxy() && getCoord().equals(cast.getCoord()) && getOrbit() == cast.getOrbit();
    }
}