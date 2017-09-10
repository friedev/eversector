package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.PopupTerminal;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.rng;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.storage.Options;
import boldorf.eversector.storage.Paths;
import boldorf.eversector.storage.Symbol;
import boldorf.util.Utility;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import squidpony.squidmath.Coord;

/** The menu screen that is displayed at the start of the game. */
public class StartScreen extends Screen
{
    /** The version number of the game. */
    public static final String VERSION = "v0.6";
    
    /** The longest version that can be compensated for with spaces. */
    public static final int MAX_VERSION_LENGTH = 22;
    
    /**
     * The name of the game's developer, stored as an array to make it harder to
     * find in one piece and change.
     */
    public static final char[] DEVELOPER = {'B', 'o', 'l', 'd', 'o', 'r', 'f',
                             ' ', 'S', 'm', 'o', 'k', 'e', 'b', 'a', 'n', 'e'};
    
    /** The year the game is copyrighted in. */
    public static final int COPYRIGHT_YEAR = 2017;
    
    public static final double STARS_PER_TILE = 0.0125;
    
    /** The character printed for each star. */
    public static final ColorChar STAR_CHARACTER =
            new ColorChar(Symbol.SUBDWARF.get(), AsciiPanel.brightWhite);
    
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
        getDisplay().writeCenter(getDisplay().getCenterY() - 
                getTitleArt().length / 2 - window.getContents().size() / 2 - 2,
                getTitleArt());
        window.display();
        if (namePrompt != null)
            namePrompt.displayOutput();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        promptCheck:
        if (namePrompt != null)
        {
            namePrompt = (PopupTerminal) namePrompt.processInput(key);
            if (namePrompt != null)
                return this;
        }
        
        if (!(key.getKeyCode() == KeyEvent.VK_ENTER ||
                key.getKeyCode() == KeyEvent.VK_SPACE))
            return this;
        
        String name = Main.options.getProperty(Options.SHIP_NAME);
        if (name == null || name.isEmpty())
        {
            namePrompt = new NamePromptScreen(getDisplay(), "your ship",
                    Options.SHIP_NAME);
            return this;
        }
        
        name = Main.options.getProperty(Options.CAPTAIN_NAME);
        if (name == null || name.isEmpty())
        {
            namePrompt = new NamePromptScreen(getDisplay(),
                    "your ship's captain", Options.CAPTAIN_NAME);
            return this;
        }
        
        Main.playSoundEffect(Paths.START);
        Main.player.setName(Main.options.getProperty(Options.SHIP_NAME));
        
        for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++)
            Main.galaxy.nextTurn();

        return new GameScreen(getDisplay());
    }
    
    public static String[] getTitleArt()
    {
        // Art credit goes to patorjk.com/software/taag/
        
        /*
         __________               ________          _____
         ___  ____/  _______________  ___/____________  /______________
         __  / __ | / /  _ \_  ___/____ \_  _ \  ___/  __/  __ \_  ___/
         _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /
         / /___ ____/ \___//_/    /____/ \___/\___/ \__/ \____//_/
        /_____/ (C) 20XX Boldorf Smokebane                 vX.X.X
        */
        
        String padding =
                Utility.getSpaces(MAX_VERSION_LENGTH - VERSION.length());
        
        String infoLine = Symbol.COPYRIGHT + " " + COPYRIGHT_YEAR + " "
                + new String(DEVELOPER) + " " + padding + VERSION;
        
        List<String> titleArt = new LinkedList<>();
        
        // Comment above is final form; print is distorted by extra backslashes
        titleArt.add(" __________               ________          _____              ");
        titleArt.add(" ___  ____/  _______________  ___/____________  /______________");
        titleArt.add(" __  /___ | / /  _ \\_  ___/____ \\_  _ \\  ___/  __/  __ \\_  ___/");
        titleArt.add(" _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /    ");
        titleArt.add(" / /___ ____/ \\___//_/    /____/ \\___/\\___/ \\__/ \\____//_/     ");
        titleArt.add("/_____/ " + infoLine + "        ");
        
        return titleArt.toArray(new String[titleArt.size()]);
    }
    
    private void generateStarfield()
    {
        int nStars = (int) (STARS_PER_TILE *
                (getDisplay().getCharWidth() * getDisplay().getCharHeight()));
        starCoords = new ArrayList<>(nStars);
        for (int i = 0; i < nStars; i++)
        {
            starCoords.add(rng.nextCoord(getDisplay().getCharWidth(),
                    getDisplay().getCharHeight()));
        }
    }
    
    private void drawStarfield()
    {
        for (int i = 0; i < starCoords.size(); i++)
            getDisplay().write(starCoords.get(i), STAR_CHARACTER);
    }
}