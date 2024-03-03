package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Expander;
import maugrift.eversector.items.Resource;
import maugrift.eversector.map.Station;
import maugrift.eversector.ships.Ship;

/**
 * @author Aaron Friesen
 */
public class TransactResource implements Action
{
	public static final String SOUND_EFFECT = Paths.TRANSACTION;

	private final String resource;
	private final int quantity;
	private final boolean sound;

	public TransactResource(String resource, int quantity)
	{
		this.resource = resource;
		this.quantity = quantity;
		this.sound = true;
	}

	public TransactResource(String resource, int quantity, boolean sound)
	{
		this.resource = resource;
		this.quantity = quantity;
		this.sound = sound;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		String validateDocking = actor.validateDocking();
		if (validateDocking != null) {
			return validateDocking;
		}

		if (quantity == 0) {
			return "Quantity of items in transaction must be positive.";
		}

		Resource resourceObj = actor.getResource(resource);
		Station station = actor.getSectorLocation().getStation();

		if (resourceObj == null) {
			// Try finding if an expander was specified and continue purchase
			Expander expander = station.getExpander(resource);

			if (expander == null) {
				return "The specified item does not exist.";
			}

			resourceObj = actor.getResourceFromExpander(expander.getName());
			int price = expander.getPrice() * quantity;

			String validateFunds = actor.validateFunds(price);
			if (validateFunds != null) {
				return validateFunds;
			}

			if (quantity > 0 && resourceObj.getNExpanders() + 1 > Ship.MAX_EXPANDERS) {
				return (
					"The ship cannot store over "
					+ Ship.MAX_EXPANDERS
					+ " "
					+ expander.getName().toLowerCase()
					+ "s."
				);
			} else if (quantity < 0 && !resourceObj.canExpand(-1)) {
				return "No expanders to sell.";
			}

			return null;
		}

		if (!resourceObj.canSell() && quantity < 0) {
			return resourceObj + " cannot be sold.";
		}

		int price = station.getResource(resource).getPrice() * quantity;

		String validateFunds = actor.validateFunds(price);
		if (validateFunds != null) {
			return validateFunds;
		}

		if (!resourceObj.canHold(quantity)) {
			if (actor.isPlayer()) {
				if (quantity > 0) {
					return (
						"Inadequate storage; have "
						+ resourceObj.getCapacity()
						+ ", need"
						+ (resourceObj.getAmount() + quantity)
						+ "."
					);
				}

				return (
					"Inadequate resources to sell; have "
					+ resourceObj.getAmount()
					+ ", need "
					+ Math.abs(quantity)
					+ "."
				);
			}
		}

		return null;
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		// TODO add fields for resourceObj and others

		Resource resourceObj = actor.getResource(resource);
		Station station = actor.getSectorLocation().getStation();

		if (resourceObj == null) {
			Expander expander = station.getExpander(resource);
			resourceObj = actor.getResourceFromExpander(expander.getName());
			int price = expander.getPrice() * quantity;

			actor.changeCredits(
				station.getFaction(),
				resourceObj.getPrice() * Math.max(
					0,
					resourceObj.getAmount() - resourceObj.getCapacity()
				)
			);
			resourceObj.expand(quantity);
			actor.changeCredits(station.getFaction(), -price);
		} else {
			int price = station.getResource(resource).getPrice() * quantity;
			resourceObj.changeAmount(quantity);
			actor.changeCredits(station.getFaction(), -price);
		}

		if (sound) {
			actor.playPlayerSound(SOUND_EFFECT);
		}
		return null;
	}
}
