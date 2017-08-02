package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
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
import static boldorf.eversector.storage.Paths.DOCK;
import static boldorf.eversector.storage.Paths.TRANSACTION;
import boldorf.util.Utility;
import java.awt.Color;
import java.util.ArrayList;
import squidpony.squidgrid.Direction;

/**
 * 
 */
class StationScreen extends MenuScreen<AlignedMenu>
        implements WindowScreen<AlignedWindow>, KeyScreen
{
    private boolean buying;
    private int buyStart;
    private int sellStart;
    private int sellEnd;
    
    public StationScreen(Display display)
    {
        super(new AlignedMenu(new AlignedWindow(display, Coord.get(0, 0),
                new Border(2)), COLOR_SELECTION_FOREGROUND,
                COLOR_SELECTION_BACKGROUND));
        buying = true;
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
        Direction direction = Utility.keyToDirectionRestricted(key);
        if (getMenu().updateSelection(direction))
        {
            int index = getMenu().getSelectionIndex();
            if (buying)
            {
                if (index == sellStart)
                    getMenu().setSelectionIndex(buyStart);
                else if (index == sellEnd)
                    getMenu().setSelectionIndex(sellStart - 2);
            }
            else
            {
                if (index == buyStart)
                    getMenu().setSelectionIndex(sellStart);
                else if (index < sellStart)
                    getMenu().setSelectionIndex(sellEnd);
            }
            return this;
        }
        
        boolean nextTurn = false;
        Screen nextScreen = this;
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_ENTER:
            {
                boolean success;
                Item item = getSelectedItem();
                if (buying)
                {
                    if (item instanceof Module)
                        success = player.buyModule(item.getName());
                    else
                        success = player.buyResource(item.getName(), 1);
                }
                else
                {
                    if (item instanceof Module)
                        success = player.sellModule(item.getName());
                    else
                        success = player.buyResource(item.getName(), -1);
                }
                
                if (success)
                    playSoundEffect(TRANSACTION);
                break;
            }
            case KeyEvent.VK_LEFT: case KeyEvent.VK_RIGHT: case KeyEvent.VK_TAB:
            {
                int offset = getMenu().getSelectionIndex() -
                        (buying ? buyStart : sellStart);
                buying = !buying;
                resetSelection();
                getMenu().setSelectionIndex(Math.min(
                        getMenu().getSelectionIndex() + offset,
                        buying ? sellStart - 2 : sellEnd));
                break;
            }
            case KeyEvent.VK_R:
                if (restock())
                    playSoundEffect(TRANSACTION);
                break;
            case KeyEvent.VK_C:
                if (player.claim())
                {
                    nextTurn = true;
                    playSoundEffect(CLAIM);
                }
                break;
            case KeyEvent.VK_ESCAPE:
            {
                player.undock();
                nextTurn = true;
                playSoundEffect(DOCK);
                nextScreen = new SectorScreen(getDisplay());
                break;
            }
        }
        
        if (nextTurn)
            map.nextTurn();
        return nextScreen;
    }
    
    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("buy/sell item", "enter"));
        keybindings.add(new Keybinding("toggle buy/sell", "tab",
                Character.toString(ExtChars.ARROW1_L),
                Character.toString(ExtChars.ARROW1_R)));
        keybindings.add(new Keybinding("restock", "r"));
        keybindings.add(new Keybinding("claim", "c"));
        keybindings.add(new Keybinding("undock", "escape"));
        return keybindings;
    }

    private void resetSelection()
    {
        getMenu().setSelectionIndex(buying ||
                getWindow().getContents().get(sellStart) == null ?
                buyStart : sellStart);
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
        int prevOffset = getMenu().getSelectionIndex() -
                (buying ? buyStart : sellStart);
        
        List<ColorString> contents = getWindow().getContents();
        Station station = player.getSectorLocation().getStation();
        List<Ship> ships = station.getShips();
        
        contents.clear();
        getWindow().getSeparators().clear();
        contents.add(new ColorString(station.toString()));
        contents.add(new ColorString("Orbit: ")
                .add(new ColorString(Integer.toString(
                        player.getSectorLocation().getOrbit()), COLOR_FIELD)));
        contents.add(new ColorString("Ruler: ").add(station.getFaction()));
        
        if (ships.size() > 1)
        {
            getWindow().addSeparator(new Line(true, 2, 1));
            for (Ship ship: ships)
                if (ship != player)
                    contents.add(ship.toColorString());
        }
        
        getWindow().addSeparator(new Line(true, 2, 1));
        int index = contents.size();
        buyStart = index;
        for (BaseResource resource: station.getResources())
            index = addEntry(getItemString(resource, true), index);
        
        for (BaseResource resource: station.getResources())
            index = addEntry(getItemString(resource.getExpander(), true),
                    index);
        
        for (Module module: station.getModules())
            if (station.sells(module))
                index = addEntry(getItemString(module, true), index);
        
        getWindow().addSeparator(new Line(false, 1, 1));
        index++;
        sellStart = index;
        
        for (Resource resource: player.getResources())
            index = addEntry(getItemString(resource, false), index);
        
        for (Resource resource: player.getResources())
        {
            if (resource.getNExpanders() > 0)
            {
                index = addEntry(getItemString(station.getExpander(
                        resource.getExpander().getName()), false), index);
            }
        }
        
        for (Module module: player.getModules())
            index = addEntry(getItemString(module, false), index);
        
        for (Module module: player.getCargo())
            if (!player.getModules().contains(module))
                index = addEntry(getItemString(module, false), index);
        
        sellEnd = index  - 1;
        
        if (getMenu().getSelectionIndex() == 0)
        {
            getMenu().setSelectionIndex(buyStart);
        }
        else
        {
            getMenu().setSelectionIndex((buying ? buyStart : sellStart) +
                    prevOffset);
        }
        
        getWindow().addSeparator(new Line(true, 2, 1));
        contents.addAll(getSelectedItem().define());
    }
    
    private Item getSelectedItem()
    {
        if (getMenu().getSelectionIndex() >= getWindow().getContents().size() ||
                getMenu().getSelection() == null)
            resetSelection();
        
        String itemString = getMenu().getSelection().toString();
        if (!itemString.contains(" ("))
        {
            resetSelection();
            itemString = getMenu().getSelection().toString();
        }
        
        itemString = itemString.substring(0, itemString.indexOf(" ("));
        Item item = player.getSectorLocation().getStation().getItem(itemString);
        return item == null ? player.getModule(itemString) : item;
    }
    
    private int addEntry(ColorString line, int index)
    {
        getWindow().getContents().add(line);
        getMenu().getRestrictions().add(index);
        index++;
        return index;
    }
    
    private ColorString getItemString(Item item, boolean buying)
    {
        ItemColors colors = new ItemColors(item, buying);
        return new ColorString(item.toString(), colors.item)
                .add(new ColorString(" (" + Integer.toString(item.getPrice())
                        + "C)", colors.credits));
    }
    
    private class ItemColors
    {
        private final Color ITEM     = AsciiPanel.white;
        private final Color CREDITS  = AsciiPanel.brightWhite;
        private final Color INVALID  = AsciiPanel.red;
        private final Color DISABLED = AsciiPanel.brightBlack;
        
        Color item;
        Color credits;
        
        ItemColors(Item i, boolean buying)
        {
            if (buying)
            {
                if (i instanceof BaseResource &&
                        player.getResource(i.getName()).isFull())
                {
                    item = DISABLED;
                    credits = DISABLED;
                    return;
                }
                
                if (player.getCredits() < i.getPrice())
                {
                    item = DISABLED;
                    credits = INVALID;
                    return;
                }
                
                item = ITEM;
                credits = CREDITS;
            }
            else
            {
                if (i instanceof Module && !player.getSectorLocation()
                        .getStation().sells((Module) i))
                {
                    item = INVALID;
                    credits = DISABLED;
                    return;
                }
                
                if (i instanceof Resource)
                {
                    if (((Resource) i).isEmpty())
                    {
                        item = DISABLED;
                        credits = DISABLED;
                        return;
                    }
                    
                    if (!((Resource) i).isSellable())
                    {
                        item = INVALID;
                        credits = DISABLED;
                        return;
                    }
                }

                item = ITEM;
                credits = CREDITS;
            }
        }
        
        ItemColors()
        {
            item = ITEM;
            credits = CREDITS;
        }
    }
}