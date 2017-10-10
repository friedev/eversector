package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Land;
import boldorf.eversector.locations.PlanetLocation;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Region;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.List;

import static boldorf.eversector.Main.*;

/**
 * The screen for selecting a landing destination.
 *
 * @author Maugrift
 */
public class LandScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * The currently selected region.
     */
    private PlanetLocation selection;

    /**
     * Instantiates a new LandScreen.
     */
    public LandScreen()
    {
        super(Main.display);
        Planet planet = player.getSectorLocation().getPlanet();
        window = new PopupWindow(Main.display, new Border(1), new Line(true, 1, 1));
        window.getContents().addAll(planet.toColorStrings(Main.showFactions));
        selection = new PlanetLocation(player.getSectorLocation(), Coord.get(0, 0));
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
        Direction direction = Utility.keyToDirectionRestricted(key);
        if (direction != null)
        {
            selection = selection.moveRegion(direction);
            return this;
        }

        if (key.getKeyCode() == KeyEvent.VK_V)
        {
            Main.showFactions = !Main.showFactions;
        }

        return super.processInput(key);
    }

    @Override
    public PopupWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen onConfirm()
    {
        String landExecution = new Land(selection.getRegionCoord()).execute(player);
        if (landExecution == null)
        {
            player.getLocation().getGalaxy().nextTurn();
            return new PlanetScreen();
        }

        addError(landExecution);
        return null;
    }

    /**
     * Sets up the window and its contents.
     */
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        Planet planet = player.getSectorLocation().getPlanet();
        List<ColorString> colorStrings = planet.toColorStrings(Main.showFactions);

        Coord regionCoord = selection.getRegionCoord();
        colorStrings.get(regionCoord.y).getColorCharAt(regionCoord.x).setBackground(COLOR_SELECTION_BACKGROUND);
        contents.addAll(colorStrings);

        window.addSeparator();
        Region region = selection.getRegion();
        contents.add(new ColorString(region.toString()));
        if (region.isClaimed())
        {
            contents.add(new ColorString("Ruler: ").add(region.getFaction()));
        }
    }
}