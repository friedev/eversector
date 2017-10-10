package boldorf.eversector.screens;

import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

import java.util.List;

import static boldorf.eversector.Main.COLOR_FIELD;

/**
 * Shows a context-sensitive list of keybindings.
 *
 * @author Maugrift
 */
public class HelpScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new HelpScreen.
     *
     * @param keybindings the keybindings to display, with null acting as a separator, generally between screens
     */
    public HelpScreen(List<Keybinding> keybindings)
    {
        super(Main.display);
        window = new PopupWindow(Main.display, new Border(1), new Line(true, 1, 1));

        for (Keybinding keybinding : keybindings)
        {
            if (keybinding == null)
            {
                window.addSeparator();
            }
            else
            {
                window.getContents().add(keybinding.toColorString(null, COLOR_FIELD));
            }
        }
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
}