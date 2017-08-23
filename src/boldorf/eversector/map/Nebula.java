package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import java.awt.Color;

/**
 * 
 */
public enum Nebula implements ColorStringObject
{
    EMISION   ("Emission",   AsciiPanel.red,        1.0 / 3.0),
    REFLECTION("Reflection", AsciiPanel.cyan,       1.0 / 3.0),
    DARK      ("Dark",       new Color(48, 48, 48), 2.0 / 3.0);
    
    private String name;
    private Color  color;
    private double opacity;
    
    Nebula(String name, Color color, double opacity)
    {
        this.name    = name;
        this.color   = color;
        this.opacity = opacity;
    }
    
    @Override
    public String toString()
        {return name + " Nebula";}
    
    @Override
    public ColorString toColorString()
        {return new ColorString(toString(), color);}
    
    public Color getColor()
        {return color;}
    
    public double getOpacity()
        {return opacity;}
}