package maugrift.eversector.screens;

import maugrift.apwt.screens.MenuScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupMenu;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.StartBattle;
import maugrift.eversector.ships.Ship;

import static maugrift.eversector.Main.*;

/**
 * A menu for choosing ships to attack.
 *
 * @author Maugrift
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