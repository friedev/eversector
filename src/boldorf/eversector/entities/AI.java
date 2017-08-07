package boldorf.eversector.entities;

import boldorf.eversector.entities.locations.BattleLocation;
import boldorf.eversector.entities.locations.Location;
import boldorf.eversector.entities.locations.PlanetLocation;
import boldorf.eversector.entities.locations.SectorLocation;
import boldorf.eversector.entities.locations.StationLocation;
import boldorf.eversector.items.Module;
import boldorf.eversector.map.Map;
import boldorf.eversector.map.Sector;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Paths;
import boldorf.eversector.storage.Reputations;
import boldorf.eversector.storage.Resources;
import boldorf.util.Utility;
import java.util.List;
import squidpony.squidmath.Coord;

/**
 * 
 */
public class AI
{
    private Ship ship;
    private Location destination;
    
    public AI(Ship ship)
        {this.ship = ship;}
    
    public Ship getShip()
        {return ship;}
    
    public void act()
    {
        if (ship.isInBattle())
            return;
        
        if (ship.isShielded())
            ship.toggleActivation(Actions.SHIELD);
        
        if (ship.isCloaked())
            ship.toggleActivation(Actions.CLOAK);
        
        if (destination != null && ship.getLocation().equals(destination))
        {
            if (ship.isDocked())
            {
                if (performStationAction())
                    return;
            }
            else if (ship.isLanded())
            {
                if (performPlanetAction())
                    return;
            }
            else if (ship.isInSector())
            {
                if (performSectorAction())
                    return;
                
                if (ship.getSectorLocation().isPlanet() &&
                        ship.getSectorLocation().getPlanet().getType()
                                .canMineFromOrbit() && ship.mine())
                    return;
            }
        }
        
        if (!destinationIsValid())
            updateDestination();
        
        if (!seekDestination())
            performEmergencyAction();
    }
    
    private boolean performSectorAction()
    {
        Planet planet = ship.getSectorLocation().getPlanet();
        if (planet != null && planet.getType().canMineFromOrbit() &&
                ship.mine())
            return true;
        
        if (!ship.hasWeapons())
            return false;
        
        List<Ship> others = ship.getSectorLocation().getShips();
        if (others.isEmpty())
            return false;
        
        for (Ship other: others)
            if (ship.isHostile(other.getFaction()) &&
                    ship.startBattle(other) != null)
                return true;
        
        return false;
    }
    
    private boolean performPlanetAction()
        {return ship.claim(false) || ship.mine();}
    
    private boolean performStationAction()
    {
        sellDuplicates();
        buyResources();
        if (ship.claim(false))
            return true;
        buyItems(); // TODO replace with a loop
        buyExpanders();
        return false;
    }
    
    private void sellDuplicates()
    {
        boolean selling = true;
        while (selling && !ship.getCargo().isEmpty())
        {
            selling = false;
            for (Module module: ship.getCargo())
            {
                // Must never update selling without checking sellModule()
                // If a station will not accept a module, this loops forever
                if (module != null && ship.sellModule(module.getName()))
                {
                    selling = true;
                    break;
                }
            }
        }
    }
    
    private void buyResources()
    {
        ship.buyResource(Resources.ORE, -ship.getMaxSellAmount(Resources.ORE));
        ship.buyResource(Resources.FUEL, ship.getMaxBuyAmount(Resources.FUEL));
        ship.buyResource(Resources.HULL, ship.getMaxBuyAmount(Resources.HULL));
        ship.buyResource(Resources.ENERGY,
                ship.getMaxBuyAmount(Resources.ENERGY));
    }
    
    private void buyItems()
    {
        if (!ship.hasModule(Actions.PULSE))
            ship.buyModule(Actions.PULSE);
        
        if (!ship.hasModule(Actions.TORPEDO))
            ship.buyModule(Actions.TORPEDO);
        
        if (!ship.hasModule(Actions.LASER))
            ship.buyModule(Actions.LASER);
        
        if (!ship.hasModule(Actions.SHIELD))
            ship.buyModule(Actions.SHIELD);
        
        if (!ship.hasModule(Actions.WARP))
            ship.buyModule(Actions.WARP);
        
        if (!ship.hasModule(Actions.REFINE))
            ship.buyModule(Actions.REFINE);
        
        if (!ship.hasModule(Actions.SOLAR))
            ship.buyModule(Actions.SOLAR);
    }
    
