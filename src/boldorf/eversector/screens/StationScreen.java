package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedMenu;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.entities.Station;
import boldorf.eversector.items.BaseResource;
import boldorf.eversector.items.Item;
import boldorf.eversector.items.Module;
import boldorf.eversector.items.Resource;
import boldorf.eversector.storage.Resources;
import java.awt.event.KeyEvent;
import java.util.List;
import squidpony.squidmath.Coord;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.storage.Paths.CLAIM;
import static boldorf.eversector.storage.Paths.MINE;
import static boldorf.eversector.storage.Paths.OFF;
import static boldorf.eversector.storage.Paths.ON;
import java.awt.Color;
import java.util.ArrayList;

/**
 * 
 */
class StationScreen extends MenuScreen<AlignedMenu>
        implements WindowScreen<AlignedWindow>, CommandScreen
{
    public static final Color COLOR_AFFORDABLE = AsciiPanel.white;
    public static final Color COLOR_UNAFFORDABLE = AsciiPanel.brightBlack;
    public static final Color COLOR_CREDITS_AFFORDABLE = AsciiPanel.brightWhite;
    public static final Color COLOR_CREDITS_UNAFFORDABLE = AsciiPanel.red;
    
    public StationScreen(Display display)
    {
        super(new AlignedMenu(new AlignedWindow(display, Coord.get(0, 0),
                new Border(2)), COLOR_SELECTION_FOREGROUND,
                COLOR_SELECTION_BACKGROUND));
    }

    @Override
    public void displayOutput()
    {
        setUpMenu();
        super.displayOutput();
    }
    
    @Override
    public Screen processInput(KeyEvent key)
    {
        if (getMenu().updateSelectionRestricted(key))
            return this;
        
        boolean nextTurn = false;
        Screen nextScreen = this;
        Item item = getSelectedItem();
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_LEFT: case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_ESCAPE:
            {
                player.undock();
                nextTurn = true;
                playSoundEffect(MINE);
                nextScreen = new SectorScreen(getDisplay());
                break;
            }
            case KeyEvent.VK_EQUALS: case KeyEvent.VK_PLUS:
            case KeyEvent.VK_ENTER:
            {
                boolean onSound = item instanceof Module ?
                        player.buyModule((Module) item) :
                        player.buyResource(item.getName(), 1);
                if (onSound)
                    playSoundEffect(ON);
                break;
            }
            case KeyEvent.VK_MINUS: case KeyEvent.VK_UNDERSCORE:
            {
                boolean offSound = item instanceof Module ?
                        player.sellModule(item.getName()) :
                        player.buyResource(item.getName(), -1);
                if (offSound)
                    playSoundEffect(OFF);
                break;
            }
            case KeyEvent.VK_R:
                if (restock())
                    playSoundEffect(ON);
                break;
            case KeyEvent.VK_C:
                if (player.claim())
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
        keybindings.add(new Keybinding("undock",
                Character.toString(ExtChars.ARROW1_L),
                Character.toString(ExtChars.ARROW1_R), "escape"));
        keybindings.add(new Keybinding("buy item", "enter", "+"));
        keybindings.add(new Keybinding("sell item", "-"));
        keybindings.add(new Keybinding("restock", "r"));
        return keybindings;
    }

    /**
     * Maxes out all of the player's resources, selling ore.
     * @return true if at least one resource was restocked
     */
    private boolean restock()
    {
        boolean restocked = false;
        
        if (!player.getResource(Resources.ORE).isEmpty())
        {
            restocked = player.buyResource(Resources.ORE,
                    -player.getMaxSellAmount(Resources.ORE));
        }
        
        restocked = restock(Resources.HULL) || restocked;
        restocked = restock(Resources.FUEL) || restocked;
        restocked = restock(Resources.ENERGY) || restocked;
        return restocked;
    }
    
    /**
     * Purchases the maximum amount of one item, returning true if the item was
     * restocked.
     * @param name the name of the resource to restock
     * @return true if the resource was restocked
     */
    private boolean restock(String name)
    {
        Resource resource = player.getResource(name);
        
        if (resource == null)
            return false;
        
        return !resource.isFull() &&
                player.buyResource(name, player.getMaxBuyAmount(resource));
    }
    
    @Override
    public AlignedWindow getWindow()
        {return getMenu().getWindow();}
    
    private void setUpMenu()
    {
        List<ColorString> contents = getWindow().getContents();
        Station station = player.dockedWith();
        List<Ship> ships = station.getShips();
        
        contents.clear();
        getWindow().getSeparators().clear();
        contents.add(new ColorString(station.toString()));
        contents.add(new ColorString("Orbit: ")
                .add(new ColorString(Integer.toString(player.getOrbit()),
                        COLOR_FIELD)));
        contents.add(new ColorString("Ruler: ")
                .add(station.getFaction().toColorString()));
        
        if (ships.size() > 1)
        {
            getWindow().addSeparator(new Line(true, 2, 1));
            for (Ship ship: ships)
                if (ship != player)
                    contents.add(ship.toColorString());
        }
        
        getWindow().addSeparator(new Line(true, 2, 1));
        int index = contents.size();
        int startIndex = index;
        for (BaseResource resource: station.getResources())
        {
            contents.add(getItemString(resource));
            getMenu().getRestrictions().add(index);
            index++;
        }
        
        for (BaseResource resource: station.getResources())
        {
            contents.add(getItemString(resource.getExpander()));
            getMenu().getRestrictions().add(index);
            index++;
        }
        
        for (Module module: station.getModules())
        {
            if (station.sells(module))
            {
                contents.add(getItemString(module));
                getMenu().getRestrictions().add(index);
                index++;
            }
        }
        
        getWindow().addSeparator(new Line(false, 1, 1));
        for (BaseResource resource: station.getResources())
            contents.add(getItemPriceString(resource));
        
        for (BaseResource resource: station.getResources())
            contents.add(getItemPriceString(resource.getExpander()));
        
        for (Module module: station.getModules())
            if (station.sells(module))
                contents.add(getItemPriceString(module));
        
        if (getMenu().getSelectionIndex() == 0)
            getMenu().setSelectionIndex(startIndex);
        
        getWindow().addSeparator(new Line(true, 2, 1));
        Item item = getSelectedItem();
        if (item != null)
            contents.addAll(item.define());
    }
    
    private Item getSelectedItem()
    {
        return player.dockedWith().getItem(getMenu().getSelection().toString());
    }
    
    private ColorString getItemString(Item item)
        {return new ColorString(item.toString(), getCostColor(item));}
    
    private ColorString getItemPriceString(Item item)
    {
        return new ColorString(Integer.toString(item.getPrice()),
                getCreditColor(item))
            .add(new ColorString(" Credits", getCostColor(item)));
    }
    
    private Color getCostColor(Item item)
    {
        return player.getCredits() >= item.getPrice() ?
                COLOR_AFFORDABLE : COLOR_UNAFFORDABLE;
    }
    
    private Color getCreditColor(Item item)
    {
        return player.getCredits() >= item.getPrice() ?
                COLOR_CREDITS_AFFORDABLE : COLOR_CREDITS_UNAFFORDABLE;
    }
}