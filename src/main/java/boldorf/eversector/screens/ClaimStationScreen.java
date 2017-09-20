package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.map.Station;

import static boldorf.eversector.Main.*;
import static boldorf.eversector.Paths.CLAIM;

/**
 * The prompt to claim a station when it is under the control of a hostile faction.
 *
 * @author Boldorf Smokebane
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
        player.dock();
        playSoundEffect(CLAIM);
        galaxy.nextTurn();
        return new StationScreen();
    }
}