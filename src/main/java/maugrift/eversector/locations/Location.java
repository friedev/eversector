package maugrift.eversector.locations;

import maugrift.eversector.map.Galaxy;
import maugrift.eversector.map.Sector;
import maugrift.apwt.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * An interstellar location.
 * @author Aaron Friesen
 */
public class Location
{
	/**
	 * The string used to refer to the Location class in toString().
	 *
	 * @see Location
	 * @see #toString()
	 */
	private static final String LOCATION = "interstellar";

	/**
	 * The string used to refer to the SectorLocation class in toString().
	 *
	 * @see SectorLocation
	 * @see #toString()
	 */
	private static final String SECTOR_LOCATION = "orbital";

	/**
	 * The string used to refer to the PlanetLocation class in toString().
	 *
	 * @see PlanetLocation
	 * @see #toString()
	 */
	private static final String PLANET_LOCATION = "landed";

	/**
	 * The string used to refer to the StationLocation class in toString().
	 *
	 * @see StationLocation
	 * @see #toString()
	 */
	private static final String STATION_LOCATION = "docked";

	/**
	 * The galaxy the location is in.
	 */
	private final Galaxy galaxy;

	/**
	 * The coordinates of the location.
	 */
	private final Coord coord;

	/**
	 * Creates a new location.
	 *
	 * @param galaxy the galaxy the location is in
	 * @param coord  the coordinates of the location
	 */
	public Location(Galaxy galaxy, Coord coord)
	{
		if (!galaxy.contains(coord)) {
			throw new IndexOutOfBoundsException(
				"Given coord not found on the map"
			);
		}

		this.galaxy = galaxy;
		this.coord = coord;
	}

	/**
	 * Copies another location.
	 *
	 * @param copying the location to copy; converts subclasses of Location to
	 *                a Location
	 */
	public Location(Location copying)
	{
		this(copying.galaxy, copying.coord);
	}

	/**
	 * Gets the galaxy the location is in.
	 *
	 * @return the galaxy the location is in
	 */
	public Galaxy getGalaxy()
	{
		return galaxy;
	}

	/**
	 * Gets the coordinates of the location.
	 *
	 * @return the coordinates of the location
	 */
	public Coord getCoord()
	{
		return coord;
	}

	/**
	 * Gets the sector at the location's coordinates.
	 *
	 * @return the sector at the location's coordinates
	 */
	public Sector getSector()
	{
		return galaxy.sectorAt(coord);
	}

	/**
	 * Moves the location in the given direction.
	 *
	 * @param direction the direction in which to move the location
	 * @return the resulting location
	 */
	public Location move(Direction direction)
	{
		return moveTo(
				coord.translate(
					Direction.getDirection(
						direction.deltaX,
						direction.deltaY
					)
				)
			);
	}

	/**
	 * Moves to the given coordinates.
	 *
	 * @param destination the coordinates to move to
	 * @return the resulting location, null if not possible
	 */
	public Location moveTo(Coord destination)
	{
		return (
			galaxy.contains(destination)
			? new Location(galaxy, destination)
			: null
		);
	}

	/**
	 * Enters the current sector.
	 *
	 * @return the resulting location, null if not possible
	 */
	public SectorLocation enterSector()
	{
		return (
			getSector().isEmpty()
			? null
			: new SectorLocation(this, getSector().getOrbits())
		);
	}

	@Override
	public String toString()
	{
		StringBuilder params = new StringBuilder();

		if (this instanceof StationLocation) {
			params.append(STATION_LOCATION);
		} else if (this instanceof PlanetLocation) {
			params.append(PLANET_LOCATION);
		} else if (this instanceof SectorLocation) {
			params.append(SECTOR_LOCATION);
		} else {
			params.append(LOCATION);
		}

		params.append("; ").append(Utility.coordToOrderedPair(coord));

		if (!(this instanceof SectorLocation)) {
			return params.toString();
		}

		params.append("; ").append(((SectorLocation) this).getOrbit());

		if (this instanceof PlanetLocation) {
			params.append("; ").append(((PlanetLocation) this).getRegionCoord());
		}

		return params.toString();
	}

	/**
	 * Parses a location from a String, given its galaxy.
	 *
	 * @param galaxy the galaxy the location is in
	 * @param value  the location as generated by toString()
	 * @return the parsed location
	 * @see #toString()
	 */
	public static Location parseLocation(Galaxy galaxy, String value)
	{
		String[] params = value.split("; ");

		Coord coord = Utility.parseCoord(params[1]);
		Location location = new Location(galaxy, coord);

		if (LOCATION.equals(params[0])) {
			return location;
		}

		int orbit = Utility.parseInt(params[2]);
		SectorLocation sectorLocation = new SectorLocation(location, orbit);

		if (PLANET_LOCATION.equals(params[0])) {
			return new PlanetLocation(
					sectorLocation,
					Utility.parseCoord(params[3])
				);
		}

		if (STATION_LOCATION.equals(params[0])) {
			return new StationLocation(sectorLocation);
		}

		return sectorLocation;
	}

	/**
	 * Returns true if this location equals the given location.
	 *
	 * @param o the other location to check
	 * @return true if the locations are equal
	 */
	public boolean equals(Location o)
	{
		return (
			!(o instanceof SectorLocation)
			&& galaxy == o.galaxy
			&& coord.equals(o.coord)
		);
	}
}
