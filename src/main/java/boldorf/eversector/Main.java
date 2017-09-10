package boldorf.eversector;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import boldorf.util.FileManager;
import boldorf.util.NameGenerator;
import boldorf.util.Utility;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import static boldorf.eversector.Main.addColorMessage;
import static boldorf.eversector.Main.optionIs;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.faction.Election;
import boldorf.eversector.faction.RelationshipChange;
import boldorf.eversector.screens.GameScreen;
import boldorf.eversector.screens.StartScreen;
import static boldorf.eversector.storage.Names.GENERAL;
import boldorf.eversector.storage.Options;
import boldorf.eversector.storage.Paths;
import boldorf.eversector.storage.Symbol;
import boldorf.eversector.storage.Tileset;
import static boldorf.eversector.storage.Tips.TIPS;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import javax.sound.sampled.Clip;
import squidpony.squidmath.RNG;

/** The main class for EverSector, which primarily manages player input. */
public class Main
{
    /**
     * If true, will adjust the path to work in the developer's IDE with sources
     * and libraries separate.
     */
    public static final boolean DEV_PATH = true;
    
    public static final Color
    COLOR_FIELD = AsciiPanel.brightWhite,
    COLOR_ERROR = AsciiPanel.brightRed,
    COLOR_SELECTION_FOREGROUND = null,
    COLOR_SELECTION_BACKGROUND = new Color(0, 0, 192);
    
    /** The Display used to display the game. */
    public static Display display;
    
    /** The random generator used to create various random game elements. */
    public static RNG rng;
    
    /** The seed of the random number generator. */
    public static long seed;
    
    /** The name generator to be used in creation of random nicknames. */
    public static NameGenerator nameGenerator;
    
    /** Various game options in the form of a properties file. */
    public static Properties options;
    
    /** The game music that will loop in the background. */
    public static Clip soundtrack;
    
    /** The galaxy in which the current game is played. */
    public static Galaxy galaxy;
    
    /** A reference to galaxy.getPlayer() for use in the game. */
    public static Ship player;
    
    /** A list of ships that intend to attack the player. */
    public static Battle pendingBattle;
    
    /** True if there is an election to take place in the player's faction. */
    public static Election pendingElection;
    
    /** A list of proposed relationship changes from other factions. */
    public static Queue<RelationshipChange> pendingRelationships;
    
