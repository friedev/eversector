package boldorf.eversector.items;

import java.util.Properties;

/**
 * A version of an item designed to perform an action.
 *
 * @author Boldorf Smokebane
 */
public class Module extends Item
{
    public static final String SCANNER = "Scanner";
    public static final String REFINERY = "Refinery";
    public static final String SOLAR_ARRAY = "Solar Array";
    public static final String WARP_DRIVE = "Warp Drive";
    public static final String SHIELD = "Shield ";
    public static final String CLOAKING_DEVICE = "Cloaking Device";

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
     * The resource required for the module's action.
     */
    private String actionResource;

    /**
     * The amount of the resource required for the module's action.
     */
    private int actionCost;

    /**
     * Creates a new module with all fields defined.
     *
     * @param name           the name of the module
     * @param description    the description of the module
     * @param value          the value of the module
     * @param battle         if the module is sold at battle stations
     * @param effect         the effect the module applies when activated
     * @param actionResource the resource required for the module's action
     * @param actionCost     he amount of the resource required for the module's action
     */
    public Module(String name, String description, int value, boolean battle, String effect, String actionResource,
                  int actionCost)
    {
        super(name, description, value);
        this.battle = battle;
        this.effect = effect;
        this.isDamaged = false;
        this.actionResource = actionResource;
        this.actionCost = actionCost;
    }

    /**
     * Creates a new module with no effect.
     *
     * @param name           the name of the module
     * @param description    the description of the module
     * @param value          the value of the module
     * @param battle         if the module is sold at battle stations
     * @param actionResource the resource required for the module's action
     * @param actionCost     he amount of the resource required for the module's action
     */
    public Module(String name, String description, int value, boolean battle, String actionResource, int actionCost)
    {
        this(name, description, value, battle, null, actionResource, actionCost);
    }

    /**
     * Creates a new module with no effect or action.
     *
     * @param name        the name of the module
     * @param description the description of the module
     * @param value       the value of the module
     * @param battle      if the module is sold at battle stations
     */
    public Module(String name, String description, int value, boolean battle)
    {
        this(name, description, value, battle, null, null, 0);
    }

    /**
     * Copying constructor that creates a new module identical to the one provided.
     *
     * @param copying the module to create a copy of
     */
    public Module(Module copying)
    {
        this(copying.getName(), copying.getDescription(), copying.getValue(), copying.battle, copying.effect,
                copying.actionResource, copying.actionCost);
    }

    /**
     * Creates a new module from a set of Properties.
     *
     * @param properties the Properties object from which to construct the module
     */
    public Module(Properties properties)
    {
        super(properties);
        actionResource = properties.getProperty("resource");
        actionCost = Math.abs(Integer.parseInt(properties.getProperty("cost")));
        battle = "true".equals(properties.getProperty("battle"));
        effect = properties.getProperty("effect");
        isDamaged = false;
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
     * Gets the resource required for the module's action.
     *
     * @return the resource required for the module's action
     */
    public String getActionResource()
    {
        return actionResource;
    }

    /**
     * Gets the amount of the resource required for the module's action.
     *
     * @return the amount of the resource required for the module's action
     */
    public int getActionCost()
    {
        return actionCost;
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