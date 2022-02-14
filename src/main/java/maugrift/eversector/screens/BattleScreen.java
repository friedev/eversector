package maugrift.eversector.screens;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.*;
import maugrift.apwt.util.Utility;
import maugrift.apwt.windows.AlignedMenu;
import maugrift.apwt.windows.AlignedWindow;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Fire;
import maugrift.eversector.actions.Flee;
import maugrift.eversector.actions.Pursue;
import maugrift.eversector.actions.Scan;
import maugrift.eversector.items.Module;
import maugrift.eversector.items.Weapon;
import maugrift.eversector.ships.Battle;
import maugrift.eversector.ships.Ship;
import squidpony.squidmath.Coord;
import squidpony.squidgrid.Direction;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static maugrift.eversector.Main.*;

/**
 * A screen for managing interactions in battle.
 *
 * @author Maugrift
 */
public class BattleScreen extends MenuScreen<AlignedMenu> implements WindowScreen<AlignedWindow>, PopupMaster, KeyScreen
{
	public static final Color COLOR_SURRENDERED = AsciiPanel.brightBlack;

	/**
	 * The screen temporarily displayed over and overriding all others.
	 */
	private Screen popup;

	/**
	 * The battle the player is in.
	 */
	private Battle battle;

	/**
	 * All ships currently being scanned.
	 */
	private List<Ship> scanning;


