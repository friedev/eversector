package boldorf.eversector.ships;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Action;
import boldorf.eversector.items.Module;
import boldorf.eversector.items.Resource;
import boldorf.eversector.locations.*;
import boldorf.eversector.map.*;
import boldorf.util.Utility;
import squidpony.squidmath.Coord;

import java.util.List;

import static boldorf.eversector.Main.rng;

/**
 * The "artificial intelligence" in charge of making decisions for a ship.
 *
 * @author Boldorf Smokebane
 */
public class AI
{
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
    public void act()
    {
        if (ship.isInBattle())
        {
            return;
        }

        if (ship.isShielded())
        {
            ship.toggleActivation(Action.SHIELD);
        }

        if (ship.isCloaked())
        {
            ship.toggleActivation(Action.CLOAK);
        }

        if (attack())
        {
            return;
        }

        if (destination != null && ship.getLocation().equals(destination))
        {
            if (ship.isDocked())
            {
                if (performStationAction())
                {
                    return;
                }
            }
            else if (ship.isLanded())
            {
                if (performPlanetAction())
                {
                    return;
                }
            }
            else if (ship.isInSector())
            {
                if (performSectorAction())
                {
                    return;
                }

                if (ship.getSectorLocation().isPlanet() &&
                    ship.getSectorLocation().getPlanet().getType().canMineFromOrbit() && ship.mine())
                {
                    return;
                }
            }
        }

        if (!destinationIsValid())
        {
            updateDestination();
        }

        if (!seekDestination())
        {
            performEmergencyAction();
        }
    }

    /**
     * Performs an action for when the ship is orbital in the sector.
     *
     * @return true if an action was performed
     */
    private boolean performSectorAction()
    {
        Planet planet = ship.getSectorLocation().getPlanet();
        return planet != null && planet.getType().canMineFromOrbit() && ship.mine();
    }

    /**
     * Performs an action for when the ship is landed on a planet.
     *
     * @return true if an action was performed
     */
    private boolean performPlanetAction()
    {
        return ship.claim(false) || ship.mine();
    }

