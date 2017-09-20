package boldorf.eversector.map;

/**
 * A type of ore with a name and density.
 * @author Boldorf Smokebane
 */
public class Ore implements Comparable<Ore>
{
    /**
     * The maximum density of any type of ore.
     */
    public static final int DENSITY = 10;

    /**
     * The name of the ore.
     */
    private final String name;

    /**
     * The amount of ore units received each time the ore is mined.
     */
    private final int density;

    /**
     * Creates an ore type with a name and density.
     *
     * @param name    the name of the ore
     * @param density the density of the ore
     */
    public Ore(String name, int density)
    {
        this.name = name;
        this.density = density;
    }

    @Override
    public String toString()
    {
        return name;
    }

    /**
     * Gets the name of the ore.
     *
     * @return the name of the ore
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the density of the ore.
     *
     * @return the density of the ore
     */
    public int getDensity()
    {
        return density;
    }

    @Override
    public int compareTo(Ore other)
    {
        return Integer.compare(density, other.density);
    }
}