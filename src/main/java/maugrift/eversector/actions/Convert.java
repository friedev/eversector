package maugrift.eversector.actions;

import maugrift.eversector.faction.Faction;
import maugrift.eversector.ships.Reputation;
import maugrift.eversector.ships.Ship;

public class Convert implements Action
{
    private final Ship converting;

    public Convert(Ship converting)
    {
        this.converting = converting;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (actor == converting)
        {
            return "You cannot convert yourself.";
        }

        if (!actor.isAligned())
        {
            return "You must be part of a faction to convert ships.";
        }

        if (actor.getFaction() == converting.getFaction())
        {
            return converting + " is already a member of the " + actor.getFaction() + ".";
        }

        if (converting.isPlayer())
        {
            // Converting the player must be done through the ConvertScreen
            return "You cannot be converted.";
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

        Faction actorFaction = actor.getFaction();
        Faction oldFaction = converting.getFaction();

        if (converting.isAligned())
        {
            converting.leaveFaction();
        }

        converting.joinFaction(actorFaction);

        actor.changeReputation(actorFaction, Reputation.CONVERT);
        actor.changeReputation(oldFaction, -Reputation.CONVERT);

        converting.changeReputation(actorFaction, Reputation.CONVERT);
        converting.changeReputation(oldFaction, -Reputation.CONVERT);

        converting.addPlayerColorMessage(actor.toColorString()
                                              .add(" has converted you to the ")
                                              .add(actorFaction)
                                              .add("."));
        return null;
    }
}
