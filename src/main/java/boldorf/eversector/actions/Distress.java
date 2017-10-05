package boldorf.eversector.actions;

import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.Paths;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.ships.Reputation;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.ships.Ship.DISTRESS_CREDITS;

public class Distress implements Action
{
    public static final String SOUND_EFFECT = Paths.DISTRESS;

    private final Faction responder;

    public Distress(Faction responder)
    {
        this.responder = responder;
    }

    public Distress()
    {
        this.responder = null;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (responder == null)
        {
            return "No factions have agreed to help you.";
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

        if (responder != actor.getFaction())
        {
            actor.joinFaction(responder);
        }

        actor.addPlayerColorMessage(new ColorString("The ").add(responder)
                                                           .add(" responds and warps supplies to your location."));
        actor.changeCredits(responder, DISTRESS_CREDITS);
        responder.changeEconomy(-actor.refill());
        actor.changeReputation(responder, Reputation.DISTRESS);
        actor.playPlayerSound(SOUND_EFFECT);
        return null;
    }
}
