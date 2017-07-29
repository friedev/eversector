package boldorf.eversector.entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 */
public class Battle
{
    private List<Ship> attackers;
    private List<Ship> defenders;
    private List<Ship> destroyed;
    
    public Battle(List<Ship> attackers, List<Ship> defenders)
    {
        this.attackers = attackers;
        this.defenders = defenders;
        this.destroyed = new LinkedList<>();
    }
    
    public Battle(Ship attacker, Ship defender)
    {
        attackers = new LinkedList<>();
        attackers.add(attacker);
        defenders = new LinkedList<>();
        defenders.add(defender);
        destroyed = new LinkedList<>();
    }
    
    public List<Ship> getAttackers()
        {return attackers;}
    
    public List<Ship> getDefenders()
        {return defenders;}
    
    public List<Ship> getDestroyed()
        {return destroyed;}
    
    public List<Ship> getShips()
    {
        List<Ship> ships = new ArrayList<>(attackers.size() + defenders.size());
        ships.addAll(attackers);
        ships.addAll(defenders);
        return ships;
    }
    
    public List<Ship> getAllies(Ship ship)
    {
        List<Ship> allies = new LinkedList<>();
        allies.addAll(attackers.contains(ship) ? attackers : defenders);
        allies.remove(ship);
        return allies;
    }
    
    public List<Ship> getEnemies(Ship ship)
        {return attackers.contains(ship) ? defenders : attackers;}
    
    public boolean continues()
        {return !attackers.isEmpty() && !defenders.isEmpty();}
    
    /**
     * Processes a turn of the battle.
     * @return true if the battle will continue after this turn
     */
    public boolean processTurn()
    {
        if (!continues())
            return false;
        
        boolean attackMade = false;
        int size = Math.max(attackers.size(), defenders.size());
        for (int i = 0; i < size; i++)
        {
            if (attackers.size() >= i + 1 && attackers.get(i).getAI() != null)
            {
                attackMade = attackMade ||
                        attackers.get(i).getAI().performBattleAction();
            }
            
            if (defenders.size() >= i + 1 && defenders.get(i).getAI() != null)
            {
                attackMade = attackMade ||
                        defenders.get(i).getAI().performBattleAction();
            }
        }
        
        for (Ship ship: getShips())
        {
            if (ship.isDestroyed())
            {
                // Duplicate remove done to avoid concurrent modification
                attackers.remove(ship);
                defenders.remove(ship);
                destroyed.add(ship);
            }
        }
        
        return attackMade && continues();
    }
    
    public void processBattle()
    {
        while (continues())
            if (!processTurn())
                break;
        
        distributeLoot();
        endBattle();
    }
    
    public void distributeLoot()
    {
        List<Ship> winners = getShips();
        
        for (int i = 0; i < destroyed.size(); i++)
            winners.get(i % winners.size()).loot(destroyed.get(i));
    }
    
    public void endBattle()
    {
        for (Ship ship: getShips())
            if (ship.isInBattle())
                ship.setLocation(ship.getBattleLocation().leaveBattle());
        
        attackers.clear();
        defenders.clear();
        destroyed.clear();
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