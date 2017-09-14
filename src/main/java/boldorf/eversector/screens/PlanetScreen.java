package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Region;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.locations.PlanetLocation;
import static boldorf.eversector.storage.Paths.CLAIM;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.MINE;
import boldorf.util.Utility;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;
import static boldorf.eversector.Main.galaxy;

/**
 * 
 */
public class PlanetScreen extends Screen implements WindowScreen<AlignedWindow>,
        KeyScreen
{
    private AlignedWindow window;
    private PlanetLocation cursor;
    
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
        
        Direction direction = Utility.keyToDirectionRestricted(key);
        if (direction != null)
        {
            if (isLooking())
            {
                cursor = cursor.moveRegion(direction);
            }
            else if (player.relocate(direction))
            {
                nextTurn = true;
                playSoundEffect(ENGINE);
            }
        }
        else if (isLooking())
        {
            if (key.getKeyCode() == KeyEvent.VK_L ||
                    key.getKeyCode() == KeyEvent.VK_ESCAPE ||
                    key.getKeyCode() == KeyEvent.VK_ENTER)
            {
                cursor = null;
            }
        }
        else
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_ESCAPE:
                    if (player.takeoff())
                    {
                        nextTurn = true;
                        nextScreen = new SectorScreen(getDisplay());
                        playSoundEffect(ENGINE);
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    if (player.mine())
                    {
                        nextTurn = true;
                        playSoundEffect(MINE);
                    }
                    break;
                case KeyEvent.VK_C:
                    if (player.claim(true))
                    {
                        nextTurn = true;
                        playSoundEffect(CLAIM);
                    }
                    break;
                case KeyEvent.VK_L:
                    cursor = player.getPlanetLocation();
                    break;
                case KeyEvent.VK_V:
                    Main.showFactions = !Main.showFactions;
                    break;
            }
        }
        
        if (nextTurn)
            galaxy.nextTurn();
        return nextScreen;
    }
    
    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("change region", ExtChars.ARROW1_U,
                ExtChars.ARROW1_D, ExtChars.ARROW1_L,
                ExtChars.ARROW1_R));
        keybindings.add(new Keybinding("takeoff", "escape"));
        keybindings.add(new Keybinding("mine", "enter"));
        keybindings.add(new Keybinding("claim", "c"));
        keybindings.add(new Keybinding("look", "l"));
        keybindings.add(new Keybinding("toggle faction view", "v"));
        return keybindings;
    }
    
    @Override
    public AlignedWindow getWindow()
        {return window;}
    
    private boolean isLooking()
        {return cursor != null;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        contents.clear();
        window.getSeparators().clear();
        Planet planet = player.getSectorLocation().getPlanet();
        Region region = isLooking() ? cursor.getRegion() :
                player.getPlanetLocation().getRegion();
        contents.add(new ColorString(planet.toString()));
        contents.add(new ColorString("Orbit: ").add(new ColorString(
                Integer.toString(planet.getLocation().getOrbit()),
                COLOR_FIELD)));
        
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
        List<ColorString> colorStrings =
                planet.toColorStrings(Main.showFactions);
        if (isLooking())
        {
            colorStrings.get(cursor.getRegionCoord().y)
                    .getColorCharAt(cursor.getRegionCoord().x)
                    .setBackground(COLOR_SELECTION_BACKGROUND);
        }
        contents.addAll(colorStrings);
        
        window.addSeparator(new Line(false, 1, 2, 1));
        contents.add(new ColorString(region.toString()));
        if (region.isClaimed())
            contents.add(new ColorString("Ruler: ").add(region.getFaction()));
        
        if (isLooking() && !cursor.equals(player.getLocation()))
            return;
        
        if (region.hasOre())
        {
            contents.add(new ColorString("Ore: ")
                    .add(new ColorString(region.getOre().toString() + " ("
                            + region.getOre().getDensity() + ")",
                            COLOR_FIELD)));
        }
        
        for (Ship ship: region.getShips())
            if (ship != player)
                contents.add(ship.toColorString());
    }
}