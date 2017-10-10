package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.Option;
import boldorf.eversector.Paths;
import boldorf.eversector.Symbol;
import boldorf.eversector.map.Galaxy;
import boldorf.util.Utility;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.rng;

/**
 * The menu screen that is displayed at the start of the game.
 *
 * @author Maugrift
 */
public class StartScreen extends Screen
{
    /**
     * The version number of the game.
     */
    private static final String VERSION = "v0.7";

    /**
     * The longest version that can be compensated for with spaces.
     */
    private static final int MAX_VERSION_LENGTH = 33;

    /**
     * The name of the game's developer.
     */
    private static final String DEVELOPER = "Maugrift";

    /**
     * The year the game is copyrighted in.
     */
    private static final int COPYRIGHT_YEAR = 2017;

    /**
     * The average number of stars per tile.
     */
    private static final double STARS_PER_TILE = 0.0125;

    /**
     * The character printed for each star.
     */
    private static final ColorChar STAR_CHARACTER = new ColorChar(Symbol.SUBDWARF.get(), AsciiPanel.brightWhite);

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
        getDisplay().writeCenter(getDisplay().getCenterY() - titleArt.length / 2 - window.getContents().size() / 2 - 1,
                titleArt);
        window.display();
        if (popup != null)
        {
            popup.displayOutput();
        }
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            popup = popup.processInput(key);
            if (popup != null)
            {
                return this;
            }
        }

        if (key.getKeyCode() == KeyEvent.VK_O)
        {
            popup = new OptionsScreen();
        }

        if (!(key.getKeyCode() == KeyEvent.VK_ENTER || key.getKeyCode() == KeyEvent.VK_SPACE))
        {
            return this;
        }

        String name = Option.SHIP_NAME.getProperty();
        if (name.isEmpty())
        {
            popup = new NamePromptScreen("your ship", Option.SHIP_NAME);
            return this;
        }

        name = Option.CAPTAIN_NAME.getProperty();
        if (name.isEmpty())
        {
            popup = new NamePromptScreen("your ship's captain", Option.CAPTAIN_NAME);
            return this;
        }

        Main.playSoundEffect(Paths.START);
        Main.player.setName(Option.SHIP_NAME.getProperty());

        for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++)
        {
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

        String padding = Utility.getSpaces(MAX_VERSION_LENGTH - VERSION.length());
        String infoLine = Symbol.COPYRIGHT + " " + COPYRIGHT_YEAR + " " + DEVELOPER + " " + padding + VERSION;
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
        int nStars = (int) (STARS_PER_TILE * (getDisplay().getCharWidth() * getDisplay().getCharHeight()));
        starCoords = new ArrayList<>(nStars);
        for (int i = 0; i < nStars; i++)
        {
            Coord starCoord;
            do
            {
                starCoord = rng.nextCoord(getDisplay().getCharWidth(), getDisplay().getCharHeight());
            } while (starCoords.contains(starCoord));

            starCoords.add(starCoord);
        }
    }

    /**
     * Draws the stars in the starfield to the display.
     */
    private void drawStarfield()
    {
        for (Coord starCoord : starCoords)
        {
            getDisplay().write(starCoord, STAR_CHARACTER);
        }
    }
}