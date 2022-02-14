package maugrift.eversector.ships;

import maugrift.eversector.actions.Action;
import maugrift.eversector.actions.Fire;
import maugrift.eversector.actions.Loot;
import maugrift.eversector.locations.SectorLocation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static maugrift.eversector.Main.rng;

/**
 * A class for managing battles between ships.
 *
 * @author Maugrift
 */
public class Battle
{
	/**
	 * The attacking ship and all that joined it.
	 */
	private List<Ship> attackers;

	/**
	 * The defending ship and all that joined it.
	 */
	private List<Ship> defenders;

	/**
	 * The ships attempting to flee on the next turn.
	 */
	private List<Ship> fleeing;

	/**
	 * The ships that have surrendered to the other side.
	 */
	private List<Ship> surrendered;

	/**
	 * All ships that have been destroyed in the battle.
	 */
	private List<Ship> destroyed;

	/**
	 * Creates a battle between the given attackers and defenders.
	 *
	 * @param attackers the ships attacking the defenders
	 * @param defenders the victims of the attack
	 */
	public Battle(List<Ship> attackers, List<Ship> defenders)
	{
		this.attackers = attackers;
		this.defenders = defenders;
		this.fleeing = new LinkedList<>();
		this.surrendered = new LinkedList<>();
		this.destroyed = new LinkedList<>();
	}

	/**
	 * Creates a battle between two ships.
	 *
	 * @param attacker the ship attacking the defender
	 * @param defender the victim of the attack
	 */
	public Battle(Ship attacker, Ship defender)
	{
		attackers = new LinkedList<>();
		attackers.add(attacker);
		defenders = new LinkedList<>();
		defenders.add(defender);
		fleeing = new LinkedList<>();
		surrendered = new LinkedList<>();
		destroyed = new LinkedList<>();
	}

	/**
	 * Gets the attacking ships.
	 *
	 * @return the attacking ships
	 */
	public List<Ship> getAttackers()
	{
		return attackers;
	}

	/**
	 * Gets the defending ships.
	 *
	 * @return the defending ships
	 */
	public List<Ship> getDefenders()
	{
		return defenders;
	}

	/**
	 * Gets the ships attempting to flee on the next turn.
	 *
	 * @return the ships attempting to flee on the next turn
	 */
	public List<Ship> getFleeing()
	{
		return fleeing;
	}

	/**
	 * Gets the ships that have surrendered to the other side.
	 *
	 * @return ships that have surrendered to the other side
	 */
	public List<Ship> getSurrendered()
	{
		return surrendered;
	}

	/**
	 * Gets the ships that have been destroyed in the battle.
	 *
	 * @return the ships that have been destroyed in the battle
	 */
	public List<Ship> getDestroyed()
	{
		return destroyed;
	}

	/**
	 * Gets all attacking and defending ships.
	 *
	 * @return all attacking and defending ships
	 */
	public List<Ship> getShips()
	{
		List<Ship> ships = new ArrayList<>(attackers.size() + defenders.size());
		ships.addAll(attackers);
		ships.addAll(defenders);
		return ships;
	}

	/**
	 * Gets all the ships fighting on the same side as the given ship. This
	 * list will not include the given ship.
	 *
	 * @param ship the ship to get allies of
	 * @return all the ships fighting on the same side as the given ship,
	 *         excluding the given ship itself
	 */
	public List<Ship> getAllies(Ship ship)
	{
		List<Ship> allies = new LinkedList<>();
		allies.addAll(attackers.contains(ship) ? attackers : defenders);
		allies.remove(ship);
		return allies;
	}

	/**
	 * Gets all the ships fighting on the opposite side of the given ship.
	 *
	 * @param ship the ship to get enemies of
	 * @return all the ships fighting on the opposite side of the given ship
	 */
	public List<Ship> getEnemies(Ship ship)
	{
		return attackers.contains(ship) ? defenders : attackers;
	}

	/**
	 * Returns true if the battle is continuing. This is the case when there is
	 * at least one attacker and at least one defender.
	 *
	 * @return true if the battle is continuing
	 */
	public boolean continues()
	{
		return !attackers.isEmpty() &&
			!defenders.isEmpty() &&
			!surrendered.containsAll(attackers) &&
			!surrendered.containsAll(defenders);
	}

