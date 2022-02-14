package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.ships.Ship;

import static maugrift.eversector.Main.player;

/**
 * The screen shown when the player can take the place of a former leader.
 * <b>Currently unused.</b>
 *
 * @author Maugrift
 */
public class SuccessionScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * The leader that the player is given the choice to succeed.
	 */
	private Ship leader;

	/**
	 * Instantiates a new SuccessionScreen.
	 *
	 * @param leader the leader that the player is given the choice to succeed
	 */
	public SuccessionScreen(Ship leader)
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(leader.toColorString()
				.add(" offers you their status as leader if you spare them."));
		window.getContents().add(new ColorString("Accept the offer?"));
		this.leader = leader;
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

	@Override
	public Screen onConfirm()
	{
		player.getFaction().addNews(
				player
				+ " has defeated our leader, "
				+ leader
				+ ", and has wrested control of the faction.");
		player.getFaction().setLeader(player);
		return new SectorScreen();
	}
}
