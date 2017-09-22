package boldorf.eversector;

import asciiPanel.AsciiFont;
import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.eversector.faction.Election;
import boldorf.eversector.faction.RelationshipChange;
import boldorf.eversector.items.Action;
import boldorf.eversector.map.Galaxy;
import boldorf.eversector.screens.GameScreen;
import boldorf.eversector.screens.StartScreen;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;
import boldorf.util.FileManager;
import boldorf.util.NameGenerator;
import boldorf.util.Utility;
import squidpony.squidmath.RNG;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

import static boldorf.eversector.Names.GENERAL;

/**
 * The main class for EverSector, which primarily manages player input.
 *
 * @author Boldorf Smokebane
 */
public class Main
{
    /**
     * The color used on variable fields on the display, as well as certain highlights.
     */
    public static final Color COLOR_FIELD = AsciiPanel.brightWhite;

    /**
     * The color of error messages.
     */
    public static final Color COLOR_ERROR = AsciiPanel.brightRed;

    /**
     * The color applied to the foreground of selections.
     */
    public static final Color COLOR_SELECTION_FOREGROUND = null;

    /**
     * The color applied to the background of selections.
     */
    public static final Color COLOR_SELECTION_BACKGROUND = new Color(0, 0, 192);

    /**
     * The Display used to display the game.
     */
    public static Display display;

    /**
     * The list of all fonts.
     */
    public static File[] fonts;

    /**
     * The random generator used to create various random game elements.
     */
    public static RNG rng;

    /**
     * The seed of the random number generator.
     */
    public static long seed;

    /**
     * The name generator to be used in creation of all random names.
     */
    public static NameGenerator nameGenerator;

    /**
     * The game music that will loop in the background.
     */
    public static Clip soundtrack;

    /**
     * The galaxy in which the current game is played.
     */
    public static Galaxy galaxy;

    /**
     * A reference to galaxy.getPlayer() for use in the game.
     */
    public static Ship player;

    /**
     * A list of ships that intend to attack the player.
     */
    public static Battle pendingBattle;

    /**
     * True if there is an election to take place in the player's faction.
     */
    public static Election pendingElection;

    /**
     * A list of proposed relationship changes from other factions.
     */
    public static Queue<RelationshipChange> pendingRelationships;

    /**
     * If true, will show star Symbol on the map instead of type Symbol and faction colors.
     */
    public static boolean showStars;

    /**
     * If true, will show faction colors instead of region colors on planets.
     */
    public static boolean showFactions;

    /**
     * The number of ships destroyed by the player.
     */
    public static int kills;

