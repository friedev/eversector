package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Land;
import maugrift.eversector.locations.PlanetLocation;
import maugrift.eversector.map.Planet;
import maugrift.eversector.map.Region;
import maugrift.apwt.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.List;

import static maugrift.eversector.Main.*;

/**
 * The screen for selecting a landing destination.
 *
 * @author Maugrift
 */
public class LandScreen
	extends ConfirmationScreen
	implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * The currently selected region.
	 */
	private PlanetLocation selection;

	/**
	 * Instantiates a new LandScreen.
	 */
	public LandScreen()
	{
		super(Main.display);
		Planet planet = player.getSectorLocation().getPlanet();
		window = new PopupWindow(
				Main.display,
				new Border(1),
				new Line(true, 1, 1)
		);
		window.getContents().addAll(planet.toColorStrings(Main.showFactions));
		selection = new PlanetLocation(
				player.getSectorLocation(),
				Coord.get(0, 0)
		);
	}

	@Override
	public void displayOutput()
	{
		setUpWindow();
		window.display();
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		Direction direction = Utility.keyToDirectionRestricted(key);
		if (direction != null)
		{
			selection = selection.moveRegion(direction);
			return this;
		}

		if (key.getKeyCode() == KeyEvent.VK_V)
		{
			Main.showFactions = !Main.showFactions;
		}

		return super.processInput(key);
	}

	@Override
	public PopupWindow getWindow()
	{
		return window;
	}

	@Override
	public Screen onConfirm()
	{
		String landExecution = new Land(
				selection.getRegionCoord()
		).execute(player);
		if (landExecution == null)
		{
			player.getLocation().getGalaxy().nextTurn();
			return new PlanetScreen();
		}

		addError(landExecution);
		return null;
	}

	/**
	 * Sets up the window and its contents.
	 */
	private void setUpWindow()
	{
		List<ColorString> contents = window.getContents();
		contents.clear();
		Planet planet = player.getSectorLocation().getPlanet();
		List<ColorString> colorStrings = planet.toColorStrings(Main.showFactions);

		Coord regionCoord = selection.getRegionCoord();
		colorStrings
			.get(regionCoord.y)
			.getColorCharAt(regionCoord.x)
			.setBackground(COLOR_SELECTION_BACKGROUND);
		contents.addAll(colorStrings);

		window.addSeparator();
		Region region = selection.getRegion();
		contents.add(new ColorString(region.toString()));
		if (region.isClaimed())
		{
			contents.add(new ColorString("Ruler: ").add(region.getFaction()));
		}
	}
}
