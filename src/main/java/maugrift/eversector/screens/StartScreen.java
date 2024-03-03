package maugrift.eversector.screens;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorChar;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.Option;
import maugrift.eversector.Paths;
import maugrift.eversector.Symbol;
import maugrift.eversector.map.Galaxy;
import maugrift.apwt.util.Utility;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static maugrift.eversector.Main.COLOR_FIELD;
import static maugrift.eversector.Main.rng;

/**
 * The menu screen that is displayed at the start of the game.
 *
 * @author Aaron Friesen
 */
public class StartScreen extends Screen
{
	/**
	 * The version number of the game.
	 */
	private static final String VERSION = "0.7.3";

	/**
	 * The name of the game's developer.
	 */
	private static final String DEVELOPER = "Aaron Friesen";

	/**
	 * The years for which the game is copyrighted.
	 */
	private static final String COPYRIGHT_YEAR = "2016-2022";

	/**
	 * The maximum padding for the copyright year, developer, and version
	 * strings.
	 */
	private static final int MAX_PADDING = 45;

	/**
	 * The average number of stars per tile.
	 */
	private static final double STARS_PER_TILE = 0.0125;

	/**
	 * The character printed for each star.
	 */
	private static final ColorChar STAR_CHARACTER = new ColorChar(
		Symbol.SUBDWARF.get(),
		AsciiPanel.brightWhite
	);

	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * The screen temporarily displayed over and overriding all others.
	 */
	private Screen popup;

	/**
	 * The coordinates of stars in the background starfield.
	 */
	private List<Coord> starCoords;

	/**
	 * Instantiates a new StartScreen.
	 *
	 * @param startMessages the messages displayed
	 */
	public StartScreen(List<ColorString> startMessages)
	{
		super(Main.display);
		window = new PopupWindow(Main.display, startMessages);
		generateStarfield();
	}

	@Override
	public void displayOutput()
	{
		drawStarfield();
		ColorString[] titleArt = getTitleArt();
		getDisplay().writeCenter(
			getDisplay().getCenterY()
			- titleArt.length / 2
			- window.getContents().size() / 2
			- 1,
			titleArt
		);
		window.display();
		if (popup != null) {
			popup.displayOutput();
		}
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (popup != null) {
			popup = popup.processInput(key);
			if (popup != null) {
				return this;
			}
		}

		if (key.getKeyCode() == KeyEvent.VK_O) {
			popup = new OptionsScreen();
		}

		if (!(key.getKeyCode() == KeyEvent.VK_ENTER ||
				key.getKeyCode() == KeyEvent.VK_SPACE)) {
			return this;
		}

		String name = Option.SHIP_NAME.getProperty();
		if (name.isEmpty()) {
			popup = new NamePromptScreen("your ship", Option.SHIP_NAME);
			return this;
		}

		name = Option.CAPTAIN_NAME.getProperty();
		if (name.isEmpty()) {
			popup = new NamePromptScreen("your ship's captain", Option.CAPTAIN_NAME);
			return this;
		}

		Main.playSoundEffect(Paths.START);
		Main.player.setName(Option.SHIP_NAME.getProperty());

		for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++) {
			Main.galaxy.nextTurn();
		}

		return new GameScreen();
	}

	/**
	 * Constructs the game's title ASCII art.
	 *
	 * @return the list of ColorStrings in the title's ASCII art
	 */
	public static ColorString[] getTitleArt()
	{
		// Art credit goes to patorjk.com/software/taag/

		/*
		 __________               ________          _____
		 ___  ____/  _______________  ___/____________  /______________
		 __  / __ | / /  _ \_  ___/____ \_  _ \  ___/  __/  __ \_  ___/
		 _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /
		 / /___ ____/ \___//_/    /____/ \___/\___/ \__/ \____//_/
		/_____/
		*/

		String padding = Utility.getSpaces(
				MAX_PADDING
				- COPYRIGHT_YEAR.length()
				- DEVELOPER.length()
				- VERSION.length()
			);
		String infoLine = Symbol.COPYRIGHT
			+ " "
			+ COPYRIGHT_YEAR
			+ " "
			+ DEVELOPER
			+ " "
			+ padding
			+ VERSION;
		List<ColorString> titleArt = new LinkedList<>();

		titleArt.add(new ColorString(" __________               ________          _____              "));
		titleArt.add(new ColorString(" ___  ____/  _______________  ___/____________  /______________"));
		titleArt.add(new ColorString(" __  /___ | / /  _ \\_  ___/____ \\_  _ \\  ___/  __/  __ \\_  ___/"));
		titleArt.add(new ColorString(" _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /    "));
		titleArt.add(new ColorString(" / /___ ____/ \\___//_/    /____/ \\___/\\___/ \\__/ \\____//_/     "));
		titleArt.add(new ColorString("/_____/ ").add(new ColorString(infoLine, COLOR_FIELD)).add("      "));

		return titleArt.toArray(new ColorString[titleArt.size()]);
	}

	/**
	 * Generates the coordinates of stars in the starfield.
	 */
	private void generateStarfield()
	{
		int nStars = (int)(
				STARS_PER_TILE * (
					getDisplay().getWidthInCharacters()
					* getDisplay().getHeightInCharacters()
				)
			);
		starCoords = new ArrayList<>(nStars);
		for (int i = 0; i < nStars; i++) {
			Coord starCoord;
			do {
				starCoord = rng.nextCoord(
						getDisplay().getWidthInCharacters(),
						getDisplay().getHeightInCharacters()
					);
			} while (starCoords.contains(starCoord));

			starCoords.add(starCoord);
		}
	}

	/**
	 * Draws the stars in the starfield to the display.
	 */
	private void drawStarfield()
	{
		for (Coord starCoord : starCoords) {
			getDisplay().write(starCoord.x, starCoord.y, STAR_CHARACTER);
		}
	}
}
