package boldorf.eversector;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import boldorf.util.FileManager;
import boldorf.util.NameGenerator;
import boldorf.util.Utility;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Station;
import boldorf.eversector.map.Map;
import boldorf.eversector.map.faction.Election;
import boldorf.eversector.map.faction.RelationshipChange;
import boldorf.eversector.screens.GameScreen;
import boldorf.eversector.screens.StartScreen;
import static boldorf.eversector.storage.Names.GENERAL;
import boldorf.eversector.storage.Options;
import static boldorf.eversector.storage.Options.DEBUG_DEFAULT;
import static boldorf.eversector.storage.Options.DEFAULT_HEIGHT;
import static boldorf.eversector.storage.Options.DEFAULT_WIDTH;
import static boldorf.eversector.storage.Options.HEIGHT;
import static boldorf.eversector.storage.Options.MENU_DEFAULT;
import static boldorf.eversector.storage.Options.OPTION_TRUE;
import static boldorf.eversector.storage.Options.WIDTH;
import boldorf.eversector.storage.Paths;
import static boldorf.eversector.storage.Tips.TIPS;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.sound.sampled.Clip;
import squidpony.squidmath.RNG;

/** The main class for EverSector, which primarily manages player input. */
public class Main
{
    /** The version number of the game. */
    public static final String VERSION = "v0.5";
    
    /**
     * If true, will adjust the path to work in the developer's IDE with sources
     * and libraries separate.
     */
    public static final boolean DEV_PATH = true;
    
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
    
    public static final Color
    COLOR_FIELD = AsciiPanel.brightWhite,
    COLOR_ERROR = AsciiPanel.brightRed,
    COLOR_SELECTION_FOREGROUND = null,
    COLOR_SELECTION_BACKGROUND = new Color(0, 0, 192);
    
    public static final ColorChar
    SYMBOL_EMPTY  = new ColorChar(ExtChars.DOT),
    SYMBOL_PLAYER = new ColorChar('@', AsciiPanel.brightWhite);
    
    /** The Display used to display the game. */
    public static Display display;
    
    /** The random generator used to create various random game elements. */
    public static RNG rng;
    
    /** The seed of the random number generator. */
    public static long seed;
    
    /** The name generator to be used in creation of random nicknames. */
    public static NameGenerator nameGenerator;
    
    /** The map upon which the current game is played. */
    public static Map map;
    
    /** A reference to map.getPlayer() for use in the game. */
    public static Ship player;
    
    /** Various game options in the form of a properties file. */
    public static Properties options;
    
    /** The game music that will loop in the background. */
    public static Clip soundtrack;
    
    /** True if there is an election to take place in the player's faction. */
    public static Election pendingElection;
    
    /** A list of proposed relationship changes from other factions. */
    public static Queue<RelationshipChange> pendingRelationships;
    
    /** A list of ships that intend to attack the player. */
    public static Queue<Ship> attackers;
    
    /**
     * If true, will show star symbols on the map instead of type symbols and
     * faction colors.
     */
    public static boolean showStars;
    
    /**
     * True if the current run's final score is disqualified from the
     * leaderboard.
     */
    public static boolean disqualified;
    
    /** The number of ships destroyed by the player. */
    public static int kills;
    
    /**
     * Set up the game and prompt the player for actions.
     * @param args the command line arguments
     * @throws java.lang.Exception any uncaught exceptions will be thrown
     */
    public static void main(String[] args) throws Exception
    {
        if (DEV_PATH)
        {
            FileManager.movePathUp(4);
            FileManager.addToPath("EverSector/bundle/");
        }
        else
        {
            FileManager.movePathUp();
        }

        readInitialFiles();
        rng = new RNG();

        if (FileManager.checkExistence(Paths.OPTIONS))
            options = FileManager.load(Paths.OPTIONS);
        else
            options = new Properties();

        setUpSeed();
        nameGenerator = new NameGenerator(GENERAL, rng);
        
        List<ColorString> startMessages = startGame();
        
        display = new Display(new AsciiPanel(
                Utility.parseInt(options.getProperty(WIDTH)),
                Utility.parseInt(options.getProperty(HEIGHT)),
                AsciiFont.QBICFEET_10x10));

        display.init(new StartScreen(display, startMessages));

        soundtrack = FileManager.loopAudio(Paths.SOUNDTRACK);
        if (!optionIs(OPTION_TRUE, Options.MUSIC))
            soundtrack.stop();
    }
    
