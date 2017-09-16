package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Option;
import boldorf.eversector.Tileset;
import boldorf.util.FileManager;
import boldorf.util.Utility;

import java.awt.event.KeyEvent;
import java.util.List;

import static boldorf.eversector.Main.*;

/**
 *
 */
public class OptionsScreen extends MenuScreen<PopupMenu>
{
    public OptionsScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        updateWindow();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (key.getKeyCode() == KeyEvent.VK_LEFT || key.getKeyCode() == KeyEvent.VK_RIGHT)
        {
            Option option = getSelectedOption();
            Integer value = option.toInt();
            if (value != null)
            {
                int lowerBound;
                int upperBound;

                if (option == Option.FONT)
                {
                    lowerBound = 0;
                    upperBound = Tileset.values().length - 1;
                }
                else if (option == Option.WIDTH)
                {
                    lowerBound = Utility.parseInt(option.getDefault());
                    upperBound = Integer.MAX_VALUE;
                }
                else if (option == Option.HEIGHT)
                {
                    lowerBound = Utility.parseInt(option.getDefault());
                    upperBound = Integer.MAX_VALUE;
                }
                else
                {
                    lowerBound = 0;
                    upperBound = FileManager.MAX_VOLUME;
                }

                if (key.getKeyCode() == KeyEvent.VK_LEFT) { value = Math.max(lowerBound, value - 1); }
                else { value = Math.min(upperBound, value + 1); }

                option.setProperty(value.toString());
                updateWindow();

                if (soundtrack != null && Option.MUSIC.equals(option)) { FileManager.setVolume(soundtrack, value); }

                return this;
            }
        }

        return super.processInput(key);
    }

    @Override
    public Screen onConfirm()
    {
        Option option = getSelectedOption();
        String property = option.getProperty();

        if (option.isBoolean())
        {
            option.toggle();
            updateWindow();
        }
        return this;
    }

    private Option getSelectedOption()
    {return Option.getOption(getMenu().getSelection().toString().split(":")[0]);}

    private void updateWindow()
    {
        List<ColorString> contents = getMenu().getWindow().getContents();
        contents.clear();
        getMenu().getRestrictions().clear();

        int currentRestriction = 0;
        for (int i = 0; i < Option.values().length; i++)
        {
            Option option = Option.values()[i];

            if (!option.isVisible()) { continue; }

            String property = option.getProperty();
            if (option == Option.FONT)
            {
                Tileset tileset = Tileset.values()[Utility.parseInt(property)];
                contents.add(new ColorString(option.getKey() + ": ").add(
                        new ColorString(tileset.getName() + " (" + tileset.getSize() + "x" + tileset.getSize() + ")",
                                COLOR_FIELD)));
            }
            else
            {
                contents.add(option.toColorString());
            }
            getMenu().getRestrictions().add(currentRestriction);
            currentRestriction++;
        }

        contents.add(new ColorString("Display and font changes require a " + "restart to take effect."));
    }
}