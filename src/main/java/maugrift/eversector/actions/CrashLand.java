package maugrift.eversector.actions;

import maugrift.eversector.items.Resource;
import maugrift.eversector.map.Planet;
import maugrift.eversector.ships.Ship;
import maugrift.apwt.util.Utility;

public class CrashLand implements Action
{
	/**
	 * The amount of hull that crash will damage down to, and ships at or below
	 * it will be destroyed.
	 */
	public static final int CRASH_THRESHOLD = 1;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		if (!actor.isInSector())
		{
			return "You must be at a planet's orbit to land.";
		}

		if (actor.isDocked())
		{
			return "You cannot land while docked.";
		}

		if (actor.isLanded())
		{
			return "You are already landed.";
		}

		Planet planet = actor.getSectorLocation().getPlanet();

		if (planet == null)
		{
			return "There is no planet at this orbit.";
		}

		if (!planet.getType().canLandOn())
		{
			return "You cannot land on "
				+ Utility.addArticle(planet.getType().toString())
				+ ".";
		}

		return null;
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null)
		{
			return canExecute;
		}

		if (actor.getResource(Resource.HULL).getAmount() > CRASH_THRESHOLD)
		{
			actor.getResource(Resource.HULL).setAmount(CRASH_THRESHOLD);
		}
		else
		{
			actor.getResource(Resource.HULL).setAmount(0);
			actor.destroy(false);
			return null;
		}

		Planet planet = actor.getSectorLocation().getPlanet();
		actor.setLocation(
				actor.getSectorLocation().land(planet.getRandomCoord())
		);
		return null;
	}
}
