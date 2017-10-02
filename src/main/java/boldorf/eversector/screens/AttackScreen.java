package boldorf.eversector.screens;

import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.actions.StartBattle;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.*;

/**
 * A menu for choosing ships to attack.
 *
 * @author Boldorf Smokebane
 */
public class AttackScreen extends MenuScreen<PopupMenu> implements WindowScreen<PopupWindow>
{
    /**
     * Instantiates a new AttackScreen.
     */
    public AttackScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));

        for (Ship ship : player.getLocation().getSector().getShipsAt(player.getSectorLocation().getOrbit()))
        {
            if (!ship.isPlayer())
            {
                getMenu().getWindow().getContents().add(ship.toColorString());
            }
        }
    }

    @Override
    public PopupWindow getWindow()
    {
        return (PopupWindow) getMenu().getWindow();
    }

    @Override
    public Screen onConfirm()
    {
        Ship opponent = player.getLocation().getSector().getShip(getMenu().getSelection().toString());
        new StartBattle(opponent).execute(player);
        return new BattleScreen(player.getBattleLocation().getBattle(), true);
    }
}