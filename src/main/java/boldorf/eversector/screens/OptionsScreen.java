package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.Option;
import boldorf.util.FileManager;
import boldorf.util.Utility;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static boldorf.eversector.Main.*;

/**
 * The screen for viewing and changing game options.
 *
 * @author Boldorf Smokebane
 */
public class OptionsScreen extends MenuScreen<PopupMenu>
{
    /**
     * Instantiates a new OptionsScreen.
     */
    public OptionsScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
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

                switch (option)
                {
                    case FONT:
                        lowerBound = 0;
                        upperBound = Main.fonts.length - 1;
                        break;
                    case WIDTH:
                        lowerBound = Utility.parseInt(option.getDefault());
                        upperBound = Integer.MAX_VALUE;
                        break;
                    case HEIGHT:
                        lowerBound = Utility.parseInt(option.getDefault());
                        upperBound = Integer.MAX_VALUE;
                        break;
                    default:
                        lowerBound = 0;
                        upperBound = FileManager.MAX_VOLUME;
                        break;
                }

                if (key.getKeyCode() == KeyEvent.VK_LEFT)
                {
                    value = Math.max(lowerBound, value - 1);
                }
                else
                {
                    value = Math.min(upperBound, value + 1);
                }

                option.setProperty(value.toString());
                updateWindow();

                if (soundtrack != null && Option.MUSIC.equals(option))
                {
                    FileManager.setVolume(soundtrack, value);
                }

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

    /**
     * Returns the currently selected option.
     *
     * @return the currently selected option
     */
    private Option getSelectedOption()
    {
        return Option.getOption(getMenu().getSelection().toString().split(":")[0]);
    }

    /**
     * Updates the window.
     */
    private void updateWindow()
    {
        List<ColorString> contents = getMenu().getWindow().getContents();
        contents.clear();
        getMenu().getRestrictions().clear();

        int currentRestriction = 0;
        for (int i = 0; i < Option.values().length; i++)
        {
            Option option = Option.values()[i];

            if (!option.isVisible())
            {
                continue;
            }

            String property = option.getProperty();
            if (option == Option.FONT)
            {
                try
                {
                    Properties fontProperties = Main.getFontProperties(option.toInt());
                    contents.add(new ColorString(option.getKey() + ": ").add(new ColorString(
                            fontProperties.getProperty(Option.FONT_NAME) + " (" +
                            fontProperties.getProperty(Option.FONT_WIDTH) + "x" +
                            fontProperties.getProperty(Option.FONT_HEIGHT) + ")", COLOR_FIELD)));
                }
                catch (IOException e)
                {
                    contents.add(new ColorString(option.getKey() + ": " + Main.fonts[option.toInt()].getName()));
                }
            }
            else
            {
                contents.add(option.toColorString());
            }
            getMenu().getRestrictions().add(currentRestriction);
            currentRestriction++;
        }

        contents.add(new ColorString("Display and font changes require a restart to take effect."));
    }
}