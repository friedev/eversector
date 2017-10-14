package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.items.Resource;
import maugrift.eversector.map.Ore;
import maugrift.eversector.map.Planet;
import maugrift.eversector.map.Region;
import maugrift.eversector.ships.Reputation;
import maugrift.eversector.ships.Ship;

import static maugrift.eversector.Main.addMessage;
import static maugrift.eversector.Main.rng;

public class Mine implements Action
{
    public static final String RESOURCE = Resource.ENERGY;
    public static final int COST = 3;
    public static final String SOUND_EFFECT = Paths.MINE;

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (!actor.isInSector())
        {
            return "You must be in a sector to mine.";
        }

        Planet planet = actor.getSectorLocation().getPlanet();

        if (planet == null)
        {
            return "There is no planet here to mine from.";
        }

        if (!actor.isLanded() && !planet.getType().canMineFromOrbit())
        {
            return "You must be landed here to mine for ore.";
        }

        if (actor.isLanded())
        {
            Region region = actor.getPlanetLocation().getRegion();
            if (!region.hasOre())
            {
                return "There is no ore to mine in the " + region.toString().toLowerCase() + ".";
            }
        }

        Resource ore = actor.getResource(Resource.ORE);

        if (ore.isFull())
        {
            return "Ore storage full; cannot acquire more.";
        }

        return actor.validateResources(RESOURCE, COST, "initiate mining operation");
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }



        Ore ore = actor.isLanded() ? actor.getPlanetLocation().getRegion().getOre() :
                actor.getLocation().getGalaxy().getRandomOre();

        int discard = actor.getResource(Resource.ORE).changeAmountWithDiscard(ore.getDensity());
        actor.getResource(RESOURCE).changeAmount(-COST);

        if (actor.isLanded())
        {
            Region region = actor.getPlanetLocation().getRegion();
            region.extractOre(1);
            if (!region.hasOre())
            {
                actor.addPlayerMessage("You have mined the " + region + " dry.");
                actor.changeGlobalReputation(Reputation.MINE_DRY);
            }
        }
        else if (rng.nextBoolean())
        {
            // Chance of taking damage if mining from an asteroid belt
            actor.damage(Planet.ASTEROID_DAMAGE, false);
            actor.addPlayerMessage("Collided with an asteroid, dealing " + Planet.ASTEROID_DAMAGE + " damage.");
        }

        if (actor.isPlayer())
        {
            addMessage("Extracted 1 unit of " + ore.getName().toLowerCase() + ".");

            if (discard > 0)
            {
                addMessage("Maximum ore capacity exceeded; " + discard + " units discarded.");
            }
        }

        actor.changeGlobalReputation(Reputation.MINE);
        actor.playPlayerSound(SOUND_EFFECT);
        return null;
    }
}
