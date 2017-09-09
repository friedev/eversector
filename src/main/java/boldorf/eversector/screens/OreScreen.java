package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.map.Ore;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

/** A popup Screen used for temporarily displaying ore values. */
public class OreScreen extends ConfirmationScreen
{
    private PopupWindow window;
    
    public OreScreen(Display display)
    {
        super(display);
        List<ColorString> list = new LinkedList<>();
        for (Ore ore: Main.galaxy.getOreTypes())
        {
            list.add(new ColorString(ore.toString() + ": "
                    + ore.getDensity() + " Density"));
        }
        window = new PopupWindow(getDisplay(), list);
    }
    
    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (key.getKeyCode() == KeyEvent.VK_G)
            return null;
        return super.processInput(key);
    }
    
    @Override
    public Screen onConfirm()
        {return onCancel();}
}