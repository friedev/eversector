package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.map.Station;
import maugrift.eversector.ships.Ship;

public class Dock implements Action
{
	public static final String SOUND_EFFECT = Paths.DOCK;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		if (!actor.isInSector())
		{
			return "Ship must be in orbit to dock.";
		}

		Station station = actor.getSectorLocation().getStation();

		if (station == null)
		{
			return "There is no station at this orbit.";
		}

		if (actor.isLanded())
		{
			return "The ship cannot dock while landed.";
		}

		if (actor.isDocked())
		{
			return "The ship is already docked with " + station + ".";
		}

		if (actor.isHostile(station.getFaction()) && actor.isAligned())
		{
			actor.setLocation(actor.getSectorLocation().dock());

			String claimExecution = new Claim().canExecute(actor);
			if (claimExecution != null)
			{
				actor.setLocation(actor.getStationLocation().undock());
				return station
					+ " is controlled by the hostile "
					+ station.getFaction()
					+ ", who deny you entry.";
			}
			return null;
		}
		else
		{
			actor.setLocation(actor.getSectorLocation().dock());
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

		actor.setLocation(actor.getSectorLocation().dock());
		if (actor.isHostile(actor.getStationLocation().getStation().getFaction()))
		{
			new Claim().execute(actor);
		}

		actor.repairModules();
		actor.updatePrices();
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
