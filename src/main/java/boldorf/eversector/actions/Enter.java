package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Resource;
import boldorf.eversector.map.Sector;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class Enter implements Action
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

        Sector sector = actor.getLocation().getSector();

        if (actor.isInSector())
        {
            return "You are already in " + sector + ".";
        }

        if (sector.isEmpty())
        {
            return "There is nothing in " + sector + ".";
        }

        return actor.validateResources(RESOURCE, COST, "enter into orbit around " + sector);
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
        actor.setLocation(actor.getLocation().enterSector());

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
