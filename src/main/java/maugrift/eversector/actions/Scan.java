package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Module;
import maugrift.eversector.ships.Ship;

public class Scan implements Action
{
	public static final String MODULE = Module.SCANNER;
	public static final String SOUND_EFFECT = Paths.SCAN;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		String validateModule = actor.validateModule(MODULE, "conduct a scan");
		if (validateModule != null)
		{
			return validateModule;
		}

		Module module = actor.getModule(MODULE);
		return actor.validateResources(
				module.getActionResource(),
				module.getActionCost(),
				"conduct a scan"
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

		Module module = actor.getModule(MODULE);
		actor.getResource(module.getActionResource()).changeAmount(-module.getActionCost());
		return null;
	}
}
