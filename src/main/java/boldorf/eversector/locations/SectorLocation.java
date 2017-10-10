package boldorf.eversector.locations;

import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;
import squidpony.squidmath.Coord;

import java.util.List;

/**
 * A location in a sector.
 * @author Maugrift
 */
public class SectorLocation extends Location
{
    /**
     * The orbit of the location.
     */
    private final int orbit;

    /**
     * Creates a new sector location.
     *
     * @param location the location of the sector
     * @param orbit    the orbit in the sector
     * @throws IndexOutOfBoundsException if the given orbit is not found
     */
    public SectorLocation(Location location, int orbit)
    {
        super(location);

        if (!getSector().isValidOrbit(orbit))
        {
            throw new IndexOutOfBoundsException("Given orbit not found in sector (" + orbit + ")");
        }

        this.orbit = orbit;
    }

    /**
     * Copies another sector location.
     *
     * @param copying the sector location to copy
     */
    public SectorLocation(SectorLocation copying)
    {
        this(copying, copying.getOrbit());
    }

    /**
     * Gets the orbit of the location.
     *
     * @return the orbit of the location
     */
    public int getOrbit()
    {
        return orbit;
    }

    /**
     * Gets the planet at the location.
     *
     * @return the planet at the location, null if none exists
     */
    public Planet getPlanet()
    {
        return getSector().getPlanetAt(orbit);
    }

    /**
     * Gets the station at the location.
     *
     * @return the station at the location, null if none exists
     */
    public Station getStation()
    {
        return getSector().getStationAt(orbit);
    }

    /**
     * Returns true if there is a planet at the location.
     *
     * @return true if there is a planet at the location.
     */
    public boolean isPlanet()
    {
        return getSector().isPlanetAt(orbit);
    }

    /**
     * Returns true if there is a station at the location.
     *
     * @return true if there is a station at the location
     */
    public boolean isStation()
    {
        return getSector().isStationAt(orbit);
    }

    /**
     * Gets a list of all ships at the location.
     *
     * @return the ships at the location
     */
    public List<Ship> getShips()
    {
        return getSector().getShipsAt(orbit);
    }

    /**
     * Sets the orbit of the location.
     *
     * @param orbit the new orbit of the location
     * @return the resulting location
     */
    public SectorLocation setOrbit(int orbit)
    {
        return getSector().isValidOrbit(orbit) ? new SectorLocation(this, orbit) : null;
    }

    /**
     * Raises the orbit of the location.
     *
     * @return the resulting location, null if not possible
     */
    public SectorLocation raiseOrbit()
    {
        return orbit == getSector().getOrbits() ? null : new SectorLocation(this, orbit + 1);
    }

    /**
     * Lowers the orbit of the location.
     *
     * @return the resulting location, null if not possible
     */
    public SectorLocation lowerOrbit()
    {
        return orbit == 1 ? null : new SectorLocation(this, orbit - 1);
    }

    /**
     * Escapes from the sector at the location.
     *
     * @return the resulting location, null if not possible
     */
    public Location escapeSector()
    {
        return orbit == getSector().getOrbits() ? new Location(this) : null;
    }

    /**
     * Lands in the specified region.
     *
     * @param regionCoord the coordinates of the region to land in
     * @return the resulting location, null if not possible
     */
    public PlanetLocation land(Coord regionCoord)
    {
        return isPlanet() ? new PlanetLocation(this, regionCoord) : null;
    }

    /**
     * Docks at the station.
     *
     * @return the resulting location, null if not possible
     */
    public StationLocation dock()
    {
        return isStation() ? new StationLocation(this) : null;
    }

    /**
     * Joins the given battle.
     *
     * @param battle the battle to join
     * @return the resulting location
     */
    public BattleLocation joinBattle(Battle battle)
    {
        return new BattleLocation(this, battle);
    }

    @Override
    public boolean equals(Location o)
    {
        if (!(o instanceof SectorLocation) || o instanceof PlanetLocation || o instanceof StationLocation)
        {
            return false;
        }

        SectorLocation cast = (SectorLocation) o;
        return getGalaxy() == cast.getGalaxy() && getCoord().equals(cast.getCoord()) && orbit == cast.orbit;
    }
}