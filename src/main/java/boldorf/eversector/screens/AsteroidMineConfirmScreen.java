package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Paths.*;

/**
 * The prompt presented when the player is attempting to mine a potentially destructive asteroid.
 */
public class AsteroidMineConfirmScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new AsteroidMineConfirmScreen.
     */
    public AsteroidMineConfirmScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Your hull is dangerously low; attempt to mine the asteroid?"));
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
        if (Main.player.mine())
        {
            playSoundEffect(MINE);
            Main.galaxy.nextTurn();
        }
        return null;
    }

    @Override
    public Screen onCancel()
    {
        return null;
    }
}
