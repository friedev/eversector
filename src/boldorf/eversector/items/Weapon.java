package boldorf.eversector.items;

import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.COLOR_FIELD;
import boldorf.eversector.storage.Resources;
import java.util.List;
import java.util.Properties;

/** A module that also holds the amount of damage it deals. */
public class Weapon extends Module
{
    /** The hull damage the weapon can inflict on an undefended ship. */
    int damage;
    
    /**
     * Copying constructor that creates a new weapon identical to the one
     * provided.
     * @param w the weapon to create a copy of
     */
    public Weapon(Weapon w)
    {this (w.getName(), w.getDescription(), w.getValue(), w.damage, w.action);}
    
    /**
     * Creates a new weapon with a name, description, value, damage, and action.
     * @param n the name of the weapon
     * @param de the description of the weapon
     * @param v the value of the weapon
     * @param da the damage of the weapon
     * @param a the action the weapon can perform
     */
    public Weapon(String n, String de, int v, int da, Action a)
    {
        super(n, de, v, true, a);
        damage = Math.abs(da);
    }
    
    /**
     * Creates a new weapon from a set of Properties.
     * @param properties the Properties object used in construction of the
     * weapon
     */
    public Weapon(Properties properties)
    {
        super(properties);
        damage = Math.abs(Integer.parseInt(properties.getProperty("damage")));
    }
    
    /**
     * Returns the hull damage the weapon can inflict on an undefended ship.
     * @return the damage of the weapon
     */
    public int getDamage()
        {return damage;}
    
    /**
     * Returns true if the weapon uses energy.
     * @return true if energy is the resource depleted by the weapon's action
     */
    public boolean isEnergy()
        {return Resources.ENERGY.equals(action.getResource());}
    
    @Override
    public List<ColorString> define()
    {
        List<ColorString> definition = super.define();
        definition.add(2, new ColorString("Damage: ")
                .add(new ColorString(Integer.toString(damage), COLOR_FIELD)));
        definition.add(3, new ColorString("Type: ")
                .add(new ColorString(isEnergy() ? "Energy" : "Physical",
                        COLOR_FIELD)));
        return definition;
    }
}