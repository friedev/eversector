package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.Main.playSoundEffect;
import boldorf.eversector.entities.Planet;
import boldorf.eversector.entities.locations.PlanetLocation;
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
        window = new PopupWindow(display);
        window.getContents().addAll(planet.toColorStrings());
        selection = new PlanetLocation(player.getSectorLocation(),
                planet.getCenter());
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
        if (player.land(selection.getRegionCoords()))
        {
            playSoundEffect(ENGINE);
            player.getLocation().getMap().nextTurn();
            return new PlanetScreen(getDisplay());
        }
        
        return player.canLand() ? new CrashLandScreen(getDisplay()) : null;
    }
    
    private void setUpWindow()
    {
        window.getContents().clear();
        List<ColorString> colorStrings =
                player.getSectorLocation().getPlanet().toColorStrings();
        
        Coord regionCoords = selection.getRegionCoords();
        colorStrings.get(regionCoords.y).getColorCharAt(regionCoords.x)
                .setBackground(COLOR_SELECTION_BACKGROUND);
        window.getContents().addAll(colorStrings);
    }
}