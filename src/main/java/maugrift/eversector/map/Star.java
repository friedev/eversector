package maugrift.eversector.map;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorChar;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.glyphs.ColorStringObject;
import maugrift.eversector.Symbol;
import maugrift.apwt.util.Utility;
import maugrift.eversector.Main;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * A star that possesses a type and a power level.
 *
 * @author Maugrift
 */
public class Star implements ColorStringObject
{
	/**
	 * The first part of a star's name.
	 */
	private static final String[] NAME_PREFIX = new String[]{
			"Aquar",
			"Can",
			"Del",
			"Dom",
			"Drac",
			"Erid",
			"Ign",
			"Lyr",
			"Mag",
			"Or",
			"Ret",
			"Rig",
			"Sin",
			"Sirr",
			"Sol",
			"Stell",
			"Taur",
			"Vel",
			"Ven",
			"Xen"
	};

	/**
	 * The second part of a star's name.
	 */
	private static final String[] NAME_MIDDLE = new String[]{
			"an", "ar", "at", "it", "itar", "in", "on", "un"
	};

	/**
	 * The third part of a star's name.
	 */
	private static final String[] NAME_SUFFIX = new String[]{
			"a", "ae", "as", "ex", "i", "ii", "io", "is", "ias", "ius", "os", "us",
	};

	/**
	 * The descriptor appended to the end of a star's name.
	 */
	private static final String[] NAME_DESCRIPTOR = new String[]{
			"Alpha",
			"Beta",
			"Gamma",
			"Delta",
			"Epsilon",
			"Eta",
			"Iota",
			"Major",
			"Majoris",
			"Maximus",
			"Malus",
			"Minor",
			"Minoris",
			"Minimus",
			"Omega",
			"Prime",
			"Sigma",
			"Zeta",
	};

	/**
	 * The chance of a special star generating instead of a regular star.
	 */
	private static final double SPECIAL_CHANCE = 0.1;

	/**
	 * The mass of a star.
	 */
	public enum StarMass
	{
		/**
		 * The smallest type of star.
		 */
		SUBDWARF("Subdwarf", Symbol.SUBDWARF, 0.2, 4, true),

		/**
		 * A small, very common type of star.
		 */
		DWARF("Dwarf", Symbol.SUBDWARF, 0.6, 5, true),

		/**
		 * A large star, smaller than a giant.
		 */
		SUBGIANT("Subgiant", Symbol.SUBGIANT, 0.1, 7, false),

		/**
		 * A large star, potentially near the end of its lifespan.
		 */
		GIANT("Giant", Symbol.GIANT, 0.05, 8, false),

		/**
		 * A very massive star.
		 */
		SUPERGIANT("Supergiant", Symbol.SUPERGIANT, 0.025, 10, false),

		/**
		 * The largest known stars in the universe.
		 */
		HYPERGIANT("Hypergiant", Symbol.HYPERGIANT, 0.025, 12, false);

		/**
		 * The type of the star mass.
		 */
		private final String name;

		/**
		 * The symbol representing stars of this mass.
		 */
		private final char symbol;

		/**
		 * The probability of this star mass generating, compared to others.
		 */
		private final double probability;

		/**
		 * The mass of the star, determining how many orbits it has.
		 */
		private final int mass;

		/**
		 * If true, this star mass can generate in a nebula.
		 */
		private final boolean inNebula;

		/**
		 * Creates a new star mass with all fields defined.
		 *
		 * @param name        the type of the star mass
		 * @param symbol      the symbol representing stars of this mass
		 * @param probability the probability of this star mass generating
		 * @param mass        the mass of the star
		 * @param inNebula    if true, the star mass can generate in a nebula
		 */
		StarMass(String name, char symbol, double probability, int mass, boolean inNebula)
		{
			this.name = name;
			this.symbol = symbol;
			this.probability = probability;
			this.mass = mass;
			this.inNebula = inNebula;
		}

		/**
		 * Creates a new star mass with all fields defined. Uses a Symbol instead of a char.
		 *
		 * @param name        the type of the star mass
		 * @param symbol      the symbol representing stars of this mass
		 * @param probability the probability of this star mass generating
		 * @param mass        the mass of the star
		 * @param inNebula    if true, the star mass can generate in a nebula
		 */
		StarMass(String name, Symbol symbol, double probability, int mass, boolean inNebula)
		{
			this(name, symbol.get(), probability, mass, inNebula);
		}

