package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Planet;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Region;
import static boldorf.eversector.storage.Paths.CLAIM;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.MINE;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class PlanetScreen extends Screen implements WindowScreen<AlignedWindow>,
        KeyScreen
{
    private AlignedWindow window;
    
    public PlanetScreen(Display display)
    {
        super(display);
        window = new AlignedWindow(display, Coord.get(1, 1), new Border(2));
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
        boolean nextTurn = false;
        Screen nextScreen = this;
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_LEFT: case KeyEvent.VK_ESCAPE:
                if (player.takeoff())
                {
                    nextTurn = true;
                    nextScreen = new SectorScreen(getDisplay());
                    playSoundEffect(ENGINE);
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (player.mine())
                {
                    nextTurn = true;
                    playSoundEffect(MINE);
                }
                break;
            case KeyEvent.VK_UP:
                if (player.relocate(false))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                }
                break;
            case KeyEvent.VK_DOWN:
                if (player.relocate(true))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                }
                break;
            case KeyEvent.VK_C:
                if (player.claim(player.landedIn()))
                {
                    nextTurn = true;
                    playSoundEffect(CLAIM);
                }
                break;
        }
        
        if (nextTurn)
            map.nextTurn();
        return nextScreen;
    }
    
    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("change region", ExtChars.ARROW1_U,
                ExtChars.ARROW1_D));
        keybindings.add(new Keybinding("takeoff",
                Character.toString(ExtChars.ARROW1_L), "escape"));
        keybindings.add(new Keybinding("mine", ExtChars.ARROW1_R));
        keybindings.add(new Keybinding("claim", "c"));
        return keybindings;
    }
    
    @Override
    public AlignedWindow getWindow()
        {return window;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        window.getSeparators().clear();
        Planet planet = player.getSector().getPlanetAt(player.getOrbit());
        contents.add(new ColorString(planet.toString()));
        contents.add(new ColorString("Orbit: ").add(new ColorString(
                Integer.toString(planet.getOrbit()), COLOR_FIELD)));
        
        if (planet.isClaimed())
        {
            contents.add(new ColorString("Ruler: ")
                    .add(new ColorString(planet.getFaction().toString(), 
                    planet.getFaction().getColor())));
        }
        else
        {
            contents.add(new ColorString("Ruler: ").add(
                    new ColorString("Disputed", COLOR_FIELD)));
        }
        
        window.addSeparator(new Line(true, 2, 1));
        for (Region region: planet.getRegions())
        {
            ColorChar symbol;
            if (player.landedIn() == region)
                symbol = new ColorChar('@', AsciiPanel.brightWhite);
            else
                symbol = new ColorChar(ExtChars.DOT);
            contents.add(new ColorString(symbol));
        }
        
        window.addSeparator(new Line(false, 1, 2, 1));
        for (Region region: planet.getRegions())
            contents.add(region.toColorString());
        
        window.addSeparator(new Line(false, 1, 2, 1));
        contents.add(new ColorString(player.landedIn().toString()));
        if (player.landedIn().isClaimed())
        {
            contents.add(new ColorString("Ruler: ")
                    .add(player.landedIn().getFaction()));
        }
        
        if (player.landedIn().hasOre())
        {
            contents.add(new ColorString("Ore: ")
                    .add(new ColorString(player.landedIn().getOre().toString()
                            + " (" + player.landedIn().getOre().getDensity()
                            + ")", COLOR_FIELD)));
        }
        
        for (Ship ship: player.landedIn().getShips())
            if (ship != player)
                contents.add(ship.toColorString());
    }
}