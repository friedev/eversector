package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class Takeoff implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 2;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		if (!actor.isLanded()) {
			return "You are not landed.";
		}

		return actor.validateResources(
				RESOURCE,
				COST,
				"takeoff from the " + actor.getPlanetLocation().getRegion()
			);
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		actor.getResource(RESOURCE).changeAmount(-COST);
		actor.setLocation(actor.getPlanetLocation().takeoff());
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
