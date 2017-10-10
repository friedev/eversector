package boldorf.eversector.locations;

/**
 * The location of a station. Does not currently differ from SectorLocation, but is used to denote being docked with a
 * station.
 * @author Maugrift
 */
public class StationLocation extends SectorLocation
{
    /**
     * Creates a new station location.
     *
     * @param location the location of the station
     * @throws IllegalArgumentException if no station is found at the given location
     */
    public StationLocation(SectorLocation location)
    {
        super(location);

        if (!location.isStation())
        {
            throw new IllegalArgumentException("No station found at the given location");
        }
    }

    /**
     * Undocks from the station.
     *
     * @return the resulting location
     */
    public SectorLocation undock()
    {
        return new SectorLocation(this);
    }

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