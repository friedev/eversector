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
import boldorf.eversector.storage.Tileset;
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
                
                switch (option)
                {
                    case Options.FONT:
                        lowerBound = 0;
                        upperBound = Tileset.values().length - 1;
                        break;
                    case Options.WIDTH:
                        lowerBound = Options.DEFAULT_WIDTH;
                        upperBound = Integer.MAX_VALUE;
                        break;
                    case Options.HEIGHT:
                        lowerBound = Options.DEFAULT_HEIGHT;
                        upperBound = Integer.MAX_VALUE;
                        break;
                    default: // MUSIC or SFX
                        lowerBound = 0;
                        upperBound = FileManager.MAX_VOLUME;
                        break;
                }
                
                if (key.getKeyCode() == KeyEvent.VK_LEFT)
                    value = Math.max(lowerBound, value - 1);
                else
                    value = Math.min(upperBound, value + 1);
                
                options.setProperty(option, value.toString());
                updateWindow();
                
                if (soundtrack != null && Options.MUSIC.equals(option))
                    FileManager.setVolume(soundtrack, value);
                
                return this;
            }
        }
        
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
            if (Options.FONT.equals(key))
            {
                Tileset tileset = Tileset.values()[Utility.parseInt(property)];
                contents.add(new ColorString(key + ": ")
                        .add(new ColorString(tileset.getName()
                                + " (" + tileset.getSize() + "x"
                                + tileset.getSize() + ")",
                                COLOR_FIELD)));
            }
            else
            {
                contents.add(new ColorString(key + ": ")
                        .add(new ColorString(property, COLOR_FIELD)));
            }
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
        
        contents.add(new ColorString("Display and font changes require a "
                + "restart to take effect."));
    }
}