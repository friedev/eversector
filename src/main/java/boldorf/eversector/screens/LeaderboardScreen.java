package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * A popup Screen used for temporarily displaying ore values.
 */
public class LeaderboardScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    private PopupWindow window;

    public LeaderboardScreen(Display display, List<ColorString> leaderboard)
    {
        super(display);
        getConfirmCodes().add(KeyEvent.VK_B);
        window = new PopupWindow(display, leaderboard);
    }

    @Override
    public void displayOutput()
    {window.display();}

    @Override
    public PopupWindow getWindow()
    {return window;}
}