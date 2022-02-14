package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Distress;
import maugrift.eversector.faction.Faction;

import static maugrift.eversector.Main.player;

/**
 * The prompt displayed when another faction offers to aid the player in
 * distress.
 *
 * @author Maugrift
 */
public class DistressConvertScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * The faction offering help.
	 */
	private Faction converting;

	/**
	 * Instantiates a new DistressConvertScreen.
	 *
	 * @param converting the faction offering help
	 */
	public DistressConvertScreen(Faction converting)
	{
		super(Main.display);
		window = new PopupWindow(Main.display);
		window.getContents().add(new ColorString("The ")
				.add(converting)
				.add(" offers to aid you if you join them."));
		window.getContents().add(new ColorString("Accept the offer?"));
		this.converting = converting;
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
		new Distress(converting).execute(player);
		return null;
	}
}
