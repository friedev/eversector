package boldorf.eversector.entities;

import boldorf.eversector.entities.locations.BattleLocation;
import boldorf.eversector.entities.locations.Location;
import boldorf.eversector.entities.locations.PlanetLocation;
import boldorf.eversector.entities.locations.SectorLocation;
import boldorf.eversector.entities.locations.StationLocation;
import boldorf.eversector.items.Module;
import boldorf.eversector.map.Sector;
import boldorf.eversector.storage.Actions;
import boldorf.eversector.storage.Paths;
import boldorf.eversector.storage.Resources;
import boldorf.util.Utility;
import java.util.List;

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
        
        if (seekDestination())
            return;
        
        updateDestination();
        
        if (!seekDestination())
            distressOrDestroy();
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
        {return ship.claim() || ship.mine();}
    
    private boolean performStationAction()
    {
        sellDuplicates();
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
    
    /** Buys an item from a list in order of importance. */
    private void buyItems()
    {
        ship.buyResource(Resources.ORE, -ship.getMaxSellAmount(Resources.ORE));
        ship.buyResource(Resources.FUEL, ship.getMaxBuyAmount(Resources.FUEL));
        ship.buyResource(Resources.HULL, ship.getMaxBuyAmount(Resources.HULL));
        ship.buyResource(Resources.ENERGY,
                ship.getMaxBuyAmount(Resources.ENERGY));
        
        if (!ship.hasModule(Actions.PULSE))
            ship.buyModule(Actions.PULSE);
        
        if (!ship.hasModule(Actions.TORPEDO))
            ship.buyModule(Actions.TORPEDO);
        
        if (!ship.hasModule(Actions.LASER))
            ship.buyModule(Actions.LASER);
        
        if (!ship.hasModule(Actions.SHIELD))
            ship.buyModule(Actions.SHIELD);
        
        if (!ship.hasModule(Actions.REFINE))
            ship.buyModule(Actions.REFINE);
    }
    
    /** Purchases the expander with the highest priority (intended for NPCs). */
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
        
        // TODO search for planets in other sectors
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
            if (Sector.STATION_SYSTEM.equals(sector.getType()))
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
        
        // TODO search for stations in other sectors
        return null;
    }
    
    private StationLocation getStationDestination(Station station)
    {
        if (station == null)
            return null;
        
        if (!ship.isHostile(station.getFaction()) ||
                ship.getCredits() >= station.getClaimCost())
            return station.getLocation().dock();
        return null;
    }
    
    private boolean seekDestination()
    {
        if (destination == null || ship.getLocation().equals(destination) ||
                destination instanceof BattleLocation)
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
                        ship.getPlanetLocation().getRegionCoords(),
                        ((PlanetLocation) destination).getRegionCoords()));
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
                                .getRegionCoords());
                    }
                }
                
                return ship.orbit(ship.getSectorLocation().getOrbit() <
                        ((SectorLocation) destination).getOrbit());
            }
            
            if (ship.escape())
                return true;
            
            return ship.orbit(true);
        }
        
        if (ship.getLocation().getCoords().equals(destination.getCoords()))
            return ship.enter();
        
        return ship.burn(Utility.toGoToCardinal(ship.getLocation().getCoords(),
                destination.getCoords()));
    }
    
    /**
     * Distress if respected enough, otherwise destroy the ship (intended for
     * NPCs).
     */
    private void distressOrDestroy()
    {
        if (ship.canDistress())
            ship.distress();
        else
            ship.destroy(false);
    }
    
    public boolean joinBattle(Battle battle)
    {
        boolean attackersHostile = false;
        boolean defendersHostile = false;
        
        for (Ship attacker: battle.getAttackers())
        {
            if (ship.isHostile(attacker.getFaction()))
            {
                attackersHostile = true;
                break;
            }
        }
        
        for (Ship defender: battle.getDefenders())
        {
            if (ship.isHostile(defender.getFaction()))
            {
                defendersHostile = true;
                break;
            }
        }
        
        if (attackersHostile == defendersHostile)
            return false;
        
        if (attackersHostile)
            battle.getDefenders().add(ship);
        else
            battle.getAttackers().add(ship);
        
        return true;
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
            if (target == null || target.getAmountOf(Resources.HULL) <
                    enemy.getAmountOf(Resources.HULL))
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
                target.addPlayerColorMessage(ship.toColorString()
                        .add(" attempts to flee."));
                return false;
            }
            
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
            return true;
        }
        
        if (ship.canFire(ship.getWeapon(Actions.TORPEDO.getName()), target))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" fires a torpedo."));
            target.playPlayerSound(Paths.TORPEDO);
            ship.fire(Actions.TORPEDO, target);
            return true;
        }
        
        if (ship.canFire(Actions.LASER, target))
        {
            target.addPlayerColorMessage(ship.toColorString()
                    .add(" fires a laser."));
            target.playPlayerSound(Paths.LASER);
            ship.fire(Actions.LASER, target);
            return true;
        }
        
        // Note that the code above under !willAttack() is borrowed from here
        
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
        
        return false;
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
            
            if (ship.getLocation().getCoords().equals(
                    candidate.getLocation().getCoords()))
            {
                preferences[i] += 3;
            }
            else if (ship.getLocation().getCoords()
                    .isAdjacent(candidate.getLocation().getCoords()))
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
    
