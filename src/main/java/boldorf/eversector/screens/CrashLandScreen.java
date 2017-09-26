package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.CrashLand;

import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.Paths.DEATH;
import static boldorf.eversector.Paths.TORPEDO;

/**
 * The prompt presented when the player has insufficient fuel to land.
 *
 * @author Boldorf Smokebane
 */
public class CrashLandScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new CrashLandScreen.
     */
    public CrashLandScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Insufficient fuel to land; crash land?"));
    }

    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public PopupWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen onConfirm()
    {
        if (new CrashLand().executeBool(player))
        {
            if (player.isDestroyed())
            {
                playSoundEffect(DEATH);
                return new EndScreen(new ColorString(
                        "You crash into the surface of " + player.getSectorLocation().getPlanet() +
                        ", obliterating your ship."), true, false);
            }

            playSoundEffect(TORPEDO);
            player.getLocation().getGalaxy().nextTurn();
            return new PlanetScreen();
        }
        return null;
    }

    @Override
    public Screen onCancel()
    {
        return null;
    }
}