package boldorf.eversector.screens;

import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Burn;
import boldorf.eversector.actions.Enter;
import boldorf.eversector.actions.Warp;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static boldorf.eversector.Main.*;

/**
 * The screen for viewing and navigating the galactic map.
 *
 * @author Maugrift
 */
public class MapScreen extends Screen implements WindowScreen<AlignedWindow>, PopupMaster, KeyScreen
{
    /**
     * The window.
     */
    private AlignedWindow window;

    /**
     * The screen temporarily displayed over and overriding all others.
     */
    private Screen popup;

    /**
     * The coordinates where the player is looking. Null if none.
     */
    private Coord cursor;

    /**
     * True if the player is selecting a sector to warp to.
     */
    private boolean warping;

    /**
     * Instantiates a new MapScreen.
     */
    public MapScreen()
    {
        super(Main.display);
        window = new AlignedWindow(Main.display, Coord.get(0, 0), new Border(2));
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
                {
                    cursor = targetLocation;
                }
                return this;
            }
            else
            {
                String burnExecution = new Burn(direction).execute(player);
                if (burnExecution == null)
                {
                    nextTurn = true;
                }
                else if (player.getLocation().move(direction) == null && player.validateResources(Burn.RESOURCE,
                        -Burn.COST, "burn") == null)
                {
                    popup = new IntergalacticScreen();
                    return this;
                }
                else
                {
                    addError(burnExecution);
                }
            }
        }
        else if (isLooking())
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_L:
                    if (warping)
                    {
                        break;
                    }
                case KeyEvent.VK_ESCAPE:
                    warping = false;
                case KeyEvent.VK_ENTER:
                    if (warping)
                    {
                        String warpExecution = new Warp(cursor).execute(player);
                        if (warpExecution == null)
                        {
                            nextTurn = true;
                        }
                        else
                        {
                            addError(warpExecution);
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
                    String enterExecution = new Enter().execute(player);
                    if (enterExecution == null)
                    {
                        nextTurn = true;
                        nextScreen = new SectorScreen();
                        break;
                    }

                    addError(enterExecution);
                    break;
                case KeyEvent.VK_W:
                    if (!player.hasModule(Warp.MODULE))
                    {
                        break;
                    }
                    warping = true;
                case KeyEvent.VK_L:
                    cursor = player.getLocation().getCoord();
                    break;
                case KeyEvent.VK_V:
                    Main.showStars = !Main.showStars;
                    break;
            }
        }

        if (nextTurn)
        {
            galaxy.nextTurn();
        }
        return nextScreen;
    }

    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(
                new Keybinding("burn to neighboring sectors", ExtChars.ARROW1_U, ExtChars.ARROW1_D, ExtChars.ARROW1_L,
                        ExtChars.ARROW1_R));
        keybindings.add(new Keybinding("enter a sector", "enter"));
        keybindings.add(new Keybinding("look", "l"));
        keybindings.add(new Keybinding("toggle star view", "v"));
        if (player.hasModule(Warp.MODULE))
        {
            keybindings.add(new Keybinding("warp to any sector", "w"));
        }
        return keybindings;
    }

    @Override
    public AlignedWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen getPopup()
    {
        return popup;
    }

    /**
     * Returns true if the player is looking around the map.
     *
     * @return true if the player is looking around the map
     */
    private boolean isLooking()
    {
        return cursor != null;
    }

    /**
     * Sets up the window and its contents.
     */
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        window.getSeparators().clear();

        contents.addAll(galaxy.toColorStrings(player, Main.showStars, cursor));
        window.addSeparator(new Line(true, 2, 1));
        Coord location = isLooking() ? cursor : player.getLocation().getCoord();
        contents.add(new ColorString(galaxy.sectorAt(location).toString()));

        if (!galaxy.sectorAt(location).isEmpty())
        {
            contents.add(new ColorString("Star: ").add(galaxy.sectorAt(location).getStar()));
        }

        if (galaxy.sectorAt(location).hasNebula())
        {
            contents.add(new ColorString("Nebula: ").add(galaxy.sectorAt(location).getNebula()));
        }

        if (galaxy.sectorAt(location).isClaimed())
        {
            contents.add(new ColorString("Faction: ").add(galaxy.sectorAt(location).getFaction()));
        }
    }
}