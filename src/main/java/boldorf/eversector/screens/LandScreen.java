package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.Main.playSoundEffect;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Region;
import boldorf.eversector.locations.PlanetLocation;
import static boldorf.eversector.storage.Paths.ENGINE;
import boldorf.util.Utility;
import java.awt.event.KeyEvent;
import java.util.List;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class LandScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private PlanetLocation selection;
    
    public LandScreen(Display display)
    {
        super(display);
        Planet planet = player.getSectorLocation().getPlanet();
        window = new PopupWindow(display, new Border(1), new Line(true, 1, 1));
        window.getContents().addAll(planet.toColorStrings(Main.showFactions));
        selection = new PlanetLocation(player.getSectorLocation(),
                Coord.get(0, 0));
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
        
        return super.processInput(key);
    }
    
    @Override
    public PopupWindow getWindow()
        {return window;}
    
    @Override
    public Screen onConfirm()
    {
        if (player.land(selection.getRegionCoord()))
        {
            playSoundEffect(ENGINE);
            player.getLocation().getGalaxy().nextTurn();
            return new PlanetScreen(getDisplay());
        }
        
        return null;
    }
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        Planet planet = player.getSectorLocation().getPlanet();
        List<ColorString> colorStrings =
                planet.toColorStrings(Main.showFactions);
        
        Coord regionCoord = selection.getRegionCoord();
        colorStrings.get(regionCoord.y).getColorCharAt(regionCoord.x)
                .setBackground(COLOR_SELECTION_BACKGROUND);
        contents.addAll(colorStrings);
        
        window.addSeparator();
        Region region = selection.getRegion();
        contents.add(new ColorString(region.toString()));
        if (region.isClaimed())
            contents.add(new ColorString("Ruler: ").add(region.getFaction()));
    }
}