package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;

import static boldorf.eversector.Main.player;

/**
 *
 */
public class LeaveScreen extends ConfirmationScreen
{
    private PopupWindow window;
    private boolean redirect;

    public LeaveScreen(Display display, boolean redirect)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(new ColorString("Really leave the ").add(player.getFaction()).add("?"));
        this.redirect = redirect;
    }

    @Override
    public void displayOutput()
    {window.display();}

    @Override
    public Screen onConfirm()
    {
        player.leaveFaction();
        return redirect ? new JoinScreen(getDisplay()) : null;
    }
}