package boldorf.eversector.storage;

/** A wrapper class for all of the file paths loaded from the main manifest. */
public abstract class Paths
{
    public static final String
    // General data files
    SAVE        = "local/save.propeties",
    OPTIONS     = "local/options.properties",
    LEADERBOARD = "local/leaderboard/",
    CRASH       = "crash.txt",
    // Images
    FONTS       = "assets/fonts/",
    ICON        = "assets/icon.png",
    // Audio files
    SOUNDTRACK  = "assets/audio/soundtrack.wav",
    START       = "assets/audio/start.wav",
    ON          = "assets/audio/on.wav",
    OFF         = "assets/audio/off.wav",
    ENGINE      = "assets/audio/enginve.wav",
    MINE        = "assets/audio/mine.wav",
    DOCK        = "assets/audio/dock.wav",
    TRANSACTION = "assets/audio/transaction.wav", 
    CLAIM       = "assets/audio/claim.wav",
    DISTRESS    = "assets/audio/distress.wav",
    SCAN        = "assets/audio/scan.wav",
    REFINE      = "assets/audio/refine.wav",
    WARP        = "assets/audio/warp.wav",
    LASER       = "assets/audio/laser.wav",
    TORPEDO     = "assets/audio/torpedo.wav",
    PULSE       = "assets/audio/pulse.wav",
    DEATH       = "assets/audio/death.wav";
}