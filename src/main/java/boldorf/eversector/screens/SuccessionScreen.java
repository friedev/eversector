package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.player;

/**
 *
 */
public class SuccessionScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private Ship leader;

    public SuccessionScreen(Display display, Ship leader)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(leader.toColorString().add(" offers you their status as leader if you spare them."));
        window.getContents().add(new ColorString("Accept the offer?"));
        this.leader = leader;
    }

    @Override
    public void displayOutput()
    {window.display();}

    @Override
    public PopupWindow getWindow()
    {return window;}

    @Override
    public Screen onConfirm()
    {
        player.getFaction().addNews(
                player + " has defeated our leader, " + leader + ", and has wrested control of the faction.");
        player.getFaction().setLeader(player);
        return new SectorScreen(getDisplay());
    }
}