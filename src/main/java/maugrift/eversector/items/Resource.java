package maugrift.eversector.items;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;

import java.awt.Color;

/**
 * A resource with all the properties of an item, in addition to amounts and
 * capacities for use on ships.  <b>To be removed in v0.7.2.</b>
 *
 * @author Maugrift
 */
public class Resource extends BaseResource
{
	/**
	 * The name of the fuel resource.
	 */
	public static final String FUEL = "Fuel";

	/**
	 * The name of the energy resource.
	 */
	public static final String ENERGY = "Energy";

	/**
	 * The name of the ore resource.
	 */
	public static final String ORE = "Ore";

	/**
	 * The name of the hull resource.
	 */
	public static final String HULL = "Hull";

	/**
	 * The name of the expander for the fuel resource.
	 */
	public static final String FUEL_EXPANDER = "Fuel Tank";

	/**
	 * The name of the expander for the energy resource.
	 */
	public static final String ENERGY_EXPANDER = "Energy Cell";

	/**
	 * The name of the expander for the ore resource.
	 */
	public static final String ORE_EXPANDER = "Cargo Bay";

	/**
	 * The name of the expander for the hull resource.
	 */
	public static final String HULL_EXPANDER = "Hull Frame";

	/**
	 * The default capacity of resources.
	 */
	private static final int CAPACITY = 15;

	/**
	 * The ratio of amount to capacity at which there is considered to be a
	 * high amount of the resource. This is inclusive, and extends upwards to
	 * 1.
	 */
	private static final double RATIO_HIGH = 0.75;

	/**
	 * The ratio of amount to capacity at which there is considered to be a low
	 * amount of the resource. This is exclusive, and extends downwards to 0.
	 */
	private static final double RATIO_LOW = 0.25;

	/**
	 * The color to color a fraction's amount when it is in the "high" range.
	 */
	private static final Color COLOR_HIGH = AsciiPanel.brightGreen;

	/**
	 * The color to color a fraction's amount when it is between the "high" and
	 * "low" ranges.
	 */
	private static final Color COLOR_MEDIUM = AsciiPanel.brightYellow;

	/**
	 * The color to color a fraction's amount when it is in the "low" range.
	 */
	private static final Color COLOR_LOW = AsciiPanel.brightRed;

	/**
	 * The color of a fraction's denominator.
	 */
	private static final Color COLOR_CAP = AsciiPanel.brightWhite;

	/**
	 * The number of units of the resource.
	 */
	private int amount;

	/**
	 * The starting capacity of the resource. The capacity can never be lower
	 * than this.
	 */
	private int baseCapacity;

	/**
	 * The current capacity of the resource.
	 */
	private int capacity;

	/**
	 * Creates a resource from a name, description, value, capacity, amount,
	 * expander, and ability to be sold.
	 *
	 * @param name        the name of the resource
	 * @param description the description of the resource
	 * @param value       the value of one unit of the resource
	 * @param capacity    the capacity of the resource
	 * @param amount      the amount of the resource
	 * @param canSell     true if the resource can be sold
	 * @param expander    the resource's expander
	 */
	public Resource(
			String name,
			String description,
			int value,
			int capacity,
			int amount,
			boolean canSell,
			Expander expander
	) {
		super(name, description, value, canSell, expander);
		this.baseCapacity = Math.abs(capacity);
		this.capacity = baseCapacity;
		this.amount = Math.min(Math.abs(amount), capacity);
	}

	/**
	 * Creates a resource from a BaseResource, a capacity, and an amount, using
	 * the fields of the former.
	 *
	 * @param base     the BaseResource that the name, description, value, and
	 *                 expander will be gathered from
	 * @param capacity the capacity of the resource
	 * @param amount   the amount of the resource
	 */
	public Resource(BaseResource base, int capacity, int amount)
	{
		this(
				base.getName(),
				base.getDescription(),
				base.getValue(),
				capacity,
				amount,
				base.canSell(),
				base.getExpander()
		);
	}

	/**
	 * Creates a resource from a BaseResource and a capacity, using the fields
	 * of the former.
	 *
	 * @param base     the BaseResource that the name, description, value, and
	 *                 expander will be gathered from
	 * @param capacity the capacity of the resource
	 */
	public Resource(BaseResource base, int capacity)
	{
		this(
				base.getName(),
				base.getDescription(),
				base.getValue(),
				capacity,
				capacity,
				base.canSell(),
				base.getExpander()
		);
	}

