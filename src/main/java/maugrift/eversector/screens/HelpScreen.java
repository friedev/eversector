package maugrift.eversector.screens;

import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Keybinding;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;

import java.util.List;

import static maugrift.eversector.Main.COLOR_FIELD;

/**
 * Shows a context-sensitive list of keybindings.
 *
 * @author Maugrift
 */
public class HelpScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new HelpScreen.
	 *
	 * @param keybindings the keybindings to display, with null acting as a
	 *                    separator, generally between screens
	 */
	public HelpScreen(List<Keybinding> keybindings)
	{
		super(Main.display);
		window = new PopupWindow(
				Main.display,
				new Border(1),
				new Line(true, 1, 1)
		);

		for (Keybinding keybinding : keybindings)
		{
			if (keybinding == null)
			{
				window.addSeparator();
			}
			else
			{
				window.getContents().add(keybinding.toColorString(null, COLOR_FIELD));
			}
		}
	}

	@Override
	public void displayOutput()
	{
		window.display();
	}

	@Override
	public PopupWindow getWindow()
	{
		return window;
	}
}
