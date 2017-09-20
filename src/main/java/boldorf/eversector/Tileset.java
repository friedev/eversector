package boldorf.eversector;

import asciiPanel.AsciiFont;
import boldorf.util.FileManager;

/**
 * The basic data for each tileset.
 */
public enum Tileset
{
    /**
     * The qbcifeet tileset, as seen in AsciiPanel.
     */
    QBICFEET("qbicfeet", 10),

    /**
     * Alloy's curses-like Dwarf Fortress tileset.
     */
    ALLOY("Alloy", 12),

    /**
     * Cooz's curses-like Dwarf Fortress tileset.
     */
    COOZ("Cooz", 16);

    /**
     * The display name and filename of the tileset.
     */
    private final String name;

    /**
     * The side length of the tiles in the set, in pixels.
     */
    private final int size;

    /**
     * Creates a tileset with all fields defined.
     *
     * @param name the name of the tileset
     * @param size the size of the tiles in the set, in pixels
     */
    Tileset(String name, int size)
    {
        this.name = name;
        this.size = size;
    }

    /**
     * Gets the display name and filename of the tileset.
     *
     * @return the name of the tileset
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the size of the tiles in the set, in pixels.
     *
     * @return the size of the tiles in the set, in pixels
     */
    public int getSize()
    {
        return size;
    }

    /**
     * Loads the tileset's AsciiFont.
     *
     * @param tiles if true, will load the version of the font with tiles
     * @return the loaded AsciiFont
     */
    public AsciiFont toFont(boolean tiles)
    {
        String fileName = name.toLowerCase() + "_" + (tiles ? "tiles" : "ascii") + ".png";
        return new AsciiFont(FileManager.getPath() + Paths.FONTS + fileName, size, size);
    }
}