package boldorf.eversector.storage;

import boldorf.util.FileManager;
import java.io.IOException;
import java.util.Properties;

/** A wrapper class for all of the file paths loaded from the main manifest. */
public abstract class Paths
{
    public static final String MANIFEST_PATH = "manifest.properties";
    
    public static String
    // General data files
        SAVE,
        OPTIONS,
        LEADERBOARD,
    // Audio files
        SOUNDTRACK,
        START,
        ON,
        OFF,
        ENGINE,
        MINE,
        CLAIM,
        DISTRESS,
        SCAN,
        REFINE,
        WARP,
        LASER,
        TORPEDO,
        PULSE,
        DEATH,
    // Station manifests
        MODULES,
        WEAPONS,
        RESOURCES,
        EXPANDERS;
    
    public static void initialize() throws IOException
        {initialize(FileManager.load(MANIFEST_PATH));}
    
    public static void initialize(Properties manifest)
    {
        // General data file
        SAVE        = manifest.getProperty("save");
        OPTIONS     = manifest.getProperty("options");
        LEADERBOARD = manifest.getProperty("leaderboard");
        
        // Audio files
        SOUNDTRACK = manifest.getProperty("soundtrack");
        START      = manifest.getProperty("start");
        ON         = manifest.getProperty("on");
        OFF        = manifest.getProperty("off");
        ENGINE     = manifest.getProperty("engine");
        MINE       = manifest.getProperty("mine");
        CLAIM      = manifest.getProperty("claim");
        DISTRESS   = manifest.getProperty("distress");
        SCAN       = manifest.getProperty("scan");
        REFINE     = manifest.getProperty("refine");
        WARP       = manifest.getProperty("warp");
        LASER      = manifest.getProperty("laser");
        TORPEDO    = manifest.getProperty("torpedo");
        PULSE      = manifest.getProperty("pulse");
        DEATH      = manifest.getProperty("death");

        // Station manifests
        MODULES   = manifest.getProperty("modules");
        WEAPONS   = manifest.getProperty("weapons");
        RESOURCES = manifest.getProperty("resources");
        EXPANDERS = manifest.getProperty("expanders");
    }
}