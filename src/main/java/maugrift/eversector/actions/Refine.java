package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Module;
import maugrift.eversector.items.Resource;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class Refine implements Action
{
	public static final String MODULE = Module.REFINERY;
	public static final String SOUND_EFFECT = Paths.REFINE;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		if (!actor.getResource(Resource.ORE).canHold(-1))
		{
			return "Ship has no ore to refine.";
		}

		if (!actor.getResource(Resource.FUEL).canHold(1))
		{
			return "Insufficient fuel storage.";
		}

		return actor.validateModule(MODULE, "refine ore");
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null)
		{
			return canExecute;
		}

		actor.getResource(Resource.ORE).changeAmount(-1);
		actor.getResource(Resource.FUEL).changeAmount(1);
		return null;
	}
}
