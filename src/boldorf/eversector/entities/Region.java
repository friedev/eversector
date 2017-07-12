package boldorf.eversector.entities;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.glyphs.ColorStringObject;
import static boldorf.eversector.Main.rng;
import boldorf.eversector.map.faction.Faction;
import java.util.LinkedList;
import java.util.List;

/** A planetary region with various characteristics. */
public class Region implements ColorStringObject
{
    public static String[] BARREN_TYPES = new String[]
    {
        "Cold", "Cracked", "Cratered", "Dry", "Flat", "Mountainous", "Obsidian",
        "Volcanic"
    };
    
    public static String[] TERRAN_TYPES = new String[]
    {
        "Arctic", "Desertic", "Dune", "Forested", "Hilly", "Marsh", "Mountainous",
        "Oceanic", "Plains", "Rocky", "Tropical", "Tundra"
    };
    
    public static String[] GLACIAL_TYPES = new String[]
    {
        "Arctic", "Barren", "Cavernous", "Cratered", "Flat", "Geyser",
        "Mountainous", "Rocky", "Tundra"
    };
    
    private List<Ship> ships;
    private Planet     planet;
    private String     type;
    private Faction    faction;
    private Ore        ore;
    
    /**
     * Generates a claimed region of the given faction on the given planet.
     * @param p the planet on which to generate the region
     * @param f the faction that starts with control of the region
     */
    public Region(Planet p, Faction f)
    {
        if (p == null)
            throw new NullPointerException();
        
        ships = new LinkedList<>();
        planet = p;
        
        do
        {
            type = generateType();
            // TODO attempt to reduce duplicate region types
        } while (planet.hasRegion(type));
        
        faction  = f;
        ore = planet.getRandomOre();
    }
    
    /**
     * Generates an unclaimed region on the given planet.
     * @param p the planet on which to generate the region
     */
    public Region(Planet p)
        {this(p, null);}
    
    @Override
    public String toString()
        {return type + " Region";}
    
    @Override
    public ColorString toColorString()
    {
        return isClaimed() ? new ColorString(toString(), faction.getColor()) :
                new ColorString(toString());
    }
    
    public Planet  getPlanet()   {return planet;                    }
    public String  getType()     {return type;                      }
    public Faction getFaction()  {return faction;                   }
    public Ore     getOre()      {return ore;                       }
    public boolean isClaimed()   {return faction != null;           }
    public boolean hasOre()      {return ore != null;               }
    public List<Ship> getShips() {return ships;                     }
    
    /**
     * Claims the region for a given faction and updates the faction of the
     * planet it's on to match.
     * @param f the faction that will claim the region
     */
    public void claim(Faction f)
    {
        if (faction == f)
            return;
        
        faction = f;
        planet.updateFaction();
    }
    
    /**
     * Returns the number of ships that belong to a specified faction.
     * @param faction the faction to count ships in
     * @return the number of ships docked with the station that belong to the
     * faction, will be non-negative
     */
    public int getNShips(Faction faction)
    {
        int nShips = 0;
        
        for (Ship ship: ships)
            if (ship.isInFaction(faction))
                nShips++;
        
        return nShips;
    }
    
    /**
     * Generates a type from the list of valid types for the type of planet the
     * region is on.
     * @return a String representing the type of the region, generated based on
     * the planet's type
     */
    private String generateType()
    {
        switch (planet.getType())
        {
            case BARREN_PLANET:
                return (String) rng.getRandomElement(BARREN_TYPES);
            case TERRAN_PLANET:
                return (String) rng.getRandomElement(TERRAN_TYPES);
            case GLACIAL_PLANET:
                return (String) rng.getRandomElement(GLACIAL_TYPES);
            default:
                return null;
        }
    }
}