package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedMenu;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Battle;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.items.Action;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Paths;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.SCAN;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class BattleScreen extends MenuScreen<AlignedMenu>
        implements WindowScreen<AlignedWindow>, PopupMaster, KeyScreen
{
    private Screen popup;
    private Battle battle;
    private List<Ship> scanning;
    
    public BattleScreen(Display display, Battle battle, boolean nextTurn)
    {
        super(new AlignedMenu(new AlignedWindow(display, Coord.get(0, 0),
                new Border(2)), COLOR_SELECTION_FOREGROUND,
                COLOR_SELECTION_BACKGROUND));
        this.battle = battle;
        scanning = new LinkedList<>();
        
        // If possible, do this after the battle is over
        if (nextTurn)
            map.nextTurn();
    }
    
    @Override
    public void displayOutput()
    {
        setUpWindow();
        super.displayOutput();
        
        if (popup != null)
            popup.displayOutput();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            Screen result = popup.processInput(key);
            /*
            if (result == null && popup instanceof BattleConvertScreen)
                deniedConversion = true;
            */
            popup = result;
            updateBattle();
            return popup instanceof SectorScreen ? popup : this;
        }
        
        if (getMenu().updateSelectionRestricted(key))
            return this;
        
        boolean nextAttack = false;
        Ship selected = getSelectedShip();
        List<Ship> enemies = battle.getEnemies(player);
        boolean isOpponent = enemies.contains(selected);
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_L:
                if (isOpponent && player.fire(Actions.LASER, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.LASER);
                }
                break;
            case KeyEvent.VK_T:
                if (isOpponent && player.fire(Actions.TORPEDO, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.TORPEDO);
                }
                break;
            case KeyEvent.VK_P:
                if (isOpponent && player.fire(Actions.PULSE, selected))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.PULSE);
                }
                break;
            case KeyEvent.VK_F:
            {
                Action flee = Actions.FLEE;

                if (!player.validateResources(flee, "flee"))
                    break;
                
                player.changeResourceBy(flee);
                playSoundEffect(ENGINE);
                battle.getFleeing().add(player);
                
                /*
                if (player.isCloaked() || !opponent.willPursue(player))
                {
                    if (player.isCloaked())
                    {
                        addColorMessage(new ColorString("You have escaped ")
                                .add(opponent)
                                .add(" with the help of your cloaking."));
                    }
                    else if (!opponent.willPursue(player))
                    {
                        addColorMessage(opponent.toColorString()
                                .add(" does not pursue you."));
                    }

                    return endBattle();
                }
                */

                // The opponent has enough resources to pursue because 
                // it is checked in willPursue()
                nextAttack = true;
//                addColorMessage(opponent.toColorString().add(" pursues you."));
//                opponent.changeResourceBy(flee);
                break;
            }
            case KeyEvent.VK_S:
                if (selected != player && !scanning.contains(selected) &&
                        player.scan())
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
                
                return new EndScreen(getDisplay(), opponent.toColorString()
                    .add(" strips your ship of its components and departs."),
                        true);
            }
            */
            case KeyEvent.VK_PERIOD: case KeyEvent.VK_SPACE:
                nextAttack = true;
                break;
            case KeyEvent.VK_Q:
                popup = new QuitScreen(getDisplay());
                break;
        }
        
        if (nextAttack)
        {
            battle.processAttacks();
            
            if (player.isDestroyed())
            {
                return new EndScreen(getDisplay(),
                        new ColorString("You have been destroyed."), true);
            }
            
            if (!battle.getFleeing().contains(player) &&
                    player.validateResources(Actions.PURSUE, "pursue"))
            {
                List<Ship> enemiesEscaping = new LinkedList<>();
                for (Ship escaping: battle.getFleeing())
                {
                    if (enemies.contains(escaping))
                        enemiesEscaping.add(escaping);
                    else
                        battle.processEscape(escaping);
                }
                
                if (!enemiesEscaping.isEmpty())
                {
                    popup = new PursuitScreen(getDisplay(), battle,
                            enemiesEscaping);
                    return this;
                }
            }
            else
            {
                battle.processEscapes();
            }
            
            if (player.isInBattle())
                updateBattle();
            else
                return endBattle();
            
            if (!battle.continues())
            {
                battle.distributeLoot();
                battle.endBattle();
                return endBattle();
            }
        }
        
        return this;
        
        /*
        if (!nextAttack)
            return this;
        
        if (!deniedConversion && opponent.willConvert()
                && opponent.canConvert(player)
                && opponent.getAmountOf(Resources.HULL)
                > player.getAmountOf(Resources.HULL))
        {
            popup = new BattleConvertScreen(getDisplay(), opponent);
        }
        else if (!opponent.attack(player))
        {
            if (!opponent.validateResources(Actions.FLEE, "flee"))
            {
                if (opponent.isLeader() &&
                        player.isInFaction(opponent.getFaction()))
                {
                    popup = new SuccessionScreen(getDisplay(), opponent);
                    return this;
                }

                popup = new BattleWinScreen(getDisplay(), opponent,
                        opponent.toColorString()
                                .add(" surrenders its cargo to you."));
                return this;
            }
            
            opponent.changeResourceBy(Actions.FLEE);

            if (opponent.isCloaked())
            {
                addColorMessage(opponent.toColorString()
                .add(" becomes impossible to track due to their cloaking."));
                return endBattle();
            }

            if (!player.validateResources(Actions.PURSUE, "pursue"))
            {
                addColorMessage(opponent.toColorString()
                       .add(" escapes freely."));
                return endBattle();
            }
            
            popup = new PursueScreen(getDisplay(), opponent);
        }
        */
    }
    
    private Ship getSelectedShip()
    {
        String selectedText = getMenu().getSelection().toString();
        for (Ship ship: battle.getShips())
            if (selectedText.equals(ship.toString()))
                return ship;
        return null;
    }
    
    private void updateBattle()
        {battle = player.getBattleLocation().getBattle();}
    
    private SectorScreen endBattle()
    {
        Main.pendingBattle = null;
        return new SectorScreen(getDisplay());
    }

    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("keybindings", "h", "?"));
        keybindings.add(new Keybinding("quit", "Q"));
        keybindings.add(null);
        if (player.hasModule(Actions.LASER))
            keybindings.add(new Keybinding("fire laser", "l"));
        if (player.hasModule(Actions.TORPEDO))
            keybindings.add(new Keybinding("fire torpedo", "t"));
        if (player.hasModule(Actions.PULSE))
            keybindings.add(new Keybinding("fire pulse beam", "p"));
        if (player.hasActivationModules())
            keybindings.add(new Keybinding("toggle module activation", "m"));
        if (player.hasModule(Actions.SCAN))
            keybindings.add(new Keybinding("scan selected ship", "s"));