		@Override
		public String toString()
		{
			return name;
		}

		/**
		 * Gets the type of the star mass.
		 *
		 * @return the type of the star mass
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Gets the symbol representing stars of this mass.
		 *
		 * @return the symbol representing stars of this mass
		 */
		public char getSymbol()
		{
			return symbol;
		}

		/**
		 * Gets the probability of this star mass generating.
		 *
		 * @return the probability of this star mass generating
		 */
		public double getProbability()
		{
			return probability;
		}

		/**
		 * Gets the mass of the star.
		 *
		 * @return the mass of the star
		 */
		public int getMass()
		{
			return mass;
		}

		/**
		 * Returns true if this star type can generate in a nebula.
		 *
		 * @return true if this star type can generate in a nebula
		 */
		public boolean inNebula()
		{
			return inNebula;
		}

		public static StarMass getLargest()
		{
			StarMass largest = StarMass.values()[0];
			for (StarMass mass : StarMass.values())
			{
				if (mass.mass > largest.mass)
				{
					largest = mass;
				}
			}

			return largest;
		}

		public static StarMass getSmallest()
		{
			StarMass smallest = StarMass.values()[0];
			for (StarMass mass : StarMass.values())
			{
				if (mass.mass < smallest.mass)
				{
					smallest = mass;
				}
			}

			return smallest;
		}

		/**
		 * Selects a random star mass.
		 *
		 * @return a random star mass
		 */
		public static StarMass select()
		{
			double[] probabilities = new double[StarMass.values().length];
			for (int i = 0; i < StarMass.values().length; i++)
			{
				probabilities[i] = StarMass.values()[i].probability;
			}
			return (StarMass) Utility.select(Main.rng, StarMass.values(), probabilities);
		}

		/**
		 * Selects a random star mass for a sector in the given nebula type.
		 *
		 * @param nebula the nebula to generate a star mass for
		 * @return a random star mass that can generate in the given nebula
		 */
		public static StarMass select(Nebula nebula)
		{
			if (nebula == null)
			{
				return select();
			}

			List<StarMass> masses = new LinkedList<>();
			for (StarMass mass : values())
			{
				if (mass.inNebula)
				{
					masses.add(mass);
				}
			}

			double[] probabilities = new double[masses.size()];
			double totalProbability = 0.0;
			for (int i = 0; i < masses.size(); i++)
			{
				probabilities[i] = masses.get(i).probability;
				totalProbability += masses.get(i).probability;
			}

			probabilities[0] += 1.0 - totalProbability;

			return (StarMass) Utility.select(Main.rng, masses.toArray(), probabilities);
		}
	}

	/**
	 * The temperature of a star.
	 */
	public enum StarTemperature
	{
		/**
		 * The coldest, most common type of star.
		 */
		RED("Red", AsciiPanel.brightRed, false, StarMass.SUBDWARF.mass, StarMass.HYPERGIANT.mass),

		/**
		 * A mid-range star temperature.
		 */
		YELLOW("Yellow", AsciiPanel.brightYellow, false, StarMass.DWARF.mass, StarMass.HYPERGIANT.mass),

		/**
		 * The hottest, brightest stars.
		 */
		BLUE("Blue", AsciiPanel.brightCyan, true, StarMass.GIANT.mass, StarMass.HYPERGIANT.mass);

		/**
		 * The type of the star temperature.
		 */
		private final String name;

		/**
		 * The color of the star temperature.
		 */
		private final Color color;

		/**
		 * If true, the star emits radiation that prevents rocky planets with atmospheres from forming.
		 */
		private final boolean radiation;

		/**
		 * The lowest mass of a star that can have this temperature.
		 */
		private final int minMass;

		/**
		 * The highest mass of a star that can have this temperature.
		 */
		private final int maxMass;