    private void buyExpanders()
    {
        int tanks  = ship.getResource(Resources.FUEL  ).getNExpanders();
        int bays   = ship.getResource(Resources.ORE   ).getNExpanders();
        int cells  = ship.getResource(Resources.ENERGY).getNExpanders();
        int plates = ship.getResource(Resources.HULL  ).getNExpanders();
        
        // If there are the least fuel tanks, buy more
        int maxTanks = ship.getMaxBuyAmount(ship.getExpander(
                Resources.FUEL_EXPANDER));
        if (maxTanks > 0 && tanks < bays && tanks < cells && tanks < plates)
            ship.buyResource(Resources.FUEL_EXPANDER, maxTanks);
        
        // If there are fewer cargo bays than cells and plates, buy more
        int maxBays = ship.getMaxBuyAmount(ship.getExpander(
                Resources.ORE_EXPANDER));
        if (maxBays > 0 && bays < cells && bays < plates)
            ship.buyResource(Resources.ORE_EXPANDER, maxBays);
        
        // If there are the fewer energy cells than hull plates, buy more
        int maxCells = ship.getMaxBuyAmount(ship.getExpander(
                Resources.ENERGY_EXPANDER));
        if (maxCells > 0 && cells < plates)
            ship.buyResource(Resources.ENERGY_EXPANDER, maxCells);
        
        // Buy hull frames by default
        ship.buyResource(Resources.HULL_EXPANDER, ship.getMaxBuyAmount(
                ship.getExpander(Resources.HULL_EXPANDER)));
    }
    
    private void updateDestination()
    {
        if (ship.isLanded() || (ship.isInSector() &&
                ship.getSectorLocation().isPlanet() &&
                ship.getSectorLocation().getPlanet().getType()
                        .canMineFromOrbit()))
        {
            destination = findClosestStation();
            return;
        }
        
        destination = findClosestMiningDestination();
    }
    
    private SectorLocation findClosestMiningDestination()
    {
        if (ship.isInSector())
        {
            // TODO check for regions without ore
            if (ship.isLanded())
                return ship.getPlanetLocation();
            
            SectorLocation current = getPlanetDestination(
                    ship.getSectorLocation().getPlanet());
            if (current != null)
                return current;
            
            Sector sector = ship.getLocation().getSector();
            int orbit = ship.getSectorLocation().getOrbit();
            for (int offset = 1; offset < sector.getOrbits(); offset++)
            {
                SectorLocation minusOffset = getPlanetDestination(
                        sector.getPlanetAt(orbit - offset));
                if (minusOffset != null)
                    return minusOffset;
                
                SectorLocation plusOffset = getPlanetDestination(
                        sector.getPlanetAt(orbit + offset));
                if (plusOffset != null)
                    return plusOffset;
            }
        }
        
        List<Coord> fov = ship.getFOV();
        fov.sort(Utility.createDistanceComparator(
                ship.getLocation().getCoord()));
        Map map = ship.getLocation().getMap();
        for (Coord coord: fov)
        {
            if (!map.contains(coord))
                continue;
            
            Sector sector = map.sectorAt(coord);
            if (sector.isEmpty())
                continue;
            
            for (int orbit = sector.getOrbits(); orbit > 0; orbit--)
            {
                SectorLocation planetLocation = getPlanetDestination(
                        sector.getPlanetAt(orbit));
                if (planetLocation != null)
                    return planetLocation;
            }
        }
        
        return null;
    }
    
    private SectorLocation getPlanetDestination(Planet planet)
    {
        if (planet == null)
            return null;
        
        if (planet.getType().canMineFromOrbit())
            return planet.getLocation();

        if (planet.getType().canMine() && planet.getType().canLandOn())
        {
            Region oreRegion = planet.getRandomOreRegion();
            return oreRegion == null ? null : oreRegion.getLocation();
        }
        
        return null;
    }
    
