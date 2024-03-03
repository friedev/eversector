package maugrift.eversector;

import asciiPanel.AsciiPanel;
import maugrift.apwt.ExtChars;
import maugrift.apwt.glyphs.ColorChar;

import java.util.HashMap;

/**
 * All symbols in the game, with separate versions for tiles and ASCII.
 *
 * @author Aaron Friesen
 */
public enum Symbol {
	ELLIPSIS,
	COPYRIGHT,
	UNDISCOVERED,
	EMPTY,
	CREDITS,
	PLAYER,
	WEAK_SHIP,
	MEDIUM_SHIP,
	STRONG_SHIP,
	ROCKY_PLANET,
	GAS_GIANT,
	ASTEROID_BELT,
	TRADE_STATION,
	BATTLE_STATION,
	SUBDWARF,
	DWARF,
	SUBGIANT,
	GIANT,
	SUPERGIANT,
	HYPERGIANT,
	BINARY_STAR,
	NEUTRON_STAR,
	PULSAR,
	LIQUID_REGION,
	FLAT_REGION,
	HILL_REGION,
	MOUNTAIN_REGION,
	FOREST_REGION;

	private static HashMap<Symbol, Character> tiles = new HashMap<>(26);
	private static HashMap<Symbol, Character> ascii = new HashMap<>(26);

	static
	{
		tiles.put(ELLIPSIS, ExtChars.DOT_SMALL);
		tiles.put(COPYRIGHT, (char) 0);
		tiles.put(UNDISCOVERED, ' ');
		tiles.put(EMPTY, ExtChars.DOT);
		tiles.put(CREDITS, '$');
		tiles.put(PLAYER, (char) 1);
		tiles.put(WEAK_SHIP, (char) 2);
		tiles.put(MEDIUM_SHIP, (char) 3);
		tiles.put(STRONG_SHIP, (char) 4);
		tiles.put(ROCKY_PLANET, (char) 5);
		tiles.put(GAS_GIANT, (char) 6);
		tiles.put(ASTEROID_BELT, (char) 7);
		tiles.put(TRADE_STATION, (char) 8);
		tiles.put(BATTLE_STATION, (char) 9);
		tiles.put(SUBDWARF, (char) 10);
		tiles.put(DWARF, (char) 11);
		tiles.put(SUBGIANT, (char) 12);
		tiles.put(GIANT, (char) 12); // SUBGIANT
		tiles.put(SUPERGIANT, (char) 13);
		tiles.put(HYPERGIANT, (char) 13); // SUPERGIANT
		tiles.put(BINARY_STAR, (char) 14);
		tiles.put(NEUTRON_STAR, (char) 10); // SUBDWARF
		tiles.put(PULSAR, (char) 15);
		tiles.put(LIQUID_REGION, (char) 19);
		tiles.put(FLAT_REGION, (char) 20);
		tiles.put(HILL_REGION, (char) 21);
		tiles.put(MOUNTAIN_REGION, (char) 22);
		tiles.put(FOREST_REGION, (char) 28);

		ascii.put(ELLIPSIS, ExtChars.DOT_SMALL);
		ascii.put(COPYRIGHT, (char) 0);
		ascii.put(UNDISCOVERED, ' ');
		ascii.put(EMPTY, ExtChars.DOT);
		ascii.put(CREDITS, ExtChars.STAR);
		ascii.put(PLAYER, '@');
		ascii.put(WEAK_SHIP, '>');
		ascii.put(MEDIUM_SHIP, ExtChars.ARROW2_R);
		ascii.put(STRONG_SHIP, ExtChars.SIGMA);
		ascii.put(ROCKY_PLANET, ExtChars.THETA);
		ascii.put(GAS_GIANT, ExtChars.CIRCLE);
		ascii.put(ASTEROID_BELT, ExtChars.INFINITY);
		ascii.put(TRADE_STATION, '#');
		ascii.put(BATTLE_STATION, '%');
		ascii.put(SUBDWARF, '+');
		ascii.put(DWARF, '*');
		ascii.put(SUBGIANT, ExtChars.STAR);
		ascii.put(GIANT, ExtChars.STAR);
		ascii.put(SUPERGIANT, ExtChars.CIRCLE);
		ascii.put(HYPERGIANT, ExtChars.CIRCLE);
		ascii.put(BINARY_STAR, ExtChars.INFINITY);
		ascii.put(NEUTRON_STAR, '+');
		ascii.put(PULSAR, '*');
		ascii.put(LIQUID_REGION, ExtChars.APPROX_EQUAL);
		ascii.put(FLAT_REGION, '+');
		ascii.put(HILL_REGION, ExtChars.BUMP);
		ascii.put(MOUNTAIN_REGION, ExtChars.TRIANGLE_U);
		ascii.put(FOREST_REGION, ExtChars.SPADE);
	}

	private static HashMap<Symbol, Character> map;

	public static void setMap(boolean usingTiles)
	{
		if (map == null) {
			map = usingTiles ? tiles : ascii;
		}
	}

	public static ColorChar empty()
	{
		return new ColorChar(EMPTY.get(), AsciiPanel.brightBlack);
	}

	public static ColorChar player()
	{
		return new ColorChar(PLAYER.get(), AsciiPanel.brightWhite);
	}

	@Override
	public String toString()
	{
		return Character.toString(get());
	}

	public char get()
	{
		return map.get(this);
	}
}
