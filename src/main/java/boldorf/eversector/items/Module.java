package boldorf.eversector.items;

import java.util.Properties;

/**
 * A version of an item designed to perform an action.
 *
 * @author Boldorf Smokebane
 */
public class Module extends Item
{
    /**
     * The action that the module can perform.
     */
    private final Action action;

    /**
     * True if the module is sold at a battle station.
     */
    private final boolean battle;

    /**
     * The effect the module applies when activated.
     */
    private final String effect;

    /**
     * True if the module has been damaged.
     */
    private boolean isDamaged;

    /**
     * Creates a new module with a name, description, value, and action.
     *
     * @param name        the name of the module
     * @param description the description of the module
     * @param value       the value of the module
     * @param battle      if the module is sold at battle stations
     * @param effect      the effect the module applies when activated
     * @param action      the action that the module can perform
     */
    public Module(String name, String description, int value, boolean battle, String effect, Action action)
    {
        super(name, description, value);
        this.action = action;
        this.battle = battle;
        this.effect = effect;
        this.isDamaged = false;
    }

    /**
     * Creates a new module with a name, description, value, and action.
     *
     * @param name        the name of the module
     * @param description the description of the module
     * @param value       the value of the module
     * @param battle      if the module is sold at battle stations
     * @param action      the action that the module can perform
     */
    public Module(String name, String description, int value, boolean battle, Action action)
    {
        this(name, description, value, battle, null, action);
    }

    /**
     * Copying constructor that creates a new module identical to the one provided.
     *
     * @param copying the module to create a copy of
     */
    public Module(Module copying)
    {
        this(copying.getName(), copying.getDescription(), copying.getValue(), copying.battle, copying.effect,
                copying.action);
    }

    /**
     * Creates a new module from a set of Properties.
     *
     * @param properties the Properties object from which to construct the module
     */
    public Module(Properties properties)
    {
        super(properties);
        String resource = properties.getProperty("resource");
        if (resource == null)
        {
            throw new NullPointerException("Empty resource field found while generating modules.");
        }

        int cost = Math.abs(Integer.parseInt(properties.getProperty("cost")));

        action = new Action(resource, cost);
        battle = "true".equals(properties.getProperty("battle"));
        effect = properties.getProperty("effect");
        isDamaged = false;
    }

    /**
     * Returns the action the module can perform.
     *
     * @return the action the module can perform
     */
    public Action getAction()
    {
        return action;
    }

    /**
     * Returns the effect that the module applies when activated.
     *
     * @return the effect that the module applies when activated
     */
    public String getEffect()
    {
        return effect;
    }

    /**
     * Returns true if the module applies an effect when activated.
     *
     * @return true if the module applies an effect when activated
     */
    public boolean hasEffect()
    {
        return effect != null;
    }

    /**
     * Returns true if the module applies the specified effect.
     *
     * @param effect the effect to check
     * @return true if the module applies the specified effect
     */
    public boolean isEffect(String effect)
    {
        return this.effect != null && this.effect.equals(effect);
    }

    /**
     * Returns true if the module is sold at a battle station.
     *
     * @return true if the module is sold at a battle station
     */
    public boolean isBattle()
    {
        return battle;
    }

    /**
     * Returns true if the module is damaged.
     *
     * @return true if the module is damaged
     */
    public boolean isDamaged()
    {
        return isDamaged;
    }

    /**
     * Damages the module, returning true if it was possible to damage
     *
     * @return true if the module was damaged, false if it was already damaged
     */
    public boolean damage()
    {
        return setDamage(true);
    }

    /**
     * Repairs the module, returning true if it was possible to repair.
     *
     * @return true if the module was repaired, false if it was already repaired
     */
    public boolean repair()
    {
        return setDamage(false);
    }

    /**
     * Sets the damage to either true or false, returning true if the damaged status was changed.
     *
     * @param newDamage the state to set the damage to
     * @return true if the damaged status was changed
     */
    private boolean setDamage(boolean newDamage)
    {
        boolean damageChanged = isDamaged == newDamage;
        isDamaged = newDamage;
        return damageChanged;
    }
}