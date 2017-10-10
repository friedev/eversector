package boldorf.eversector.items;

import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.Symbol;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.rng;

/**
 * An item with a name and price for both ships and stations.
 *
 * @author Maugrift
 */
public abstract class Item
{
    /**
     * The name of the item.
     */
    private final String name;

    /**
     * The description of the item.
     */
    private final String description;

    /**
     * The objective value of the item in credits.
     */
    private final int value;

    /**
     * The local price of the item in credits. To be updated on a per-station basis.
     */
    private int price;

    /**
     * Creates a new item with a name, description, and value.
     *
     * @param name        the name of the item
     * @param description the description of the item
     * @param value       the value of the item
     */
    public Item(String name, String description, int value)
    {
        this.name = name;
        this.description = description;
        this.value = Math.abs(value);
        this.price = value;
    }

    /**
     * Creates a new expander from a set of Properties.
     *
     * @param properties the Properties object to use in construction that should contain the properties "name,"
     *                   "description," "value," and optionally "nickname"
     */
    public Item(Properties properties)
    {
        name = properties.getProperty("name");
        description = properties.getProperty("description");
        if (description == null)
        {
            throw new NullPointerException("Empty description field found while generating items.");
        }

        value = Math.abs(Integer.parseInt(properties.getProperty("value")));
        price = value;
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Gets the name of the item.
     *
     * @return the item's name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the description of the item.
     *
     * @return the item's description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets the value of the item in credits.
     *
     * @return the item's value in credits
     */
    public int getValue()
    {
        return value;
    }

    /**
     * Gets the local price of the item in credits.
     *
     * @return the item's local price
     */
    public int getPrice()
    {
        return price;
    }

    /**
     * Sets the local price of the item.
     *
     * @param price the new local price of the item
     */
    public void setPrice(int price)
    {
        this.price = price;
    }

    /**
     * Resets the price back to the value of the item.
     */
    public void resetPrice()
    {
        price = value;
    }

    /**
     * Creates a definition for the item, including name, value, and description.
     *
     * @return a List of ColorStrings that define the item
     */
    public List<ColorString> define()
    {
        List<ColorString> definition = new LinkedList<>();
        definition.add(new ColorString(name, COLOR_FIELD));
        definition.add(
                new ColorString("Value: ").add(new ColorString(Integer.toString(value) + Symbol.CREDITS, COLOR_FIELD)));
        definition.add(new ColorString("Description: ").add(new ColorString(description, COLOR_FIELD)));
        return definition;
    }

    /**
     * Generates the price of the item after fluctuation.
     */
    public void generatePrice()
    {
        // The greatest amount that the price can fluctuate
        int maxFluctuation = value / 5;

        // Generates a random number within the allowed fluctuation range
        int fluctuation = rng.nextInt(maxFluctuation * 2 + 1) - maxFluctuation * 2 / 2;

        // Add the fluctuation to the base value to get the local price
        price = value + fluctuation;
    }
}