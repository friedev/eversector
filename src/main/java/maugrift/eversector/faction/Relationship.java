package maugrift.eversector.faction;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.glyphs.ColorStringObject;
import maugrift.eversector.ships.Ship;
import maugrift.apwt.util.Utility;

import java.awt.*;

import static maugrift.eversector.Main.pendingRelationships;
import static maugrift.eversector.Main.rng;

/**
 * A relationship between factions, containing the two factions and the terms
 * between them.
 *
 * @author Aaron Friesen
 */
public class Relationship
{
	/**
	 * All types of relationships factions can be in.
	 *
	 * @author Aaron Friesen
	 */
	public enum RelationshipType implements ColorStringObject
	{
		/**
		 * Factions are at war.
		 */
		WAR("War", AsciiPanel.brightRed),

		/**
		 * Factions are at peace.
		 */
		PEACE("Peace", null),

		/**
		 * Factions are allied.
		 */
		ALLIANCE("Allied", AsciiPanel.brightGreen);

		/**
		 * A String describing the relationship type.
		 */
		private final String description;

		/**
		 * The color to display the relationship type with.
		 */
		private final Color color;

		/**
		 * Creates a RelationshipType with a description and color.
		 *
		 * @param description the RelationshipType's description
		 * @param color       the RelationshipType's color
		 */
		RelationshipType(String description, Color color)
		{
			this.description = description;
			this.color = color;
		}

		@Override
		public String toString()
		{
			return description;
		}

		/**
		 * Gets the color of the relationship type.
		 *
		 * @return the color of the relationship type
		 */
		public Color getColor()
		{
			return color;
		}

		@Override
		public ColorString toColorString()
		{
			return new ColorString(description, color);
		}
	}

	/**
	 * The first faction in the relationship.
	 */
	private final Faction faction1;

	/**
	 * The second faction in the relationship.
	 */
	private final Faction faction2;

	/**
	 * The type of relationship.
	 */
	private RelationshipType type;

	/**
	 * Creates a new relationship between two factions.
	 *
	 * @param faction1 the first faction
	 * @param faction2 the second faction
	 * @param type     the relationship type
	 */
	public Relationship(Faction faction1, Faction faction2, RelationshipType type)
	{
		this.faction1 = faction1;
		this.faction2 = faction2;
		this.type = type == null ? generateRelationship() : type;
	}

	/**
	 * Creates a new relationship between two factions, randomly choosing a
	 * relationship type.
	 *
	 * @param faction1 the first faction
	 * @param faction2 the second faction
	 */
	public Relationship(Faction faction1, Faction faction2)
	{
		this(faction1, faction2, null);
	}

	/**
	 * Gets the type of relationship.
	 *
	 * @return the relationship type
	 */
	public RelationshipType getType()
	{
		return type;
	}

	/**
	 * Returns the faction in the relationship that is not the one specified.
	 *
	 * @param faction the faction that will not be returned
	 * @return the faction that does not equal the one specified, null if
	 *         neither faction was entered
	 */
	public Faction getOtherFaction(Faction faction)
	{
		if (faction == faction1)
		{
			return faction2;
		}

		if (faction == faction2)
		{
			return faction1;
		}

		return null;
	}

	/**
	 * Returns true if one of the factions in the relationship is the specified
	 * one.
	 *
	 * @param faction the faction to check for in the relationship
	 * @return true if the faction is one of the two involved in this
	 *         relationship
	 */
	public boolean hasFaction(Faction faction)
	{
		return faction1 == faction || faction2 == faction;
	}

	/**
	 * Sets the relationship type.
	 *
	 * @param type the relationship type
	 */
	public void setRelationship(RelationshipType type)
	{
		this.type = type;
	}

	/**
	 * Adds this relationship to both of the factions which it involves.
	 */
	public void addToFactions()
	{
		faction1.addRelationship(this);
		faction2.addRelationship(this);
	}

