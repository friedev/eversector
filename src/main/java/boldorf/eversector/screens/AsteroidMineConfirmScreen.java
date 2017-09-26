package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Mine;

import java.awt.event.KeyEvent;

import static boldorf.eversector.Main.addError;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Paths.DEATH;

/**
 * The prompt presented when the player is attempting to mine a potentially destructive asteroid.
 *
 * @author Dale Campbell
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
        getConfirmCodes().remove((Integer) KeyEvent.VK_ENTER);
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
        String mineExecution = new Mine().execute(Main.player);
        if (mineExecution != null)
        {
            addError(mineExecution);
            return null;
        }

        if (Main.player.isDestroyed())
        {
            playSoundEffect(DEATH);
            return new EndScreen(new ColorString("You collide with the asteroid, which breaches your hull!"), true,
                    false);
        }
        
        Main.galaxy.nextTurn();
        return null;
    }

    @Override
    public Screen onCancel()
    {
        return null;
    }
}
