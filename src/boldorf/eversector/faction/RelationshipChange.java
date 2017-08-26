package boldorf.eversector.faction;

import boldorf.apwt.glyphs.ColorString;
import boldorf.eversector.faction.Relationship.RelationshipType;

/**
 * 
 */
public class RelationshipChange
{
    private Faction otherFaction;
    private RelationshipType relationship;
    private String question;
    private ColorString message;
    private boolean negateAnswer;
    
    public RelationshipChange(Faction otherFaction,
            RelationshipType relationship, String question, ColorString message,
            boolean negateAnswer)
    {
        this.otherFaction = otherFaction;
        this.relationship = relationship;
        this.question = question;
        this.message = message;
        this.negateAnswer = negateAnswer;
    }
    
    public Faction getOtherFaction()
        {return otherFaction;}
    
    public RelationshipType getRelationship()
        {return relationship;}
    
    public String getQuestion()
        {return question;}
    
    public ColorString getMessage()
        {return message;}
    
    public boolean negateAnswer()
        {return negateAnswer;}
}