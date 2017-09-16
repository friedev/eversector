package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.PopupTerminal;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.Option;
import boldorf.eversector.Paths;
import boldorf.eversector.Symbol;
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
 */
public class StartScreen extends Screen
{
    /**
     * The version number of the game.
     */
    public static final String VERSION = "v0.7";

    /**
     * The longest version that can be compensated for with spaces.
     */
    public static final int MAX_VERSION_LENGTH = 22;

    /**
     * The name of the game's developer.
     */
    public static final String DEVELOPER = "Boldorf Smokebane";

    /**
     * The year the game is copyrighted in.
     */
    public static final int COPYRIGHT_YEAR = 2017;

    public static final double STARS_PER_TILE = 0.0125;

    /**
     * The character printed for each star.
     */
    public static final ColorChar STAR_CHARACTER = new ColorChar(Symbol.SUBDWARF.get(), AsciiPanel.brightWhite);

    private PopupWindow window;
    private PopupTerminal namePrompt;
    private List<Coord> starCoords;

    public StartScreen(Display display, List<ColorString> startMessages)
    {
        super(display);
        window = new PopupWindow(display, startMessages);
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
        if (namePrompt != null) { namePrompt.displayOutput(); }
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (namePrompt != null)
        {
            namePrompt = (PopupTerminal) namePrompt.processInput(key);
            if (namePrompt != null) { return this; }
        }

        if (!(key.getKeyCode() == KeyEvent.VK_ENTER || key.getKeyCode() == KeyEvent.VK_SPACE)) { return this; }

        String name = Option.SHIP_NAME.getProperty();
        if (name.isEmpty())
        {
            namePrompt = new NamePromptScreen(getDisplay(), "your ship", Option.SHIP_NAME);
            return this;
        }

        name = Option.CAPTAIN_NAME.getProperty();
        if (name.isEmpty())
        {
            namePrompt = new NamePromptScreen(getDisplay(), "your ship's captain", Option.CAPTAIN_NAME);
            return this;
        }

        Main.playSoundEffect(Paths.START);
        Main.player.setName(Option.SHIP_NAME.getProperty());

        for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++)
        { Main.galaxy.nextTurn(); }

        return new GameScreen(getDisplay());
    }

    public static ColorString[] getTitleArt()
    {
        // Art credit goes to patorjk.com/software/taag/
        
        /*
         __________               ________          _____
         ___  ____/  _______________  ___/____________  /______________
         __  / __ | / /  _ \_  ___/____ \_  _ \  ___/  __/  __ \_  ___/
         _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /
         / /___ ____/ \___//_/    /____/ \___/\___/ \__/ \____//_/
        /_____/ C 20XX Boldorf Smokebane                   vX.X.X
        */

        String padding = Utility.getSpaces(MAX_VERSION_LENGTH - VERSION.length() + 2);

        String infoLine = Symbol.COPYRIGHT + " " + COPYRIGHT_YEAR + " " + DEVELOPER + " " + padding + VERSION;

        List<ColorString> titleArt = new LinkedList<>();

        // Comment above is final form; print is distorted by extra backslashes
        titleArt.add(new ColorString(" __________               ________          _____              "));
        titleArt.add(new ColorString(" ___  ____/  _______________  ___/____________  /______________"));
        titleArt.add(new ColorString(" __  /___ | / /  _ \\_  ___/____ \\_  _ \\  ___/  __/  __ \\_  ___/"));
        titleArt.add(new ColorString(" _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /    "));
        titleArt.add(new ColorString(" / /___ ____/ \\___//_/    /____/ \\___/\\___/ \\__/ \\____//_/     "));
        titleArt.add(new ColorString("/_____/ ").add(new ColorString(infoLine, COLOR_FIELD)).add("      "));

        return titleArt.toArray(new ColorString[titleArt.size()]);
    }

    private void generateStarfield()
    {
        int nStars = (int) (STARS_PER_TILE * (getDisplay().getCharWidth() * getDisplay().getCharHeight()));
        starCoords = new ArrayList<>(nStars);
        for (int i = 0; i < nStars; i++)
        {
            starCoords.add(rng.nextCoord(getDisplay().getCharWidth(), getDisplay().getCharHeight()));
        }
    }

    private void drawStarfield()
    {
        for (int i = 0; i < starCoords.size(); i++)
        { getDisplay().write(starCoords.get(i), STAR_CHARACTER); }
    }
}