	/**
	 * Creates a resource from a BaseResource, using its fields as well as the
	 * default capacity and amount.
	 *
	 * @param base the BaseResource that the name, description, value, and
	 *             expander will be gathered from
	 */
	public Resource(BaseResource base)
	{
		this(
				base.getName(),
				base.getDescription(),
				base.getValue(),
				CAPACITY,
				CAPACITY,
				base.canSell(),
				base.getExpander()
		);
	}

	/**
	 * Creates a resource with a name, description, value, capacity, and
	 * expander, setting the amount to full.
	 *
	 * @param name        the name of the resource
	 * @param description the description of the resource
	 * @param value       the value of one unit of the resource
	 * @param capacity    the capacity of the resource
	 * @param expander    the resource's expander
	 */
	public Resource(
			String name,
			String description,
			int value,
			int capacity,
			Expander expander
	) {
		this(name, description, value, capacity, capacity, expander);
	}

	/**
	 * Creates a resource from a name, description, value, capacity, amount,
	 * and expander.
	 *
	 * @param name        the name of the resource
	 * @param description the description of the resource
	 * @param value       the value of one unit of the resource
	 * @param capacity    the capacity of the resource
	 * @param amount      the amount of the resource
	 * @param expander    the resource's expander
	 */
	public Resource(
			String name,
			String description,
			int value,
			int capacity,
			int amount,
			Expander expander
	) {
		this(name, description, value, capacity, amount, true, expander);
	}

	/**
	 * Creates a resource with a name, description, value, and expander,
	 * setting capacity to default and amount to full.
	 *
	 * @param name        the name of the resource
	 * @param description the description of the resource
	 * @param value       the value of one unit of the resource
	 * @param expander    the resource's expander
	 */
	public Resource(
			String name,
			String description,
			int value,
			Expander expander
	) {
		this(name, description, value, CAPACITY, CAPACITY, expander);
	}

	/**
	 * Gets amount.
	 *
	 * @return the amount
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * Gets base capacity.
	 *
	 * @return the base capacity
	 */
	public int getBaseCapacity()
	{
		return baseCapacity;
	}

	/**
	 * Gets capacity.
	 *
	 * @return the capacity
	 */
	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * Gets ratio.
	 *
	 * @return the ratio
	 */
	public double getRatio()
	{
		return (double) amount / (double) capacity;
	}

	/**
	 * Returns the resource as a String in the format Amount/Capacity.
	 *
	 * @return a String as described above
	 */
	public String getAmountAsFraction()
	{
		return amount + "/" + capacity;
	}

	/**
	 * Gets amount as colored fraction.
	 *
	 * @return the amount as colored fraction
	 */
	public ColorString getAmountAsColoredFraction()
	{
		double ratio = getRatio();
		Color color;
		if (ratio >= RATIO_HIGH)
		{
			color = COLOR_HIGH;
		}
		else if (ratio < RATIO_LOW)
		{
			color = COLOR_LOW;
		}
		else
		{
			color = COLOR_MEDIUM;
		}

		return new ColorString(Integer.toString(amount), color).add("/").add(
				new ColorString(Integer.toString(capacity), COLOR_CAP));
	}

	/**
	 * Returns the difference between the capacity and amount of the resource.
	 *
	 * @return an integer as described above, should be non-negative
	 */
	public int getRemainingSpace()
	{
		return capacity - amount;
	}

	/**
	 * Returns the value of the resource multiplied by its quantity.
	 *
	 * @return the resource's base value times its amount
	 */
	public int getTotalValue()
	{
		return getValueForAmount(amount);
	}

	/**
	 * Returns the price of resource multiplied by its quantity.
	 *
	 * @return the resource's price times its amount
	 */
	public int getTotalPrice()
	{
		return getPriceForAmount(amount);
	}

	/**
	 * Returns the value of the resource for a specified amount.
	 *
	 * @param a the amount of the resource to calculate the value of
	 * @return the resource's value times the specified amount
	 */
	public int getValueForAmount(int a)
	{
		return getValue() * a;
	}

	/**
	 * Returns the price of the resource for a specified amount.
	 *
	 * @param a the amount of the resource to calculate the price of
	 * @return the resource's price times the specified amount
	 */
	public int getPriceForAmount(int a)
	{
		return getPrice() * a;
	}

	/**
	 * Changes the amount of the resource by a specified amount.
	 *
	 * @param change the amount to change the resource's amount by
	 * @return true if the amount was changed
	 */
	public boolean changeAmount(int change)
	{
		return setAmount(amount + change);
	}

	/**
	 * Sets the amount of the resource to a new amount.
	 *
	 * @param amount the amount to set set the resource's amount to
	 * @return true if the amount was set
	 */
	public boolean setAmount(int amount)
	{
		if (!isValidAmount(amount))
		{
			return false;
		}

		this.amount = amount;
		return true;
	}

