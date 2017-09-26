package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class Undock implements Action
{
    public static final String SOUND_EFFECT = Paths.DOCK;

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (!actor.isDocked())
        {
            return "You are not docked.";
        }

        return null;
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        actor.setLocation(actor.getStationLocation().undock());

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
