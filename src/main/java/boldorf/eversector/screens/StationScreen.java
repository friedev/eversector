package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.*;
import boldorf.apwt.windows.AlignedMenu;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import boldorf.eversector.Symbol;
import boldorf.eversector.items.BaseResource;
import boldorf.eversector.items.Item;
import boldorf.eversector.items.Module;
import boldorf.eversector.items.Resource;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Ship;
import boldorf.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static boldorf.eversector.Main.*;
import static boldorf.eversector.Paths.*;

/**
 * The screen used to interact with stations, especially buying and selling items.
 *
 * @author Boldorf Smokebane
 */
class StationScreen extends MenuScreen<AlignedMenu> implements WindowScreen<AlignedWindow>, KeyScreen
{
    /**
     * True if the player is buying items.
     */
    private boolean buying;

    /**
     * The start of the list of items that can be bought.
     */
    private int buyStart;

    /**
     * The start of the list of items that can be sold.
     */
    private int sellStart;

    /**
     * The end of the list of items that can be sold.
     */
    private int sellEnd;

    /**
     * Instantiates a new StationScreen.
     */
    public StationScreen()
    {
        super(new AlignedMenu(new AlignedWindow(Main.display, Coord.get(0, 0), new Border(2)),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
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
                {
                    getMenu().setSelectionIndex(buyStart);
                }
                else if (index == sellEnd)
                {
                    getMenu().setSelectionIndex(sellStart - 2);
                }
            }
            else
            {
                if (index == buyStart)
                {
                    getMenu().setSelectionIndex(sellStart);
                }
                else if (index < sellStart)
                {
                    getMenu().setSelectionIndex(sellEnd);
                }
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
                    {
                        success = player.buyModule(item.getName());
                    }
                    else
                    {
                        success = player.buyResource(item.getName(), 1);
                    }
                }
                else
                {
                    if (item instanceof Module)
                    {
                        success = player.sellModule(item.getName());
                    }
                    else
                    {
                        success = player.buyResource(item.getName(), -1);
                    }
                }

                if (success)
                {
                    playSoundEffect(TRANSACTION);
                }
                break;
            }
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_TAB:
            {
                int offset = getMenu().getSelectionIndex() - (buying ? buyStart : sellStart);
                buying = !buying;
                resetSelection();
                getMenu().setSelectionIndex(
                        Math.min(getMenu().getSelectionIndex() + offset, buying ? sellStart - 2 : sellEnd));
                break;
            }
            case KeyEvent.VK_R:
                if (restock())
                {
                    playSoundEffect(TRANSACTION);
                }
                break;
            case KeyEvent.VK_C:
                if (player.claim(true))
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
                nextScreen = new SectorScreen();
                break;
            }
        }

        if (nextTurn)
        {
            galaxy.nextTurn();
        }
        return nextScreen;
    }

    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("buy/sell item", "enter"));
        keybindings.add(new Keybinding("toggle buy/sell", "tab", Character.toString(ExtChars.ARROW1_L),
                Character.toString(ExtChars.ARROW1_R)));
        keybindings.add(new Keybinding("restock", "r"));
        keybindings.add(new Keybinding("claim", "c"));
        keybindings.add(new Keybinding("undock", "escape"));
        return keybindings;
    }

    private void resetSelection()
    {
        getMenu().setSelectionIndex(buying || getWindow().getContents().get(sellStart) == null ? buyStart : sellStart);
    }

    /**
     * Fills all of the player's resources, selling ore.
     *
     * @return true if at least one resource was restocked
     */
    private boolean restock()
    {
        boolean restocked = false;

        if (!player.getResource(Resource.ORE).isEmpty())
        {
            restocked = player.buyResource(Resource.ORE, -player.getMaxSellAmount(Resource.ORE));
        }

        restocked = restock(Resource.HULL) || restocked;
        restocked = restock(Resource.FUEL) || restocked;
        restocked = restock(Resource.ENERGY) || restocked;
        return restocked;
    }

    /**
     * Purchases the maximum amount of one item, returning true if the item was restocked.
     *
     * @param name the name of the resource to restock
     * @return true if the resource was restocked
     */
    private boolean restock(String name)
    {
        Resource resource = player.getResource(name);
        return resource != null && !resource.isFull() && player.buyResource(name, player.getMaxBuyAmount(resource));
    }

    @Override
    public AlignedWindow getWindow()
    {
        return getMenu().getWindow();
    }

    private void setUpMenu()
    {
        int prevOffset = getMenu().getSelectionIndex() - (buying ? buyStart : sellStart);

        List<ColorString> contents = getWindow().getContents();
        Station station = player.getSectorLocation().getStation();
        List<Ship> ships = station.getShips();

        contents.clear();
        getWindow().getSeparators().clear();
        contents.add(new ColorString(station.toString()));
        contents.add(new ColorString("Orbit: ").add(
                new ColorString(Integer.toString(player.getSectorLocation().getOrbit()), COLOR_FIELD)));
        contents.add(new ColorString("Ruler: ").add(station.getFaction()));

        if (ships.size() > 1)
        {
            getWindow().addSeparator(new Line(true, 2, 1));
            for (Ship ship : ships)
            {
                if (ship != player)
                {
                    contents.add(ship.toColorString());
                }
            }
        }

        getWindow().addSeparator(new Line(true, 2, 1));
        int index = contents.size();
        buyStart = index;
        for (BaseResource resource : station.getResources())
        {
            index = addEntry(getItemString(resource, true), index);
        }

        for (BaseResource resource : station.getResources())
        {
            index = addEntry(getItemString(resource.getExpander(), true), index);
        }

        for (Module module : station.getModules())
        {
            if (station.sells(module))
            {
                index = addEntry(getItemString(module, true), index);
            }
        }

        getWindow().addSeparator(new Line(false, 1, 1));
        index++;
        sellStart = index;

        for (Resource resource : player.getResources())
        {
            index = addEntry(getItemString(resource, false), index);
        }

        for (Resource resource : player.getResources())
        {
            if (resource.getNExpanders() > 0)
            {
                index = addEntry(getItemString(station.getExpander(resource.getExpander().getName()), false), index);
            }
        }

        for (Module module : player.getModules())
        {
            index = addEntry(getItemString(module, false), index);
        }

        for (Module module : player.getCargo())
        {
            if (!player.getModules().contains(module))
            {
                index = addEntry(getItemString(module, false), index);
            }
        }

        sellEnd = index - 1;

        if (getMenu().getSelectionIndex() == 0)
        {
            getMenu().setSelectionIndex(buyStart);
        }
        else
        {
            getMenu().setSelectionIndex((buying ? buyStart : sellStart) + prevOffset);
        }

        getWindow().addSeparator(new Line(true, 2, 1));
        contents.addAll(getSelectedItem().define());
    }

    /**
     * Gets the currently selected item.
     *
     * @return the currently selected item
     */
    private Item getSelectedItem()
    {
        if (getMenu().getSelectionIndex() >= getWindow().getContents().size() || getMenu().getSelection() == null)
        {
            resetSelection();
        }

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

    /**
     * Adds the given line to the contents and restrictions and increments the given index.
     *
     * @param line  the line to add
     * @param index the index to increment and to add a restriction at
     * @return the incremented index
     */
    private int addEntry(ColorString line, int index)
    {
        getWindow().getContents().add(line);
        getMenu().getRestrictions().add(index);
        return index + 1;
    }

    /**
     * Gets ColorString for the given item using ItemColors.
     *
     * @param item   the item being bought or sold
     * @param buying true if the item is being bought
     * @return the ColorString for the given item
     * @see ItemColors
     */
    private ColorString getItemString(Item item, boolean buying)
    {
        ItemColors colors = new ItemColors(item, buying);
        return new ColorString(item.toString(), colors.item).add(
                new ColorString(" (" + Integer.toString(item.getPrice()) + Symbol.CREDITS + ")", colors.credits));
    }

    /**
     * The colors of an item, based on whether the player can buy or sell it.
     */
    private class ItemColors
    {
        /**
         * The default color of the item.
         */
        private final Color ITEM = AsciiPanel.white;

        /**
         * The default color of the credits.
         */
        private final Color CREDITS = AsciiPanel.brightWhite;

        /**
         * The color applied to invalid fields.
         */
        private final Color INVALID = AsciiPanel.red;

        /**
         * The color applied to disabled fields.
         */
        private final Color DISABLED = AsciiPanel.brightBlack;

        /**
         * The item.
         */
        final Color item;

        /**
         * The cost of the item in credits.
         */
        final Color credits;

        /**
         * Creates a new set of item colors.
         *
         * @param item   the item
         * @param buying true if the item is being bought
         */
        ItemColors(Item item, boolean buying)
        {
            if (buying)
            {
                if (item instanceof BaseResource && player.getResource(item.getName()).isFull())
                {
                    this.item = DISABLED;
                    credits = DISABLED;
                    return;
                }

                if (player.getCredits() < item.getPrice())
                {
                    this.item = DISABLED;
                    credits = INVALID;
                    return;
                }

                this.item = ITEM;
                credits = CREDITS;
            }
            else
            {
                if (item instanceof Module && !player.getSectorLocation().getStation().sells((Module) item))
                {
                    this.item = INVALID;
                    credits = DISABLED;
                    return;
                }

                if (item instanceof Resource)
                {
                    if (((Resource) item).isEmpty())
                    {
                        this.item = DISABLED;
                        credits = DISABLED;
                        return;
                    }

                    if (!((Resource) item).canSell())
                    {
                        this.item = INVALID;
                        credits = DISABLED;
                        return;
                    }
                }

                this.item = ITEM;
                credits = CREDITS;
            }
        }

        /**
         * Creates a set of item colors using the default colors.
         */
        ItemColors()
        {
            item = ITEM;
            credits = CREDITS;
        }
    }
}