    /**
     * Set up the game and prompt the player for actions.
     *
     * @param args the command line arguments
     * @throws Exception if any are encountered
     */
    public static void main(String[] args) throws Exception
    {
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) ->
        {
            try
            {
                e.printStackTrace();

                FileManager.delete(Paths.CRASH);

                StackTraceElement[] stackTrace = e.getStackTrace();
                String[] stackTraceStrings = new String[stackTrace.length + 1];

                stackTraceStrings[0] = "The game has crashed! Please send the contents of this file to the " +
                                       "developer to help fix the problem.";
                for (int i = 0; i < stackTrace.length; i++)
                {
                    stackTraceStrings[i + 1] = stackTrace[i].toString();
                }

                FileManager.writeToFile(Paths.CRASH, stackTraceStrings);
            }
            catch (Exception ex)
            {
            }
            finally
            {
                System.exit(1);
            }
        });

        if (FileManager.getPath().contains("lib"))
        {
            FileManager.movePathUp();
            FileManager.addToPath("bundle/");
        }

        fonts = new File(FileManager.getPath() + Paths.FONTS).listFiles(File::isDirectory);

        if (FileManager.checkExistence(Paths.OPTIONS))
        {
            Option.options = FileManager.load(Paths.OPTIONS);
        }
        else
        {
            Option.options = new Properties();
        }

        Action.initItems();

        List<ColorString> startMessages = startGame();

        int fontIndex = Option.FONT.toInt();
        if (fontIndex < 0 || fontIndex >= fonts.length)
        {
            fontIndex = 0;
            Option.FONT.setProperty(0);
        }

        Properties fontProperties = getFontProperties(fontIndex);
        AsciiFont font = new AsciiFont(FileManager.getPath() + Paths.FONTS + fonts[fontIndex].getName() + "/" +
                                       (Option.TILES.toBoolean() ? Paths.FONT_TILES : Paths.FONT_ASCII),
                Utility.parseInt(fontProperties.getProperty(Option.FONT_WIDTH)),
                Utility.parseInt(fontProperties.getProperty(Option.FONT_HEIGHT)));
        display = new Display(new AsciiPanel(Option.WIDTH.toInt(), Option.HEIGHT.toInt(), font));

        display.setIconImage(FileManager.loadImage(Paths.ICON));
        display.setTitle("EverSector");
        display.init(new StartScreen(startMessages));

        try
        {
            if (soundtrack == null)
            {
                soundtrack = FileManager.loopAudio(Paths.SOUNDTRACK);
                FileManager.setVolume(soundtrack, Option.MUSIC.toInt());
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * Starts a game.
     *
     * @return a list of start messages for StartScreen
     * @throws Exception if any are encountered
     * @see StartScreen#StartScreen(List)
     */
    public static List<ColorString> startGame() throws Exception
    {
        pendingElection = null;
        pendingRelationships = new LinkedList<>();
        pendingBattle = null;
        showStars = false;
        showFactions = false;
        kills = 0;

        Option.applyDefaults();

        // Create the galaxy and update the player as needed
        setUpSeed();
        nameGenerator = new NameGenerator(GENERAL, rng);
        Symbol.setMap(Option.TILES.toBoolean());
        galaxy = new Galaxy();

        boolean savedGame = FileManager.checkExistence(Paths.SAVE);
        if (savedGame)
        {
            Properties save = FileManager.load(Paths.SAVE);
            player = new Ship(galaxy, save);
            galaxy.setPlayer(player);
        }
        else
        {
            Option.CAPTAIN_NAME.setProperty("");
            Option.SHIP_NAME.setProperty("");
            galaxy.createNewPlayer();
            player = galaxy.getPlayer();
        }

        List<ColorString> startMessages = new LinkedList<>();

        startMessages.add(new ColorString("Welcome to EverSector!"));
        startMessages.add(new ColorString("Please consult the bundled README to learn how to play."));
        startMessages.add(new ColorString("By playing, you accept the Terms of Use in the README."));

        String startAction;
        if (savedGame)
        {
            startAction = "continue your saved game";
        }
        else
        {
            if (Option.KEEP_SEED.toBoolean())
            {
                startMessages.add(new ColorString("Your chosen seed is: ").add(
                        new ColorString(Long.toString(seed), COLOR_FIELD)));
            }

            startAction = "begin";
        }

        startMessages.add(new ColorString("Press ").add(new ColorString("enter", COLOR_FIELD))
                                                   .add(" or ")
                                                   .add(new ColorString("space", COLOR_FIELD))
                                                   .add(new ColorString(" to " + startAction + ".")));

        return startMessages;
    }

    /**
     * Moves the player to a new galaxy.
     */
    public static void changeGalaxy()
    {
        galaxy = new Galaxy();
        for (int i = 0; i < Galaxy.SIMULATED_TURNS; i++)
        {
            galaxy.nextTurn();
        }

        galaxy.setPlayer(player);
        player.setLocation(galaxy.getRandomEdgeSector().getLocation());
        player.setFaction(null);
        player.createReputations();
    }

    /**
     * Adds a colored message to the message log.
     *
     * @param message the message to add
     */
    public static void addColorMessage(ColorString message)
    {
        Screen currentScreen = display.getScreen();
        if (currentScreen instanceof GameScreen)
        {
            ((GameScreen) currentScreen).addMessage(message);
        }
    }

    /**
     * Adds an error to the message log.
     *
     * @param error the error to add
     */
    public static void addError(String error)
    {
        addColorMessage(new ColorString(error, COLOR_ERROR));
    }

    /**
     * Adds a message of the default color to the message log.
     *
     * @param message the message to add
     */
    public static void addMessage(String message)
    {
        addColorMessage(new ColorString(message));
    }

    /**
     * Plays the sound effect at the given path.
     *
     * @param path the path of the sound effect to play
     */
    public static void playSoundEffect(String path)
    {
        try
        {
            Clip soundEffect = FileManager.playAudio(path);
            FileManager.setVolume(soundEffect, Option.SFX.toInt());
        }
        catch (Exception e)
        {
        }
    }

    /**
     * Set the seed based on the status of the keepSeed property.
     */
    public static void setUpSeed()
    {
        long newSeed = 0;
        if (Option.KEEP_SEED.toBoolean())
        {
            String seedString = Option.SEED.getProperty();
            if (seedString != null)
            {
                try
                {
                    newSeed = Long.parseLong(seedString);
                }
                catch (NumberFormatException nf)
                {
                }
            }
        }

        seed = newSeed == 0 ? new RNG().nextLong() : newSeed;
        rng = new RNG(seed);
    }

    public static Properties getFontProperties(int index) throws IOException
    {
        return FileManager.load(Paths.FONTS + fonts[index].getName() + "/" + Paths.FONT_PROPERTIES);
    }
}