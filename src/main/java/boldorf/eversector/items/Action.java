package boldorf.eversector.items;

import boldorf.eversector.map.Station;

/**
 * A wrapper for certain amount of a resource.
 */
public class Action
{
    public static final Action BURN = new Action(Resource.FUEL, 4);
    public static final Action ORBIT = new Action(Resource.FUEL, 1);
    public static final Action RELOCATE = new Action(Resource.FUEL, 1);
    public static final Action ENTER = new Action(Resource.FUEL, 1);
    public static final Action ESCAPE = new Action(Resource.FUEL, 1);
    public static final Action LAND = new Action(Resource.FUEL, 2);
    public static final Action TAKEOFF = new Action(Resource.FUEL, 2);
    public static final Action MINE = new Action(Resource.ENERGY, 3);
    public static final Action FLEE = new Action(Resource.FUEL, 1);
    public static final Action PURSUE = new Action(Resource.FUEL, 1);

    public static Module SCAN;
    public static Module REFINE;
    public static Module SOLAR;
    public static Module WARP;
    public static Module SHIELD;
    public static Module CLOAK;

    public static Weapon LASER;
    public static Weapon TORPEDO;
    public static Weapon PULSE;

    /**
     * The name of the resource used in the action.
     */
    String resource;

    /**
     * The amount of the resource needed to perform the action.
     */
    int cost;

    /**
     * Creates an action with the name of the resource and the cost.
     *
     * @param resource the name of the resource to use in the action
     * @param cost     the amount of the resource needed to perform the action
     */
    public Action(String resource, int cost)
    {
        this.resource = resource;
        this.cost = cost;
    }

    /**
     * Returns the amount and name of the resource, for example, "5 fuel".
     *
     * @return the cost of the action, a space, and the name of the resource in lower case
     */
    @Override
    public String toString()
    {return cost + " " + resource.toLowerCase();}

    public String getResource()
    {return resource;}

    public int getCost()
    {return cost;}

    public void setResource(String resource)
    {this.resource = resource;}

    public void setCost(int cost)
    {this.cost = cost;}

    public static void initItems()
    {
        SCAN = Station.getBaseModule("Scanner");
        REFINE = Station.getBaseModule("Refinery");
        SOLAR = Station.getBaseModule("Solar Array");
        WARP = Station.getBaseModule("Warp Drive");
        SHIELD = Station.getBaseModule("Shield");
        CLOAK = Station.getBaseModule("Cloaking Device");

        LASER = Station.getBaseWeapon("Laser");
        TORPEDO = Station.getBaseWeapon("Torpedo Tube");
        PULSE = Station.getBaseWeapon("Pulse Beam");
    }
}