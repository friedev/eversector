package boldorf.eversector.screens;

import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.Display;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import java.util.List;

/**
 * 
 */
public class HelpScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public HelpScreen(Display display, List<Keybinding> keybindings)
    {
        super(display);
        window = new PopupWindow(display, new Border(1), new Line(true, 1, 1));
        
        for (Keybinding keybinding: keybindings)
        {
            if (keybinding == null)
            {
                window.addSeparator();
            }
            else
            {
                window.getContents().add(keybinding.toColorString(null,
                        COLOR_FIELD));
            }
        }
    }

    @Override
    public void displayOutput()
        {window.display();}

    @Override
    public PopupWindow getWindow()
        {return window;}
}