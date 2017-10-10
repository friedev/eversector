package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

import static boldorf.eversector.Main.player;

/**
 * The prompt for leaving a faction.
 *
 * @author Maugrift
 */
public class LeaveScreen extends ConfirmationScreen
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * If true, will redirect to a new JoinScreen after confirming.
     */
    private boolean redirect;

    /**
     * Instantiates a new LeaveScreen.
     *
     * @param redirect if true, will redirect to a new JoinScreen after confirming
     */
    public LeaveScreen(boolean redirect)
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Really leave the ").add(player.getFaction()).add("?"));
        this.redirect = redirect;
    }

    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public Screen onConfirm()
    {
        player.leaveFaction();
        return redirect ? new JoinScreen() : null;
    }
}