	/**
	 * Gives all ships an opportunity to attack others, alternating between
	 * sides. Ships that are destroyed will be moved into the destroyed list.
	 *
	 * @return true if at least one ship attacked another
	 */
	public boolean processAttacks()
	{
		boolean attackMade = false;
		int size = Math.max(attackers.size(), defenders.size());
		for (int i = 0; i < size; i++)
		{
			if (attackers.size() >= i + 1 && shipCanAttack(attackers.get(i)))
			{
				Action action = attackers.get(i).getAI().performBattleAction();
				if (action != null)
				{
					attackMade = attackMade || action.executeBool(attackers.get(i));
				}
				updateReputation(attackers.get(i), action);
			}

			if (defenders.size() >= i + 1 && shipCanAttack(defenders.get(i)))
			{
				Action action = defenders.get(i).getAI().performBattleAction();
				if (action != null)
				{
					attackMade = attackMade || action.executeBool(defenders.get(i));
				}
				updateReputation(defenders.get(i), action);
			}
		}

		for (Ship ship : getShips())
		{
			if (ship.isDestroyed())
			{
				// Duplicate remove done to avoid concurrent modification
				attackers.remove(ship);
				defenders.remove(ship);
				fleeing.remove(ship);
				surrendered.remove(ship);
				destroyed.add(ship);
			}
		}

		return attackMade;
	}

	private void updateReputation(Ship attacker, Action attack)
	{
		if (!(attack instanceof Fire))
			return;

		Ship defender = ((Fire) attack).getTarget();

		if (defender.isDestroyed())
		{
			if (attacker.isPassive(defender))
			{
				attacker.changeReputation(attacker.getFaction(), Reputation.KILL_ALLY);
			}
			else
			{
				attacker.changeReputation(attacker.getFaction(), Reputation.KILL_ENEMY);
			}

			attacker.changeReputation(defender.getFaction(), Reputation.KILL_ALLY);
		}
	}

	/**
	 * Returns true if the given ship can attack.
	 *
	 * @param ship the ship to check
	 * @return true if the given ship can attack
	 */
	private boolean shipCanAttack(Ship ship)
	{
		return !ship.isDestroyed() &&
			!surrendered.contains(ship) &&
			ship.getAI() != null;
	}

	/**
	 * Gathers a list of ships that will pursue the given ship if they flee.
	 *
	 * @param ship the fleeing ship
	 * @return a list of ships that will pursue the given ship if they flee
	 */
	public List<Ship> getPursuers(Ship ship)
	{
		List<Ship> pursuing = new LinkedList<>();

		for (Ship enemy : getEnemies(ship))
		{
			if (enemy.getAI() != null &&
					!fleeing.contains(enemy) &&
					enemy.getAI().pursue())
			{
				pursuing.add(enemy);
			}
		}

		return pursuing;
	}

	/**
	 * Processes the given ship's escape and the pursuits of all given pursuing
	 * ships. Creates a new battle between the fleeing ship and its pursuers.
	 *
	 * @param ship     the fleeing ship, must be listed as fleeing in the
	 *                 battle
	 * @param pursuing the list of ships pursuing the fleeing ship
	 */
	public void processEscape(Ship ship, List<Ship> pursuing)
	{
		if (!fleeing.contains(ship))
		{
			return;
		}

		SectorLocation destination = ship.getSectorLocation();

		if (!ship.isCloaked())
		{
			if (rng.nextBoolean())
			{
				SectorLocation test = destination.raiseOrbit();
				destination = test == null ? destination.lowerOrbit() : test;
			}
			else
			{
				SectorLocation test = destination.lowerOrbit();
				destination = test == null ? destination.raiseOrbit() : test;
			}

			if (pursuing.isEmpty())
			{
				ship.addPlayerMessage("You successfully flee the battle.");
			}
			else
			{
				List<Ship> defenderList = new LinkedList<>();
				defenderList.add(ship);
				Battle newBattle = new Battle(pursuing, defenderList);
				destination = destination.joinBattle(newBattle);

				for (Ship pursuer : pursuing)
				{
					pursuer.setLocation(destination);
					attackers.remove(pursuer);
					defenders.remove(pursuer);
				}

				ship.addPlayerMessage("You have been pursued.");
			}
		}
		else
		{
			ship.addPlayerMessage("You escape cloaked and undetected.");
		}

		ship.setLocation(destination);
		attackers.remove(ship);
		defenders.remove(ship);
	}

	/**
	 * Processes the escape of the given ship, gathering all ships that will
	 * pursuer it.
	 *
	 * @param ship the fleeing ship
	 * @see #processEscape(Ship, List)
	 * @see #getPursuers(Ship)
	 */
	public void processEscape(Ship ship)
	{
		processEscape(ship, getPursuers(ship));
	}

	/**
	 * Processes the escapes of all ships.
	 *
	 * @see #processEscape(Ship)
	 */
	public void processEscapes()
	{
		for (Ship ship : new ArrayList<>(fleeing))
		{
			processEscape(ship);
		}
	}

