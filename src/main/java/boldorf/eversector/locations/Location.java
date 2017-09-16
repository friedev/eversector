package boldorf.eversector.locations;

import boldorf.eversector.map.Galaxy;
import boldorf.eversector.map.Sector;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 *
 */
public class Location
{
    public static final String LOCATION = "interstellar";
    public static final String SECTOR_LOCATION = "orbital";
    public static final String PLANET_LOCATION = "landed";
    public static final String STATION_LOCATION = "docked";

    private final Galaxy galaxy;
    private final Coord coord;

    public Location(Galaxy galaxy, Coord coord)
    {
        if (!galaxy.contains(coord))
        {
            throw new IndexOutOfBoundsException("Given coord not found on the map");
        }

        this.galaxy = galaxy;
        this.coord = coord;
    }

    public Location(Location copying)
    {this(copying.galaxy, copying.coord);}

    public Galaxy getGalaxy()
    {return galaxy;}

    public Coord getCoord()
    {return coord;}

    public Sector getSector()
    {return galaxy.sectorAt(coord);}

    public Location move(Direction direction)
    {
        return moveTo(coord.translate(Direction.getDirection(direction.deltaX, direction.deltaY)));
    }

    public Location moveTo(Coord destination)
    {
        return galaxy.contains(destination) ? new Location(galaxy, destination) : null;
    }

    public SectorLocation enterSector()
    {return new SectorLocation(this, getSector().getOrbits());}

    @Override
    public String toString()
    {
        StringBuilder params = new StringBuilder();

        if (this instanceof StationLocation)
        {
            params.append(STATION_LOCATION);
        }
        else if (this instanceof PlanetLocation)
        {
            params.append(PLANET_LOCATION);
        }
        else if (this instanceof SectorLocation)
        {
            params.append(SECTOR_LOCATION);
        }
        else
        {
            params.append(LOCATION);
        }

        params.append("; ").append(Utility.coordToOrderedPair(coord));

        if (!(this instanceof SectorLocation))
        {
            return params.toString();
        }

        params.append("; ").append(((SectorLocation) this).getOrbit());

        if (this instanceof PlanetLocation)
        {
            params.append("; ").append(((PlanetLocation) this).getRegionCoord());
        }

        return params.toString();
    }

    public static Location parseLocation(Galaxy map, String value)
    {
        String[] params = value.split("; ");

        Coord coord = Utility.parseCoord(params[1]);
        Location location = new Location(map, coord);

        if (LOCATION.equals(params[0]))
        {
            return location;
        }

        int orbit = Utility.parseInt(params[2]);
        SectorLocation sectorLocation = new SectorLocation(location, orbit);

        if (PLANET_LOCATION.equals(params[0]))
        {
            return new PlanetLocation(sectorLocation, Utility.parseCoord(params[3]));
        }

        if (STATION_LOCATION.equals(params[0]))
        {
            return new StationLocation(sectorLocation);
        }

        return sectorLocation;
    }

    public boolean equals(Location o)
    {return !(o instanceof SectorLocation) && galaxy == o.galaxy && coord.equals(o.coord);}
}
