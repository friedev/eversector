package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.Pursue;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;

import java.util.List;

import static boldorf.eversector.Main.player;

/**
 * The prompt presenting to the player when enemy ships flee a battle that they're in.
 *
 * @author Maugrift
 */
public class PursuitScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * The battle the player is in.
     */
    private Battle battle;

    /**
     * The ships the player has the option of pursuing.
     */
    private List<Ship> pursuing;

    /**
     * Instantiates a new PursuitScreen.
     *
     * @param battle   the battle the player is in
     * @param pursuing the ships the player has the option of pursuing
     */
    public PursuitScreen(Battle battle, List<Ship> pursuing)
    {
        super(Main.display);
        this.battle = battle;
        this.pursuing = pursuing;
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Pursue ").add(pursuing.get(0)).add("?"));
    }

    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public PopupWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen onConfirm()
    {
        new Pursue().execute(player);
        List<Ship> pursuers = battle.getPursuers(pursuing.get(0));
        pursuers.add(player);
        battle.processEscape(pursuing.get(0), pursuers);
        return null;
    }

    @Override
    public Screen onCancel()
    {
        battle.processEscape(pursuing.get(0));
        pursuing.remove(0);
        return pursuing.isEmpty() ? null : this;
    }
}