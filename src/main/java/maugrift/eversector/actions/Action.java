package maugrift.eversector.actions;

import maugrift.eversector.ships.Ship;

/**
 * An action that can be executed by a ship.
 */
public interface Action
{
    /**
     * Checks if the given ship can execute the action.
     *
     * @param actor the ship executing the action
     * @return the error message created if the action fails, null if successful
     */
    String canExecute(Ship actor);

    /**
     * Returns true if the given ship can execute the action.
     *
     * @param actor the ship executing the action
     * @return true if the ship can execute the action
     */
    default boolean canExecuteBool(Ship actor)
    {
        return canExecute(actor) == null;
    }

    /**
     * Makes the given ship execute the action.
     *
     * @param actor the ship executing the action
     * @return the error message created if the action fails, null if successful
     */
    String execute(Ship actor);

    /**
     * Makes the given ship execute the action.
     *
     * @param actor the ship executing the action
     * @return true if the execution was successful
     */
    default boolean executeBool(Ship actor)
    {
        return execute(actor) == null;
    }
}