    /**
     * If true, will show star Symbol on the map instead of type Symbol and
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
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) ->
        {
            try
            {
                FileManager.delete(Paths.CRASH);

                StackTraceElement[] stackTrace = e.getStackTrace();
                String[] stackTraceStrings = new String[stackTrace.length + 1];
                
                stackTraceStrings[0] =
                        "The game has crashed! Please send the contents of "
                        + "this file to the developer to help fix the problem.";
                for (int i = 0; i < stackTrace.length; i++)
                    stackTraceStrings[i + 1] = stackTrace[i].toString();

                FileManager.writeToFile(Paths.CRASH, stackTraceStrings);
            }
            catch (Exception ex)
            {}
            finally
            {
                System.exit(1);
            }
        });
        
        FileManager.movePathUp();

        if (FileManager.checkExistence(Paths.OPTIONS))
            options = FileManager.load(Paths.OPTIONS);
        else
            options = new Properties();
        
        List<ColorString> startMessages = startGame();
        
        AsciiFont font = Tileset.values()
                [Utility.parseInt(options.getProperty(Options.FONT))]
                .toFont(Options.toBoolean(options.getProperty(Options.TILES)));
        
        display = new Display(new AsciiPanel(
                Utility.parseInt(options.getProperty(Options.WIDTH)),
                Utility.parseInt(options.getProperty(Options.HEIGHT)), font));
        
        display.setIconImage(FileManager.loadImage(Paths.ICON));
        display.setTitle("EverSector");
        display.init(new StartScreen(display, startMessages));

        soundtrack = FileManager.loopAudio(Paths.SOUNDTRACK);
        Integer volume = Utility.parseInt(options.getProperty(Options.MUSIC));
        if (volume != null)
            FileManager.setVolume(soundtrack, volume);
    }
    
    public static List<ColorString> startGame() throws Exception
    {
        disqualified = false;
        pendingElection = null;
        pendingRelationships = new LinkedList<>();
        pendingBattle = null;
        showStars = false;
        kills = 0;

        setOptionDefaults();

        // Create the galaxy and update the player as needed
        setUpSeed();
        nameGenerator = new NameGenerator(GENERAL, rng);
        Symbol.setMap(Options.toBoolean(options.getProperty(Options.TILES)));
        galaxy = new Galaxy();
        
        boolean savedGame = FileManager.checkExistence(Paths.SAVE);
        if (savedGame)
        {
            Properties save = FileManager.load(Paths.SAVE);
            disqualified = optionIs(Options.OPTION_TRUE,
                    save.getProperty(Options.DISQUALIFIED));
            player = new Ship(galaxy, save);
            galaxy.setPlayer(player);
        }
        else
        {
            options.setProperty(Options.CAPTAIN_NAME, "");
            options.setProperty(Options.SHIP_NAME, "");
            galaxy.createNewPlayer();
            player = galaxy.getPlayer();
        }

        List<ColorString> startMessages = new LinkedList<>();

        startMessages.add(new ColorString("Welcome to EverSector!"));
        startMessages.add(new ColorString("Please consult the "
                + "bundled README to learn how to play."));
        startMessages.add(new ColorString("By playing, you accept the "
                + "Terms of Use in the README."));
        startMessages.add(new ColorString("Tip: ", COLOR_FIELD)
                        .add(rng.getRandomElement(TIPS)));

        String startAction;
        if (savedGame)
        {
            startAction = "continue your saved game";
        }
        else
        {
            if (optionIs(Options.OPTION_TRUE, Options.KEEP_SEED))
            {
                startMessages.add(new ColorString("Your chosen seed is: ")
                        .add(new ColorString(Long.toString(seed),
                                COLOR_FIELD)));
            }

            startAction = "begin";
        }
        
        startMessages.add(new ColorString("Press ")
                .add(new ColorString("enter", COLOR_FIELD))
                .add(" or ").add(new ColorString("space", COLOR_FIELD))
                .add(new ColorString(" to " + startAction + ".")));

        return startMessages;
    }
    
    public static void changeGalaxy()
    {
        galaxy = new Galaxy();
        for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++)
            galaxy.nextTurn();
        
        galaxy.setPlayer(player);
        player.setLocation(galaxy.getRandomEdgeSector().getLocation());
        player.setFaction(null);
        player.createReputations();
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
        try
        {
            Integer volume = Utility.parseInt(Options.SFX);
            FileManager.playAudio(path);
            if (volume != null)
                FileManager.setVolume(soundtrack, volume);
        }
        catch (Exception e) {}
    }
    
    /**
     * Returns true if the options property with the given key equals the given
     * String.
     * @param option the String to compare with the property
     * @param property the key of the property to check
     * @return true if the options property with the given key equals s
     */
    public static boolean optionIs(String option, String property)
    {
        if (option == null || property == null)
            return false;
        
        return option.equals(options.getProperty(property));
    }
    
    public static void setOptionDefaults()
    {
        if (options.getProperty(Options.FONT) == null ||
                Utility.parseInt(options.getProperty(Options.FONT)) == null)
        {
            options.setProperty(Options.FONT, Integer.toString(0));
        }
        
        if (options.getProperty(Options.WIDTH) == null ||
                Utility.parseInt(options.getProperty(Options.WIDTH)) == null)
        {
            options.setProperty(Options.WIDTH,
                    Integer.toString(Options.DEFAULT_WIDTH));
        }
        
        if (options.getProperty(Options.HEIGHT) == null ||
                Utility.parseInt(options.getProperty(Options.HEIGHT)) == null)
        {
            options.setProperty(Options.HEIGHT,
                    Integer.toString(Options.DEFAULT_HEIGHT));
        }
        
        if (options.getProperty(Options.MUSIC) == null ||
                Utility.parseInt(options.getProperty(Options.MUSIC)) == null)
        {
            options.setProperty(Options.MUSIC,
                    Integer.toString(FileManager.MAX_VOLUME));
        }
        
        if (options.getProperty(Options.SFX) == null ||
                Utility.parseInt(options.getProperty(Options.SFX)) == null)
        {
            options.setProperty(Options.SFX,
                    Integer.toString(FileManager.MAX_VOLUME));
        }
        
        for (String bool: Options.MENU_BOOLEANS)
        {
            if (options.getProperty(bool) == null)
                options.setProperty(bool, Options.MENU_DEFAULT);
        }
        
        for (String bool: Options.DEBUG_BOOLEANS)
        {
            if (options.getProperty(bool) == null)
                options.setProperty(bool, Options.DEBUG_DEFAULT);
        }
    }
    
    /** Set the seed based on the status of the keepSeed property. */
    public static void setUpSeed()
    {
        if (optionIs(Options.OPTION_TRUE, Options.KEEP_SEED))
        {
            String seedString = options.getProperty(Options.SEED);
            if (seedString != null)
            {
                try
                {
                    seed = Long.parseLong(seedString);
                }
                catch (NumberFormatException nf)
                {
                    seed = new RNG().nextLong();
                }
            }
        }
        else
        {
            seed = new RNG().nextLong();
        }
        
        rng = new RNG(seed);
    }
}