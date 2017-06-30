package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.util.FileManager;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.disqualified;
import static boldorf.eversector.Main.player;
import boldorf.eversector.storage.Options;
import boldorf.eversector.storage.Paths;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;

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
        LinkedList<ColorString> contents = new LinkedList<>();
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
    {
        Properties save = player.toProperties();
        save.setProperty(Options.DISQUALIFIED, Boolean.toString(disqualified));
        try
        {
            FileManager.save(save, Paths.SAVE);
            FileManager.save(Main.options, Paths.OPTIONS);
        }
        catch (IOException io) {}
        return new EndScreen(getDisplay(), false);
    }
    
    @Override
    public Screen onDeny()
    {
        try
        {
            FileManager.save(Main.options, Paths.OPTIONS);
        }
        catch (IOException io) {}
        
        FileManager.delete(Paths.SAVE);
        return new EndScreen(getDisplay(), true);
    }
}