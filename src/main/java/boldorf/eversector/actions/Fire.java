package boldorf.eversector.actions;

import boldorf.eversector.items.Resource;
import boldorf.eversector.items.Weapon;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.addColorMessage;
import static boldorf.eversector.Main.addMessage;
import static boldorf.eversector.Main.playSoundEffect;

public class Fire implements Action
{
    private final String weapon;
    private final Ship target;

    public Fire(String weapon, Ship target)
    {
        this.weapon = weapon;
        this.target = target;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        if (actor == target)
        {
            return "You cannot attack yourself.";
        }

        if (weapon == null || !Station.hasBaseWeapon(weapon))
        {
            return "The specified weapon does not exist.";
        }

        if (target == null)
        {
            return "The specified ship was not found.";
        }

        if (!actor.isInBattle())
        {
            return "You must be in a battle to fire a weapon.";
        }

        if (actor.getBattleLocation().getBattle().getSurrendered().contains(actor))
        {
            return "You may not attack after surrendering";
        }

        String validateModule = actor.validateModule(weapon, "fire");
        if (validateModule != null)
        {
            return validateModule;
        }

        Weapon weaponObj = actor.getWeapon(weapon);
        return actor.validateResources(weaponObj.getActionResource(), weaponObj.getActionCost(), "fire");
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        Weapon weaponObj = actor.getWeapon(weapon);

        if (actor.isPlayer())
        {
            if (target.isShielded() && Resource.ENERGY.equals(weaponObj.getActionResource()))
            {
                addMessage("Attack diminished by enemy shield.");
            }
            else
            {
                addMessage("Attack successful; hit confirmed.");
            }
        }
        else
        {
            Battle battle = actor.getBattleLocation().getBattle();
            Ship player = actor.getLocation().getGalaxy().getPlayer();
            if (player != null && battle.getShips().contains(player))
            {
                addColorMessage(actor.toColorString()
                                .add(" fires a pulse beam at " + (target == player ? "you" : target.toString()) + "."));
            }
        }

        target.damageWith(weaponObj, false);
        actor.getResource(weaponObj.getActionResource()).changeAmount(-weaponObj.getActionCost());
        actor.playPlayerSound(weaponObj.getSoundEffect());
        return null;
    }

    public Ship getTarget()
    {
        return target;
    }
}
