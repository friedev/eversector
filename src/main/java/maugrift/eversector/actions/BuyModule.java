package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Module;
import maugrift.eversector.items.Weapon;
import maugrift.eversector.map.Station;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class BuyModule implements Action
{
	private final String module;
	public static final String SOUND_EFFECT = Paths.TRANSACTION;

	public BuyModule(String module)
	{
		this.module = module;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		String validateDocking = actor.validateDocking();
		if (validateDocking != null)
		{
			return validateDocking;
		}

		// Module must be retrieved after it is known that the ship is docked
		Station station = actor.getSectorLocation().getStation();
		Module moduleObj = station.getModule(module);

		if (moduleObj == null || moduleObj.getName() == null)
		{
			if (Station.hasBaseModule(module))
			{
				return station + " does not sell modules of this type.";
			}

			return "The specified module does not exist.";
		}

		if (!station.sells(moduleObj))
		{
			return station + " does not sell modules of this type.";
		}

		if (moduleObj instanceof Weapon && actor.isPirate())
		{
			return station + " refuses to sell weaponry to pirates.";
		}

		int price = moduleObj.getPrice();

		return actor.validateFunds(price);
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null)
		{
			return canExecute;
		}

		Station station = actor.getSectorLocation().getStation();
		Module moduleObj = station.getModule(module);

		actor.addModule(moduleObj);
		actor.changeCredits(station.getFaction(), -moduleObj.getPrice());
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
