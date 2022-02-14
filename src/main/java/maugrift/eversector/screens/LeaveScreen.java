package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;

import static maugrift.eversector.Main.player;

/**
 * The prompt for leaving a faction.
 *
 * @author Aaron Friesen
 */
public class LeaveScreen extends ConfirmationScreen
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * If true, will redirect to a new JoinScreen after confirming.
	 */
	private boolean redirect;

	/**
	 * Instantiates a new LeaveScreen.
	 *
	 * @param redirect if true, will redirect to a new JoinScreen after
	 *                 confirming
	 */
	public LeaveScreen(boolean redirect)
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(new ColorString("Really leave the ")
				.add(player.getFaction())
				.add("?"));
		this.redirect = redirect;
	}

	@Override
	public void displayOutput()
	{
		window.display();
	}

	@Override
	public Screen onConfirm()
	{
		player.leaveFaction();
		return redirect ? new JoinScreen() : null;
	}
}
