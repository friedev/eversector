package boldorf.eversector.actions;

import boldorf.eversector.items.Module;
import boldorf.eversector.items.Resource;
import boldorf.eversector.ships.Ship;
import boldorf.util.Utility;

import static boldorf.eversector.Main.rng;
import static boldorf.eversector.ships.Ship.LOOT_MODIFIER;

public class Loot implements Action
{
    private final Ship looting;

    public Loot(Ship looting)
    {
        this.looting = looting;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (actor == looting)
        {
            return "You cannot loot yourself.";
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

        looting.destroy(false);
        int salvagedCredits = looting.getCredits() / LOOT_MODIFIER;
        if (salvagedCredits > 0)
        {
            actor.changeCredits(salvagedCredits);
            actor.addPlayerMessage("Salvaged " + salvagedCredits + " credits.");
        }

        for (Module module : looting.getModules())
        {
            if (module != null && rng.nextDouble() <= (1.0 / (double) LOOT_MODIFIER))
            {
                actor.addModule(module);
                actor.addPlayerMessage("Salvaged " + Utility.addArticle(module.getName()) + ".");
            }
        }

        for (Resource resource : looting.getResources())
        {
            if (resource != null)
            {
                Resource yourResource = actor.getResource(resource.getName());
                int nExpanders = resource.getNExpanders() / LOOT_MODIFIER;
                yourResource.expand(Math.min(Ship.MAX_EXPANDERS - yourResource.getNExpanders(), nExpanders));

                if (nExpanders > 0)
                {
                    actor.addPlayerMessage("Salvaged " + nExpanders + " " +
                                           Utility.makePlural(resource.getExpander().getName().toLowerCase(),
                                                   nExpanders) + ".");
                }

                int oldAmount = yourResource.getAmount();
                yourResource.changeAmountWithDiscard(resource.getAmount() / LOOT_MODIFIER);

                int amountIncrease = yourResource.getAmount() - oldAmount;

                if (amountIncrease > 0)
                {
                    actor.addPlayerMessage("Salvaged " + amountIncrease + " " + resource.getName().toLowerCase() + ".");
                }
            }
        }

        return null;
    }
}
