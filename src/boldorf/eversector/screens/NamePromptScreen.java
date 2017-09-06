package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.PopupTerminal;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.options;
import boldorf.eversector.storage.Options;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class NamePromptScreen extends PopupTerminal
{
    private String option;
    
    public NamePromptScreen(Display display, String naming, String option)
    {
        super(new PopupWindow(display, toContentList(naming)),
                new ColorString(), display.getWidth() - 2, COLOR_FIELD);
        this.option = option;
    }
    
    private static List<ColorString> toContentList(String naming)
    {
        List<ColorString> contents = new ArrayList<>(1);
        contents.add(new ColorString("Enter the name of " + naming
                + ". ").add(new ColorString("(Enter to skip.)",
                        AsciiPanel.brightBlack)));
        return contents;
    }
    
    @Override
    public Screen onConfirm()
    {
        String input = getInput().trim();
        if (input == null || input.isEmpty())
            input = Options.DEFAULT_NAME;
        options.setProperty(option, input);
        return null;
    }
}