package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Mine;

import java.awt.event.KeyEvent;

import static maugrift.eversector.Main.addError;
import static maugrift.eversector.Main.playSoundEffect;
import static maugrift.eversector.Paths.DEATH;

/**
 * The prompt presented when the player is attempting to mine a potentially
 * destructive asteroid.
 *
 * @author Dale Campbell
 */
public class AsteroidMineConfirmScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new AsteroidMineConfirmScreen.
	 */
	public AsteroidMineConfirmScreen()
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(
				new ColorString(
					"Your hull is dangerously low; attempt to mine the asteroid?"
				)
		);
		getConfirmCodes().remove((Integer) KeyEvent.VK_ENTER);
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
		String mineExecution = new Mine().execute(Main.player);
		if (mineExecution != null)
		{
			addError(mineExecution);
			return null;
		}

		if (Main.player.isDestroyed())
		{
			playSoundEffect(DEATH);
			return new EndScreen(
					new ColorString(
						"You collide with the asteroid, which breaches your hull!"
					),
					true,
					false
			);
		}

		Main.galaxy.nextTurn();
		return null;
	}

	@Override
	public Screen onCancel()
	{
		return null;
	}
}
