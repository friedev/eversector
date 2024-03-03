package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.locations.Location;
import maugrift.eversector.ships.Ship;
import squidpony.squidgrid.Direction;

/**
 * @author Aaron Friesen
 */
public class Burn implements Action
{
	public static final String RESOURCE = Resource.FUEL;
	public static final int COST = 4;
	public static final String SOUND_EFFECT = Paths.ENGINE;

	private final Direction direction;

	public Burn(Direction direction)
	{
		this.direction = direction;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		if (actor.isInSector()) {
			return "You must escape the sector before performing an interstellar burn.";
		}

		if (direction.isDiagonal()) {
			return "Diagonal burns are not allowed.";
		}

		if (getDestination(actor) == null) {
			return "Invalid burn destination.";
		}

		return actor.validateResources(RESOURCE, COST, "initiate a burn");
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		actor.setLocation(getDestination(actor));
		actor.getResource(RESOURCE).changeAmount(-COST);
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}

	/**
	 * Returns the location the actor is attempting to burn to.
	 *
	 * @param actor the actor
	 * @return the location the actor is attempting to burn to
	 */
	private Location getDestination(Ship actor)
	{
		return actor.getLocation().move(direction);
	}
}
