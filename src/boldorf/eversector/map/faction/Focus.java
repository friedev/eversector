package boldorf.eversector.map.faction;

/** A class used to group the two components of a focus. */
public enum Focus
{
    INVADE("invade", "Invade enemy sectors."),
    DEFEND("defend", "Defend claimed sectors."),
    EXPAND("expand", "Expand to new sectors.");
    
    private String name;
    private String description;
    
    Focus(String n, String d)
    {
        name        = n;
        description = d;
    }
    
    public String getName()
        {return name;}
    
    public String getDescription()
        {return description;}
}