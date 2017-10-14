package maugrift.eversector.ships;

import maugrift.eversector.actions.*;
import maugrift.eversector.items.Module;
import maugrift.eversector.items.Resource;
import maugrift.eversector.items.Weapon;
import maugrift.eversector.locations.*;
import maugrift.eversector.map.*;
import maugrift.util.Utility;
import maugrift.eversector.actions.*;
import maugrift.eversector.locations.*;
import maugrift.eversector.map.*;
import squidpony.squidmath.Coord;

import java.util.List;

import static maugrift.eversector.Main.rng;

/**
 * The "artificial intelligence" in charge of making decisions for a ship.
 *
 * @author Maugrift
 */
public class AI
{
    public static final String[] modulePriority = new String[]{
            Weapon.PULSE_BEAM,
            Weapon.TORPEDO_TUBE,
            Weapon.LASER,
            Module.SHIELD,
            Module.CLOAKING_DEVICE,
            Module.WARP_DRIVE,
            Module.REFINERY,
            Module.SOLAR_ARRAY,
            Module.SCANNER
    };

    public static final String[] expanderPriority = new String[]{
            Resource.FUEL, Resource.ORE, Resource.ENERGY, Resource.HULL
    };

    /**
     * The ship the AI is controlling.
     */
    private final Ship ship;

    /**
     * The ship's current destination.
     */
    private Location destination;

    /**
     * Creates a new AI for the given ship.
     *
     * @param ship the ship
     */
    public AI(Ship ship)
    {
        this.ship = ship;
    }

    /**
     * Gets the ship controlled by the AI.
     *
     * @return the ship controlled by the AI
     */
    public Ship getShip()
    {
        return ship;
    }

    /**
     * Makes the ship perform an action. Has no effect if the ship is in a battle.
     *
     * @see #performBattleAction()
     */
    public Action act()
    {
        if (ship.isInBattle())
        {
            return null;
        }

        if (ship.isShielded())
        {
            return new Toggle(Module.SHIELD);
        }

        if (ship.isCloaked())
        {
            return new Toggle(Module.CLOAKING_DEVICE);
        }

        Action attack = attack();
        if (attack != null)
        {
            return attack;
        }

        if (destination != null && ship.getLocation().equals(destination))
        {
            if (ship.isDocked())
            {
                Action stationAction = performStationAction();
                if (stationAction != null && stationAction.canExecuteBool(ship))
                {
                    return stationAction;
                }
            }
            else if (ship.isLanded())
            {
                Action planetAction = performPlanetAction();
                if (planetAction != null && planetAction.canExecuteBool(ship))
                {
                    return planetAction;
                }
            }
            else if (ship.isInSector())
            {
                Action sectorAction = performSectorAction();
                if (sectorAction != null && sectorAction.canExecuteBool(ship))
                {
                    return sectorAction;
                }

                if (ship.getSectorLocation().isPlanet() &&
                    ship.getSectorLocation().getPlanet().getType().canMineFromOrbit())
                {
                    Mine mine = new Mine();
                    if (mine.canExecuteBool(ship))
                    {
                        return mine;
                    }
                }
            }
        }

        if (!destinationIsValid())
        {
            updateDestination();
        }

        Action seekDestination = seekDestination();
        if (seekDestination != null && seekDestination.canExecuteBool(ship))
        {
            return seekDestination;
        }

        return performEmergencyAction();
    }

    /**
     * Performs an action for when the ship is orbital in the sector.
     *
     * @return true if an action was performed
     */
    private Action performSectorAction()
    {
        Planet planet = ship.getSectorLocation().getPlanet();
        if (planet != null && planet.getType().canMineFromOrbit())
        {
            return new Mine();
        }

        return null;
    }

    /**
     * Performs an action for when the ship is landed on a planet.
     *
     * @return true if an action was performed
     */
    private Action performPlanetAction()
    {
        Claim claim = new Claim();
        return claim.canExecuteBool(ship) ? claim : new Mine();
    }