    /**
     * Performs an action for when the ship is docked at a station.
     *
     * @return true if an action was performed
     */
    private boolean performStationAction()
    {
        sellDuplicates();
        buyResources();
        if (ship.claim(false))
        {
            return true;
        }

        if (rng.nextBoolean())
        {
            buyItems(); // TODO replace with a loop
            buyExpanders();
        }
        return false;
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
                if (module != null && ship.sellModule(module.getName()))
                {
                    selling = true;
                    break;
                }
            }
        }
    }

    /**
     * Restocks resources when docked at a station.
     */
    private void buyResources()
    {
        ship.buyResource(Resource.ORE, -ship.getMaxSellAmount(Resource.ORE));
        ship.buyResource(Resource.FUEL, ship.getMaxBuyAmount(Resource.FUEL));
        ship.buyResource(Resource.HULL, ship.getMaxBuyAmount(Resource.HULL));
        ship.buyResource(Resource.ENERGY, ship.getMaxBuyAmount(Resource.ENERGY));
    }

    /**
     * Buys items when docked at a station.
     */
    private void buyItems()
    {
        if (!ship.hasModule(Action.PULSE))
        {
            ship.buyModule(Action.PULSE);
        }

        if (!ship.hasModule(Action.TORPEDO))
        {
            ship.buyModule(Action.TORPEDO);
        }

        if (!ship.hasModule(Action.LASER))
        {
            ship.buyModule(Action.LASER);
        }

        if (!ship.hasModule(Action.SHIELD))
        {
            ship.buyModule(Action.SHIELD);
        }

        if (!ship.hasModule(Action.WARP))
        {
            ship.buyModule(Action.WARP);
        }

        if (!ship.hasModule(Action.REFINE))
        {
            ship.buyModule(Action.REFINE);
        }

        if (!ship.hasModule(Action.SOLAR))
        {
            ship.buyModule(Action.SOLAR);
        }

        if (!ship.hasModule(Action.SCAN))
        {
            ship.buyModule(Action.SCAN);
        }
    }

    /**
     * Buys expanders when docked at a station.
     */
    private void buyExpanders()
    {
        int tanks = ship.getResource(Resource.FUEL).getNExpanders();
        int bays = ship.getResource(Resource.ORE).getNExpanders();
        int cells = ship.getResource(Resource.ENERGY).getNExpanders();
        int plates = ship.getResource(Resource.HULL).getNExpanders();

        // If there are the least fuel tanks, buy more
        int maxTanks = ship.getMaxBuyAmount(ship.getExpander(Resource.FUEL_EXPANDER));
        if (maxTanks > 0 && tanks < bays && tanks < cells && tanks < plates)
        {
            ship.buyResource(Resource.FUEL_EXPANDER, maxTanks);
        }

        // If there are fewer cargo bays than cells and plates, buy more
        int maxBays = ship.getMaxBuyAmount(ship.getExpander(Resource.ORE_EXPANDER));
        if (maxBays > 0 && bays < cells && bays < plates)
        {
            ship.buyResource(Resource.ORE_EXPANDER, maxBays);
        }

        // If there are the fewer energy cells than hull plates, buy more
        int maxCells = ship.getMaxBuyAmount(ship.getExpander(Resource.ENERGY_EXPANDER));
        if (maxCells > 0 && cells < plates)
        {
            ship.buyResource(Resource.ENERGY_EXPANDER, maxCells);
        }

        // Buy hull frames by default
        ship.buyResource(Resource.HULL_EXPANDER, ship.getMaxBuyAmount(ship.getExpander(Resource.HULL_EXPANDER)));
    }

    /**
     * Decides on whether to attack another ship when orbital in a sector.
     *
     * @return true if the ship attacked
     */
    private boolean attack()
    {
        if (!ship.hasWeapons() || !ship.isOrbital())
        {
            return false;
        }

        Ship player = ship.getLocation().getGalaxy().getPlayer();
        if (player.getLocation().equals(ship.getLocation()) && ship.isHostile(player.getFaction()) && ship.startBattle(
                player) != null)
        {
            return true;
        }

        List<Ship> others = ship.getSectorLocation().getShips();
        if (!others.isEmpty())
        {
            for (Ship other : others)
            {
                if (ship.isHostile(other.getFaction()) && ship.startBattle(other) != null)
                {
                    return true;
                }
            }
        }

        return false;
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

        if (ship.getResource(Resource.ORE).isFull() || !ship.validateResources(Action.MINE, "mine"))
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
            // TODO check for regions without ore
            if (ship.isLanded())
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
    private boolean seekDestination()
    {
        if (!destinationIsValid())
        {
            return false;
        }

        if (ship.isDocked())
        {
            return ship.undock();
        }

        if (ship.isLanded())
        {
            if (destination instanceof PlanetLocation &&
                ship.getSectorLocation().getPlanet() == ((SectorLocation) destination).getPlanet())
            {
                return ship.relocate(Utility.toGoToCardinal(ship.getPlanetLocation().getRegionCoord(),
                        ((PlanetLocation) destination).getRegionCoord()));
            }

            return ship.takeoff();
        }

        if (ship.isInSector())
        {
            if (destination instanceof SectorLocation && ship.getLocation().getSector() == destination.getSector())
            {
                if (ship.getSectorLocation().getOrbit() == ((SectorLocation) destination).getOrbit())
                {
                    if (destination instanceof StationLocation)
                    {
                        return ship.dock();
                    }

                    if (destination instanceof PlanetLocation)
                    {
                        return ship.land(((PlanetLocation) destination).getRegionCoord());
                    }
                }

                return ship.orbit(ship.getSectorLocation().getOrbit() < ((SectorLocation) destination).getOrbit());
            }

            return ship.orbit(true);
        }

        if (ship.getLocation().getCoord().equals(destination.getCoord()))
        {
            return ship.enter();
        }

        return (!destination.getCoord().isAdjacent(ship.getLocation().getCoord()) && ship.warpTo(
                destination.getCoord())) || ship.burn(
                Utility.toGoToCardinal(ship.getLocation().getCoord(), destination.getCoord()));
    }

    /**
     * Performs an action when no others are possible. Will destroy the ship if no emergency actions can be performed.
     */
    private void performEmergencyAction()
    {
        if (ship.refine())
        {
            return;
        }
        else if (ship.canDistress())
        {
            ship.distress();
        }
        else
        {
            ship.destroy(false);
        }
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
    public boolean performBattleAction()
    {
        if (!ship.isInBattle() || ship.isDestroyed())
        {
            return false;
        }

        Battle battle = ship.getBattleLocation().getBattle();
        List<Ship> enemies = battle.getEnemies(ship);
        Ship target = null;
        for (Ship enemy : enemies)
        {
            if (!battle.getFleeing().contains(enemy) && (target == null || target.getAmountOf(Resource.HULL) <
                                                                           enemy.getAmountOf(Resource.HULL)))
            {
                target = enemy;
            }
        }

        if (target == null)
        {
            return false;
        }

        boolean playerInBattle =
                ship.getLocation().getGalaxy().getPlayer() != null && ship.getBattleLocation().getShips().contains(
                        ship.getLocation().getGalaxy().getPlayer());

        if (!willAttack())
        {
            if (ship.validateResources(Action.FLEE, "flee"))
            {
                ship.changeResourceBy(Action.FLEE);
                battle.getFleeing().add(ship);
                target.addPlayerColorMessage(ship.toColorString().add(" attempts to flee."));
                return false;
            }

            ship.destroy(playerInBattle);
            return false;
        }

        if (!ship.isShielded() && ship.toggleActivation(Action.SHIELD))
        {
            target.addPlayerColorMessage(ship.toColorString().add(" activates a shield."));
            // No return since shielding is a free action
        }

        if (ship.canFire(Action.PULSE, target))
        {
            target.addPlayerColorMessage(ship.toColorString().add(" fires a pulse beam."));
            target.playPlayerSound(Paths.PULSE);
            ship.fire(Action.PULSE, target);
        }
        else if (ship.canFire(ship.getWeapon(Action.TORPEDO.getName()), target))
        {
            target.addPlayerColorMessage(ship.toColorString().add(" fires a torpedo."));
            target.playPlayerSound(Paths.TORPEDO);
            ship.fire(Action.TORPEDO, target);
        }
        else if (ship.canFire(Action.LASER, target))
        {
            target.addPlayerColorMessage(ship.toColorString().add(" fires a laser."));
            target.playPlayerSound(Paths.LASER);
            ship.fire(Action.LASER, target);
        }
        else
        {
            // The code above under !willAttack() is borrowed from here
            if (ship.validateResources(Action.FLEE, "flee"))
            {
                if (!ship.isCloaked() && ship.toggleActivation(Action.CLOAK))
                {
                    target.addPlayerColorMessage(ship.toColorString().add(" activates a cloaking device."));
                    // No return since cloaking is a free action
                }

                target.addPlayerColorMessage(ship.toColorString().add(" attempts to flee."));
                return false;
            }

            ship.destroy(playerInBattle);
            return false;
        }

        if (target.isDestroyed())
        {
            if (ship.isPassive(target))
            {
                ship.changeReputation(ship.getFaction(), Reputation.KILL_ALLY);
            }
            else
            {
                ship.changeReputation(ship.getFaction(), Reputation.KILL_ENEMY);
            }

            ship.changeReputation(target.getFaction(), Reputation.KILL_ALLY);
        }

        return true;
    }

    /**
     * Decides on whether to pursue any enemy.
     *
     * @return true if the ship pursued
     */
    public boolean pursue()
    {
        Battle battle = ship.getBattleLocation().getBattle();
        return willAttack() && battle.getEnemies(ship).size() - battle.getFleeing().size() < 1 && ship.changeResourceBy(
                Action.PURSUE);
    }

    /**
     * Returns true if the ship is willing to enter a fight.
     *
     * @return true if the ship is willing to enter a fight
     */
    public boolean willAttack()
    {
        return ship.hasWeapons() && ship.getAmountOf(Resource.HULL) >= ship.getCapOf(Resource.HULL) / 2;
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