package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Burn;

import static maugrift.eversector.Main.playSoundEffect;
import static maugrift.eversector.Main.player;
import static maugrift.eversector.Paths.START;

/**
 * The prompt to travel to a new galaxy.
 *
 * @author Aaron Friesen
 */
public class IntergalacticScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new IntergalacticScreen.
	 */
	public IntergalacticScreen()
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(new ColorString("Travel to another galaxy?"));
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
		player.getResource(Burn.RESOURCE).changeAmount(-Burn.COST);
		playSoundEffect(START);
		Main.changeGalaxy();
		return null;
	}
}
