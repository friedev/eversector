package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Dock;
import maugrift.eversector.map.Station;

import static maugrift.eversector.Main.*;

/**
 * The prompt to claim a station when it is under the control of a hostile faction.
 *
 * @author Maugrift
 */
public class ClaimStationScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new ClaimStationScreen.
     */
    public ClaimStationScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        Station claiming = player.getSectorLocation().getStation();
        window.getContents().add(new ColorString("Claim ").add(claiming)
                                                          .add(" for ")
                                                          .add(new ColorString(Integer.toString(Station.CLAIM_COST),
                                                                  COLOR_FIELD))
                                                          .add(" credits?"));
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
        new Dock().execute(player);
        galaxy.nextTurn();
        return new StationScreen();
    }
}