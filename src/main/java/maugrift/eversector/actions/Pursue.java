package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class Pursue implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 1;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		if (!actor.isInBattle())
		{
			return "You must be in a battle to pursue.";
		}

		return actor.validateResources(RESOURCE, COST, "pursue");
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
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
