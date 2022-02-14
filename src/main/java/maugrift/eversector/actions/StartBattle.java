package maugrift.eversector.actions;

import maugrift.apwt.glyphs.ColorString;
import maugrift.eversector.Main;
import maugrift.eversector.ships.Battle;
import maugrift.eversector.ships.Ship;

import java.util.List;

/**
 * Starts a battle with the given ship. Prompts other ships to join the battle. Processes the battle if the player does
 * not participate in it.
 */
public class StartBattle implements Action
{
	private final Ship opponent;

	public StartBattle(Ship opponent)
	{
		this.opponent = opponent;
	}

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null)
		{
			return "Ship not found.";
		}

		if (actor == opponent)
		{
			return "You cannot attack yourself.";
		}

		if (!actor.getLocation().equals(opponent.getLocation()))
		{
			return "You must be at the same location as the chosen ship.";
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

		Battle battle = new Battle(actor, opponent);
		actor.setLocation(actor.getSectorLocation().joinBattle(battle));
		opponent.setLocation(actor.getLocation());

		List<Ship> others = actor.getSectorLocation().getShips();
		for (Ship other : others)
		{
			if (other.getAI() != null && !other.isInBattle())
			{
				other.getAI().joinBattle(battle);
			}
		}

		if (opponent.isPlayer())
		{
			Main.pendingBattle = battle;
			opponent.addPlayerColorMessage(new ColorString("You are under attack from ").add(actor).add("!"));
			return null;
		}

		if (!actor.isPlayer())
		{
			battle.processBattle();
		}

		return null;
	}
}
