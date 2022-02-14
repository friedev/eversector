package maugrift.eversector.ships;

import asciiPanel.AsciiPanel;
import maugrift.eversector.faction.Faction;

import java.awt.Color;

/**
 * A container class for an integer representing reputation, and the faction
 * the reputation is with.
 *
 * @author Aaron Friesen
 */
public class Reputation implements Comparable<Reputation>
{
	/**
	 * The reputation change for mining.
	 */
	public static final int MINE = 3;

	/**
	 * The reputation change for mining a region dry of ore.
	 */
	public static final int MINE_DRY = -30;

	/**
	 * The reputation change for joining a faction.
	 */
	public static final int JOIN = 20;

	/**
	 * The reputation change for leaving a faction.
	 */
	public static final int LEAVE = -50;

	/**
	 * The reputation change for killing an enemy.
	 */
	public static final int KILL_ENEMY = 125;

	/**
	 * The reputation change for killing a peaceful or allied ship.
	 */
	public static final int KILL_ALLY = -250;

	/**
	 * The reputation change for converting a ship to your faction.
	 */
	public static final int CONVERT = 100;

	/**
	 * The reputation change for claiming territory for your faction. This
	 * decreases on planets based on the number of regions.
	 */
	public static final int CLAIM = 175;

	/**
	 * The reputation change for claiming territory for your faction that was
	 * formerly owned by an allied faction.
	 */
	public static final int CLAIM_ALLY = 100;

	/**
	 * The reputation change for sending a distress signal with no response.
	 */
	public static final int DISTRESS_ATTEMPT = -10;

	/**
	 * The reputation change for sending a distress signal and receiving help.
	 */
	public static final int DISTRESS = -100;

	/**
	 * The reputation change for being reelected.
	 */
	public static final int REELECTION = -100;

	/**
	 * The reputation at which a ship is rejected from their faction.
	 */
	public static final int REJECTION = -100;

	/**
	 * A range of reputations and its descriptors.
	 */
	public enum ReputationRange
	{
		HEROIC("Heroic", "Admires", AsciiPanel.brightYellow, 4.0, 4.1),
		RESPECTED("Respected", "Respects", AsciiPanel.brightGreen, 1.5, 4.0),
		POSITIVE("Positive", "Likes", AsciiPanel.green, 0.5, 1.5),
		NEGATIVE("Negative", "Dislikes", AsciiPanel.red, -1.5, -0.5),
		DESPISED("Despised", "Despises", AsciiPanel.brightRed, -4.0, -1.5),
		INFAMOUS("Infamous", "Loathes", AsciiPanel.brightMagenta, -4.1, -4.0),
		NEUTRAL("Neutral", "Ignores", null, -0.5, 0.5);

		/**
		 * The default reputation range.
		 */
		public static final ReputationRange DEFAULT = NEUTRAL;

		/**
		 * The adjective used to describe ships of this reputation.
		 */
		private String adjective;

		/**
		 * The verb describing the faction's feelings toward ships of this
		 * reputation.
		 */
		private String verb;

		/**
		 * The color of the reputation range.
		 */
		private Color color;

		/**
		 * The fraction of the average reputation defining the lower bound of
		 * this range.
		 */
		private double min;

		/**
		 * The fraction of the average reputation defining the upper bound of
		 * this range.
		 */
		private double max;

		/**
		 * Creates a reputation range with all fields defined.
		 *
		 * @param adjective the adjective describing ships of this reputation
		 * @param verb      the verb describing the faction's feelings toward
		 *                  ships of this reputation
		 * @param color     the color of the reputation range
		 * @param min       the fraction of the average reputation defining the
		 *                  lower bound of this range
		 * @param max       the fraction of the average reputation defining the
		 *                  upper bound of this range
		 */
		ReputationRange(
				String adjective,
				String verb,
				Color color,
				double min,
				double max
		) {
			this.adjective = adjective;
			this.verb = verb;
			this.color = color;
			this.min = min;
			this.max = max;
		}

		/**
		 * Gets the adjective used to describe ships of this reputation.
		 *
		 * @return the adjective used to describe ships of this reputation
		 */
		public String getAdjective()
		{
			return adjective;
		}

