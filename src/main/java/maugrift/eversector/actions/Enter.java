package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.map.Sector;
import maugrift.eversector.ships.Ship;

public class Enter implements Action
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

		Sector sector = actor.getLocation().getSector();

		if (actor.isInSector())
		{
			return "You are already in " + sector + ".";
		}

		if (sector.isEmpty())
		{
			return "There is nothing in " + sector + ".";
		}

		return actor.validateResources(RESOURCE, COST, "enter into orbit around " + sector);
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
		actor.setLocation(actor.getLocation().enterSector());
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
