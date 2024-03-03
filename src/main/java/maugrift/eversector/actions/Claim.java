package maugrift.eversector.actions;

import maugrift.eversector.Paths;
import maugrift.eversector.faction.Faction;
import maugrift.eversector.map.Planet;
import maugrift.eversector.map.Region;
import maugrift.eversector.map.Station;
import maugrift.eversector.ships.Reputation;
import maugrift.eversector.ships.Ship;

import static maugrift.eversector.faction.Relationship.RelationshipType.ALLIANCE;

/**
 * @author Aaron Friesen
 */
public class Claim implements Action
{
	public static final String SOUND_EFFECT = Paths.CLAIM;

	@Override
	public String canExecute(Ship actor)
	{
		if (actor == null) {
			return "Ship not found.";
		}

		if (!actor.isAligned()) {
			return "You must be part of a faction to claim territory.";
		}

		Faction faction = actor.getFaction();

		if (actor.isLanded()) {
			return canExecute(actor, actor.getPlanetLocation().getRegion());
		}

		if (actor.isDocked()) {
			return canExecute(actor, actor.getSectorLocation().getStation());
		}

		return "You must be landed or docked to claim territory.";
	}

	public String canExecute(Ship actor, Region region)
	{
		Faction faction = actor.getFaction();
		Planet planet = region.getLocation().getPlanet();

		if (actor.getCredits() < planet.getClaimCost()) {
			return "You cannot afford the "
				+ planet.getClaimCost()
				+ " credit cost to claim territory on "
				+ planet
				+ ".";
		}

		if (!region.getType().isLand()) {
			return "The "
				+ region.toString().toLowerCase()
				+ " cannot be claimed.";
		}

		if (region.getFaction() == faction) {
			return "The "
				+ region.toString().toLowerCase()
				+ " is already claimed by the "
				+ faction
				+ ".";
		}

		if (region.getNShips(region.getFaction()) > 0) {
			return "There are currently ships of the "
				+ region.getFaction()
				+ " guarding the "
				+ region.toString().toLowerCase()
				+ ".";
		}

		return null;
	}

	public String canExecute(Ship actor, Station station)
	{
		Faction faction = actor.getFaction();

		if (actor.getCredits() < Station.CLAIM_COST) {
			return "You cannot afford the "
				+ Station.CLAIM_COST
				+ " credit cost to claim "
				+ station
				+ ".";
		}

		// If the body is already claimed by solely your faction, return false
		if (station.getFaction() == faction) {
			return station + " is already claimed by the " + faction + ".";
		}

		if (station.getNShips(station.getFaction()) > 0) {
			return "There are currently ships of the "
				+ station.getFaction()
				+ " guarding "
				+ station
				+ ".";
		}

		return null;
	}

	@Override
	public String execute(Ship actor)
	{
		String canExecute = canExecute(actor);
		if (canExecute != null) {
			return canExecute;
		}

		Faction faction = actor.getFaction();

		if (actor.isLanded()) {
			Region region = actor.getPlanetLocation().getRegion();
			Planet planet = actor.getSectorLocation().getPlanet();
			int nRegions = planet.getNRegions();
			actor.changeCredits(region.getFaction(), -planet.getClaimCost());

			if (ALLIANCE == faction.getRelationship(region.getFaction())) {
				actor.changeReputation(faction, Reputation.CLAIM_ALLY / nRegions);
			} else {
				actor.changeReputation(faction, Reputation.CLAIM / nRegions);
			}

			actor.changeReputation(
				region.getFaction(),
				-Reputation.CLAIM / nRegions
			);

			// Claim must be done here so the faction relations can be checked
			region.claim(faction);
			actor.playPlayerSound(SOUND_EFFECT);
			return null;
		}

		Station station = actor.getSectorLocation().getStation();
		actor.changeCredits(station.getFaction(), -Station.CLAIM_COST);

		if (ALLIANCE.equals(faction.getRelationship(station.getFaction()))) {
			actor.changeReputation(faction, Reputation.CLAIM_ALLY);
		} else {
			actor.changeReputation(faction, Reputation.CLAIM);
		}

		actor.changeReputation(station.getFaction(), -Reputation.CLAIM);
		station.claim(faction);
		actor.playPlayerSound(SOUND_EFFECT);
		return null;
	}
}
