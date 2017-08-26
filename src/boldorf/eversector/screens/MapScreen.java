package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.util.Utility;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.storage.Actions;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.WARP;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class MapScreen extends Screen implements WindowScreen<AlignedWindow>,
        PopupMaster, KeyScreen
{
    private AlignedWindow window;
    private Screen popup;
    private Coord cursor;
    private boolean warping;
    
    public MapScreen(Display display)
    {
        super(display);
        window = new AlignedWindow(display, Coord.get(0, 0), new Border(2));
        popup = null;
        cursor = null;
        warping = false;
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
            popup = popup.processInput(key);
            return this;
        }
        
        boolean nextTurn = false;
        Screen nextScreen = this;
        Direction direction = Utility.keyToDirectionRestricted(key);
        
        if (direction != null)
        {
            if (isLooking())
            {
                Coord targetLocation = cursor.translate(direction);
                if (player.getFOV().contains(targetLocation))
                    cursor = targetLocation;
                return this;
            }
            else if (player.burn(direction))
            {
                nextTurn = true;
                playSoundEffect(ENGINE);
            }
            else if (player.getLocation().move(direction) == null)
            {
                if (player.getResource(Actions.BURN.getResource())
                        .getAmount() >= Actions.BURN.getCost())
                {
                    popup = new IntergalacticScreen(getDisplay());
                    return this;
                }
            }
        }
        else if (isLooking())
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_L:
                    if (warping)
                        break;
                case KeyEvent.VK_ESCAPE:
                    warping = false;
                case KeyEvent.VK_ENTER:
                    if (warping)
                    {
                        if (player.warpTo(cursor))
                        {
                            nextTurn = true;
                            playSoundEffect(WARP);
                        }
                        warping = false;
                        cursor = null;
                        break;
                    }
                    else
                    {
                        cursor = null;
                        return this;
                    }
            }
        }
        else
        {
            if (isLooking() && key.getKeyCode() == KeyEvent.VK_L)
            {
                cursor = null;
                return this;
            }
            
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_ENTER:
                    if (player.enter())
                    {
                        nextTurn = true;
                        nextScreen = new SectorScreen(getDisplay());
                    }
                    break;
                case KeyEvent.VK_W:
                    if (!player.hasModule(Actions.WARP))
                        break;
                    warping = true;
                case KeyEvent.VK_L:
                    cursor = player.getLocation().getCoord();
                    break;
                case KeyEvent.VK_V:
                    Main.showStars = player.hasModule(Actions.SCAN) &&
                            !Main.showStars;
                    break;
            }
        }
        
        if (nextTurn)
            map.nextTurn();
        return nextScreen;
    }
    
    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("burn to neighboring sectors",
                ExtChars.ARROW1_U, ExtChars.ARROW1_D, ExtChars.ARROW1_L,
                ExtChars.ARROW1_R));
        keybindings.add(new Keybinding("enter a sector", "enter"));
        keybindings.add(new Keybinding("look", "l"));
        if (player.hasModule(Actions.SCAN))
            keybindings.add(new Keybinding("toggle star view", "v"));
        if (player.hasModule(Actions.WARP))
            keybindings.add(new Keybinding("warp to any sector", "w"));
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
    
    private boolean isLooking()
        {return cursor != null;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        window.getSeparators().clear();
        
        contents.addAll(map.toColorStrings(player,
                player.hasModule(Actions.SCAN) && Main.showStars, cursor));
        
        window.addSeparator(new Line(true, 2, 1));
        Coord location = isLooking() ? cursor : player.getLocation().getCoord();
        contents.add(new ColorString(map.sectorAt(location).toString()));
        
        if (player.hasModule(Actions.SCAN) && !map.sectorAt(location).isEmpty())
        {
            contents.add(new ColorString("Star: ")
                    .add(map.sectorAt(location).getStar()));
        }
        
        if (map.sectorAt(location).hasNebula())
        {
            contents.add(new ColorString("Nebula: ")
                    .add(map.sectorAt(location).getNebula()));
        }
    }
}