package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Resource;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class Escape implements Action
{
    public static final String RESOURCE = Resource.FUEL;
    public static final int COST = 1;
    public static final String SOUND_EFFECT = Paths.ENGINE;

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (actor.isLanded())
        {
            return "You must be orbital before attempting an escape.";
        }

        if (actor.isDocked())
        {
            return "You must undock before attempting an escape.";
        }

        if (!actor.isInSector())
        {
            return "You must be in a sector to escape from one.";
        }

        if (actor.getSectorLocation().getOrbit() < actor.getLocation().getSector().getOrbits())
        {
            return "You must be at the furthest orbit of " + actor.getLocation().getSector() + " to attempt an escape.";
        }

        return actor.validateResources(RESOURCE, COST, "escape the gravity of " + actor.getLocation().getSector());
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        actor.getResource(RESOURCE).changeAmount(-COST);
        actor.setLocation(actor.getSectorLocation().escapeSector());

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