		/**
		 * Creates a new StarTemperature with all fields defined.
		 *
		 * @param name      the type of the star temperature
		 * @param color     the color of the star temperature
		 * @param radiation if true, the star emits radiation
		 * @param minMass   the lowest mass of star that can have this temperature
		 * @param maxMass   the highest mass of star that can have this temperature
		 */
		StarTemperature(String name, Color color, boolean radiation, int minMass, int maxMass)
		{
			this.name = name;
			this.color = color;
			this.radiation = radiation;
			this.minMass = minMass;
			this.maxMass = maxMass;
		}

		@Override
		public String toString()
		{
			return name;
		}

		/**
		 * Gets the type of the star temperature.
		 *
		 * @return the type of the star temperature
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Gets the color of the star temperature.
		 *
		 * @return the color of the star temperature
		 */
		public Color getColor()
		{
			return color;
		}

		/**
		 * Returns true if the star emits substantial radiation.
		 *
		 * @return true if the star emits substantial radiation
		 */
		public boolean hasRadiation()
		{
			return radiation;
		}

		/**
		 * Gets the lowest mass of a star that can have this temperature.
		 *
		 * @return the lowest mass of a star that can have this temperature
		 */
		public int getMinMass()
		{
			return minMass;
		}

		/**
		 * Gets the highest mass of a star that can have this temperature.
		 *
		 * @return the highest mass of a star that can have this temperature
		 */
		public int getMaxMass()
		{
			return maxMass;
		}

		/**
		 * Returns true if the given mass of star can have this temperature.
		 *
		 * @param mass the star mass to check
		 * @return true if the given mass of star can have this temperature
		 */
		public boolean isInMassRange(int mass)
		{
			return minMass <= mass && maxMass >= mass;
		}

		/**
		 * Selects a random star temperature.
		 *
		 * @return a random star temperature
		 */
		public static StarTemperature select()
		{
			return Main.rng.getRandomElement(StarTemperature.values());
		}

		/**
		 * Selects a random star temperature for the given star mass.
		 *
		 * @param mass the mass of star to generate a temperature for
		 * @return a random star temperature for the given star mass
		 */
		public static StarTemperature select(StarMass mass)
		{
			List<StarTemperature> temperatures = new LinkedList<>();
			for (StarTemperature temperature : StarTemperature.values())
			{
				if (temperature.isInMassRange(mass.mass))
				{
					temperatures.add(temperature);
				}
			}

			return Main.rng.getRandomElement(temperatures);
		}
	}

	/**
	 * All types of stars that cannot be generated through combinations of common masses and temperatures.
	 */
	private enum SpecialStar
	{
		/**
		 * A small, cold, protostar that cannot sustain fusion.
		 */
		BROWN_DWARF(
				new Star("Brown Dwarf", AsciiPanel.yellow, StarMass.SUBDWARF.getSymbol(), StarMass.SUBDWARF.getMass(),
						false)),

		/**
		 * The dense core of a dead star.
		 */
		WHITE_DWARF(new Star("White Dwarf", AsciiPanel.brightWhite, StarMass.SUBDWARF.getSymbol(),
				StarMass.SUBDWARF.getMass(), false)),

		/**
		 * Two stars that orbit each other.
		 */
		BINARY_STAR(
				new Star("Binary Star", AsciiPanel.brightWhite, Symbol.BINARY_STAR.get(), StarMass.SUBGIANT.getMass(),
						false)),

		/**
		 * The corpse of a large star, held together by neutron degeneracy.
		 */
		NEUTRON_STAR(
				new Star("Neutron Star", AsciiPanel.brightWhite, Symbol.NEUTRON_STAR.get(), StarMass.GIANT.getMass(),
						true)),

		/**
		 * A rapidly-spinning neutron star that appears to emit pulses of light.
		 */
		PULSAR(new Star("Pulsar", NEUTRON_STAR.star.color, Symbol.PULSAR.get(), NEUTRON_STAR.star.mass, true));

		/**
		 * The special type of star.
		 */
		private Star star;

		/**
		 * Creates a special star.
		 *
		 * @param star the special star
		 */
		SpecialStar(Star star)
		{
			this.star = star;
		}
	}

	/**
	 * The name of the star.
	 */
	private String name;

	/**
	 * The type of the star.
	 */
	private final String type;

	/**
	 * The color of the star.
	 */
	private final Color color;

	/**
	 * The symbol representing the star.
	 */
	private final char symbol;

