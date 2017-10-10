package boldorf.eversector.map;

/**
 * A type of ore with a name and density.
 *
 * @author Maugrift
 */
public class Ore implements Comparable<Ore>
{
    public static final String[] NAME_PREFIX = new String[]{
            "Al",
            "Aux",
            "Bith",
            "Burl",
            "Bar",
            "Cen",
            "Chul",
            "Churr",
            "Cirr",
            "Clar",
            "Coll",
            "Dar",
            "Davn",
            "Dyn",
            "El",
            "Err",
            "Etern",
            "Hex",
            "Hurn",
            "Hyp",
            "Hyz",
            "Ir",
            "Ilt",
            "Ith",
            "Ix",
            "Jal",
            "Jalm",
            "Jav",
            "Jov",
            "Kelv",
            "Kev",
            "Lar",
            "Len",
            "Lox",
            "Maun",
            "Max",
            "Murn",
            "Myth",
            "Nal",
            "Nil",
            "Non",
            "Off",
            "Oft",
            "Olk",
            "Ord",
            "Pax",
            "Parn",
            "Qual",
            "Quan",
            "Quin",
            "Ran",
            "Rar",
            "Rath",
            "Rob",
            "Roz",
            "Sar",
            "Sel",
            "Soph",
            "Soth",
            "Star",
            "Stan",
            "Tath",
            "Tan",
            "Tec",
            "Tech",
            "Tof",
            "Toth",
            "Ul",
            "Ur",
            "Un",
            "Unad",
            "Val",
            "Vaz",
            "Vex",
            "Vox",
            "Xel",
            "Xor",
            "Zan",
            "Zith",
            "Zor"
    };

    public static final String[] NAME_SUFFIX = new String[]{
            "agen",
            "ine",
            "inite",
            "inium",
            "inum",
            "inyte",
            "ion",
            "ite",
            "ium",
            "ogen",
            "on",
            "onite",
            "onium",
            "onum",
            "onyte",
            "um",
            "ygen",
            "yte"
    };

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