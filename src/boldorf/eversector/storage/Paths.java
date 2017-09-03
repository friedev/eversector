package boldorf.eversector.storage;

/** A wrapper class for all of the file paths loaded from the main manifest. */
public abstract class Paths
{
    public static final String
    // General data files
    SAVE        = "local/save.propeties",
    OPTIONS     = "local/options.properties",
    LEADERBOARD = "local/leaderboard/",
    // Images
    FONTS       = "assets/fonts/",
    ICON        = "assets/icon.png",
    // Audio files
    SOUNDTRACK  = "audio/soundtrack.wav",
    START       = "audio/start.wav",
    ON          = "audio/on.wav",
    OFF         = "audio/off.wav",
    ENGINE      = "audio/enginve.wav",
    MINE        = "audio/mine.wav",
    DOCK        = "audio/dock.wav",
    TRANSACTION = "audio/transaction.wav", 
    CLAIM       = "audio/claim.wav",
    DISTRESS    = "audio/distress.wav",
    SCAN        = "audio/scan.wav",
    REFINE      = "audio/refine.wav",
    WARP        = "audio/warp.wav",
    LASER       = "audio/laser.wav",
    TORPEDO     = "audio/torpedo.wav",
    PULSE       = "audio/pulse.wav",
    DEATH       = "audio/death.wav";
}