package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Resource;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class Takeoff implements Action
{
    public static final String RESOURCE = Resource.FUEL;
    public static final int COST = 2;
    public static final String SOUND_EFFECT = Paths.ENGINE;

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (!actor.isLanded())
        {
            return "You are not landed.";
        }

        return actor.validateResources(RESOURCE, COST, "takeoff from the " + actor.getPlanetLocation().getRegion());
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
        actor.setLocation(actor.getPlanetLocation().takeoff());

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
