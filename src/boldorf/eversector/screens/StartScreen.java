package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.util.Console;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.PopupTerminal;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COPYRIGHT_YEAR;
import static boldorf.eversector.Main.DEVELOPER;
import static boldorf.eversector.Main.MAX_VERSION_LENGTH;
import static boldorf.eversector.Main.VERSION;
import static boldorf.eversector.Main.rng;
import boldorf.eversector.map.Map;
import boldorf.eversector.storage.Options;
import boldorf.eversector.storage.Paths;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import squidpony.squidmath.Coord;

/** The menu screen that is displayed at the start of the game. */
public class StartScreen extends Screen
{
    /** The number of stars in the background starfield. */
    public static final int STARS = 60;
    
    /** The character printed for each star. */
    public static final ColorChar STAR_CHARACTER =
            new ColorChar(ExtChars.DOT, AsciiPanel.brightWhite);
    
    private PopupWindow window;
    private PopupTerminal namePrompt;
    private List<Coord> starCoords;
    
    public StartScreen(Display display, List<ColorString> startMessages)
    {
        super(display);
        window = new PopupWindow(display, startMessages);
        starCoords = new ArrayList<>(STARS);
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
            Screen result = namePrompt.processInput(key);
            if (result == null)
            {
                String nameInput = namePrompt.getInput();
                if (nameInput != null && nameInput.length() > 0)
                {
                    Main.options.setProperty(Options.NAME, nameInput);
                    namePrompt = null;
                    break promptCheck;
                }
                
                namePrompt = null;
                return this;
            }
            
            if (result instanceof PopupTerminal)
                namePrompt = (PopupTerminal) result;
            return this;
        }
        
        if (key.getKeyCode() == KeyEvent.VK_ESCAPE)
            return this;
        
        if (Main.options.getProperty(Options.NAME) == null)
        {
            setUpTerminal();
            return this;
        }
        
        Main.playSoundEffect(Paths.START);
        Main.player.setName(Main.options.getProperty(Options.NAME));
        
        for (int i = 0; i < Map.SIMULATED_TURNS; i++)
            Main.map.nextTurn();

        return new GameScreen(getDisplay());
    }
    
    private void setUpTerminal()
    {
        List<ColorString> contentList = new LinkedList<>();
        contentList.add(new ColorString(
                "Enter the name to use throughout all of your games."));
        
        namePrompt = new PopupTerminal(new PopupWindow(getDisplay(),
                contentList), new ColorString(), getDisplay().getWidth() - 2,
                COLOR_FIELD);
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
                Console.getSpaces(MAX_VERSION_LENGTH - VERSION.length());
        
        String infoLine = "(C) " + COPYRIGHT_YEAR + " " + new String(DEVELOPER)
                + " " + padding + VERSION;
        
        List<String> titleArt = new LinkedList<>();
        
        // Comment above is final form; print is distorted by extra backslashes
        titleArt.add(" __________               ________          _____              ");
        titleArt.add(" ___  ____/  _______________  ___/____________  /______________");
        titleArt.add(" __  /___ | / /  _ \\_  ___/____ \\_  _ \\  ___/  __/  __ \\_  ___/");
        titleArt.add(" _  __/__ |/ //  __/  /   ____/ //  __/ /__ / /_ / /_/ /  /    ");
        titleArt.add(" / /___ ____/ \\___//_/    /____/ \\___/\\___/ \\__/ \\____//_/     ");
        titleArt.add("/_____/ " + infoLine + "      ");
        
        return titleArt.toArray(new String[titleArt.size()]);
    }
    
    private void generateStarfield()
    {
        for (int i = 0; i < STARS; i++)
        {
            starCoords.add(rng.nextCoord(getDisplay().getCharWidth(),
                    getDisplay().getCharHeight()));
        }
    }
    
    private void drawStarfield()
    {
        for (int i = 0; i < STARS; i++)
            getDisplay().write(starCoords.get(i), STAR_CHARACTER);
    }
}