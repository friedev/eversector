package boldorf.eversector.ships;

import static boldorf.eversector.Main.rng;
import boldorf.eversector.locations.SectorLocation;
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
    private List<Ship> fleeing;
    private List<Ship> destroyed;
    
    public Battle(List<Ship> attackers, List<Ship> defenders)
    {
        this.attackers = attackers;
        this.defenders = defenders;
        this.fleeing   = new LinkedList<>(); 
        this.destroyed = new LinkedList<>();
    }
    
    public Battle(Ship attacker, Ship defender)
    {
        attackers = new LinkedList<>();
        attackers.add(attacker);
        defenders = new LinkedList<>();
        defenders.add(defender);
        fleeing = new LinkedList<>();
        destroyed = new LinkedList<>();
    }
    
    public List<Ship> getAttackers()
        {return attackers;}
    
    public List<Ship> getDefenders()
        {return defenders;}
    
    public List<Ship> getFleeing()
        {return fleeing;}
    
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
    
    public boolean processAttacks()
    {
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
                fleeing.remove(ship);
                destroyed.add(ship);
            }
        }
        
        return attackMade;
    }
    
    public List<Ship> getPursuers(Ship ship)
    {
        List<Ship> pursuing = new LinkedList<>();

        for (Ship enemy: getEnemies(ship))
        {
            if (enemy.getAI() != null && !fleeing.contains(enemy) &&
                    enemy.getAI().pursue())
            {
                pursuing.add(enemy);
            }
        }
        
        return pursuing;
    }
    
    public void processEscape(Ship ship, List<Ship> pursuing)
    {
        if (!fleeing.contains(ship))
            return;
        
        SectorLocation destination = ship.getSectorLocation();

        if (!ship.isCloaked())
        {
            if (rng.nextBoolean())
            {
                SectorLocation test = destination.raiseOrbit();
                destination = test == null ?
                        destination.lowerOrbit() : test;
            }
            else
            {
                SectorLocation test = destination.lowerOrbit();
                destination = test == null ?
                        destination.raiseOrbit() : test;
            }

            if (pursuing.isEmpty())
            {
                ship.addPlayerMessage("You successfully flee the battle.");
            }
            else
            {
                List<Ship> defenderList = new LinkedList<>();
                defenderList.add(ship);
                Battle newBattle = new Battle(pursuing, defenderList);
                destination = destination.joinBattle(newBattle);

                for (Ship pursuer: pursuing)
                {
                    pursuer.setLocation(destination);
                    attackers.remove(pursuer);
                    defenders.remove(pursuer);
                }
                
                ship.addPlayerMessage("You have been pursued.");
            }
        }
        else
        {
            ship.addPlayerMessage("You escape cloaked and undetected.");
        }

        ship.setLocation(destination);
        attackers.remove(ship);
        defenders.remove(ship);
    }
    
    public void processEscape(Ship ship)
        {processEscape(ship, getPursuers(ship));}
    
    public void processEscapes()
    {
        for (Ship ship: new ArrayList<>(fleeing))
            processEscape(ship);
    }
    
    public void distributeLoot()
    {
        List<Ship> winners = getShips();
        
        if (winners == null || winners.isEmpty())
            return;
        
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
        fleeing.clear();
        destroyed.clear();
    }
    
    public void processBattle()
    {
        while (continues())
        {
            if (!processAttacks() || !continues())
                break;
            processEscapes();
        }
        
        distributeLoot();
        endBattle();
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