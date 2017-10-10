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
import boldorf.eversector.actions.Claim;
import boldorf.eversector.actions.Mine;
import boldorf.eversector.actions.Relocate;
import boldorf.eversector.actions.Takeoff;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Region;
import boldorf.eversector.ships.Ship;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static boldorf.eversector.Main.*;

/**
 * The screen for interacting with planets and navigating their regions.
 *
 * @author Maugrift
 */
public class PlanetScreen extends Screen implements WindowScreen<AlignedWindow>, KeyScreen
{
    /**
     * The window.
     */
    private AlignedWindow window;

    /**
     * The region currently selected. Null if not looking.
     */
    private PlanetLocation cursor;

    /**
     * Instantiates a new PlanetScreen.
     */
    public PlanetScreen()
    {
        super(Main.display);
        window = new AlignedWindow(Main.display, Coord.get(1, 1), new Border(2));
    }

    @Override
    public void displayOutput()
    {
        setUpWindow();
        window.display();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        boolean nextTurn = false;
        Screen nextScreen = this;

        Direction direction = Utility.keyToDirectionRestricted(key);
        if (direction != null)
        {
            if (isLooking())
            {
                cursor = cursor.moveRegion(direction);
            }
            else
            {
                String relocateExecution = new Relocate(direction).execute(player);
                if (relocateExecution == null)
                {
                    nextTurn = true;
                }
                else
                {
                    addError(relocateExecution);
                }
            }
        }
        else if (isLooking())
        {
            if (key.getKeyCode() == KeyEvent.VK_L || key.getKeyCode() == KeyEvent.VK_ESCAPE ||
                key.getKeyCode() == KeyEvent.VK_ENTER)
            {
                cursor = null;
            }
        }
        else
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_ESCAPE:
                    String takeoffExecution = new Takeoff().execute(player);
                    if (takeoffExecution == null)
                    {
                        nextTurn = true;
                        nextScreen = new SectorScreen();
                        break;
                    }
                    addError(takeoffExecution);
                    break;
                case KeyEvent.VK_ENTER:
                    String mineExecution = new Mine().execute(player);
                    if (mineExecution == null)
                    {
                        nextTurn = true;
                        break;
                    }
                    addError(mineExecution);
                    break;
                case KeyEvent.VK_C:
                    String claimExecution = new Claim().execute(player);
                    if (claimExecution == null)
                    {
                        nextTurn = true;
                        break;
                    }
                    addError(claimExecution);
                    break;
                case KeyEvent.VK_L:
                    cursor = player.getPlanetLocation();
                    break;
                case KeyEvent.VK_V:
                    Main.showFactions = !Main.showFactions;
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
        keybindings.add(new Keybinding("change region", ExtChars.ARROW1_U, ExtChars.ARROW1_D, ExtChars.ARROW1_L,
                ExtChars.ARROW1_R));
        keybindings.add(new Keybinding("takeoff", "escape"));
        keybindings.add(new Keybinding("mine", "enter"));
        keybindings.add(new Keybinding("claim", "c"));
        keybindings.add(new Keybinding("look", "l"));
        keybindings.add(new Keybinding("toggle faction view", "v"));
        return keybindings;
    }

    @Override
    public AlignedWindow getWindow()
    {
        return window;
    }

    /**
     * Returns true if the player is using the cursor to look around.
     *
     * @return true if the player is using the cursor to look around
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
        Planet planet = player.getSectorLocation().getPlanet();
        Region region = isLooking() ? cursor.getRegion() : player.getPlanetLocation().getRegion();
        contents.add(new ColorString(planet.toString()));
        contents.add(new ColorString("Orbit: ").add(
                new ColorString(Integer.toString(planet.getLocation().getOrbit()), COLOR_FIELD)));

        if (planet.isClaimed())
        {
            contents.add(new ColorString("Ruler: ").add(
                    new ColorString(planet.getFaction().toString(), planet.getFaction().getColor())));
        }
        else
        {
            contents.add(new ColorString("Ruler: ").add(new ColorString("Disputed", COLOR_FIELD)));
        }

        window.addSeparator(new Line(true, 2, 1));
        List<ColorString> colorStrings = planet.toColorStrings(Main.showFactions);
        if (isLooking())
        {
            colorStrings.get(cursor.getRegionCoord().y).getColorCharAt(cursor.getRegionCoord().x).setBackground(
                    COLOR_SELECTION_BACKGROUND);
        }
        contents.addAll(colorStrings);

        window.addSeparator(new Line(false, 1, 2, 1));
        contents.add(new ColorString(region.toString()));
        if (region.isClaimed())
        {
            contents.add(new ColorString("Ruler: ").add(region.getFaction()));
        }

        if (isLooking() && !cursor.equals(player.getLocation()))
        {
            return;
        }

        if (region.hasOre())
        {
            contents.add(new ColorString("Ore: ").add(
                    new ColorString(region.getOre().toString() + " (" + region.getOre().getDensity() + ")",
                            COLOR_FIELD)));
        }

        for (Ship ship : region.getShips())
        {
            if (ship != player)
            {
                contents.add(ship.toColorString());
            }
        }
    }
}