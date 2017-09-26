package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class Dock implements Action
{
    public static final String SOUND_EFFECT = Paths.DOCK;

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (!actor.isInSector())
        {
            return "Ship must be in orbit to dock.";
        }

        Station station = actor.getSectorLocation().getStation();

        if (station == null)
        {
            return "There is no station at this orbit.";
        }

        if (actor.isLanded())
        {
            return "The ship cannot dock while landed.";
        }

        if (actor.isDocked())
        {
            return "The ship is already docked with " + station + ".";
        }

        if (actor.isHostile(station.getFaction()) && actor.isAligned())
        {
            actor.setLocation(actor.getSectorLocation().dock());

            String claimExecution = new Claim().canExecute(actor);
            if (claimExecution != null)
            {
                actor.setLocation(actor.getStationLocation().undock());
                return station + " is controlled by the hostile " + station.getFaction() + ", who deny you entry.";
            }
            return null;
        }
        else
        {
            actor.setLocation(actor.getSectorLocation().dock());
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

        actor.setLocation(actor.getSectorLocation().dock());
        new Claim().execute(actor);
        actor.repairModules();
        actor.updatePrices();

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
