package boldorf.eversector.items;

import boldorf.eversector.map.Station;

/**
 * A wrapper for certain amount of a resource.
 *
 * @author Boldorf Smokebane
 */
public class Action
{
    /**
     * Moving one sector on the galactic map.
     */
    public static final Action BURN = new Action(Resource.FUEL, 4);

    /**
     * Moving one orbit in a sector.
     */
    public static final Action ORBIT = new Action(Resource.FUEL, 1);

    /**
     * Moving one region on a planet
     */
    public static final Action RELOCATE = new Action(Resource.FUEL, 1);

    /**
     * Entering a sector from the galactic map.
     */
    public static final Action ENTER = new Action(Resource.FUEL, 1);

    /**
     * Escaping a sector.
     */
    public static final Action ESCAPE = new Action(Resource.FUEL, 1);

    /**
     * Landing on a planet.
     */
    public static final Action LAND = new Action(Resource.FUEL, 2);

    /**
     * Taking off from a planet.
     */
    public static final Action TAKEOFF = new Action(Resource.FUEL, 2);

    /**
     * Mining ore from a planet or asteroid belt.
     */
    public static final Action MINE = new Action(Resource.ENERGY, 3);

    /**
     * Fleeing from a battle.
     */
    public static final Action FLEE = new Action(Resource.FUEL, 1);

    /**
     * Pursuing a fleeing ship.
     */
    public static final Action PURSUE = new Action(Resource.FUEL, 1);

    /**
     * Scanning a ship in battle.
     */
    public static Module SCAN;

    /**
     * Refining one unit of ore into one unit of fuel.
     */
    public static Module REFINE;

    /**
     * Regenerating energy with a solar array.
     */
    public static Module SOLAR;

    /**
     * Warping from one sector to another.
     */
    public static Module WARP;

    /**
     * Blocking an energy attack in battle
     */
    public static Module SHIELD;

    /**
     * Cloaking the ship to avoid detection.
     */
    public static Module CLOAK;

    /**
     * Firing a laser.
     */
    public static Weapon LASER;

    /**
     * Firing a torpedo.
     */
    public static Weapon TORPEDO;

    /**
     * Firing a pulse beam.
     */
    public static Weapon PULSE;

    /**
     * The name of the resource used in the action.
     */
    private final String resource;

    /**
     * The amount of the resource needed to perform the action.
     */
    private final int cost;

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
    {
        return cost + " " + resource.toLowerCase();
    }

    /**
     * Gets the resource used in the action.
     *
     * @return the resource used in the action
     */
    public String getResource()
    {
        return resource;
    }

    /**
     * Gets the cost of the action.
     *
     * @return the cost of the action
     */
    public int getCost()
    {
        return cost;
    }

    /**
     * Initializes all items.
     */
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