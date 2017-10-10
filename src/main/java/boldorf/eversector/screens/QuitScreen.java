package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

import java.util.LinkedList;
import java.util.List;

/**
 * The screen presented to the player when they attempt to quit.
 *
 * @author Maugrift
 */
public class QuitScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new QuitScreen.
     */
    public QuitScreen()
    {
        super(Main.display);
        List<ColorString> contents = new LinkedList<>();
        contents.add(new ColorString("Save before quitting?", AsciiPanel.brightRed));
        window = new PopupWindow(Main.display, contents);
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
        return new EndScreen(false, true);
    }

    @Override
    public Screen onDeny()
    {
        return new EndScreen(true, false);
    }
}