package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import static boldorf.eversector.Main.addColorMessage;
import static boldorf.eversector.Main.addError;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.items.Action;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Paths;
import static boldorf.eversector.storage.Paths.CLAIM;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.SCAN;
import boldorf.eversector.storage.Resources;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class BattleScreen extends Screen implements WindowScreen<AlignedWindow>,
        PopupMaster, CommandScreen
{
    private AlignedWindow window;
    private Screen popup;
    private Ship opponent;
    private boolean deniedConversion;
    private boolean scanning;
    
    public BattleScreen(Display display, Ship opponent, boolean nextTurn)
    {
        super(display);
        window = new AlignedWindow(display, Coord.get(0, 0), new Border(1));
        this.opponent = opponent;
        deniedConversion = false;
        scanning = false;
        
        // If possible, do this after the battle is over
        if (nextTurn)
            map.nextTurn();
    }
    
    @Override
    public void displayOutput()
    {
        setUpWindow();
        window.display();
        
        if (popup != null)
            popup.displayOutput();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            Screen result = popup.processInput(key);
            if (result == null && popup instanceof BattleConvertScreen)
                deniedConversion = true;
            popup = result;
            return popup instanceof SectorScreen ? popup : this;
        }
        
        boolean nextAttack = false;
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_L:
                if (player.fire(Actions.LASER, opponent))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.LASER);
                }
                break;
            case KeyEvent.VK_T:
                if (player.fire(Actions.TORPEDO, opponent))
                {
                    nextAttack = true;
                    playSoundEffect(Paths.TORPEDO);
                }
                break;
            case KeyEvent.VK_P:
                if (player.fire(Actions.PULSE, opponent))
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

                // The opponent has enough resources to pursue because 
                // it is checked in willPursue()
                nextAttack = true;
                addColorMessage(opponent.toColorString().add(" pursues you."));
                opponent.changeResourceBy(flee);
                break;
            }
            case KeyEvent.VK_M:
                if (player.hasActivationModules())
                    popup = new ToggleScreen(getDisplay());
                else
                    addError("The ship has no modules that can be activated.");
                break;
            case KeyEvent.VK_S:
                if (scanning)
                    break;
                
                if (player.scan())
                {
                    nextAttack = true;
                    scanning = true;
                    playSoundEffect(SCAN);
                }
                break;
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
            case KeyEvent.VK_PERIOD: case KeyEvent.VK_SPACE:
                nextAttack = true;
                break;
            case KeyEvent.VK_SLASH:
                if (!key.isShiftDown())
                    break;
            case KeyEvent.VK_H:
                popup = new HelpScreen(getDisplay(), getKeybindings());
                break;
            case KeyEvent.VK_Q:
                popup = new QuitScreen(getDisplay());
                break;
        }
        
        if (opponent.isDestroyed())
        {
            popup = new BattleWinScreen(getDisplay(), opponent,
                    new ColorString("You have destroyed ")
                            .add(opponent.toColorString()).add("."));
            return this;
        }
        
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
        else if (player.isDestroyed())
        {
            return new EndScreen(getDisplay(),
                    new ColorString("You have been destroyed by ").add(opponent)
                            .add("."), true);
        }
        
        if (opponent.isDestroyed())
        {
            popup = new BattleWinScreen(getDisplay(), opponent,
                    new ColorString("You have destroyed ").add(opponent)
                            .add("."));
        }
        
        return this;
    }
    
    private SectorScreen endBattle()
        {return new SectorScreen(getDisplay());}

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
            keybindings.add(new Keybinding("scan opponent", "s"));
        if (player.isAligned())
            keybindings.add(new Keybinding("convert opponent", "c"));
        keybindings.add(new Keybinding("flee", "f"));
        keybindings.add(new Keybinding("surrender", "u"));
        return keybindings;
    }
    
    @Override
    public AlignedWindow getWindow()
        {return window;}
    
    @Override
    public Screen getPopup()
        {return popup;}
    
    @Override
    public boolean hasPopup()
        {return popup != null;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        
        contents.clear();
        window.getSeparators().clear();
        
        contents.add(new ColorString("Opponent: ").add(opponent));
        
        if (!scanning)
            return;
        
        window.addSeparator(new Line(true, 1, 1));
        List<ColorString> statusList = opponent.getStatusList();
        for (ColorString line: statusList)
        {
            if (line == null)
                window.addSeparator(new Line(true, 1, 1));
            else
                window.getContents().add(line);
        }
    }
}