	/**
	 * The mass of the star.
	 */
	private final int mass;

	/**
	 * If true, the star emits radiation that prevents rocky planets with atmospheres from forming.
	 */
	private final boolean radiation;

	/**
	 * Creates a star with all fields defined.
	 *
	 * @param type      the type of the star
	 * @param color     the color of the star
	 * @param symbol    the symbol representing the star
	 * @param mass      the mass of the star
	 * @param radiation true if the star emits substantial radiation
	 */
	private Star(String type, Color color, char symbol, int mass, boolean radiation)
	{
		this.name = generateName();
		this.type = type;
		this.color = color;
		this.symbol = symbol;
		this.mass = mass;
		this.radiation = radiation;
	}

	/**
	 * Creates a star with the given mass and temperature.
	 *
	 * @param mass        the mass of the star
	 * @param temperature the temperature of the star
	 */
	private Star(StarMass mass, StarTemperature temperature)
	{
		this(temperature.getName() + " " + mass.getName(), temperature.getColor(), mass.getSymbol(), mass.getMass(),
				temperature.hasRadiation());
	}

	/**
	 * Generates a star.
	 *
	 * @param nebula the nebula to generate a star in
	 * @return the generated star
	 */
	public static Star generate(Nebula nebula)
	{
		if (nebula == null && Utility.getChance(Main.rng, SPECIAL_CHANCE))
		{
			return Main.rng.getRandomElement(SpecialStar.values()).star;
		}

		StarMass mass = StarMass.select(nebula);
		StarTemperature temperature = StarTemperature.select(mass);
		return new Star(mass, temperature);
	}

	/**
	 * Generates a star.
	 *
	 * @return the generated star
	 */
	public static Star generate()
	{
		return generate(null);
	}

	/**
	 * Generates a star name from the constant name arrays.
	 *
	 * @return a star name generated from the constant name arrays
	 * @see #NAME_PREFIX
	 * @see #NAME_MIDDLE
	 * @see #NAME_SUFFIX
	 * @see #NAME_DESCRIPTOR
	 */
	public static String generateName()
	{
		return Main.rng.getRandomElement(NAME_PREFIX) + (Main.rng.nextBoolean() ? Main.rng.getRandomElement(NAME_MIDDLE) : "") +
		       Main.rng.getRandomElement(NAME_SUFFIX) + (Main.rng.nextBoolean() ? " " + Main.rng.getRandomElement(NAME_DESCRIPTOR) : "");
	}

	@Override
	public String toString()
	{
		return type;
	}

	@Override
	public ColorString toColorString()
	{
		return new ColorString(type, color);
	}

	/**
	 * Gets the name of the star.
	 *
	 * @return the name of the star
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the type of star.
	 *
	 * @return the type of star
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Gets the symbol representing the star.
	 *
	 * @return the symbol representing the star
	 */
	public ColorChar getSymbol()
	{
		return new ColorChar(symbol, color);
	}

	/**
	 * Gets the mass of the star.
	 *
	 * @return the mass of the star
	 */
	public int getMass()
	{
		return mass;
	}

	/**
	 * Returns true if the star emits substantial radiation.
	 *
	 * @return true if the star emits substantial radiation
	 */
	public boolean hasRadiation()
	{
		return radiation;
	}

	/**
	 * Calculates the power level of the star at a certain orbit.
	 *
	 * @param orbit the orbit at which to calculate the star's power, must be a valid orbit
	 * @return the reduced power of the star at the orbit, -1 if the orbit is invalid
	 */
	public int getPowerAt(int orbit)
	{
		return orbit > 0 ? Math.max(0, mass - (orbit - 1)) : -1;
	}

	/**
	 * Calculates the amount of energy generated by a solar array at the given orbit.
	 *
	 * @param orbit the orbit at which to calculate the star's solar power, must be a valid orbit
	 * @return the power generated by a solar array at the given orbit of the star, -1 if the orbit is invalid
	 */
	public int getSolarPowerAt(int orbit)
	{
		return getPowerAt(orbit) / (StarMass.SUBDWARF.getMass()) + 1;
	}

	/**
	 * Sets the star's name to the given string.
	 *
	 * @param name the string to become the star's new name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
}