    public static List<ColorString> startGame() throws Exception
    {
        disqualified = false;
        pendingElection = null;
        pendingRelationships = new LinkedList<>();
        attackers = new LinkedList<>();
        showStars = false;
        kills = 0;

        setOptionDefaults();

        // Create the map and update the player as needed
        map = new Map();
        boolean savedGame = FileManager.checkExistence(Paths.SAVE);
        if (savedGame)
        {
            Properties save = FileManager.load(Paths.SAVE);
            if (optionIs(OPTION_TRUE,
                    save.getProperty(Options.DISQUALIFIED)))
                disqualified = true;
            player = new Ship(map, save);
            map.setPlayer(player);
        }
        else
        {
            map.createNewPlayer();
            player = map.getPlayer();
        }

        map.reveal(player.getLocation());
        // Reveal here to avoid overrideable calls in Map constructor

        LinkedList<ColorString> startMessages = new LinkedList<>();

        if (options.getProperty(Options.NAME) == null)
        {
            startMessages.add(new ColorString("Welcome to EverSector!"));
        }
        else
        {
            startMessages.add(new ColorString("Welcome back to EverSector, "
                    ).add(new ColorString(options.getProperty(Options.NAME),
                            COLOR_FIELD))
                    .add("!"));
        }

        startMessages.add(new ColorString("Please consult the "
                + "bundled README to learn how to play."));
        startMessages.add(new ColorString("By playing, you accept the "
                + "Terms of Use in the README."));
        startMessages.add(new ColorString("Tip: ", COLOR_FIELD)
                        .add(rng.getRandomElement(TIPS)));

        if (savedGame)
        {
            startMessages.add(new ColorString(
                    "Press any key to continue your saved game."));
        }
        else
        {
            if (optionIs(OPTION_TRUE, Options.KEEP_SEED))
            {
                startMessages.add(new ColorString("Your chosen seed is: ")
                        .add(new ColorString(Long.toString(seed),
                                COLOR_FIELD)));
            }

            startMessages.add(new ColorString("Press any key to begin."));
        }

        return startMessages;
    }
    
    public static void addColorMessage(ColorString message)
    {
        Screen currentScreen = display.getScreen();
        if (currentScreen instanceof GameScreen)
            ((GameScreen) currentScreen).addMessage(message);
    }
    
    public static void addError(String error)
        {addColorMessage(new ColorString(error, COLOR_ERROR));}
    
    public static void addMessage(String message)
        {addColorMessage(new ColorString(message));}
    
    public static void playSoundEffect(String path)
    {
        if (!optionIs(OPTION_TRUE, Options.SFX))
            return;
        
        try
        {
            FileManager.playAudio(path);
        }
        catch (Exception e) {}
    }
    
    public static void readInitialFiles() throws FileNotFoundException,
            IOException
    {
        Paths.initialize();
        Station.initializeModules();
        Station.initializeResources();
    }
    
    /**
     * Returns true if the options property with the given key equals the given
     * String.
     * @param s the String to compare with the property
     * @param property the key of the property to check
     * @return true if the options property with the given key equals s
     */
    public static boolean optionIs(String s, String property)
    {
        if (s == null || property == null ||
                options.getProperty(property) == null)
            return false;
        
        return s.equals(options.getProperty(property));
    }
    
    public static void setOptionDefaults()
    {
        if (options.getProperty(WIDTH) == null ||
                Utility.parseInt(options.getProperty(WIDTH)) == null)
        {
            options.setProperty(WIDTH, Integer.toString(DEFAULT_WIDTH));
        }
        
        if (options.getProperty(HEIGHT) == null ||
                Utility.parseInt(options.getProperty(HEIGHT)) == null)
        {
            options.setProperty(HEIGHT, Integer.toString(DEFAULT_HEIGHT));
        }
        
        for (String bool: Options.MENU_BOOLEANS)
        {
            if (options.getProperty(bool) == null)
                options.setProperty(bool, MENU_DEFAULT);
        }
        
        for (String bool: Options.DEBUG_BOOLEANS)
        {
            if (options.getProperty(bool) == null)
                options.setProperty(bool, DEBUG_DEFAULT);
        }
    }
    
    /** Set the seed based on the status of the keepSeed property. */
    public static void setUpSeed()
    {
        if (optionIs(OPTION_TRUE, Options.KEEP_SEED))
        {
            String seedString = options.getProperty(Options.SEED);
            if (seedString != null)
            {
                try
                {
                    long newSeed = Long.parseLong(seedString);
                    rng = new RNG(newSeed);
                }
                catch (NumberFormatException nf)
                {
                    // Do nothing, but don't set the seed
                }
            }
        }
    }
}