	/**
	 * Instantiates a new BattleScreen.
	 *
	 * @param battle   the battle the player is in
	 * @param nextTurn if true, will advance to the next turn
	 */
	public BattleScreen(Battle battle, boolean nextTurn)
	{
		super(new AlignedMenu(new AlignedWindow(Main.display, 0, 0, new Border(2)),
				COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
		this.battle = battle;
		scanning = new LinkedList<>();

		// If possible, do this after the battle is over
		if (nextTurn)
		{
			galaxy.nextTurn();
		}
	}

	@Override
	public void displayOutput()
	{
		setUpWindow();
		super.displayOutput();

		if (popup != null)
		{
			popup.displayOutput();
		}
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (popup != null)
		{
			popup = popup.processInput(key);
			updateBattle();
			if (battle.getEnemies(player).isEmpty())
			{
				battle.endBattle();
				return endBattle();
			}
			return popup instanceof SectorScreen ? popup : this;
		}

		Direction direction = Utility.keyToDirectionRestricted(key);
		if (getMenu().select(direction.deltaY))
		{
			return this;
		}

		boolean nextAttack = false;
		Ship selected = getSelectedShip();
		List<Ship> enemies = battle.getEnemies(player);
		boolean isOpponent = enemies.contains(selected);
		// Possibly add a local action variable and process at end

		switch (key.getKeyCode())
		{
			case KeyEvent.VK_L:
				if (isOpponent)
				{
					String fire = new Fire(Weapon.LASER, selected).execute(player);
					if (fire == null)
					{
						nextAttack = true;
						break;
					}

					addError(fire);
				}
				break;
			case KeyEvent.VK_T:
				if (isOpponent)
				{
					String fire = new Fire(Weapon.TORPEDO_TUBE, selected).execute(player);
					if (fire == null)
					{
						nextAttack = true;
					}

					addError(fire);
				}
				break;
			case KeyEvent.VK_P:
				if (isOpponent)
				{
					String fire = new Fire(Weapon.PULSE_BEAM, selected).execute(player);
					if (fire == null)
					{
						nextAttack = true;
					}

					addError(fire);
				}
				break;
			case KeyEvent.VK_S:
				if (selected != player && !scanning.contains(selected))
				{
					String scanExecution = new Scan().execute(player);
					if (scanExecution != null)
					{
						addError(scanExecution);
						break;
					}

					nextAttack = true;
					scanning.add(selected);
				}
				break;
			case KeyEvent.VK_F:
			{
				if (battle.getSurrendered().contains(player))
				{
					addError("You may not flee after surrendering.");
					break;
				}

				String fleeExecution = new Flee().execute(player);
				if (fleeExecution != null)
				{
					addError(fleeExecution);
					break;
				}

				nextAttack = true;
				break;
			}
			case KeyEvent.VK_U:
				addMessage("You surrender.");
				battle.getSurrendered().add(player);
				nextAttack = true;
				break;
			/*
			case KeyEvent.VK_C:
			{
				if (!player.isAligned())
				{
					addError("You must be part of a faction to convert ships.");
					break;
				}

				if (opponent.isInFaction(player.getFaction()))
				{
					addColorMessage(opponent.toColorString()
							.add(" is already in the ").add(player.getFaction())
							.add("."));
					break;
				}

				if (!opponent.willAttack())
				{
					player.convert(opponent);
					addColorMessage(opponent.toColorString()
							.add(" has surrendered and joined the ")
							.add(player.getFaction()).add("."));
					playSoundEffect(CLAIM);
					return endBattle();
				}
				else
				{
					nextAttack = true;
					addColorMessage(opponent.toColorString()
							.add(" has refused to join the ")
							.add(player.getFaction()).add("."));
				}
				break;
			}
			*/
			case KeyEvent.VK_PERIOD:
			case KeyEvent.VK_SPACE:
				nextAttack = true;
				break;
			case KeyEvent.VK_Q:
				popup = new QuitScreen();
				break;
		}

		if (nextAttack)
		{
			if (!battle.continues())
			{
				battle.distributeLoot();
				battle.endBattle();

				if (player.isDestroyed())
				{
					return new EndScreen(new ColorString("You have been destroyed."), true, false);
				}

				return endBattle();
			}

			battle.processAttacks();

			if (player.isDestroyed())
			{
				return new EndScreen(new ColorString("You have been destroyed."), true, false);
			}

			if (!battle.getFleeing().contains(player) && new Pursue().canExecute(player) == null)
			{
				List<Ship> enemiesEscaping = new LinkedList<>();
				for (Ship escaping : battle.getFleeing())
				{
					if (enemies.contains(escaping))
					{
						enemiesEscaping.add(escaping);
					}
					else
					{
						battle.processEscape(escaping);
					}
				}

				if (!enemiesEscaping.isEmpty())
				{
					popup = new PursuitScreen(battle, enemiesEscaping);
					return this;
				}
			}
			else
			{
				battle.processEscapes();
			}

			if (player.isInBattle())
			{
				updateBattle();
			}
			else
			{
				return endBattle();
			}

			if (!battle.continues())
			{
				battle.distributeLoot();
				battle.endBattle();
				return endBattle();
			}
		}

		return this;
	}

	/**
	 * Gets the ship currently selected.
	 *
	 * @return the ship currently selected
	 */
	private Ship getSelectedShip()
	{
		String selectedText = getMenu().getSelection().toString();
		for (Ship ship : battle.getShips())
		{
			if (selectedText.equals(ship.toString()))
			{
				return ship;
			}
		}
		return null;
	}

	/**
	 * Updates the battle to the one the player is in.
	 */
	private void updateBattle()
	{
		battle = player.getBattleLocation().getBattle();
	}

	/**
	 * Ends the battle.
	 *
	 * @return a new SectorScreen
	 */
	private SectorScreen endBattle()
	{
		Main.pendingBattle = null;
		return new SectorScreen();
	}

	@Override
	public List<Keybinding> getKeybindings()
	{
		List<Keybinding> keybindings = new ArrayList<>();
		keybindings.add(new Keybinding("keybindings", "h", "?"));
		keybindings.add(new Keybinding("quit", "Q"));
		keybindings.add(null);
		if (player.hasModule("Laser"))
		{
			keybindings.add(new Keybinding("fire laser", "l"));
		}
		if (player.hasModule("Torpedo Tube"))
		{
			keybindings.add(new Keybinding("fire torpedo", "t"));
		}
		if (player.hasModule("Pulse Beam"))
		{
			keybindings.add(new Keybinding("fire pulse beam", "p"));
		}
		if (player.hasActivationModules())
		{
			keybindings.add(new Keybinding("toggle module activation", "m"));
		}
		if (player.hasModule(Module.SCANNER))
		{
			keybindings.add(new Keybinding("scan selected ship", "s"));
		}
		if (new Flee().canExecuteBool(player))
		{
			keybindings.add(new Keybinding("flee", "f"));
		}
		keybindings.add(new Keybinding("surrender", "u"));
		return keybindings;
	}

	@Override
	public AlignedWindow getWindow()
	{
		return getMenu().getWindow();
	}

	@Override
	public Screen getPopup()
	{
		return popup;
	}

	/**
	 * Sets up the window and its contents.
	 */
	private void setUpWindow()
	{
		List<ColorString> contents = getWindow().getContents();

		contents.clear();
		getWindow().getSeparators().clear();

		int index = 0;
		if (!battle.getAllies(player).isEmpty())
		{
			contents.add(new ColorString("Allies", COLOR_FIELD));
			index++;

			for (Ship ally : battle.getAllies(player))
			{
				getMenu().getRestrictions().add(index);
				ColorString allyString = ally.toColorString();
				if (battle.getSurrendered().contains(ally))
				{
					allyString.setForeground(COLOR_SURRENDERED);
				}
				contents.add(allyString);
				index++;
			}

			getWindow().addSeparator(new Line(true, 2, 1));
			index++;
		}

		contents.add(new ColorString("Enemies", COLOR_FIELD));
		index++;

		for (Ship enemy : battle.getEnemies(player))
		{
			getMenu().getRestrictions().add(index);
			ColorString enemyString = enemy.toColorString();
			if (battle.getSurrendered().contains(enemy))
			{
				enemyString.setForeground(COLOR_SURRENDERED);
			}
			contents.add(enemyString);
			index++;
		}

		Ship selected = getSelectedShip();
		if (scanning.contains(selected))
		{
			getWindow().addSeparator(new Line(false, 2, 1));
			List<ColorString> statusList = selected.getStatusList();
			for (ColorString line : statusList)
			{
				if (line == null)
				{
					getWindow().addSeparator(new Line(false, 2, 1));
				}
				else
				{
					contents.add(line);
				}
			}
		}

		if (!getMenu().getRestrictions().contains(getMenu().getSelectionIndex()))
		{
			getMenu().setSelectionIndex(getMenu().getRestrictions().get(0));
		}
	}
}
