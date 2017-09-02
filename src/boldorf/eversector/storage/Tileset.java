package boldorf.eversector.storage;

import asciiPanel.AsciiFont;
import boldorf.util.FileManager;

/**
 * 
 */
public enum Tileset
{
    QBICFEET("qbicfeet", 10),
    ALLOY("Alloy", 12),
    COOZ("Cooz", 16);
    
    private String name;
    private int size;

    Tileset(String name, int size)
    {
        this.name = name;
        this.size = size;
    }
    
    public String getName()
        {return name;}
    
    public int getSize()
        {return size;}
    
    public AsciiFont toFont(boolean tiles)
    {
        String fileName = name.toLowerCase() + "_" + (tiles ? "tiles" : "ascii")
                + ".png";
        return new AsciiFont(FileManager.getPath() + "assets/fonts/"
                + fileName, size, size);
    }
}