//        if (player.isAligned())
//            keybindings.add(new Keybinding("convert opponent", "c"));
        keybindings.add(new Keybinding("flee", "f"));
//        keybindings.add(new Keybinding("surrender", "u"));
        return keybindings;
    }
    
    @Override
    public AlignedWindow getWindow()
        {return getMenu().getWindow();}
    
    @Override
    public Screen getPopup()
        {return popup;}
    
    @Override
    public boolean hasPopup()
        {return popup != null;}
    
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

            for (Ship ally: battle.getAllies(player))
            {
                getMenu().getRestrictions().add(index);
                contents.add(ally.toColorString());
                index++;
            }

            getWindow().addSeparator(new Line(true, 2, 1));
            index++;
        }
        
        contents.add(new ColorString("Enemies", COLOR_FIELD));
        index++;
        
        for (Ship enemy: battle.getEnemies(player))
        {
            getMenu().getRestrictions().add(index);
            contents.add(enemy.toColorString());
            index++;
        }
        
        Ship selected = getSelectedShip();
        if (scanning.contains(selected))
        {
            getWindow().addSeparator(new Line(false, 2, 1));
            List<ColorString> statusList = selected.getStatusList();
            for (ColorString line: statusList)
            {
                if (line == null)
                    getWindow().addSeparator(new Line(false, 2, 1));
                else
                    contents.add(line);
            }
        }
        
        if (!getMenu().getRestrictions().contains(
                getMenu().getSelectionIndex()))
            getMenu().setSelectionIndex(getMenu().getRestrictions().get(0));
    }
}