//    {
//        // Removes shielding/cloaking if not necessary
//        if (ship.isShielded())
//            ship.toggleActivation(Actions.SHIELD);
//        
//        if (ship.isCloaked())
//            ship.toggleActivation(Actions.CLOAK);
//        
//        if (ship.isDocked())
//        {
//            buyItems();
//            ship.undock();
//            return;
//        }
//        
//        // Travel to a new sector if willing
//        if (willClaim())
//        {
//            if (destination == null || ship.getLocation().getCoords().equals(
//                    destination.getCoords()))
//            {
//                Sector possibility = ship.getLocation().getMap().sectorAt(
//                        ship.getLocation().getMap().adjacentTypeTo(
//                                Sector.STATION_SYSTEM,
//                                ship.getLocation().getCoords()));
//                
//                if (!ship.isAligned())
//                {
//                    destination = new Location(ship.getLocation().getMap(),
//                            possibility.getLocation().getCoords());
//                }
//                else if (possibility != null)
//                {
//                    String focus = ship.getFaction().getFocus().getName();
//                    RelationshipType relationship;
//                    if (possibility.getFaction() == null)
//                    {
//                        relationship = null;
//                    }
//                    else
//                    {
//                        relationship = possibility.getFaction().getRelationship(
//                                        ship.getFaction());
//                    }
//
//                    if (Focus.INVADE.getName().equals(focus) &&
//                            (relationship == null || WAR.equals(relationship)))
//                    {
//                        // If attacking and the sector is at war or disputed
//                        destination = new Location(ship.getLocation().getMap(),
//                            possibility.getLocation().getCoords());
//                    }
//                    else if (Focus.EXPAND.getName().equals(focus) &&
//                            (relationship == null ||
//                            PEACE.equals(relationship) ||
//                            ALLIANCE.equals(relationship)))
//                    {
//                        // If claiming and the sector is unclaimed or peaceful
//                        destination = new Location(ship.getLocation().getMap(),
//                            possibility.getLocation().getCoords());
//                    }
//                    else if (Focus.DEFEND.getName().equals(focus))
//                    {
//                        // Stay in the current sector
//                        destination = null;
//                    }
//                }
//            }
//            
//            if (destination != null && willExplore(destination) &&
//                    seekSector(destination))
//                return;
//        }
//        
//        // Must be done after destination updates to make sure powerful ships
//        // update their destinations
//        if (ship.isDocked() && willClaim() && ship.claim())
//            return;
//        
//        // Claim the planet if powerful enough
//        // The isLanded() check is to confirm that the planet is claimable
//        if (ship.isLanded() && willClaim() && ship.claim())
//            return;
//        
//        // If the ship is in a position to mine
//        if (ship.canMine() && (ship.isLanded() ||
//                ship.getResource(Resources.HULL).getAmount()
//                > Planet.ASTEROID_DAMAGE))
//        {
//            if (ship.mine())
//                return;
//            
//            // This method will ensure that other emergency options are used
//            seekStation();
//            return;
//        }
//        
//        // If ship is running low on fuel while distant from a station, return
//        // to the nearest station
//        if (ship.getAmountOf(Resources.FUEL) <= calculateStationDistance()
//                + Actions.ORBIT.getCost())
//        {
//            seekStation();
//            return;
//        }
//        
//        Sector sector = ship.getLocation().getSector();
//        
//        // Attack a ship if not in the center
//        if (!sector.isCenter())
//        {
//            // Find a ship at the same orbit
//            Ship otherShip = sector.getFirstHostileShip(ship);
//            if (otherShip != null && willAttack() && !otherShip.isCloaked())
//            {
//                if (otherShip.isPlayer())
//                {
//                    attackers.add(ship);
//                    addColorMessage(
//                            new ColorString("You are under attack from ")
//                                    .add(ship.toColorString()).add("!"));
//                }
//                else
//                {
//                    ship.controlAIBattle(otherShip);
//                }
//                return;
//            }
//        }
//        
//        if (ship.getResource(Resources.ORE).isFull())
//        {
//            seekStation();
//            return;
//        }
//        
//        if (!ship.getResource(Resources.ENERGY).isEmpty() && ship.isInSector())
//        {
//            Planet planet = ship.getSectorLocation().getPlanet();
//            if (planet != null && planet.getType().canLandOn())
//            {
//                Coord target = planet.getRandomOreCoord();
//                if ((target == null && ship.land(planet.getRandomCoord())) ||
//                        (target != null && ship.land(target)))
//                    return;
//            }
//            
//            int orbit = ship.getSectorLocation().getOrbit();
//            int closestPlanet = sector.closestMineablePlanetTo(orbit);
//            if (closestPlanet > 0 && ship.orbit(orbit < closestPlanet))
//                return;
//        }
//        
//        seekStation();
//    }
//    
//    /**
//     * Will perform the next action in getting to a station, and will distress
//     * or destroy the ship if not possible.
//     */
//    private void seekStation()
//    {
//        Sector sector = ship.getLocation().getSector();
//        
//        if (ship.isInSector() && ship.getSectorLocation().isStation() &&
//                !ship.isDocked() && !ship.isLanded())
//        {
//            Station station = ship.getSectorLocation().getStation();
//            
//            if (ship.isHostile(station.getFaction()) && ship.isAligned())
//            {
//                if (!ship.dock() && ship.getCredits() < station.getClaimCost())
//                {
//                    if (!ship.isInFaction(station.getFaction()) && isAligned())
//                    {
//                        ship.leaveFaction();
//                        
//                        if (ship.getReputation(station.getFaction()).get()
//                                >= Reputations.REQ_REJECTION)
//                            ship.joinFaction(station.getFaction());
//                        else
//                            distressOrDestroy();
//                        return;
//                    }
//                }
//                return;
//            }
//            
//            if (ship.getCredits() + ship.getResource(Resources.ORE)
//                    .getTotalPrice() >= MIN_DOCK_CREDITS)
//                ship.dock();
//            else
//                distressOrDestroy();
//            return;
//        }
//        
//        if (ship.isLanded() && ship.takeoff())
//            return;
//        
//        if (Sector.STATION_SYSTEM.equals(sector.getType()))
//        {
//            if (!ship.isInSector())
//            {
//                if (enter())
//                {
//                    return;
//                }
//                else
//                {
//                    distressOrDestroy();
//                    return;
//                }
//            }
//            
//            int orbit = ship.getSectorLocation().getOrbit();
//            if (ship.isInSector() && ship.orbit(orbit <
//                    sector.closestStationTo(orbit, ship.faction)))
//                return;
//            
//            if (ship.enter())
//                return;
//        }
//        
//        if (ship.isInSector() && escape())
//            return;
//        
//        Coord adjacentStation = ship.getLocation().getMap().adjacentTypeTo(
//                Sector.STATION_SYSTEM, ship.getLocation().getCoords());
//        if (adjacentStation != null && ship.burnTowards(adjacentStation))
//            return;
//        
//        if (doRandomBurn())
//            return;
//        
//        if (ship.refine())
//            return;
//        
//        // On its own, this distressOrDestroy seems to harm unendangered ships
//        if (ship.getAmountOf(Resources.FUEL) < Actions.ORBIT.getCost())
//            distressOrDestroy();
//    }
//    
//    /**
//     * Returns false if the ship is in the sector, otherwise will try to fly
//     * there.
//     * @param target the sector the ship will try to reach
//     * @return true if seeking the sector, false if impossible
//     */
//    public boolean seekSector(Sector target)
//    {
//        if (target == null || !ship.getLocation().getMap().contains(
//                target.getLocation().getCoords()))
//            return false;
//        
//        if (ship.getLocation().getSector() == target)
//        {
//            if (!ship.isInSector())
//                ship.enter();
//            
//            return false;
//        }
//        
//        if (!ship.isInSector())
//            return ship.burnTowards(target.getLocation().getCoords());
//        
//        if (ship.isLanded())
//            return ship.takeoff();
//        else if (ship.isDocked())
//            return ship.undock();
//        else
//            return ship.escape();
//    }
//    
//    public boolean seekSector(Coord target)
//        {return seekSector(ship.getLocation().getMap().sectorAt(target));}
//    
//    /**
//     * Returns the amount of fuel required to get to the specified sector.
//     * @return the amount of fuel required to get to the sector, -1 if null or
//     * too far away
//     */
//    private int calculateSectorDistance(Sector target)
//    {
//        if (target == null)
//            return -1;
//        
//        if (target == ship.getLocation().getSector())
//            return (Actions.ORBIT.getCost() * 2);
//        
//        if (ship.getLocation().getCoords().isAdjacent(
//                target.getLocation().getCoords()))
//        {
//            if (isLanded())
//                return Actions.TAKEOFF.getCost() + Actions.BURN.getCost()
//                        + (Actions.ORBIT.getCost() * 2);
//            
//            return Actions.BURN.getCost() + (Actions.ORBIT.getCost() * 2);
//        }
//        
//        return -1;
//    }
//    
//    /**
//     * Returns the amount of fuel required to get to the nearest station.
//     * @return the amount of fuel required to get to a station, -1 if further
//     * than one sector away from one
//     */
//    private int calculateStationDistance()
//    {
//        if (ship.isInSector() && ship.getSectorLocation().getStation() != null)
//            return 0;
//        
//        Sector sector = ship.getLocation().getSector();
//        
//        if (ship.isInSector() && Sector.STATION_SYSTEM.equals(sector.getType()))
//        {
//            int cost = 0;
//            int orbit = ship.getSectorLocation().getOrbit();
//            if (ship.isLanded())
//                cost += Actions.TAKEOFF.getCost();
//            
//            cost += Math.abs(sector.closestStationTo(orbit) - orbit)
//                    * Actions.ORBIT.getCost();
//            return cost;
//        }
//        
//        if (ship.getLocation().getMap().adjacentTypeTo(Sector.STATION_SYSTEM,
//                ship.getLocation().getCoords()) != null)
//        {
//            int cost = 0;
//            if (ship.isLanded())
//            {
//                cost += Actions.TAKEOFF.getCost();
//            }
//            if (ship.isInSector())
//            {
//                cost += Actions.ESCAPE.getCost() + (Actions.ORBIT.getCost() *
//                        (sector.getOrbits() -
//                        ship.getSectorLocation().getOrbit()));
//            }
//            
//            cost += Actions.BURN.getCost() + Actions.ORBIT.getCost();
//            return cost;
//        }
//        
//        return -1;
//    }
//    
//    /**
//     * Burns in a random direction (intended for NPCs).
//     * @return true if the burn was successful
//     */
//    private boolean doRandomBurn()
//        {return ship.burn(Utility.select(Direction.CARDINALS));}
//    
//    /**
//     * Returns true if the NPC is willing to pursue a ship.
//     * @param other the ship that is fleeing
//     * @return true if the attacking ship will pursue the fleeing ship
//     */
//    public boolean willPursue(Ship other)
//    {
//        return other.getAmountOf(Resources.HULL) <=
//                ship.getAmountOf(Resources.HULL) && ship.hasWeapons() &&
//                willUseFuel(Actions.PURSUE.getCost());
//    }
//    
//    /**
//     * Returns true if the NPC is willing to convert another ship.
//     * @return true if the NPC is willing to convert another ship
//     */
//    public boolean willConvert()
//        {return ship.getHighestLevel() >= Levels.BASE_LEVEL;}
//    
//    /**
//     * Returns true if this ship is willing to claim a planet.
//     * @return true if the NPC is willing to claim a planet
//     */
//    public boolean willClaim()
//    {
//        return ship.getTotalLevel() >= Levels.BASE_LEVEL * Levels.LEVEL_AMT * 4;
//    }
//    
//    /**
//     * Returns true if the ship is willing the use the specified amount of fuel,
//     * making their decision based off of their distance from a station.
//     * @param fuel the amount of fuel to check that the ship will use
//     * @return true if the ship's amount of fuel minus the specified amount of
//     * fuel is greater than or equal to the approximate cost of reaching a
//     * station
//     */
//    public boolean willUseFuel(int fuel)
//    {
//        return ship.getAmountOf(Resources.FUEL) - fuel >=
//                calculateStationDistance();
//    }
//    
//    public boolean willExplore(Sector target)
//    {
//        return target == null ? false : ship.getAmountOf(Resources.FUEL) >=
//               calculateSectorDistance(target) && willClaim();
//    }
}