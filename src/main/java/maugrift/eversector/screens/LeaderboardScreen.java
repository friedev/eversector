package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;

import java.awt.event.KeyEvent;
import java.util.List;

/**
 * A screen used for temporarily displaying the leaderboard.
 *
 * @author Maugrift
 */
public class LeaderboardScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new LeaderboardScreen.
     *
     * @param leaderboard the leaderboard
     * @see LeaderboardScore#buildLeaderboard()
     */
    public LeaderboardScreen(List<ColorString> leaderboard)
    {
        super(Main.display);
        getConfirmCodes().add(KeyEvent.VK_B);
        window = new PopupWindow(Main.display, leaderboard);
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