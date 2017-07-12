package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.storage.Actions;

/**
 * 
 */
public class PursueScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public PursueScreen(Display display, Ship pursuing)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(new ColorString("Pursue ").add(pursuing)
                .add("?"));
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
        player.changeResourceBy(Actions.PURSUE);
        return null;
    }
    
    @Override
    public Screen onCancel()
        {return new SectorScreen(getDisplay());}
}