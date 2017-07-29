package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.options;
import static boldorf.eversector.Main.soundtrack;
import boldorf.eversector.storage.Options;
import boldorf.util.FileManager;
import boldorf.util.Utility;
import java.awt.event.KeyEvent;
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
    public Screen processInput(KeyEvent key)
    {
        if (key.getKeyCode() == KeyEvent.VK_LEFT ||
                key.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            String option = getSelectedOption();
            Integer value = Utility.parseInt(options.getProperty(option));
            if (value != null)
            {
                int lowerBound;
                int upperBound;
                
                if (Options.WIDTH.equals(option))
                {
                    lowerBound = Options.DEFAULT_WIDTH;
                    upperBound = Integer.MAX_VALUE;
                }
                else if (Options.HEIGHT.equals(option))
                {
                    lowerBound = Options.DEFAULT_HEIGHT;
                    upperBound = Integer.MAX_VALUE;
                }
                else
                {
                    lowerBound = 0;
                    upperBound = FileManager.MAX_VOLUME;
                }
                
                if (key.getKeyCode() == KeyEvent.VK_LEFT)
                    value = Math.max(lowerBound, value - 1);
                else
                    value = Math.min(upperBound, value + 1);
                
                options.setProperty(option, value.toString());
                updateWindow();
                
                if (Options.MUSIC.equals(option))
                    FileManager.setVolume(soundtrack, value);
                
                return this;
            }
        }
        
        /*
        if (Options.MUSIC.equals(key))
        {
            if (Options.toBoolean(options.getProperty(Options.MUSIC)))
            {
                soundtrack.start();
                soundtrack.loop(Clip.LOOP_CONTINUOUSLY);
            }
            else
            {
                soundtrack.stop();
            }
        }
        */
        
        return super.processInput(key);
    }
    
    @Override
    public Screen onConfirm()
    {
        String key = getSelectedOption();
        String property = options.getProperty(key);
        
        if (Utility.parseInt(property) != null)
            return this;
        
        options.setProperty(key, Options.getOpposite(property));
        updateWindow();
        return this;
    }
    
    private String getSelectedOption()
        {return getMenu().getSelection().toString().split(":")[0];}
    
    private void updateWindow()
    {
        List<ColorString> contents = getMenu().getWindow().getContents();
        contents.clear();
        getMenu().getRestrictions().clear();
        
        for (int i = 0; i < Options.MENU_NUMBERS.length; i++)
        {
            String key = Options.MENU_NUMBERS[i];
            String property = options.getProperty(key);
            contents.add(new ColorString(key + ": ")
                    .add(new ColorString(property, COLOR_FIELD)));
            getMenu().getRestrictions().add(i);
        }
        
        for (int i = 0; i < Options.MENU_BOOLEANS.length; i++)
        {
            String key = Options.MENU_BOOLEANS[i];
            String property = options.getProperty(key);
            contents.add(new ColorString(key + ": ")
                    .add(new ColorString(property,
                            Options.getColor(property))));
            getMenu().getRestrictions().add(Options.MENU_NUMBERS.length + i);
        }
        
        contents.add(new ColorString("Display dimension changes require a "
                + "restart to take effect."));
    }
}