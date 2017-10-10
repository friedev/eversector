package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Distress;
import boldorf.eversector.faction.Faction;

import static boldorf.eversector.Main.player;

/**
 * The prompt displayed when another faction offers to aid the player in distress.
 *
 * @author Maugrift
 */
public class DistressConvertScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * The faction offering help.
     */
    private Faction converting;

    /**
     * Instantiates a new DistressConvertScreen.
     *
     * @param converting the faction offering help
     */
    public DistressConvertScreen(Faction converting)
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("The ").add(converting).add(" offers to aid you if you join them."));
        window.getContents().add(new ColorString("Accept the offer?"));
        this.converting = converting;
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
        new Distress(converting).execute(player);
        return null;
    }
}