package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Burn;

import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.Paths.START;

/**
 * The prompt to travel to a new galaxy.
 *
 * @author Maugrift
 */
public class IntergalacticScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new IntergalacticScreen.
     */
    public IntergalacticScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Travel to another galaxy?"));
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
        player.getResource(Burn.RESOURCE).changeAmount(-Burn.COST);
        playSoundEffect(START);
        Main.changeGalaxy();
        return null;
    }
}