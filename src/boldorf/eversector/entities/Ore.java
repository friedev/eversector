package boldorf.eversector.entities;

import boldorf.util.Nameable;

/** A type of ore with a name and density. */
public class Ore extends Nameable implements Comparable<Ore>
{
    /** The maximum density of any type of ore. */
    public static final int DENSITY = 10;
    
    private int density;
    
    public Ore(String n, int d)
    {
        super(n);
        density = d;
    }
    
    public int getDensity()
        {return density;}
    public void setDensity(int d)
        {density = d;}

    @Override
    public int compareTo(Ore other)
        {return Integer.compare(density, other.density);}
}