    private StationLocation findClosestStation()
    {
        if (ship.isInSector())
        {
            if (ship.isDocked())
                return ship.getStationLocation();
            
            if (ship.getSectorLocation().isStation())
                return ship.getSectorLocation().dock();
            
            Sector sector = ship.getLocation().getSector();
            if (sector.hasStations())
            {
                int orbit = ship.getSectorLocation().getOrbit();
                for (int offset = 1; offset < sector.getOrbits(); offset++)
                {
                    StationLocation minusOffset = getStationDestination(
                            sector.getStationAt(orbit - offset));
                    if (minusOffset != null)
                        return minusOffset;
                    
                    StationLocation plusOffset = getStationDestination(
                            sector.getStationAt(orbit + offset));
                    if (plusOffset != null)
                        return plusOffset;
                }
            }
        }
        
        
        List<Coord> fov = ship.getFOV();
        fov.sort(Utility.createDistanceComparator(
                ship.getLocation().getCoord()));
        Map map = ship.getLocation().getMap();
        for (Coord coord: fov)
        {
            if (!map.contains(coord))
                continue;
            
            Sector sector = map.sectorAt(coord);
            if (!sector.hasStations())
                continue;
            
            for (int orbit = sector.getOrbits(); orbit > 0; orbit--)
            {
                StationLocation stationLocation = getStationDestination(
                        sector.getStationAt(orbit));
                if (stationLocation != null)
                    return stationLocation;
            }
        }
        
        return null;
    }
    
    private StationLocation getStationDestination(Station station)
    {
        if (station == null)
            return null;
        
        if (!ship.isHostile(station.getFaction()) ||
                ship.getCredits() >= Station.CLAIM_COST)
            return station.getLocation().dock();
        return null;
    }
    
    private boolean destinationIsValid()
    {
        return !(destination == null ||
                ship.getLocation().equals(destination) ||
                destination instanceof BattleLocation);
    }
    
    private boolean seekDestination()
    {
        if (!destinationIsValid())
            return false;
        
        if (ship.isDocked())
            return ship.undock();
        
        if (ship.isLanded())
        {
            if (destination instanceof PlanetLocation &&
                    ship.getSectorLocation().getPlanet() ==
                    ((SectorLocation) destination).getPlanet())
            {
                return ship.relocate(Utility.toGoToCardinal(
                        ship.getPlanetLocation().getRegionCoord(),
                        ((PlanetLocation) destination).getRegionCoord()));
            }
            
            return ship.takeoff();
        }
        
        if (ship.isInSector())
        {
            if (destination instanceof SectorLocation &&
                    ship.getLocation().getSector() == destination.getSector())
            {
                if (ship.getSectorLocation().getOrbit() ==
                        ((SectorLocation) destination).getOrbit())
                {
                    if (destination instanceof StationLocation)
                        return ship.dock();
                    
                    if (destination instanceof PlanetLocation)
                    {
                        return ship.land(((PlanetLocation) destination)
                                .getRegionCoord());
                    }
                }
                
                return ship.orbit(ship.getSectorLocation().getOrbit() <
                        ((SectorLocation) destination).getOrbit());
            }
            
            return ship.orbit(true);
        }
        
        if (ship.getLocation().getCoord().equals(destination.getCoord()))
            return ship.enter();
        
        if (!destination.getCoord().isAdjacent(ship.getLocation().getCoord()) &&
                ship.warpTo(destination.getCoord()))
            return true;
        
        return ship.burn(Utility.toGoToCardinal(ship.getLocation().getCoord(),
                destination.getCoord()));
    }
    
    private void performEmergencyAction()
    {
        if (ship.refine())
            return;
        else if (ship.canDistress())
            ship.distress();
        else
            ship.destroy(false);
    }
    
    public boolean joinBattle(Battle battle)
    {
        if (!willAttack() || !ship.isOrbital())
            return false;
        
        int attackerFriendliness = 0;
        int defenderFriendliness = 0;
        
        for (Ship attacker: battle.getAttackers())
            attackerFriendliness += getFriendliness(attacker);
        
        for (Ship defender: battle.getDefenders())
            defenderFriendliness += getFriendliness(defender);
        
        if (attackerFriendliness > defenderFriendliness)
            battle.getAttackers().add(ship);
        else
            battle.getDefenders().add(ship);
        
        ship.setLocation(ship.getSectorLocation().joinBattle(battle));
        return true;
    }
    
    private int getFriendliness(Ship other)
    {
        if (!other.isAligned())
            return 0;

        if (ship.getFaction() == other.getFaction())
            return 4;

        switch (other.getFaction().getRelationship(ship.getFaction()))
        {
            case ALLIANCE: return 3;
            case PEACE: return 1;
        }
        
        return 0;
    }
    