		/**
		 * Gets the verb describing the faction's feelings toward ships of this
		 * reputation.
		 *
		 * @return the verb describing the faction's feelings toward ships of
		 *         this reputation
		 */
		public String getVerb()
		{
			return verb;
		}

		/**
		 * Gets the color of the reputation range.
		 *
		 * @return the color of the reputation range
		 */
		public Color getColor()
		{
			return color;
		}

		/**
		 * Gets the fraction of the average reputation defining the lower bound
		 * of this range.
		 *
		 * @return the fraction of the average reputation defining the lower
		 *         bound of this range
		 */
		public double getMin()
		{
			return min;
		}

		/**
		 * Gets the fraction of the average reputation defining the upper bound
		 * of this range.
		 *
		 * @return the fraction of the average reputation defining the upper
		 *         bound of this range
		 */
		public double getMax()
		{
			return max;
		}

		/**
		 * Gets the actual lower bound of the range for the given extreme
		 * reputation.
		 *
		 * @return the actual lower bound of the range
		 */
		public double getMin(double range)
		{
			return min * Math.abs(range);
		}

		/**
		 * Gets the actual upper bound of the range for the given extreme
		 * reputation.
		 *
		 * @return the actual upper bound of the range
		 */
		public double getMax(double range)
		{
			return max * Math.abs(range);
		}

		/**
		 * Returns true if the given value is in the actual range for the given
		 * extreme reputation.
		 *
		 * @param value the value to check
		 * @param range the average reputation with the faction
		 * @return true if the given value is in the actual reputation range
		 */
		public boolean isInRange(double value, double range)
		{
			return value >= getMin(range) && value <= getMax(range);
		}

		/**
		 * Gets the highest reputation range.
		 *
		 * @return the highest reputation range
		 */
		public static ReputationRange getHighestRange()
		{
			ReputationRange highestRange = DEFAULT;
			for (ReputationRange range : values())
			{
				if (range.getMax() > highestRange.getMax())
				{
					highestRange = range;
				}
			}
			return highestRange;
		}

		/**
		 * Gets the lowest reputation range.
		 *
		 * @return the lowest reputation range
		 */
		public static ReputationRange getLowestRange()
		{
			ReputationRange lowestRange = DEFAULT;
			for (ReputationRange range : values())
			{
				if (range.getMin() < lowestRange.getMin())
				{
					lowestRange = range;
				}
			}
			return lowestRange;
		}
	}

	/**
	 * The amount of reputation.
	 */
	private int reputation;

	/**
	 * The faction the reputation is with.
	 */
	private Faction faction;

	/**
	 * Creates reputation at the given level for the faction.
	 *
	 * @param reputation the reputation to start with
	 * @param faction    the faction the reputation is with
	 */
	public Reputation(int reputation, Faction faction)
	{
		this.reputation = reputation;
		this.faction = faction;
	}

	/**
	 * Creates a new reputation for the faction, starting at 0.
	 *
	 * @param faction the faction the reputation is with
	 */
	public Reputation(Faction faction)
	{
		this(0, faction);
	}

	/**
	 * Gets the amount of reputation.
	 *
	 * @return the amount of reputation
	 */
	public int get()
	{
		return reputation;
	}

	/**
	 * Changes the amount of reputation by the given value.
	 *
	 * @param change the amount to change the reputation by
	 */
	public void change(int change)
	{
		this.reputation += change;
	}

	/**
	 * Gets the faction the reputation is with.
	 *
	 * @return the faction the reputation is with
	 */
	public Faction getFaction()
	{
		return faction;
	}

	@Override
	public int compareTo(Reputation other)
	{
		return Integer.compare(reputation, other.reputation);
	}

	/**
	 * Gets the reputation range that this reputation value lies in.
	 *
	 * @return the reputation range that this reputation value lies in
	 */
	public ReputationRange getRange()
	{
		double range = faction.getAverageReputation();

		for (ReputationRange rangeLevel : ReputationRange.values())
		{
			if (rangeLevel.isInRange(reputation, range))
			{
				return rangeLevel;
			}
		}

		if (reputation >= ReputationRange.getHighestRange().getMax(range))
		{
			return ReputationRange.getHighestRange();
		}

		if (reputation <= ReputationRange.getLowestRange().getMin(range))
		{
			return ReputationRange.getLowestRange();
		}

		return ReputationRange.DEFAULT;
	}
}
