package boldorf.eversector.storage;

/** A wrapper class for all the reputation changes applied to ships. */
public abstract class Reputations
{
    public static int
    MINE          = 3,
    JOIN          = 20,
    LEAVE         = -50,
    KILL_ENEMY    = 75,
    KILL_ALLY     = -200,
    CENTER_ATTACK = -125,
    CONVERT       = 100,
    CLAIM         = 175,
    CLAIM_ALLY    = 100,
    DISTRESS      = -100,
    NO_ELECTION   = -100,
    REQ_SUPPORT   = 100,
    REQ_REJECTION = -100;
}