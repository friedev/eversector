package maugrift.eversector.locations;

import maugrift.eversector.ships.Battle;

/**
 * The location of a battle.
 * @author Aaron Friesen
 */
public class BattleLocation extends SectorLocation
{
	/**
	 * The battle taking place at the location.
	 */
	private Battle battle;

	/**
	 * Creates a new BattleLocation.
	 *
	 * @param location the location of the battle in a sector
	 * @param battle   the battle
	 */
	public BattleLocation(SectorLocation location, Battle battle)
	{
		super(location);
		this.battle = battle;
	}

	/**
	 * Gets the battle at the location.
	 *
	 * @return the battle at the location
	 */
	public Battle getBattle()
	{
		return battle;
	}

	/**
	 * Returns the location that a ship would be in after leaving the battle.
	 *
	 * @return the resulting location
	 */
	public SectorLocation leaveBattle()
	{
		return new SectorLocation(this);
	}

	@Override
	public boolean equals(Location o)
	{
		if (!(o instanceof BattleLocation)) {
			return false;
		}

		BattleLocation cast = (BattleLocation) o;
		return (
			getGalaxy() == cast.getGalaxy()
			&& getCoord().equals(cast.getCoord())
			&& getOrbit() == cast.getOrbit()
		);
	}
}
