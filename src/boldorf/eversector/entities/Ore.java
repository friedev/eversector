package boldorf.eversector.entities;

/** A type of ore with a name and density. */
public class Ore implements Comparable<Ore>
{
    /** The maximum density of any type of ore. */
    public static final int DENSITY = 10;
    
    private String name;
    private int density;
    
    public Ore(String name, int density)
    {
        this.name = name;
        this.density = density;
    }
    
    @Override
    public String toString()
        {return name;}
    
    public String getName()
        {return name;}
    
    public int getDensity()
        {return density;}
    
    public void setDensity(int density)
        {this.density = density;}

    @Override
    public int compareTo(Ore other)
        {return Integer.compare(density, other.density);}
}