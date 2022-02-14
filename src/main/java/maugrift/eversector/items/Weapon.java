package maugrift.eversector.items;

import maugrift.apwt.glyphs.ColorString;

import java.util.List;
import java.util.Properties;

import static maugrift.eversector.Main.COLOR_FIELD;

/**
 * A module that also holds the amount of damage it deals.
 *
 * @author Aaron Friesen
 */
public class Weapon extends Module
{
	public static final String LASER = "Laser";
	public static final String TORPEDO_TUBE = "Torpedo Tube";
	public static final String PULSE_BEAM = "Pulse Beam";

	/**
	 * The hull damage the weapon can inflict on an undefended ship.
	 */
	private final int damage;

	/**
	 * The path to the sound effect that plays when the player fires the
	 * weapon.
	 */
	private final String soundEffect;

	/**
	 * Creates a new weapon with a name, description, value, damage, and
	 * action.
	 *
	 * @param name           the name of the weapon
	 * @param description    the description of the weapon
	 * @param value          the value of the weapon
	 * @param damage         the damage of the weapon
	 * @param actionResource the resource required for the module's action
	 * @param actionCost     the amount of the resource required for the
	 *                       module's action
	 * @param soundEffect    the path to the sound effect that plays when the
	 *                       player fires the weapon
	 */
	public Weapon(
			String name,
			String description,
			int value,
			int damage,
			String actionResource,
			int actionCost,
			String soundEffect
	) {
		super(name, description, value, true, actionResource, actionCost);
		this.damage = Math.abs(damage);
		this.soundEffect = soundEffect;
	}

	/**
	 * Copying constructor that creates a new weapon identical to the one
	 * provided.
	 *
	 * @param copying the weapon to create a copy of
	 */
	public Weapon(Weapon copying)
	{
		this(
				copying.getName(),
				copying.getDescription(),
				copying.getValue(),
				copying.damage,
				copying.getActionResource(),
				copying.getActionCost(),
				copying.soundEffect
		);
	}

	/**
	 * Creates a new weapon from a set of Properties.
	 *
	 * @param properties the Properties object used in construction of the
	 *                   weapon
	 */
	public Weapon(Properties properties)
	{
		super(properties);
		damage = Math.abs(Integer.parseInt(properties.getProperty("damage")));
		soundEffect = properties.getProperty("sound");
	}

	/**
	 * Returns the hull damage the weapon can inflict on an undefended ship.
	 *
	 * @return the damage of the weapon
	 */
	public int getDamage()
	{
		return damage;
	}

	/**
	 * Gets the path to the sound effect that plays when the player fires the
	 * weapon.
	 *
	 * @return the path to the sound effect that plays when the player fires
	 * the weapon
	 */
	public String getSoundEffect()
	{
		return soundEffect;
	}

	@Override
	public List<ColorString> define()
	{
		List<ColorString> definition = super.define();
		definition.add(2, new ColorString("Damage: ")
				.add(new ColorString(Integer.toString(damage), COLOR_FIELD)));
		definition.add(3, new ColorString("Type: ")
				.add(new ColorString(getActionResource(), COLOR_FIELD)));
		return definition;
	}
}
