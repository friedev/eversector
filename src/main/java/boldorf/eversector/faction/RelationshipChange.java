package boldorf.eversector.faction;

import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.faction.Relationship.RelationshipType;

/**
 * A class to store information about a relationship change for one faction.
 *
 * @author Maugrift
 */
public class RelationshipChange
{
    /**
     * The other faction in the relationship. Generally the first faction in the relationship is the one creating the
     * class.
     */
    private final Faction otherFaction;

    /**
     * The relationship type to change to.
     */
    private final RelationshipType relationship;

    /**
     * The question posed to the leader of the other faction.
     */
    private final String question;

    /**
     * The message displayed as news once the change is complete.
     */
    private final ColorString message;

    /**
     * If true, will negate the answer to the question. By default, saying yes to the question will agree to the
     * change.
     */
    private final boolean negateAnswer;

    /**
     * Creates a new relationship change.
     *
     * @param otherFaction the other faction
     * @param relationship the relationship type
     * @param question     the question
     * @param message      the message
     * @param negateAnswer true if the answer will be negated
     */
    public RelationshipChange(Faction otherFaction, RelationshipType relationship, String question, ColorString message,
                              boolean negateAnswer)
    {
        this.otherFaction = otherFaction;
        this.relationship = relationship;
        this.question = question;
        this.message = message;
        this.negateAnswer = negateAnswer;
    }

    /**
     * Gets the other faction.
     *
     * @return the other faction
     */
    public Faction getOtherFaction()
    {
        return otherFaction;
    }

    /**
     * Gets the relationship type.
     *
     * @return the relationship type
     */
    public RelationshipType getRelationship()
    {
        return relationship;
    }

    /**
     * Gets the question.
     *
     * @return the question
     */
    public String getQuestion()
    {
        return question;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public ColorString getMessage()
    {
        return message;
    }

    /**
     * Returns true if the answer is negated.
     *
     * @return true if the answer is negated
     */
    public boolean negateAnswer()
    {
        return negateAnswer;
    }
}