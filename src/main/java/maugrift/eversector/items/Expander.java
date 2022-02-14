package maugrift.eversector.items;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.util.Utility;

import java.util.List;
import java.util.Properties;

import static maugrift.eversector.Main.COLOR_FIELD;

/**
 * An item designed to be included in resources that hold the information about increasing the resource's capacity.
 * <b>To be removed in v0.7.2.</b>
 *
 * @author Maugrift
 */
public class Expander extends Item
{
	/**
	 * The default amount of expansion for new Expanders.
	 */
	private static final int EXPANSION = 5;

	/**
	 * The amount that the expander causes a resource's capacity to increase.
	 */
	private final int expansion;

	/**
	 * Creates a new Expander with a name, description, value, and the default expansion amount.
	 *
	 * @param name        the name of the Expander
	 * @param description the description of the Expander
	 * @param value       the value of the Expander
	 */
	public Expander(String name, String description, int value)
	{
		this(name, description, value, EXPANSION);
	}

	/**
	 * Creates a new Expander with a name, description, value, and expansion amount.
	 *
	 * @param name        the name of the Expander
	 * @param description the description of the Expander
	 * @param value       the value of the Expander
	 * @param expansion   the expansion amount of the Expander
	 */
	public Expander(String name, String description, int value, int expansion)
	{
		super(name, description, value);
		this.expansion = Math.abs(expansion);
	}

	/**
	 * Copying constructor that creates another Expander identical to the existing one.
	 *
	 * @param copying the Expander to copy
	 */
	public Expander(Expander copying)
	{
		this(copying.getName(), copying.getDescription(), copying.getValue(), copying.expansion);
	}

	/**
	 * Creates a new Expander from a set of Properties.
	 *
	 * @param properties the Properties object to use in the construction of the Expander
	 */
	public Expander(Properties properties)
	{
		super(properties);
		expansion = Math.abs(Utility.parseInt(properties.getProperty("expansion"), EXPANSION));
	}

	/**
	 * Returns the amount that the expander causes a resource's capacity to increase.
	 *
	 * @return the expansion amount of the resource
	 */
	public int getExpansion()
	{
		return expansion;
	}

	@Override
	public List<ColorString> define()
	{
		List<ColorString> definition = super.define();
		definition.add(2, new ColorString("Expansion: ").add(new ColorString("+" + expansion + " Units", COLOR_FIELD)));
		return definition;
	}
}