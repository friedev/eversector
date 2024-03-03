package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class Orbit implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 1;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	private final boolean increase;

	public Orbit(boolean increase)
	{
		this.increase = increase;
	}

	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		if (!actor.isInSector()) {
			return "You must be in a sector to orbit it.";
		}

		if (actor.getLocation().getSector().isEmpty()) {
			return "There is nothing to orbit in this sector.";
		}

		if (actor.isLanded()) {
			return "You must be orbital before attempting a maneuver.";
		}

		if (actor.isDocked()) {
			return "You must undock before attempting an orbital maneuver.";
		}

		int orbit = actor.getSectorLocation().getOrbit();
		int target = increase ? orbit + 1 : orbit - 1;

		if (!actor.getLocation().getSector().isValidOrbit(target)) {
			if (increase) {
				return new Escape().canExecute(actor);
			}

			return (
				"Invalid orbit. Must be between 1 and "
				+ actor.getLocation().getSector().getOrbits()
				+ "."
			);
		}

		Resource resource = actor.getResource(RESOURCE);

		if (resource == null) {
			return "Resource not found.";
		}

		return actor.validateResources(
				resource,
				COST,
				"perform an orbital maneuver"
			);
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		int orbit = actor.getSectorLocation().getOrbit();
		int target = increase ? orbit + 1 : orbit - 1;

		if (!actor.getLocation().getSector().isValidOrbit(target)) {
			// All other cases are ruled out by canExecute()
			return new Escape().execute(actor);
		}

		actor.setLocation(actor.getSectorLocation().setOrbit(target));
		actor.getResource(RESOURCE).changeAmount(-COST);
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
