package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.map.Station;
import static boldorf.eversector.storage.Paths.CLAIM;
import static boldorf.eversector.Main.galaxy;

/**
 * 
 */
public class ClaimStationScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public ClaimStationScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        Station claiming = player.getSectorLocation().getStation();
        window.getContents().add(new ColorString("Claim ").add(claiming)
                .add(" for ")
                .add(new ColorString(Integer.toString(Station.CLAIM_COST),
                        COLOR_FIELD)).add(" credits?"));
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
        player.dock();
        playSoundEffect(CLAIM);
        galaxy.nextTurn();
        return new StationScreen(getDisplay());
    }
}