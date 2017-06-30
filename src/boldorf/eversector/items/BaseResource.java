package boldorf.eversector.items;

import java.util.Properties;

/** A resource to be sold in an undefined quantity by a station. */
public class BaseResource extends Item
{
    /** The expander used to increase storage capacity of any Resources. */
    private Expander expander;
    
    /** True if the resource can be sold at stations. */
    private boolean sellable;
    
    /**
     * Creates a new BaseResource with a name, description, value, and expander.
     * @param name the name of the BaseResource
     * @param description the description of the BaseResource
     * @param value the value of one unit of the BaseResource
     * @param expander the expander for the BaseResource
     */
    public BaseResource(String name, String description, int value,
            Expander expander)
    {
        super(name, description, value);
        this.expander = expander;
        this.sellable = true;
    }
    
    /**
     * Copying constructor that creates another BaseResource identical to the
     * existing one.
     * @param copying the BaseResource to copy
     */
    public BaseResource(BaseResource copying)
    {
        this(copying.getName(), copying.getDescription(), copying.getValue(),
            new Expander(copying.expander));
    }
    
    /**
     * Creates a new BaseResource with two sets of properties, one for the
     * BaseResource itself, and the other for its expander.
     * @param properties the Properties object used in the construction of the
     * BaseResource
     * @param expanderProperties the Properties object used in the construction
     * of the BaseResource's expander
     */
    public BaseResource(Properties properties, Properties expanderProperties)
    {
        super(properties);
        expander = new Expander(expanderProperties);
        
        // False is checked because sellable should default to true
        sellable = !"false".equals(properties.getProperty("sellable"));
    }
    
    /**
     * Returns the BaseResource's expander.
     * @return the BaseResource's expander
     */
    public Expander getExpander()
        {return expander;}
    
    /**
     * Returns true if the resource can be sold.
     * @return true if the resource's sellable flag is enabled
     */
    public boolean isSellable()
        {return sellable;}
    
    /**
     * Changes the resource's sellable status.
     * @param newSellable the new value to be assigned to the sellable flag
     */
    public void setSellable(boolean newSellable)
        {sellable = newSellable;}
}