	/**
	 * Changes the amount of the resource up to its maximum capacity, returning
	 * the amount discarded.
	 *
	 * @param increase the amount to add, must be positive
	 * @return the amount discarded, -1 if could not change the amount
	 */
	public int changeAmountWithDiscard(int increase)
	{
		if (increase <= 0)
		{
			return -1;
		}

		if (isFull())
		{
			return -1;
		}

		int discard = Math.max(amount + increase - capacity, 0);
		amount = Math.min(amount + increase, capacity);
		return discard;
	}

	/**
	 * Increases the capacity by the expansion amount of its expander.
	 *
	 * @return true if the capacity was changed
	 */
	public boolean expand()
	{
		return changeCapacity(getExpander().getExpansion());
	}

	/**
	 * Increases the capacity by the expansion amount of its expander a
	 * specified number of times.
	 *
	 * @param times the amount of times to expand the resource's capacity
	 * @return true if the capacity was changed
	 */
	public boolean expand(int times)
	{
		return changeCapacity(getExpander().getExpansion() * times);
	}

	/**
	 * Changes the capacity of the resource by a specified amount.
	 *
	 * @param change the amount to change the capacity by
	 * @return true if the capacity was changed
	 */
	public boolean changeCapacity(int change)
	{
		return setCapacity(capacity + change);
	}

	/**
	 * Sets the capacity of the resource to a specified amount.
	 *
	 * @param capacity the amount to set the capacity to
	 * @return true if the capacity was changed
	 */
	public boolean setCapacity(int capacity)
	{
		if (!isValidCapacity(capacity))
		{
			return false;
		}

		this.capacity = capacity;
		amount = Math.min(amount, capacity);
		return true;
	}

	/**
	 * Sets the least possible capacity to a specified amount, as well as
	 * updating the capacity itself.
	 *
	 * @param capacity the amount to set the base capacity to
	 * @return true if the base capacity was modified
	 */
	public boolean setBaseCapacity(int capacity)
	{
		if (capacity <= 0)
		{
			return false;
		}

		baseCapacity = capacity;
		this.capacity = baseCapacity;

		return true;
	}

	/**
	 * Returns true if the amount specified is a valid capacity.
	 *
	 * @param capacity the amount to validate
	 * @return true if the amount specified is greater than or equal to the
	 *         base capacity
	 */
	public boolean isValidCapacity(int capacity)
	{
		return capacity >= baseCapacity;
	}

	/**
	 * Returns true if the capacity would be valid after a specified number of
	 * expansions (intended for use in removing expanders).
	 *
	 * @param times the amount of expansions to check
	 * @return true if the capacity would be valid after expanding the
	 *         resource's capacity
	 */
	public boolean canExpand(int times)
	{
		return isValidCapacity(capacity + getExpander().getExpansion() * times);
	}

	/**
	 * Returns true if the specified amount would be a valid amount of the
	 * resource.
	 *
	 * @param amount the amount to validate
	 * @return true if the amount is less than or equal to the capacity of the
	 *         resource, yet greater than or equal to zero
	 */
	public boolean isValidAmount(int amount)
	{
		return amount <= capacity && amount >= 0;
	}

	/**
	 * Returns true if a specified amount of change in the resource's amount
	 * would be valid.
	 *
	 * @param amount the amount to change the resource by for validation
	 * @return true if the addition of the specified amount leaves the total
	 *         amount still valid
	 */
	public boolean canHold(int amount)
	{
		return isValidAmount(this.amount + amount);
	}

	/**
	 * Returns true if the resource's amount is equal to its capacity.
	 *
	 * @return true if the amount of the resource equals its capacity
	 */
	public boolean isFull()
	{
		return amount == capacity;
	}

	/**
	 * Returns true if the resource's amount is equal to zero.
	 *
	 * @return true if the amount of the resource equals zero
	 */
	public boolean isEmpty()
	{
		return amount == 0;
	}

	/**
	 * Sets the resource's amount to its maximum capacity.
	 *
	 * @return the amount added to the resource to fill it
	 */
	public int fill()
	{
		int increase = capacity - amount;
		amount = capacity;
		return increase;
	}

	/**
	 * Sets the resource's amount to zero.
	 */
	public void empty()
	{
		amount = 0;
	}

	/**
	 * Returns the amount of expanders that have been purchased.
	 *
	 * @return the number of expanders, calculated by the change in capacity;
	 *         will be non-negative
	 */
	public int getNExpanders()
	{
		return (capacity - baseCapacity) / getExpander().getExpansion();
	}
}