    /**
     * Performs an action for when the ship is docked at a station.
     *
     * @return true if an action was performed
     */
    private Action performStationAction()
    {
        sellDuplicates();
        buyResources();
        Claim claim = new Claim();
        if (claim.canExecuteBool(ship))
        {
            return claim;
        }

        if (rng.nextBoolean())
        {
            buyItems();
            buyExpanders();
        }
        return null;
    }

    /**
     * Sells items in cargo when at a station.
     */
    private void sellDuplicates()
    {
        boolean selling = true;
        while (selling && !ship.getCargo().isEmpty())
        {
            selling = false;
            for (Module module : ship.getCargo())
            {
                // Must never update selling without checking sellModule()
                // If a station will not accept a module, this loops forever
                if (module != null)
                {
                    SellModule sellModule = new SellModule(module.getName());
                    if (sellModule.executeBool(ship))
                    {
                        selling = true;
                        break;
                    }
                }
            }
        }
    }

    /**
     * Restocks resources when docked at a station.
     */
    private void buyResources()
    {
        new TransactResource(Resource.ORE, -ship.getMaxSellAmount(Resource.ORE)).execute(ship);
        new TransactResource(Resource.FUEL, ship.getMaxBuyAmount(Resource.FUEL)).execute(ship);
        new TransactResource(Resource.HULL, ship.getMaxBuyAmount(Resource.HULL)).execute(ship);
        new TransactResource(Resource.ENERGY, ship.getMaxBuyAmount(Resource.ENERGY)).execute(ship);
    }

    /**
     * Buys items when docked at a station.
     */
    private void buyItems()
    {
        for (String module : modulePriority)
        {
            if (!ship.hasModule(module))
            {
                new BuyModule(module).execute(ship);
            }
        }
    }

    /**
     * Buys expanders when docked at a station.
     */
    private void buyExpanders()
    {
        for (int i = 0; i < expanderPriority.length; i++)
        {
            Resource resource = ship.getResource(expanderPriority[i]);
            int nExpanders = resource.getNExpanders();

            boolean skip = false;
            for (int j = i + 1; j < expanderPriority.length; j++)
            {
                if (ship.getResource(expanderPriority[j]).getNExpanders() >= nExpanders)
                {
                    skip = true;
                    break;
                }
            }

            if (skip)
            {
                continue;
            }

            String expanderName = resource.getExpander().getName();
            new TransactResource(expanderName, ship.getMaxBuyAmount(expanderName)).execute(ship);
        }
    }

