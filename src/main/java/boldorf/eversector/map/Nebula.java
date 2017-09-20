package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;

import java.awt.Color;

/**
 * A cloud of gas and dust in space that blocks vision.
 * @author Boldorf Smokebane
 */
public enum Nebula implements ColorStringObject
{
    /**
     * Emission nebula. Color is created by the emission of charged particles for large stars. Quite hot and energetic
     * compared to other nebulae.
     */
    EMISSION("Emission", AsciiPanel.red),

    /**
     * Reflection nebula. Consists of gas and dust that reflect light from nearby stars.
     */
    REFLECTION("Reflection", AsciiPanel.cyan),

    /**
     * Dark nebula. Made primarily of dust, thick enough to block light from stars.
     */
    DARK("Dark", new Color(48, 48, 48));

    /**
     * The name of the nebula.
     */
    private final String name;

    /**
     * The color of the nebula.
     */
    private final Color color;

    /**
     * Creates a nebula with a name and a color.
     *
     * @param name  the nebula's name
     * @param color the nebula's color
     */
    Nebula(String name, Color color)
    {
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString()
    {
        return name + " Nebula";
    }

    @Override
    public ColorString toColorString()
    {
        return new ColorString(toString(), color);
    }

    /**
     * Gets the name of the nebula.
     *
     * @return the name of the nebula
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the color of the nebula.
     *
     * @return the color of the nebula
     */
    public Color getColor()
    {
        return color;
    }
}