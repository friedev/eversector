package maugrift.eversector.actions;

import maugrift.eversector.ships.Battle;
import maugrift.eversector.ships.Ship;

import static maugrift.eversector.Main.addMessage;

/**
 * @author Aaron Friesen
 */
public class Surrender implements Action
{
	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		return actor.isInBattle()
			? null
			: "You must be in a battle to surrender.";
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		Battle battle = actor.getBattleLocation().getBattle();
		Ship player = actor.getLocation().getGalaxy().getPlayer();
		if (player != null && battle.getShips().contains(player)) {
			addMessage(
				actor == player
				? "You surrender."
				: actor + " surrenders."
			);
		}

		actor.getBattleLocation().getBattle().getSurrendered().add(actor);
		return null;
	}
}
