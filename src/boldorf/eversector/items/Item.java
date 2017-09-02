package boldorf.eversector.items;

import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.rng;
import static boldorf.eversector.Main.COLOR_FIELD;
import boldorf.eversector.storage.Symbols;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/** An item with a name and price for both ships and stations. */
public class Item
{
    private String name;
    private String description;
    private int value;
    private int price;
    
    /**
     * Creates a new item with a name, description, and value.
     * @param name the name of the item
     * @param description the description of the item
     * @param value the value of the item
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
     * @param properties the Properties object to use in construction that
     * should contain the properties "name," "description," "value," and
     * optionally "nickname"
     */
    public Item(Properties properties)
    {
        name = properties.getProperty("name");
        description = properties.getProperty("description");
        if (description == null)
        {
            throw new NullPointerException("Empty description field found "
                    + "while generating items.");
        }
        
        value = Math.abs(Integer.parseInt(properties.getProperty("value")));
        price = value;
    }
    
    @Override
    public String toString()
        {return name;}
    
    public String getName()
        {return name;}
    
    public String getDescription()
        {return description;}
    
    public int getValue()
        {return value;}
    
    public int getPrice()
        {return price;}
    
    public void setPrice(int price)
        {this.price = price;}
    
    /** Resets the price back to the value of the item. */
    public void resetPrice()
        {price = value;}
    
    /**
     * Creates a definition for the item, including name, value, and
     * description.
     * @return a List of ColorStrings that define the item
     */
    public List<ColorString> define()
    {
        List<ColorString> definition = new LinkedList<>();
        definition.add(new ColorString(name.toUpperCase()));
        definition.add(new ColorString("Value: ")
                .add(new ColorString(value + "" + Symbols.credits(),
                        COLOR_FIELD)));
        definition.add(new ColorString("Description: ")
                .add(new ColorString(description, COLOR_FIELD)));
        return definition;
    }
    
    /** Generates the price of the item after fluctuation. */
    public void generatePrice()
    {
        // The greatest amount that the price can fluctuate
        int maxFluctuation = value / 5;

        // Generates a random number within the allowed fluctuation range
        int fluctuation = rng.nextInt(maxFluctuation * 2 + 1)
                                                  - maxFluctuation * 2 / 2;

        // Add the fluctuation to the base value to get the local price
        price = value + fluctuation;
    }
}