package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Module;
import boldorf.eversector.locations.Location;
import boldorf.eversector.ships.Ship;
import squidpony.squidmath.Coord;

public class Warp implements Action
{
    public static final String MODULE = Module.WARP_DRIVE;
    public static final String SOUND_EFFECT = Paths.WARP;

    private final Coord coord;

    public Warp(Coord coord)
    {
        this.coord = coord;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (coord == null || getDestination(actor) == null)
        {
            return "The target location was not found.";
        }

        String validateModule = actor.validateModule(MODULE, "warp");
        if (validateModule != null)
        {
            return validateModule;
        }

        Module module = actor.getModule(MODULE);
        return actor.validateResources(module.getActionResource(), module.getActionCost(), "charge warp drive");
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        Module module = actor.getModule(MODULE);
        actor.setLocation(getDestination(actor));
        actor.getResource(module.getActionResource()).changeAmount(-module.getActionCost());
        return null;
    }

    /**
     * Returns the location the actor is attempting to warp to.
     *
     * @param actor the actor
     * @return the location the actor is attempting to warp to
     */
    private Location getDestination(Ship actor)
    {
        return actor.getLocation().moveTo(coord);
    }
}
