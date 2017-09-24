package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.*;
import boldorf.apwt.windows.AlignedMenu;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import boldorf.eversector.Paths;
import boldorf.eversector.items.Action;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;
import squidpony.squidmath.Coord;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.*;
import static boldorf.eversector.Paths.ENGINE;
import static boldorf.eversector.Paths.SCAN;

/**
 * A screen for managing interactions in battle.
 *
 * @author Boldorf Smokebane
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
        super(new AlignedMenu(new AlignedWindow(Main.display, Coord.get(0, 0), new Border(2)),
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

        if (getMenu().updateSelectionRestricted(key))
        {
            return this;
        }

        boolean nextAttack = false;
        Ship selected = getSelectedShip();
        List<Ship> enemies = battle.getEnemies(player);
        boolean isOpponent = enemies.contains(selected);

        switch (key.getKeyCode())
        {
            case KeyEvent.VK_L:
                if (isOpponent && player.fire(Action.LASER, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.LASER);
                }
                break;
            case KeyEvent.VK_T:
                if (isOpponent && player.fire(Action.TORPEDO, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.TORPEDO);
                }
                break;
            case KeyEvent.VK_P:
                if (isOpponent && player.fire(Action.PULSE, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.PULSE);
                }
                break;
            case KeyEvent.VK_F:
            {
                Action flee = Action.FLEE;

                if (!player.validateResources(flee, "flee"))
                {
                    break;
                }

                player.changeResourceBy(flee);
                playSoundEffect(ENGINE);
                battle.getFleeing().add(player);

                // The opponent has enough resources to pursue because 
                // it is checked in willPursue()
                nextAttack = true;
                break;
            }
            case KeyEvent.VK_S:
                if (selected != player && !scanning.contains(selected) && player.scan())
                {
                    nextAttack = true;
                    scanning.add(selected);
                    playSoundEffect(SCAN);
                }
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
            case KeyEvent.VK_U:
            {
                if (!opponent.willAttack())
                {
                    addColorMessage(opponent.toColorString()
                            .add(" uses this opportunity to escape."));
                    return endBattle();
                }

                if (opponent.willConvert() && opponent.convert(player))
                    return endBattle();
                
                return new EndScreen(opponent.toColorString()
                    .add(" strips your ship of its components and departs."),
                        true);
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
            battle.processAttacks();

            if (player.isDestroyed())
            {
                return new EndScreen(new ColorString("You have been destroyed."), true, false);
            }

            if (!battle.getFleeing().contains(player) && player.validateResources(Action.PURSUE, "pursue"))
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
        if (player.hasModule(Action.LASER))
        {
            keybindings.add(new Keybinding("fire laser", "l"));
        }
        if (player.hasModule(Action.TORPEDO))
        {
            keybindings.add(new Keybinding("fire torpedo", "t"));
        }
        if (player.hasModule(Action.PULSE))
        {
            keybindings.add(new Keybinding("fire pulse beam", "p"));
        }
        if (player.hasActivationModules())
        {
            keybindings.add(new Keybinding("toggle module activation", "m"));
        }
        if (player.hasModule(Action.SCAN))
        {
            keybindings.add(new Keybinding("scan selected ship", "s"));
        }
        keybindings.add(new Keybinding("flee", "f"));
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

    @Override
    public boolean hasPopup()
    {
        return popup != null;
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