	/**
	 * Distributes the loot of the destroyed ships among the victorious ships.
	 */
	public void distributeLoot()
	{
		List<Ship> winners = null;

		for (Ship ship : attackers)
		{
			if (!surrendered.contains(ship))
			{
				winners = attackers;
			}
		}

		if (winners == null)
		{
			for (Ship ship : defenders)
			{
				if (!surrendered.contains(ship))
				{
					winners = defenders;
				}
			}
		}

		if (winners == null)
		{
			return;
		}

		List<Ship> looting = destroyed;
		for (Ship ship : surrendered)
		{
			if (!winners.contains(ship))
			{
				looting.add(ship);
			}
		}

		int looterIndex = 0;
		for (Ship lootedShip : looting)
		{
			Ship looter = winners.get(looterIndex);

			while (surrendered.contains(looter))
			{
				looterIndex++;
				if (looterIndex >= winners.size())
				{
					looterIndex = 0;
				}

				looter = winners.get(looterIndex);
			}

			if (lootedShip.isLeader() &&
					looter.getFaction() == lootedShip.getFaction())
			{
				looter.getFaction().setLeader(looter);
				looter.getFaction().addNews(looter.toColorString()
						.add(" has destroyed our leader, ")
						.add(lootedShip)
						.add(", and taken control of the faction."));
			}

			lootedShip.destroy(false);
			new Loot(lootedShip).execute(looter);
		}
	}

	/**
	 * Ends the battle, moving all ships in the battle back to the sector.
	 */
	public void endBattle()
	{
		for (Ship ship : getShips())
		{
			if (ship.isInBattle())
			{
				ship.setLocation(ship.getBattleLocation().leaveBattle());
			}
		}

		attackers.clear();
		defenders.clear();
		fleeing.clear();
		surrendered.clear();
		destroyed.clear();
	}

	/**
	 * Processes the entire battle. Only to be used when the player is not
	 * participating in the battle.
	 */
	public void processBattle()
	{
		while (continues())
		{
			if (!processAttacks() || !continues())
			{
				break;
			}
			processEscapes();
		}

		distributeLoot();
		endBattle();
	}

	/*
	public void controlAIBattle(Ship ship)
	{
		do
		{
			// If this ship chooses to flee but is pursued
			if (!attack(ship))
			{
				if (changeResourceBy(Action.FLEE) &&
						(!ship.willPursue(this) ||
						 !ship.changeResourceBy(Action.PURSUE)))
				{
					// If the other ship is powerful enough, they will convert
					// this ship
					if (ship.willConvert() && ship.convert(this))
						return;

					// If the conversion fails, the fight continues
				}
				else
				{
					break;
				}
			}

			// If the other ship flees but is pursued
			if (!ship.attack(this))
			{
				if (ship.changeResourceBy(Action.PURSUE) &&
						(!willPursue(ship) || !changeResourceBy(Action.FLEE)))
				{
					if (willConvert() && convert(ship))
						return;
				}
			}
		} while (!isDestroyed() && !ship.isDestroyed());

		// If this ship is destroyed or cannot flee while the other ship lives
		if (isDestroyed() || (!validateResources(Action.FLEE, "flee") &&
				!ship.isDestroyed()))
		{
			if (isLeader() && ship.isInFaction(faction))
			{
				faction.setLeader(ship);
				faction.addNews(ship + " has defeated our leader, " + toString()
						+ ", and has wrested control of the faction.");
			}
			else
			{
				if (ship.isPassive(this))
					ship.changeReputation(ship.faction, Reputation.KILL_ALLY);
				else
					ship.changeReputation(ship.faction, Reputation.KILL_ENEMY);

				ship.changeReputation(faction, Reputation.KILL_ALLY);

				if (isLeader())
				{
					faction.addNews(ship + " of the " + ship.faction
							+ " has destroyed our leader, " + toString() + ".");
				}

				ship.loot(this);

				if (!isDestroyed())
					destroy(false);
			}
		}
		else
		{
			if (ship.isLeader() && isInFaction(faction))
			{
				ship.faction.setLeader(this);
				ship.faction.addNews(toString() + " has defeated our leader, "
						+ ship + ", and has wrested control of the faction.");
			}
			else
			{
				if (isPassive(ship))
					changeReputation(faction, Reputation.KILL_ALLY);
				else
					changeReputation(faction, Reputation.KILL_ENEMY);

				changeReputation(ship.faction, Reputation.KILL_ALLY);

				if (ship.isLeader())
				{
					ship.faction.addNews(toString() + " of the " + faction
							+ " has destroyed our leader, " + ship.toString()
							+ ".");
				}

				loot(ship);

				if (!ship.isDestroyed())
					ship.destroy(false);
			}
		}
	}
	*/
}
