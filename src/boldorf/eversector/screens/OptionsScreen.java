package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.options;
import static boldorf.eversector.Main.soundtrack;
import boldorf.eversector.storage.Options;
import java.util.List;

/**
 * 
 */
public class OptionsScreen extends MenuScreen<PopupMenu>
{
    public OptionsScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        updateWindow();
    }
    
    @Override
    public Screen onConfirm()
    {
        String key = getMenu().getSelection().toString().split(":")[0];
        String property = options.getProperty(key);
        
        options.setProperty(key, Options.getOpposite(property));
        updateWindow();
        
        if (Options.MUSIC.equals(key))
        {
            if (Options.toBoolean(options.getProperty(Options.MUSIC)))
                soundtrack.start();
            else
                soundtrack.stop();
        }
            
        
        return this;
    }
    
    private void updateWindow()
    {
        List<ColorString> contents = getMenu().getWindow().getContents();
        contents.clear();
        
        for (int i = 0; i < Options.MENU_BOOLEANS.length; i++)
        {
            String key = Options.MENU_BOOLEANS[i];
            String property = options.getProperty(key);
            contents.add(new ColorString(key + ": ")
                    .add(new ColorString(property,
                            Options.getColor(property))));
        }
    }
}