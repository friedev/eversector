package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.PopupTerminal;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Option;

import java.util.ArrayList;
import java.util.List;

import static boldorf.eversector.Main.COLOR_FIELD;

/**
 *
 */
public class NamePromptScreen extends PopupTerminal
{
    private String naming;
    private Option option;

    public NamePromptScreen(Display display, String naming, Option option)
    {
        super(new PopupWindow(display, toContentList(naming)), new ColorString(), display.getWidth() - 2, COLOR_FIELD);
        this.naming = naming;
        this.option = option;
    }

    private static List<ColorString> toContentList(String naming)
    {
        List<ColorString> contents = new ArrayList<>(1);
        contents.add(buildPrompt(naming));
        return contents;
    }

    private static ColorString buildPrompt(String naming)
    {
        return new ColorString("Enter the name of " + naming + ". ").add(
                new ColorString("(Enter to skip.)", AsciiPanel.brightBlack));
    }

    private ColorString buildPrompt()
    {
        return new ColorString("Enter the name of " + naming + ". ").add(
                new ColorString(getInput().isEmpty() ? "(Enter to skip.)" : "(Enter to confirm.)",
                        AsciiPanel.brightBlack));
    }

    @Override
    public void displayOutput()
    {
        getWindow().getContents().set(0, buildPrompt());
        super.displayOutput();
    }

    @Override
    public Screen onConfirm()
    {
        String input = getInput().trim();
        option.setProperty(input.isEmpty() ? option.getDefault() : input);
        return null;
    }
}