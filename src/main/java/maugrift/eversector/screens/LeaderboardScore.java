package maugrift.eversector.screens;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;
import maugrift.eversector.Paths;
import maugrift.apwt.util.FileManager;
import maugrift.apwt.util.Utility;

import java.io.IOException;
import java.util.*;

import static maugrift.eversector.screens.EndScreen.COLOR_HEADER;

/**
 * A score on the leaderboard, consisting of a  name, credits, and reputation.
 *
 * @author Maugrift
 */
public class LeaderboardScore implements Comparable<LeaderboardScore>
{
	/**
	 * The number of scores to display on the leaderboard.
	 */
	public static final int DISPLAYED_SCORES = 5;

	/**
	 * The string used in place of invalid fields.
	 */
	private static final String INVALID = "INVALID";

	/**
	 * The name of the ship's captain in the score.
	 */
	private String name;

	/**
	 * The value of the ship in credits.
	 */
	private Integer score;

	/**
	 * The number of turns played.
	 */
	private Integer turns;

	/**
	 * The number of ships destroyed.
	 */
	private Integer kills;

	/**
	 * The reputation of the ship.
	 */
	private String reputation;

	/**
	 * True if the ship became a leader.
	 */
	private boolean leader;

	/**
	 * Loads a LeaderboardScore from a Properties object.
	 *
	 * @param properties the Properties object containing information about the score
	 */
	public LeaderboardScore(Properties properties)
	{
		if (properties == null || properties.isEmpty())
		{
			throw new IllegalArgumentException();
		}

		name = properties.getProperty("shipName");
		score = Utility.parseInt(properties.getProperty("score"));
		turns = Utility.parseInt(properties.getProperty("turns"));
		kills = Utility.parseInt(properties.getProperty("kills"));
		reputation = properties.getProperty("reputation");
		leader = "true".equals(properties.getProperty("leader"));
	}

	/**
	 * Creates a new LeaderboardScore from its three components.
	 *
	 * @param name       the name of the captain to attribute the score to
	 * @param score      the score
	 * @param turns      the number of turns played
	 * @param kills      the number of ships destroyed
	 * @param reputation the reputation
	 * @param leader     true if the player was a leader
	 */
	public LeaderboardScore(String name, int score, int turns, int kills, String reputation, boolean leader)
	{
		this.name = name;
		this.score = score;
		this.turns = turns;
		this.kills = kills;
		this.reputation = reputation;
		this.leader = leader;
	}

	/**
	 * Creates a new LeaderboardScore without a name accompanying it.
	 *
	 * @param score      the score
	 * @param turns      the number of turns played
	 * @param kills      the number of ships destroyed
	 * @param reputation the reputation of the score
	 * @param leader     true if the player was a leader
	 */
	public LeaderboardScore(int score, int turns, int kills, String reputation, boolean leader)
	{
		this(null, score, turns, kills, reputation, leader);
	}

	@Override
	public String toString()
	{
		if (!isValid())
		{
			return INVALID;
		}

		StringBuilder builder = new StringBuilder();

		if (name != null)
		{
			builder.append(name).append(": ");
		}

		builder.append(score).append(" Credits, ").append(turns).append(" Turns, ");

		if (kills > 0)
		{
			builder.append(kills).append(" ").append(Utility.makePlural("Kill", kills)).append(", ");
		}

		builder.append(reputation);
		if (leader)
		{
			builder.append(" (Leader)");
		}

		return builder.toString();
	}

	/**
	 * Returns a Properties object representing the score.
	 *
	 * @return a Properties object representing the score
	 */
	public Properties toProperties()
	{
		Properties properties = new Properties();

		if (name != null)
		{
			properties.setProperty("shipName", name);
		}

		properties.setProperty("score", score.toString());
		properties.setProperty("turns", turns.toString());
		properties.setProperty("kills", kills.toString());
		properties.setProperty("reputation", reputation);
		properties.setProperty("leader", Boolean.toString(leader));
		return properties;
	}

	/**
	 * Returns true if the two required fields (score and reputation) are set.
	 *
	 * @return true if the score and reputation of the object are not their default values
	 */
	public boolean isValid()
	{
		return score != null && turns != null && kills != null && reputation != null;
	}

	@Override
	public int compareTo(LeaderboardScore other)
	{
		return Integer.compare(score, other.score);
	}

	/**
	 * Constructs the leaderboard window contents.
	 *
	 * @return the leaderboard as window contents
	 */
	public static List<ColorString> buildLeaderboard()
	{
		List<ColorString> leaderboard = new LinkedList<>();
		FileManager.createContainingFolders(Paths.LEADERBOARD);
		List<LeaderboardScore> scores = getLeaderboardScores();

		if (scores == null || scores.isEmpty())
		{
			return leaderboard;
		}

		leaderboard.add(buildLeaderboardHeader(scores.size()));
		for (int i = 0; i < Math.min(scores.size(), DISPLAYED_SCORES); i++)
		{
			leaderboard.add(new ColorString(scores.get(i).toString()));
		}

		return leaderboard;
	}

	/**
	 * Builds the header of the leaderboard.
	 *
	 * @param nScores the total number of scores on the leaderboard
	 * @return the header of the leaderboard
	 */
	public static ColorString buildLeaderboardHeader(int nScores)
	{
		ColorString header = new ColorString("LEADERBOARD", COLOR_HEADER);
		if (nScores > DISPLAYED_SCORES)
		{
			header.add(new ColorString(" (" + nScores + " Scores Total)", AsciiPanel.brightBlack));
		}
		return header;
	}

	/**
	 * Returns a sorted list of every leaderboard score.
	 *
	 * @return an ArrayList of Integers parsed from the leaderboard file and sorted from greatest to least
	 */
	public static List<LeaderboardScore> getLeaderboardScores()
	{
		List<LeaderboardScore> scores = new ArrayList<>();

		try
		{
			int index = 1;
			while (scores.add(
					new LeaderboardScore(FileManager.load(Paths.LEADERBOARD + "score_" + index + ".properties"))))
			{
				index++;
			}
		}
		catch (IllegalArgumentException | IOException e)
		{
		}
		// Do nothing, but stop the loop

		// Sort scores from highest to lowest
		scores.sort(Comparator.reverseOrder());
		return scores;
	}
}