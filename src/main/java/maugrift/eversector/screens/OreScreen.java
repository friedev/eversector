package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.map.Ore;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

/**
 * A popup Screen used for temporarily displaying ore values. <b>Currently unused.</b>
 *
 * @author Maugrift
 */
public class OreScreen extends ConfirmationScreen
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new OreScreen.
	 */
	public OreScreen()
	{
		super(Main.display);
		List<ColorString> list = new LinkedList<>();
		for (Ore ore : Main.galaxy.getOreTypes())
		{
			list.add(new ColorString(ore.toString() + ": " + ore.getDensity() + " Density"));
		}
		window = new PopupWindow(getDisplay(), list);
	}

	@Override
	public void displayOutput()
	{
		window.display();
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (key.getKeyCode() == KeyEvent.VK_G)
		{
			return null;
		}
		return super.processInput(key);
	}

	@Override
	public Screen onConfirm()
	{
		return onCancel();
	}
}