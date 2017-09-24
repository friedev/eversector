package boldorf.eversector.screens;

import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import boldorf.eversector.map.Planet;
import boldorf.eversector.map.Sector;
import boldorf.eversector.map.Station;
import boldorf.eversector.ships.Ship;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static boldorf.eversector.Main.*;
import static boldorf.eversector.Paths.*;

/**
 * The screen used for navigating the orbits of sectors.
 *
 * @author Boldorf Smokebane
 */
class SectorScreen extends Screen implements WindowScreen<AlignedWindow>, PopupMaster, KeyScreen
{
    /**
     * The window.
     */
    private AlignedWindow window;

    /**
     * The screen temporarily displayed over and overriding all others.
     */
    private Screen popup;

    /**
     * The orbit that is being looked at. 0 if not looking.
     */
    private int cursor;

    /**
     * Instantiates a new SectorScreen.
     */
    public SectorScreen()
    {
        super(Main.display);
        window = new AlignedWindow(Main.display, Coord.get(0, 0), new Border(2));
        cursor = 0;
    }

    @Override
    public void displayOutput()
    {
        setUpWindow();
        window.display();

        if (popup != null)
        {
            if (popup instanceof WindowScreen && ((WindowScreen) popup).getWindow() instanceof AlignedWindow)
            {
                ((AlignedWindow) ((WindowScreen) popup).getWindow()).setLocation(
                        Coord.get(1, window.getBottomRight().y + 3));
            }

            popup.displayOutput();
        }
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            popup = popup.processInput(key);

            if (popup instanceof SectorScreen)
            {
                popup = null;
                return this;
            }

            return popup instanceof PlanetScreen || popup instanceof StationScreen || popup instanceof BattleScreen ||
                   popup instanceof EndScreen ? popup : this;
        }

        // This is necessary both here and below to avoid interruptions
        if (pendingBattle != null)
        {
            return new BattleScreen(pendingBattle, false);
        }

        boolean nextTurn = false;
        Screen nextScreen = this;

        if (isLooking())
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    if (player.getLocation().getSector().isValidOrbit(cursor - 1))
                    {
                        cursor--;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (player.getLocation().getSector().isValidOrbit(cursor + 1))
                    {
                        cursor++;
                    }
                    break;
                case KeyEvent.VK_L:
                case KeyEvent.VK_ENTER:
                case KeyEvent.VK_ESCAPE:
                    cursor = 0;
                    break;
            }

            return this;
        }

        switch (key.getKeyCode())
        {
            case KeyEvent.VK_UP:
                if (player.orbit(false))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                }
                break;
            case KeyEvent.VK_DOWN:
                if (player.orbit(true))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                    if (!player.isInSector())
                    {
                        nextScreen = new MapScreen();
                    }
                }
                break;
            case KeyEvent.VK_LEFT:
                Station station = player.getSectorLocation().getStation();
                if (station != null && player.isHostile(station.getFaction()) && player.canClaim(station, false))
                {
                    popup = new ClaimStationScreen();
                }
                else if (player.dock())
                {
                    nextTurn = true;
                    nextScreen = new StationScreen();
                    playSoundEffect(DOCK);
                }
                break;
            case KeyEvent.VK_RIGHT:
                Planet planet = player.getSectorLocation().getPlanet();
                if (planet == null)
                {
                    player.addPlayerError("There is no planet at this orbit.");
                    break;
                }

                if (planet.getType().canMineFromOrbit())
                {
                    if (player.canMine(false) && player.isDangerousToMine())
                    {
                        popup = new AsteroidMineConfirmScreen();
                        break;
                    }
                    else if (player.mine(true))
                    {
                        playSoundEffect(MINE);
                        nextTurn = true;
                        break;
                    }
                }
                else if (player.canLand())
                {
                    popup = new LandScreen();
                }
                else if (player.canCrashLand(false))
                {
                    popup = new CrashLandScreen();
                }
                break;
            case KeyEvent.VK_A:
            {
                if (!player.hasWeapons())
                {
                    break;
                }

                Sector sector = player.getLocation().getSector();
                int orbit = player.getSectorLocation().getOrbit();
                if (sector.getShipsAt(orbit).size() <= 1)
                {
                    break;
                }

                if (sector.getShipsAt(orbit).size() == 2)
                {
                    List<Ship> ships = player.getSectorLocation().getSector().getShipsAt(
                            player.getSectorLocation().getOrbit());
                    return new BattleScreen(player.startBattle(ships.get(0) == player ? ships.get(1) : ships.get(0)),
                            true);
                }
                else
                {
                    popup = new AttackScreen();
                }
                break;
            }
            case KeyEvent.VK_L:
                cursor = player.getSectorLocation().getOrbit();
                break;
        }

        if (nextTurn)
        {
            galaxy.nextTurn();
        }

        if (pendingBattle != null)
        {
            return new BattleScreen(pendingBattle, false);
        }

        return nextScreen;
    }

    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("change orbit", ExtChars.ARROW1_U, ExtChars.ARROW1_D));
        if (!isLooking())
        {
            keybindings.add(new Keybinding("land", ExtChars.ARROW1_R));
            keybindings.add(new Keybinding("dock", ExtChars.ARROW1_L));

            if (player.hasWeapons())
            {
                keybindings.add(new Keybinding("attack", "a"));
            }
        }
        keybindings.add(new Keybinding("look", "l"));
        return keybindings;
    }

    @Override
    public AlignedWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen getPopup()
    {
        return popup;
    }

    /**
     * Returns true if the player is looking with the cursor.
     *
     * @return true if the player is looking with the cursor
     */
    private boolean isLooking()
    {
        return cursor != 0;
    }

    /**
     * Sets up the window and its contents.
     */
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        Sector sector = Main.player.getLocation().getSector();
        contents.clear();
        window.getSeparators().clear();

        contents.add(new ColorString(sector.toString()));
        contents.add(new ColorString("Star: ").add(sector.getStar()));

        if (sector.hasNebula())
        {
            contents.add(new ColorString("Nebula: ").add(sector.getNebula()));
        }

        contents.add(new ColorString("Ruler: ").add(
                sector.isClaimed() ? sector.getFaction().toColorString() : new ColorString("Disputed", COLOR_FIELD)));

        window.addSeparator(new Line(true, 2, 1));
        for (int orbit = 1; orbit <= sector.getOrbits(); orbit++)
        {
            ColorString orbitSymbol = sector.getSymbolsForOrbit(orbit);
            if (cursor == orbit)
            {
                orbitSymbol.setBackground(COLOR_SELECTION_BACKGROUND);
            }
            contents.add(orbitSymbol);
        }

        window.addSeparator(new Line(false, 1, 2, 1));
        contents.addAll(sector.getOrbitContents(isLooking() ? cursor : player.getSectorLocation().getOrbit()));
    }
}
