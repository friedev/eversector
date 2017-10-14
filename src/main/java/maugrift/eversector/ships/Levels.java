package maugrift.eversector.ships;

/**
 * A collection of ship classifications.
 *
 * @author Maugrift
 */
public abstract class Levels
{
    /**
     * The amount of points per level.
     */
    public static final int LEVEL_AMOUNT = 3;

    /**
     * The first level above the lowest possible level.
     */
    public static final int BASE_LEVEL = 1;

    /**
     * The highest level.
     */
    public static final int MAX_LEVEL = 3;

    /**
     * The name of the level below the base level.
     */
    public static final String LOWEST_LEVEL = "Scout";

    /**
     * The name of any levels above the highest level.
     */
    public static final String HIGHEST_LEVEL = "Dreadnought";

    /**
     * The in-bound classifications for ships with a higher mining level.
     */
    public static final String[] MINING_LEVELS = {"Miner", "Transport", "Freighter"};

    /**
     * The in-bound classifications for ships with a higher battle level.
     */
    public static final String[] BATTLE_LEVELS = {"Fighter", "Cruiser", "Battleship"};
}