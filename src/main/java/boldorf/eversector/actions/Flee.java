package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Resource;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.addMessage;
import static boldorf.eversector.Main.playSoundEffect;

public class Flee implements Action
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

        if (!actor.isInBattle())
        {
            return "You must be in a battle to flee.";
        }

        if (actor.getBattleLocation().getBattle().getSurrendered().contains(actor))
        {
            return "You may not flee after surrendering";
        }

        return actor.validateResources(RESOURCE, COST, "flee");
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        Battle battle = actor.getBattleLocation().getBattle();
        Ship player = actor.getLocation().getGalaxy().getPlayer();
        if (player != null && actor != player && battle.getShips().contains(player))
        {
            addMessage(actor + " flees the battle.");
        }

        actor.getResource(RESOURCE).changeAmount(-COST);
        actor.getBattleLocation().getBattle().getFleeing().add(actor);
        actor.playPlayerSound(SOUND_EFFECT);
        return null;
    }
}
