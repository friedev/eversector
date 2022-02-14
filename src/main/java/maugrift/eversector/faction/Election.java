package maugrift.eversector.faction;

import maugrift.apwt.glyphs.ColorString;
import maugrift.eversector.ships.Reputation;
import maugrift.eversector.ships.Ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static maugrift.eversector.Main.player;

/**
 * A class to handle leader elections for factions.
 *
 * @author Maugrift
 */
public class Election
{
	/**
	 * The number of candidates that are selected for an election.
	 */
	private static final int CANDIDATES = 4;

	/**
	 * The faction that the election takes place for.
	 */
	private final Faction faction;

	/**
	 * The list of Ships that have been nominated as candidates.
	 */
	private List<Ship> candidates;

	/**
	 * The votes gathered for each candidate. Indices in this list correlate
	 * directly to the candidates list.
	 */
	private List<Integer> votes;

	/**
	 * True if the election is taking place after the destruction of the
	 * former.
	 */
	private final boolean emergency;

	/**
	 * Creates an election for the given faction.
	 *
	 * @param faction   the election's faction
	 * @param emergency true if the election is an emergency
	 */
	public Election(Faction faction, boolean emergency)
	{
		this.faction = faction;
		candidates = new ArrayList<>(CANDIDATES);
		votes = new ArrayList<>(CANDIDATES);
		this.emergency = emergency;
	}

	/**
	 * Gets the faction that the election is for.
	 *
	 * @return the election's faction
	 */
	public Faction getFaction()
	{
		return faction;
	}

	/**
	 * Gets the candidates in this election.
	 *
	 * @return the candidates in the election
	 */
	public List<Ship> getCandidates()
	{
		return candidates;
	}

	/**
	 * Gets the votes for each candidate in the election.
	 *
	 * @return the votes in the election
	 */
	public List<Integer> getVotes()
	{
		return votes;
	}

	/**
	 * Returns true if the election is an emergency.
	 *
	 * @return true if the election is an emergency
	 */
	public boolean isEmergency()
	{
		return emergency;
	}

	/**
	 * Gets the lowest reputation of any candidate in the election.
	 *
	 * @return the lowest reputation in the election
	 */
	public int getMinimumReputation()
	{
		int minRep = candidates.get(0).getReputation(faction).get();
		for (Ship candidate : candidates)
		{
			minRep = Math.min(minRep, candidate.getReputation(faction).get());
		}
		return minRep;
	}

	/**
	 * Generates a description of the election.
	 *
	 * @return a description of the election
	 */
	public ColorString getDescription()
	{
		return emergency
			? new ColorString("The ")
				.add(faction)
				.add(" is holding an emergency election for leader.")
			: new ColorString("The scheduled leader election for the ")
				.add(faction)
				.add(" has arrived.");
	}

	/**
	 * Finds candidates, gathers votes, and chooses the winner of election.
	 *
	 * @return the winner of the election who will become the faction's leader
	 * @see #findCandidates()
	 * @see #gatherVotes()
	 * @see #getWinner()
	 */
	public Ship electLeader()
	{
		findCandidates();

		// This biases votes toward higher-reputation candidates
		candidates.sort(Comparator.reverseOrder());
		gatherVotes();
		Ship winner = getWinner();
		lowerWinnerReputation(winner);
		return winner;
	}

	/**
	 * Finds ships in the factions to be candidates in the election.
	 */
	public void findCandidates()
	{
		int minRep = 0;

		for (Ship ship : faction.getGalaxy().getShips())
		{
			if (faction == ship.getFaction() &&
					(ship.getReputation(faction).get() > minRep) ||
					candidates.size() < CANDIDATES)
			{
				candidates.add(ship);

				if (candidates.size() > CANDIDATES)
				{
					candidates.sort(Comparator.naturalOrder());
					candidates.remove(0);
				}

				int lowestRep = ship.getReputation(faction).get();

				for (Ship candidate : candidates)
				{
					if (candidate.getReputation(faction).get() < lowestRep)
					{
						lowestRep = candidate.getReputation(faction).get();
					}
				}

				minRep = lowestRep;
			}
		}

		candidates.sort(Comparator.reverseOrder());
	}

	/**
	 * Gathers votes for the candidates by polling each ship in the faction.
	 *
	 * @throws IllegalStateException if called before findCandidates()
	 * @see #findCandidates()
	 */
	public void gatherVotes()
	{
		if (candidates.isEmpty())
		{
			throw new IllegalStateException(
					"gatherVotes() called before findCandidates()"
			);
		}

		// Fill the vote list with 0s as a starting point
		for (Ship candidate : candidates)
		{
			votes.add(0);
		}

		for (Ship ship : faction.getGalaxy().getShips())
		{
			if (faction == ship.getFaction() && !candidates.contains(ship))
			{
				Ship vote = ship.getAI().vote(candidates);
				int index = candidates.indexOf(vote);
				votes.set(index, votes.get(index) + 1);
			}
		}
	}

	/**
	 * Gets the winner of the election based on the votes for each candidate.
	 * In the event of a tie, will return the candidate earlier in the list.
	 *
	 * @return the winner of the election
	 * @throws IllegalStateException if called before findCandidates()
	 * @see #findCandidates()
	 */
	public Ship getWinner()
	{
		if (candidates.isEmpty())
		{
			throw new IllegalStateException(
					"getWinner() called before findCandidates()"
			);
		}

		int winnerIndex = 0;
		int highestVotes = 0;

		for (int i = 0; i < votes.size(); i++)
		{
			if (votes.get(i) > highestVotes)
			{
				winnerIndex = i;
				highestVotes = votes.get(i);
			}
		}

		return candidates.get(winnerIndex);
	}

	/**
	 * Returns true if, were the given ship to win the election, they would
	 * win.
	 *
	 * @param winner the ship to check as the winner
	 * @return true if the given ship would be reelected
	 */
	public boolean isReelected(Ship winner)
	{
		return faction.getLeader() == winner;
	}

	/**
	 * Lowers the reputation of the winner if they are reelected.
	 *
	 * @param winner the winner of the election
	 * @see #isReelected(Ship)
	 */
	public void lowerWinnerReputation(Ship winner)
	{
		if (isReelected(winner))
		{
			winner.changeReputation(faction, Reputation.REELECTION);
		}
	}

	/**
	 * Adds the player as a candidate in the election, replacing the candidate
	 * with the lowest reputation.
	 */
	public void addPlayer()
	{
		candidates.sort(Comparator.naturalOrder());
		candidates.remove(0);
		candidates.add(player);
		candidates.sort(Comparator.reverseOrder());
	}

	/**
	 * Adds a vote for the given candidate.
	 *
	 * @param candidate the candidate to add a vote for
	 * @throws IllegalStateException if called before findCandidates()
	 * @see #findCandidates()
	 */
	public void addVote(String candidate)
	{
		if (candidates.isEmpty())
		{
			throw new IllegalStateException(
					"addVote() called before findCandidates()"
			);
		}

		for (int i = 0; i < candidates.size(); i++)
		{
			if (candidates.get(i).toString().equals(candidate))
			{
				votes.set(i, votes.get(i) + 1);
				return;
			}
		}
	}
}
