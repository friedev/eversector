package boldorf.eversector.entities;

import boldorf.util.Nameable;
import boldorf.eversector.map.Sector;

/** Any object that orbits in a sector. */
public abstract class Satellite extends Nameable
{
    /** The default orbit which to place satellites. */
    public static final int ORBIT = 1;
    
    /** The orbit level at which the satellite orbits. */
    private int orbit;
    
    /**
     * Creates a satellite with the given name and orbit.
     * @param n the name of the satellite
     * @param o the orbit level at which the satellite will orbit initially
     */
    public Satellite(String n, int o)
    {
        super(n);
        
        if (o >= 0 && o <= Sector.MAX_ORBITS)
            orbit = o;
        else
            orbit = ORBIT;
    }
    
    /**
     * Creates a satellite with the given name and the default orbit.
     * @param n the name of the satellite
     */
    public Satellite(String n)
        {this(n, ORBIT);}
    
    /**
     * Returns the orbit level the satellite is orbiting at.
     * @return the current orbit level of the satellite
     */
    public int getOrbit()
        {return orbit;}
    
    /**
     * Sets the orbit to the closest orbit level in bounds to the one specified.
     * @param o the orbit level to attempt placing the satellite at
     */
    public void setOrbit(int o)
        {orbit = Math.max(0, Math.min(o, Sector.MAX_ORBITS));}
    
    /**
     * Returns true if the satellite is in an orbit.
     * @return true if the orbit of the satellite is greater than zero
     */
    public boolean isOrbiting()
        {return orbit > 0;}
}