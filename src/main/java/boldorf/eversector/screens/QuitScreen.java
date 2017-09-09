package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class QuitScreen extends ConfirmationScreen implements
        WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public QuitScreen(Display display)
    {
        super(display);
        List<ColorString> contents = new LinkedList<>();
        contents.add(new ColorString("Save before quitting?",
                AsciiPanel.brightRed));
        window = new PopupWindow(display, contents);
    }

    @Override
    public void displayOutput()
        {window.display();}
    
    @Override
    public PopupWindow getWindow()
        {return window;}
    
    @Override
    public Screen onConfirm()
        {return new EndScreen(getDisplay(), false, true);}
    
    @Override
    public Screen onDeny()
        {return new EndScreen(getDisplay(), true, false);}
}