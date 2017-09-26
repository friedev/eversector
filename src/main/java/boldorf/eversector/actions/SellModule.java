package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Module;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;

public class SellModule implements Action
{
    public static final String SOUND_EFFECT = Paths.TRANSACTION;

    private final String module;

    public SellModule(String module)
    {
        this.module = module;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        String validateDocking = actor.validateDocking();
        if (validateDocking != null)
        {
            return validateDocking;
        }

        if (!actor.hasModule(module))
        {
            return "You do not have the specified module installed.";
        }

        // Module must be retrieved after it is known that the ship is docked
        Station station = actor.getSectorLocation().getStation();
        Module moduleObj = station.getModule(module);

        if (moduleObj == null)
        {
            return Station.hasBaseModule(module) ? station + " will not accept a module of this type." :
                    "The specified module does not exist.";
        }

        if (!station.sells(moduleObj))
        {
            return station + " will not accept a module of this type.";
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

        Station station = actor.getSectorLocation().getStation();
        Module moduleObj = station.getModule(module);

        actor.removeModule(moduleObj);
        actor.changeCredits(station.getFaction(), moduleObj.getPrice());

        if (actor.isPlayer())
        {
            playSoundEffect(SOUND_EFFECT);
        }
        return null;
    }
}