    /**
     * Decides on whether to attack another ship when orbital in a sector.
     *
     * @return true if the ship attacked
     */
    private Action attack()
    {
        if (!ship.hasWeapons() || !ship.isOrbital())
        {
            return null;
        }

        Ship player = ship.getLocation().getGalaxy().getPlayer();
        if (player != null && player.getLocation().equals(ship.getLocation()) && ship.isHostile(player.getFaction()))
        {
            StartBattle startBattle = new StartBattle(player);
            if (startBattle.canExecuteBool(ship))
            {
                return startBattle;
            }
        }

        List<Ship> others = ship.getSectorLocation().getShips();
        if (!others.isEmpty())
        {
            for (Ship other : others)
            {
                if (ship.isHostile(other.getFaction()))
                {
                    StartBattle startBattle = new StartBattle(other);
                    if (startBattle.canExecuteBool(ship))
                    {
                        return startBattle;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Chooses a new destination based on the ship's current situation.
     */
    private void updateDestination()
    {
        Location invasionDestination = findInvasionDestination();
        if (invasionDestination != null)
        {
            destination = invasionDestination;
            return;
        }

        Location claimingDestination = findClosestUnclaimedTerritory();
        if (claimingDestination != null)
        {
            destination = claimingDestination;
            return;
        }

        if (ship.getResource(Resource.ORE).isFull() || ship.validateResources(Mine.RESOURCE, Mine.COST, "mine") != null)
        {
            destination = findClosestStation();
            return;
        }

        destination = findClosestMiningDestination();
    }

    /**
     * Finds the closest location where the ship can mine.
     *
     * @return the closest location where the ship can mine
     */
    private SectorLocation findClosestMiningDestination()
    {
        if (ship.isInSector())
        {
            if (ship.isLanded() && ship.getPlanetLocation().getRegion().hasOre())
            {
                return ship.getPlanetLocation();
            }

            SectorLocation current = getPlanetMiningDestination(ship.getSectorLocation().getPlanet());
            if (current != null)
            {
                return current;
            }

            Sector sector = ship.getLocation().getSector();
            int orbit = ship.getSectorLocation().getOrbit();
            for (int offset = 1; offset < sector.getOrbits(); offset++)
            {
                SectorLocation minusOffset = getPlanetMiningDestination(sector.getPlanetAt(orbit - offset));
                SectorLocation plusOffset = getPlanetMiningDestination(sector.getPlanetAt(orbit + offset));

                if (minusOffset != null)
                {
                    if (plusOffset == null)
                    {
                        return minusOffset;
                    }

                    return rng.nextBoolean() ? minusOffset : plusOffset;
                }
                else if (plusOffset != null)
                {
                    return plusOffset;
                }
            }
        }

        List<Coord> fov = ship.getFOV();
        fov.sort(Utility.createDistanceComparator(ship.getLocation().getCoord()));
        Galaxy galaxy = ship.getLocation().getGalaxy();
        for (Coord coord : fov)
        {
            if (!galaxy.contains(coord))
            {
                continue;
            }

            Sector sector = galaxy.sectorAt(coord);
            if (sector.isEmpty())
            {
                continue;
            }

            for (int orbit = sector.getOrbits(); orbit > 0; orbit--)
            {
                SectorLocation planetLocation = getPlanetMiningDestination(sector.getPlanetAt(orbit));
                if (planetLocation != null)
                {
                    return planetLocation;
                }
            }
        }

        return null;
    }

    /**
     * Generates a mining location for the given planet.
     *
     * @param planet the planet to get a location for
     * @return a mining location for the given planet
     */
    private SectorLocation getPlanetMiningDestination(Planet planet)
    {
        if (planet == null)
        {
            return null;
        }

        if (planet.getType().canMineFromOrbit())
        {
            return planet.getLocation();
        }

        if (planet.getType().canMine() && planet.getType().canLandOn())
        {
            Region oreRegion = planet.getRandomOreRegion();
            return oreRegion == null ? null : oreRegion.getLocation();
        }

        return null;
    }

    /**
     * Finds the closest passive station.
     *
     * @return the closest passive station
     */
    private StationLocation findClosestStation()
    {
        if (ship.isInSector())
        {
            if (ship.isDocked())
            {
                return ship.getStationLocation();
            }

            if (ship.getSectorLocation().isStation())
            {
                return ship.getSectorLocation().dock();
            }

            Sector sector = ship.getLocation().getSector();
            if (sector.hasStations())
            {
                int orbit = ship.getSectorLocation().getOrbit();
                for (int offset = 1; offset < sector.getOrbits(); offset++)
                {
                    StationLocation minusOffset = getStationDestination(sector.getStationAt(orbit - offset));
                    StationLocation plusOffset = getStationDestination(sector.getStationAt(orbit + offset));

                    if (minusOffset != null)
                    {
                        if (plusOffset == null)
                        {
                            return minusOffset;
                        }

                        return rng.nextBoolean() ? minusOffset : plusOffset;
                    }
                    else if (plusOffset != null)
                    {
                        return plusOffset;
                    }
                }
            }
        }

        List<Coord> fov = ship.getFOV();
        fov.sort(Utility.createDistanceComparator(ship.getLocation().getCoord()));
        Galaxy galaxy = ship.getLocation().getGalaxy();
        for (Coord coord : fov)
        {
            if (!galaxy.contains(coord))
            {
                continue;
            }

            Sector sector = galaxy.sectorAt(coord);
            if (!sector.hasStations())
            {
                continue;
            }

            for (int orbit = sector.getOrbits(); orbit > 0; orbit--)
            {
                StationLocation stationLocation = getStationDestination(sector.getStationAt(orbit));
                if (stationLocation != null)
                {
                    return stationLocation;
                }
            }
        }

        return null;
    }

    /**
     * Generates a location for the given passive station.
     *
     * @param station the station to get a location for
     * @return a location for the given passive station
     */
    private StationLocation getStationDestination(Station station)
    {
        if (station == null)
        {
            return null;
        }

        if (!ship.isHostile(station.getFaction()) || ship.getCredits() >= Station.CLAIM_COST)
        {
            return station.getLocation().dock();
        }
        return null;
    }

    /**
     * Finds the closest unclaimed territory on a planet.
     *
     * @return the closest unclaimed territory on a planet
     */
    private Location findClosestUnclaimedTerritory()
    {
        if (!ship.isInSector())
        {
            return null;
        }

        Sector sector = ship.getLocation().getSector();
        int orbit = ship.getSectorLocation().getOrbit();
        for (int offset = 1; offset < sector.getOrbits(); offset++)
        {
            SectorLocation minusOffset = getPlanetClaimingDestination(sector.getPlanetAt(orbit - offset));
            SectorLocation plusOffset = getPlanetClaimingDestination(sector.getPlanetAt(orbit + offset));

            if (minusOffset != null && ship.getCredits() >= minusOffset.getPlanet().getClaimCost())
            {
                if (plusOffset != null && ship.getCredits() >= plusOffset.getPlanet().getClaimCost())
                {
                    return rng.nextBoolean() ? minusOffset : plusOffset;
                }

                return minusOffset;
            }
            else if (plusOffset != null)
            {
                return plusOffset;
            }
        }

        return null;
    }

    /**
     * Generates a location with unclaimed territory for the given planet.
     *
     * @param planet the planet to get a location for
     * @return a location with unclaimed territory for the given planet
     */
    private PlanetLocation getPlanetClaimingDestination(Planet planet)
    {
        if (planet == null || !planet.getType().canLandOn())
        {
            return null;
        }

        Region unclaimedRegion = planet.getRandomRegion(ship.getFaction());
        return unclaimedRegion == null ? null : unclaimedRegion.getLocation();
    }

    /**
     * Finds the closest hostile station to invade.
     *
     * @return the closest hostile station to invade
     */
    private StationLocation findInvasionDestination()
    {
        if (ship.getCredits() < Station.CLAIM_COST || !ship.hasWeapons() || !ship.getResource(Resource.FUEL).isFull())
        {
            return null;
        }

        if (ship.isInSector())
        {
            if (ship.getSectorLocation().isStation())
            {
                StationLocation invasionDestination = getStationInvasionDestination(
                        ship.getSectorLocation().getStation());
                if (invasionDestination != null)
                {
                    return invasionDestination;
                }
            }

            Sector sector = ship.getLocation().getSector();
            if (sector.hasStations())
            {
                int orbit = ship.getSectorLocation().getOrbit();
                for (int offset = 1; offset < sector.getOrbits(); offset++)
                {
                    StationLocation minusOffset = getStationInvasionDestination(sector.getStationAt(orbit - offset));
                    StationLocation plusOffset = getStationInvasionDestination(sector.getStationAt(orbit + offset));

                    if (minusOffset != null)
                    {
                        if (plusOffset == null)
                        {
                            return minusOffset;
                        }

                        return rng.nextBoolean() ? minusOffset : plusOffset;
                    }
                    else if (plusOffset != null)
                    {
                        return plusOffset;
                    }
                }
            }
        }

        List<Coord> fov = ship.getFOV();
        fov.sort(Utility.createDistanceComparator(ship.getLocation().getCoord()));
        Galaxy galaxy = ship.getLocation().getGalaxy();
        for (Coord coord : fov)
        {
            if (!galaxy.contains(coord))
            {
                continue;
            }

            Sector sector = galaxy.sectorAt(coord);
            if (!sector.hasStations())
            {
                continue;
            }

            for (int orbit = sector.getOrbits(); orbit > 0; orbit--)
            {
                StationLocation stationLocation = getStationInvasionDestination(sector.getStationAt(orbit));
                if (stationLocation != null)
                {
                    return stationLocation;
                }
            }
        }

        return null;
    }

    /**
     * Generates a location for the given hostile station.
     *
     * @param station the station to get a location for
     * @return a location for the given hostile station
     */
    private StationLocation getStationInvasionDestination(Station station)
    {
        if (station == null)
        {
            return null;
        }

        if (ship.isHostile(station.getFaction()))
        {
            return station.getLocation().dock();
        }
        return null;
    }

    /**
     * Returns true if the stored destination exists, can be traveled to, and is not the ship's current location.
     *
     * @return true if the stored destination is valid
     */
    private boolean destinationIsValid()
    {
        return !(destination == null || ship.getLocation().equals(destination) ||
                 destination instanceof BattleLocation);
    }

    /**
     * Attempts to move toward the stored destination.
     *
     * @return true if the ship moved
     */
    private Action seekDestination()
    {
        if (!destinationIsValid())
        {
            return null;
        }

        if (ship.isDocked())
        {
            return new Undock();
        }

        if (ship.isLanded())
        {
            if (destination instanceof PlanetLocation &&
                ship.getSectorLocation().getPlanet() == ((SectorLocation) destination).getPlanet())
            {
                return new Relocate(Utility.toGoToCardinal(ship.getPlanetLocation().getRegionCoord(),
                        ((PlanetLocation) destination).getRegionCoord()));
            }

            return new Takeoff();
        }

        if (ship.isInSector())
        {
            if (destination instanceof SectorLocation && ship.getLocation().getSector() == destination.getSector())
            {
                if (ship.getSectorLocation().getOrbit() == ((SectorLocation) destination).getOrbit())
                {
                    if (destination instanceof StationLocation)
                    {
                        return new Dock();
                    }

                    if (destination instanceof PlanetLocation)
                    {
                        return new Land(((PlanetLocation) destination).getRegionCoord());
                    }
                }

                return new Orbit(ship.getSectorLocation().getOrbit() < ((SectorLocation) destination).getOrbit());
            }

            return new Orbit(true);
        }

        if (ship.getLocation().getCoord().equals(destination.getCoord()))
        {
            return new Enter();
        }

        if (!destination.getCoord().isAdjacent(ship.getLocation().getCoord()))
        {
            Warp warp = new Warp(destination.getCoord());
            if (warp.canExecuteBool(ship))
            {
                return warp;
            }
        }

        Burn burn = new Burn(Utility.toGoToCardinal(ship.getLocation().getCoord(), destination.getCoord()));
        return burn.canExecuteBool(ship) ? burn : null;
    }

    /**
     * Performs an action when no others are possible. Will destroy the ship if no emergency actions can be performed.
     */
    private Action performEmergencyAction()
    {
        Refine refine = new Refine();
        if (refine.canExecuteBool(ship))
        {
            return refine;
        }

        Distress distress = new Distress(ship.getDistressResponder());
        if (distress.canExecuteBool(ship))
        {
            return distress;
        }

        ship.destroy(false);
        return null;
    }

    /**
     * Decides on whether to join the given battle.
     *
     * @param battle the battle to join
     * @return true if the ship joined the battle
     */
    public boolean joinBattle(Battle battle)
    {
        if (!willAttack() || !ship.isOrbital())
        {
            return false;
        }

        int attackerFriendliness = 0;
        int defenderFriendliness = 0;

        for (Ship attacker : battle.getAttackers())
        {
            attackerFriendliness += getFriendliness(attacker);
        }

        for (Ship defender : battle.getDefenders())
        {
            defenderFriendliness += getFriendliness(defender);
        }

        if (attackerFriendliness > defenderFriendliness)
        {
            battle.getAttackers().add(ship);
        }
        else
        {
            battle.getDefenders().add(ship);
        }

        ship.setLocation(ship.getSectorLocation().joinBattle(battle));
        return true;
    }

    /**
     * Gets the "friendliness" of the given ship. Used when deciding which side of a battle to join.
     *
     * @param other the ship to get the friendliness of
     * @return the friendliness of the given ship
     */
    private int getFriendliness(Ship other)
    {
        if (!other.isAligned() || !ship.isAligned())
        {
            return 0;
        }

        if (ship.getFaction() == other.getFaction())
        {
            return 4;
        }

        switch (other.getFaction().getRelationship(ship.getFaction()))
        {
            case ALLIANCE:
                return 3;
            case PEACE:
                return 1;
        }

        return 0;
    }

    /**
     * Performs an action in battle.
     *
     * @return true if the ship attacked, false if not
     */
    public Action performBattleAction()
    {
        if (!ship.isInBattle() || ship.isDestroyed())
        {
            return null;
        }

        Battle battle = ship.getBattleLocation().getBattle();
        List<Ship> enemies = battle.getEnemies(ship);
        Ship target = null;
        for (Ship enemy : enemies)
        {
            if (!battle.getFleeing().contains(enemy) && (target == null || target.getResource(Resource.HULL)
                                                                                 .getAmount() < enemy.getResource(
                    Resource.HULL).getAmount()))
            {
                target = enemy;
            }
        }

        if (target == null)
        {
            return null;
        }

        Ship player = ship.getLocation().getGalaxy().getPlayer();
        boolean playerInBattle = player != null && ship.getBattleLocation().getShips().contains(player);

        if (!willAttack())
        {
            Flee flee = new Flee();
            return flee.canExecuteBool(ship) ? flee : new Surrender();
        }

        if (!ship.isShielded())
        {
            new Toggle(Module.SHIELD).execute(ship);
        }

        Fire pulse = new Fire(Weapon.PULSE_BEAM, target);
        if (pulse.canExecuteBool(ship))
        {
            return pulse;
        }

        Fire torpedo = new Fire(Weapon.TORPEDO_TUBE, target);
        if (torpedo.canExecuteBool(ship))
        {
            return torpedo;
        }

        Fire laser = new Fire(Weapon.LASER, target);
        if (laser.canExecuteBool(ship))
        {
            return laser;
        }

        // The code above under !willAttack() is borrowed from here
        Flee flee = new Flee();
        if (flee.canExecuteBool(ship))
        {
            if (!ship.isCloaked())
            {
                new Toggle(Module.CLOAKING_DEVICE).execute(ship);
            }

            return flee;
        }

        if (playerInBattle)
        {
            player.addPlayerColorMessage(ship.toColorString().add(" surrenders."));
        }
        return new Surrender();
    }

    /**
     * Decides on whether to pursue any enemy.
     *
     * @return true if the ship pursued
     */
    public boolean pursue()
    {
        Battle battle = ship.getBattleLocation().getBattle();
        return willAttack() && battle.getEnemies(ship).size() - battle.getFleeing().size() < 1 &&
               new Pursue().executeBool(ship);
    }

    /**
     * Returns true if the ship is willing to enter a fight.
     *
     * @return true if the ship is willing to enter a fight
     */
    public boolean willAttack()
    {
        return ship.hasWeapons() && ship.getResource(Resource.HULL).getAmount() >= ship.getResource(Resource.HULL)
                                                                                       .getCapacity() / 2;
    }

    /**
     * Votes on a candidate for faction leader, from the given list of candidates.
     *
     * @param candidates the candidates for faction leader that the ship can choose from
     * @return the ship that this ship is voting for
     */
    public Ship vote(List<Ship> candidates)
    {
        int[] preferences = new int[candidates.size()];

        for (int i = 0; i < candidates.size(); i++)
        {
            Ship candidate = candidates.get(i);
            preferences[i] = 0;

            if (ship.getHigherLevel() != null && ship.getHigherLevel().equals(candidate.getHigherLevel()))
            {
                preferences[i] += 2;
            }

            if (ship.getLocation().getCoord().equals(candidate.getLocation().getCoord()))
            {
                preferences[i] += 3;
            }
            else if (ship.getLocation().getCoord().isAdjacent(candidate.getLocation().getCoord()))
            {
                preferences[i]++;
            }

            if (candidate.calculateShipValue() > ship.calculateShipValue())
            {
                preferences[i]++;
            }
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
