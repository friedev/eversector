package maugrift.eversector.locations;

import maugrift.eversector.map.Region;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * The location of a region on a planet.
 * @author Aaron Friesen
 */
public class PlanetLocation extends SectorLocation
{
	/**
	 * The coordinates of the region.
	 */
	private final Coord regionCoord;

	/**
	 * Creates a planet location.
	 *
	 * @param location    the location of the planet
	 * @param regionCoord the coordinates of the region
	 * @throws IllegalArgumentException  if no planet is found at the given
	 *                                   location
	 * @throws IndexOutOfBoundsException if the given coordinates are not found
	 *                                   in the planet
	 */
	public PlanetLocation(SectorLocation location, Coord regionCoord)
	{
		super(location);

		if (!location.isPlanet()) {
			throw new IllegalArgumentException(
				"No planet found at the given location"
			);
		}

		if (!getPlanet().contains(regionCoord)) {
			throw new IndexOutOfBoundsException(
				"Given coord not found on planet"
			);
		}

		this.regionCoord = regionCoord;
	}

	/**
	 * Copies another planet location.
	 *
	 * @param copying the planet location to copy
	 */
	public PlanetLocation(PlanetLocation copying)
	{
		this(copying, copying.regionCoord);
	}

	/**
	 * Gets the coordinates of the region.
	 *
	 * @return the coordinates of the region
	 */
	public Coord getRegionCoord()
	{
		return regionCoord;
	}

	/**
	 * Gets the region at the coordinates.
	 *
	 * @return the region at the coordinates
	 */
	public Region getRegion()
	{
		return getPlanet().regionAt(regionCoord);
	}

	/**
	 * Returns the location that a ship would be in after taking off from the
	 * planet.
	 *
	 * @return the resulting location
	 */
	public SectorLocation takeoff()
	{
		return new SectorLocation(this);
	}

	/**
	 * Moves the location to the region in the given direction.
	 *
	 * @param direction the direction in which to move the location
	 * @return the resulting location
	 */
	public PlanetLocation moveRegion(Direction direction)
	{
		if (direction.isDiagonal()) {
			return null;
		}

		Coord destination = regionCoord.translate(direction);
		if (getPlanet().contains(destination)) {
			return new PlanetLocation(this, destination);
		}

		if (direction.hasUp() || direction.hasDown()) {
			return new PlanetLocation(
					this,
					regionCoord.setX(getPlanet().getOppositeSide(regionCoord.x))
				);
		}

		return (
			direction.hasRight()
			? new PlanetLocation(this, regionCoord.setX(0))
			: new PlanetLocation(
				this,
				regionCoord.setX(getPlanet().getNColumns() - 1)
			)
		);
	}

	@Override
	public boolean equals(Location o)
	{
		if (!(o instanceof PlanetLocation)) {
			return false;
		}

		PlanetLocation cast = (PlanetLocation) o;
		return (
			getGalaxy() == cast.getGalaxy()
			&& getCoord().equals(cast.getCoord())
			&& getOrbit() == cast.getOrbit()
			&& regionCoord.equals(cast.regionCoord)
		);
	}
}