	/**
	 * Creates a change in relationship and announces it if necessary.
	 *
	 * @return true if a relationship change was requested, even if not
	 *         fulfilled
	 */
	public boolean updateRelationship()
	{
		RelationshipType newRelationship;
		Faction chooser;
		Faction receiver;
		Ship player = faction1.getGalaxy().getPlayer();
		Faction playerFaction = player.getFaction();
		Faction otherFaction = getOtherFaction(playerFaction);
		boolean playerInvolved = otherFaction != null;

		if (playerInvolved && player.isLeader())
		{
			chooser = otherFaction;
		}
		else
		{
			if (rng.nextBoolean())
			{
				chooser = faction1;
			}
			else
			{
				chooser = faction2;
			}
		}

		receiver = getOtherFaction(chooser);
		newRelationship = chooser.chooseRelationship(receiver);

		if (type.equals(newRelationship))
		{
			return false;
		}

		String verb;
		String actingVerb;
		String requestVerb;
		String question;
		boolean changeable = true;
		boolean negateAnswer = false;
		switch (newRelationship)
		{
			case WAR:
				verb = "declared war on";
				actingVerb = verb;
				requestVerb = verb;
				question = null;
				changeable = false;
				break;
			case PEACE:
				if (type == RelationshipType.WAR)
				{
					verb = "made peace with";
					actingVerb = verb;
					requestVerb = "offered a peace treaty to";
					question = "Accept?";
				}
				else
				{
					verb = "broke its alliance with";
					actingVerb = "broke our alliance with";
					requestVerb = verb;
					question = "Propose an extension?";
					negateAnswer = true;
				}
				break;
			case ALLIANCE:
				verb = "formed an alliance with";
				actingVerb = verb;
				requestVerb = "proposed an alliance with";
				question = "Accept?";
				break;
			default:
				// NOT REACHED
				verb = null;
				actingVerb = null;
				requestVerb = null;
				question = null;
				break;
		}

		// Don't print any notifications if they are disabled
		// If this is getting triggered when the notifications are set to
		// something other than none, they may have been set by hibernation
		if (!player.isLeader() || !playerInvolved)
		{
			if (chooser.requestRelationship(receiver, newRelationship))
			{
				chooser.addNews(new ColorString("We " + actingVerb + " the ")
						.add(receiver)
						.add("."));
				receiver.addNews(new ColorString("The ")
						.add(chooser)
						.add(" " + verb + " us."));
			}
			else
			{
				chooser.addNews(new ColorString("We " + requestVerb + " the ")
						.add(receiver)
						.add(", but they refused it."));
				receiver.addNews(new ColorString("The ")
						.add(chooser)
						.add(" " + requestVerb + " us, but we refused it."));
			}

			return true;
		}

		if (!changeable)
		{
			type = newRelationship;
			chooser.addNews(new ColorString("We " + actingVerb + " the ")
					.add(receiver)
					.add("."));
			receiver.addNews(new ColorString("The ")
					.add(chooser)
					.add(" " + verb + " us."));
			return true;
		}

		pendingRelationships.add(
				new RelationshipChange(
					otherFaction,
					newRelationship,
					question,
					new ColorString("The ")
					.add(otherFaction)
					.add(" has " + requestVerb + " you."),
					negateAnswer
				)
		);
		return true;

		/*
		RelationshipType oldRelationship = type;

		if ((answer && !negateAnswer) || negateAnswer)
			type = newRelationship;

		if (answer && negateAnswer)
		{
			if (playerFaction.requestRelationship(otherFaction,
					oldRelationship))
			{
				Prompt.printNotification(
						"The " + otherFaction + " has accepted your offer.");

				// Returning here prevents the news from being updated since the
				// relationship ultimately stayed the same
				return true;
			}
			else
			{
				Prompt.printNotification(
						"The " + otherFaction + " has rejected your offer.");
			}
		}

		if (chooser.requestRelationship(receiver, newRelationship))
		{
			chooser.addNews("We " + actingVerb + " the " + receiver + ".");
			receiver.addNews("The " + chooser + " " + verb + " us.");
		}
		else
		{
			chooser.addNews("We " + requestVerb + " the " + receiver
					+ ", but they refused it.");
			receiver.addNews("The " + chooser + " " + requestVerb
					+ " us, but we refused it.");
		}
		return true;
		*/
	}

	/**
	 * Randomly generates a relationship between the factions.
	 *
	 * @return the generated relationship as a String
	 */
	private RelationshipType generateRelationship()
	{
		return (RelationshipType) Utility.select(
				rng,
				RelationshipType.values(),
				new double[]{0.5, 0.3, 0.2}
		);
	}
}
