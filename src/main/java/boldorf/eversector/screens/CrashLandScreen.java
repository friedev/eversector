package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.storage.Paths.DEATH;
import static boldorf.eversector.storage.Paths.TORPEDO;

/**
 * 
 */
public class CrashLandScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public CrashLandScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(
                new ColorString("Insufficient fuel to land; crash land?"));
    }

    @Override
    public void displayOutput()
        {window.display();}

    @Override
    public PopupWindow getWindow()
        {return window;}
    
    @Override
    public Screen onConfirm()
    {
        if (player.crashLand())
        {
            if (player.isDestroyed())
            {
                playSoundEffect(DEATH);
                return new EndScreen(getDisplay(), new ColorString(
                        "You crash into the surface of "
                                + player.getSectorLocation().getPlanet()
                                + ", obliterating your ship."), true, false);
            }
            
            playSoundEffect(TORPEDO);
            player.getLocation().getGalaxy().nextTurn();
            return new PlanetScreen(getDisplay());
        }
        return null;
    }
    
    @Override
    public Screen onCancel()
        {return null;}
}