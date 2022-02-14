package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.map.Planet;
import maugrift.eversector.ships.Ship;
import squidpony.squidmath.Coord;

public class Land implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 2;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	private final Coord coord;

	public Land(Coord coord)
	{
		this.coord = coord;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		String crashLandCheck = new CrashLand().canExecute(actor);
		if (crashLandCheck != null)
		{
			return crashLandCheck;
		}

		Planet planet = actor.getSectorLocation().getPlanet();

		if (!planet.contains(coord))
		{
			return "The specified region was not found on " + planet + ".";
		}

		return actor.validateResources(
				RESOURCE,
				COST,
				"land on " + actor.getSectorLocation().getPlanet()
		);
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null)
		{
			return canExecute;
		}

		actor.getResource(RESOURCE).changeAmount(-COST);
		actor.setLocation(actor.getSectorLocation().land(coord));
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
