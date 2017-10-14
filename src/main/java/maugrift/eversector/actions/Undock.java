package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.ships.Ship;

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
        actor.playPlayerSound(SOUND_EFFECT);
        return null;
    }
}
