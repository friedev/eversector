package boldorf.eversector.items;

/** A wrapper for certain amount of a resource. */
public class Action
{
    /** The name of the resource used in the action. */
    String resource;
    
    /** The amount of the resource needed to perform the action. */
    int cost;
    
    /**
     * Creates an action with the name of the resource and the cost.
     * @param r the name of the resource to use in the action
     * @param c the amount of the resource needed to perform the action
     */
    public Action(String r, int c)
    {
        resource = r;
        cost     = c;
    }
    
    /**
     * Returns the amount and name of the resource, for example, "5 fuel".
     * @return the cost of the action, a space, and the name of the resource in
     * lower case
     */
    @Override
    public String toString()
        {return cost + " " + resource.toLowerCase();}
    
    public String getResource()
        {return resource;}
    
    public int getCost()
        {return cost;}
    
    public void setResource(String r)
        {resource = r;}
    
    public void setCost(int c)
        {cost = c;}
}