    /**
     * Performs an action in battle.
     * @return true if the ship attacked, false if not
     */
    public boolean performBattleAction()
    {
        if (!ship.isInBattle())
            return false;
        
        Battle battle = ship.getBattleLocation().getBattle();
        List<Ship> enemies = battle.getEnemies(ship);
        Ship target = null;
        for (Ship enemy: enemies)
        {
            if (!battle.getFleeing().contains(enemy) && (target == null ||
                    target.getAmountOf(Resources.HULL) <
                    enemy.getAmountOf(Resources.HULL)))
            {
                target = enemy;
            }
        }
        
        if (target == null)
            return false;
        
        if (!willAttack())
        {
            if (ship.validateResources(Actions.FLEE, "flee"))
            {
                ship.changeResourceBy(Actions.FLEE);
                battle.getFleeing().add(ship);
                target.addPlayerColorMessage(ship.toColorString()
                        .add(" attempts to flee."));
                return false;
            }
            
            ship.destroy(true);
            return false;
        }
        
        if (!ship.isShielded() && ship.toggleActivation(Actions.SHIELD))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" activates a shield."));
            // No return since shielding is a free action
        }
        
        if (ship.canFire(Actions.PULSE, target))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" fires a pulse beam."));
            target.playPlayerSound(Paths.PULSE);
            ship.fire(Actions.PULSE, target);
        }
        else if (ship.canFire(ship.getWeapon(Actions.TORPEDO.getName()), target))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" fires a torpedo."));
            target.playPlayerSound(Paths.TORPEDO);
            ship.fire(Actions.TORPEDO, target);
        }
        else if (ship.canFire(Actions.LASER, target))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" fires a laser."));
            target.playPlayerSound(Paths.LASER);
            ship.fire(Actions.LASER, target);
        }
        else
        {
            // The code above under !willAttack() is borrowed from here
            if (ship.validateResources(Actions.FLEE, "flee"))
            {
                if (!ship.isCloaked() && ship.toggleActivation(Actions.CLOAK))
                {
                    target.addPlayerColorMessage(ship.toColorString()
                            .add(" activates a cloaking device."));
                    // No return since cloaking is a free action
                }

                target.addPlayerColorMessage(ship.toColorString()
                        .add(" attempts to flee."));
                return false;
            }
            
            ship.destroy(true);
            return false;
        }
        
        if (target.isDestroyed())
        {
            if (ship.isPassive(target))
            {
                ship.changeReputation(ship.getFaction(), Reputations.KILL_ALLY);
            }
            else
            {
                ship.changeReputation(ship.getFaction(),
                        Reputations.KILL_ENEMY);
            }
            
            ship.changeReputation(target.getFaction(), Reputations.KILL_ALLY);
        }
        
        return true;
    }
    
    public boolean pursue()
    {
        Battle battle = ship.getBattleLocation().getBattle();
        return willAttack() && battle.getEnemies(ship).size() -
                battle.getFleeing().size() < 1 &&
                ship.changeResourceBy(Actions.PURSUE);
    }
    
    /**
     * Returns true if the NPC is willing to enter a fight.
     * @return true if this ship will engage in any fights, based on its hull
     * strength and weaponry
     */
    public boolean willAttack()
    {
        return ship.hasWeapons() && ship.getAmountOf(Resources.HULL) >=
                ship.getCapOf(Resources.HULL) / 2;
    }
    
    public Ship vote(List<Ship> candidates)
    {
        int[] preferences = new int[candidates.size()];
        
        for (int i = 0; i < candidates.size(); i++)
        {
            Ship candidate = candidates.get(i);
            preferences[i] = 0;
            
            if (ship.getHigherLevel() != null &&
                    ship.getHigherLevel().equals(candidate.getHigherLevel()))
                preferences[i] += 2;
            
            if (ship.getLocation().getCoord().equals(
                    candidate.getLocation().getCoord()))
            {
                preferences[i] += 3;
            }
            else if (ship.getLocation().getCoord()
                    .isAdjacent(candidate.getLocation().getCoord()))
            {
                preferences[i]++;
            }
            
            if (candidate.calculateShipValue() > ship.calculateShipValue())
                preferences[i]++;
        }
        
        int highestPreference = 0;
        int index = 0;
        
        for (int i = 0; i < preferences.length; i++)
        {
            // Using > instead of >= biases the ship's vote towards the
            // candidate with the highest reputation
            if (preferences[i] > highestPreference)
            {
                highestPreference = preferences[i];
                index = i;
            }
        }
        
        return candidates.get(index);
    }
}