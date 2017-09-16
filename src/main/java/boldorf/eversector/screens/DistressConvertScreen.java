package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.faction.Faction;

import static boldorf.eversector.Main.player;

/**
 *
 */
public class DistressConvertScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private Faction converting;

    public DistressConvertScreen(Display display, Faction converting)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(new ColorString("The ").add(converting).add(" offers to aid you if you join them."));
        window.getContents().add(new ColorString("Accept the offer?"));
        this.converting = converting;
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
        player.distress(converting);
        return null;
    }
}