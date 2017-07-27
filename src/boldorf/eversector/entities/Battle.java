package boldorf.eversector.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class Battle
{
    private List<Ship> attackers;
    private List<Ship> defenders;
    
    public Battle(List<Ship> attackers, List<Ship> defenders)
    {
        this.attackers = attackers;
        this.defenders = defenders;
    }
    
    public Battle(Ship attacker, Ship defender)
    {
        attackers = new ArrayList<>();
        attackers.add(attacker);
        defenders = new ArrayList<>();
        defenders.add(defender);
    }
    
    public List<Ship> getAttackers()
        {return attackers;}
    
    public List<Ship> getDefenders()
        {return defenders;}
    
    public List<Ship> getShips()
    {
        List<Ship> ships = new ArrayList<>(attackers.size() + defenders.size());
        ships.addAll(attackers);
        ships.addAll(defenders);
        return ships;
    }
    
    public List<Ship> getAllies(Ship ship)
        {return attackers.contains(ship) ? attackers : defenders;}
    
    public List<Ship> getEnemies(Ship ship)
        {return attackers.contains(ship) ? defenders : attackers;}
    
    public boolean processTurn()
    {
        if (attackers.isEmpty() || defenders.isEmpty())
            return false;
        
        int size = Math.max(attackers.size(), defenders.size());
        for (int i = 0; i < size; i++)
        {
            if (attackers.size() >= i + 1 && attackers.get(i).getAI() != null)
                attackers.get(i).getAI().performBattleAction();
            if (defenders.size() >= i + 1 && defenders.get(i).getAI() != null)
                defenders.get(i).getAI().performBattleAction();
        }
        
        for (Ship attacker: attackers)
            if (attacker.isDestroyed())
                attackers.remove(attacker);
        
        for (Ship defender: defenders)
            if (defender.isDestroyed())
                defenders.remove(defender);
        
        return true;
    }
    
    public void processBattle()
    {
        while (!attackers.isEmpty() && !defenders.isEmpty())
            if (!processTurn())
                break;
    }
    
    /*
    public void controlAIBattle(Ship ship)
    {
        do
        {
            // If this ship chooses to flee but is pursued
            if (!attack(ship))
            {
                if (changeResourceBy(Actions.FLEE) &&
                        (!ship.willPursue(this) ||
                         !ship.changeResourceBy(Actions.PURSUE)))
                {
                    // If the other ship is powerful enough, they will convert
                    // this ship
                    if (ship.willConvert() && ship.convert(this))
                        return;
                    
                    // If the conversion fails, the fight continues
                }
                else
                {
                    break;
                }
            }
            
            // If the other ship flees but is pursued
            if (!ship.attack(this))
            {
                if (ship.changeResourceBy(Actions.PURSUE) &&
                        (!willPursue(ship) || !changeResourceBy(Actions.FLEE)))
                {
                    if (willConvert() && convert(ship))
                        return;
                }
            }
        } while (!isDestroyed() && !ship.isDestroyed());
        
        // If this ship is destroyed or cannot flee while the other ship lives
        if (isDestroyed() || (!validateResources(Actions.FLEE, "flee") &&
                !ship.isDestroyed()))
        {
            if (isLeader() && ship.isInFaction(faction))
            {
                faction.setLeader(ship);
                faction.addNews(ship + " has defeated our leader, " + toString()
                        + ", and has wrested control of the faction.");
            }
            else
            {
                if (ship.isPassive(this))
                    ship.changeReputation(ship.faction, Reputations.KILL_ALLY);
                else
                    ship.changeReputation(ship.faction, Reputations.KILL_ENEMY);
                
                ship.changeReputation(faction, Reputations.KILL_ALLY);
                
                if (isLeader())
                {
                    faction.addNews(ship + " of the " + ship.faction
                            + " has destroyed our leader, " + toString() + ".");
                }
                
                ship.loot(this);

                if (!isDestroyed())
                    destroy(false);
            }
        }
        else
        {
            if (ship.isLeader() && isInFaction(faction))
            {
                ship.faction.setLeader(this);
                ship.faction.addNews(toString() + " has defeated our leader, "
                        + ship + ", and has wrested control of the faction.");
            }
            else
            {
                if (isPassive(ship))
                    changeReputation(faction, Reputations.KILL_ALLY);
                else
                    changeReputation(faction, Reputations.KILL_ENEMY);
                
                changeReputation(ship.faction, Reputations.KILL_ALLY);
                
                if (ship.isLeader())
                {
                    ship.faction.addNews(toString() + " of the " + faction
                            + " has destroyed our leader, " + ship.toString()
                            + ".");
                }
                
                loot(ship);

                if (!ship.isDestroyed())
                    ship.destroy(false);
            }
        }
    }
    */
}