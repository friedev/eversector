package maugrift.eversector.screens;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.PopupTerminal;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.Option;

import java.util.ArrayList;
import java.util.List;

import static maugrift.eversector.Main.COLOR_FIELD;

/**
 * The screen for entering names.
 *
 * @author Aaron Friesen
 */
public class NamePromptScreen extends PopupTerminal
{
	/**
	 * The subject of the naming process.
	 */
	private String naming;

	/**
	 * The option that will be assigned this name.
	 */
	private Option option;

	/**
	 * Instantiates a new NamePromptScreen.
	 *
	 * @param naming the subject of the naming process
	 * @param option the option that will be assigned this name
	 */
	public NamePromptScreen(String naming, Option option)
	{
		super(
			new PopupWindow(
				Main.display,
				toContentList(naming)
			),
			new ColorString(),
			Main.display.getWidthInCharacters() - 2,
			COLOR_FIELD
		);
		this.naming = naming;
		this.option = option;
	}

	/**
	 * Creates the list of contents for use in the constructor.
	 *
	 * @param naming the subject of the naming process
	 * @return a list of window contents
	 * @see #NamePromptScreen(String, Option)
	 */
	private static List<ColorString> toContentList(String naming)
	{
		List<ColorString> contents = new ArrayList<>(1);
		contents.add(buildPrompt(naming));
		return contents;
	}

	/**
	 * Creates the prompt before the screen has been initialized.
	 *
	 * @param naming the subject of the naming process
	 * @return the prompt
	 */
	private static ColorString buildPrompt(String naming)
	{
		return new ColorString("Enter the name of " + naming + ". ").add(
				new ColorString("(Enter to skip.)", AsciiPanel.brightBlack));
	}

	/**
	 * Builds the prompt once the screen has been constructed.
	 *
	 * @return the prompt
	 */
	private ColorString buildPrompt()
	{
		return new ColorString("Enter the name of " + naming + ". ")
			.add(
				new ColorString(
					getInput().isEmpty()
					? "(Enter to skip.)"
					: "(Enter to confirm.)",
					AsciiPanel.brightBlack
				)
			);
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
