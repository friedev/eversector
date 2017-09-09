package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.storage.Actions;
import static boldorf.eversector.storage.Paths.START;

/**
 * 
 */
public class IntergalacticScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public IntergalacticScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(new ColorString("Travel to another galaxy?"));
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
        player.changeResourceBy(Actions.BURN);
        playSoundEffect(START);
        Main.changeGalaxy();
        return null;
    }
}