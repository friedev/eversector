package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.locations.Location;
import maugrift.eversector.ships.Ship;
import squidpony.squidgrid.Direction;

/**
 * @author Aaron Friesen
 */
public class Relocate implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 1;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	private final Direction direction;

	public Relocate(Direction direction)
	{
		this.direction = direction;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		if (!actor.isLanded()) {
			return "You must already be landed on a planet to relocate.";
		}

		if (direction.isDiagonal()) {
			return "Diagonal relocation is not allowed.";
		}

		if (getDestination(actor) == null) {
			return "Invalid region specified.";
		}

		return actor.validateResources(RESOURCE, COST, "relocate");
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		actor.getResource(RESOURCE).changeAmount(-COST);
		actor.setLocation(getDestination(actor));
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}

	/**
	 * Returns the location the actor is attempting to relocate to.
	 *
	 * @param actor the actor
	 * @return the location the actor is attempting to relocate to
	 */
	private Location getDestination(Ship actor)
	{
		return actor.getPlanetLocation().moveRegion(direction);
	}
}
