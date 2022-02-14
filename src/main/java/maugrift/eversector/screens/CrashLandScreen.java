package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.CrashLand;

import static maugrift.eversector.Main.playSoundEffect;
import static maugrift.eversector.Main.player;
import static maugrift.eversector.Paths.DEATH;
import static maugrift.eversector.Paths.TORPEDO;

/**
 * The prompt presented when the player has insufficient fuel to land.
 *
 * @author Aaron Friesen
 */
public class CrashLandScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new CrashLandScreen.
	 */
	public CrashLandScreen()
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(
				new ColorString("Insufficient fuel to land; crash land?")
		);
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
		if (new CrashLand().executeBool(player))
		{
			if (player.isDestroyed())
			{
				playSoundEffect(DEATH);
				return new EndScreen(
						new ColorString(
							"You crash into the surface of "
							+ player.getSectorLocation().getPlanet()
							+ ", obliterating your ship."
						),
						true,
						false
				);
			}

			playSoundEffect(TORPEDO);
			player.getLocation().getGalaxy().nextTurn();
			return new PlanetScreen();
		}
		return null;
	}

	@Override
	public Screen onCancel()
	{
		return null;
	}
}
