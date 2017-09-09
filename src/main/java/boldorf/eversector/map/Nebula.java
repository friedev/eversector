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
    EMISION   ("Emission",   AsciiPanel.red       ),
    REFLECTION("Reflection", AsciiPanel.cyan      ),
    DARK      ("Dark",       new Color(48, 48, 48));
    
    private String name;
    private Color  color;
    
    Nebula(String name, Color color)
    {
        this.name    = name;
        this.color   = color;
    }
    
    @Override
    public String toString()
        {return name + " Nebula";}
    
    @Override
    public ColorString toColorString()
        {return new ColorString(toString(), color);}
    
    public Color getColor()
        {return color;}
}