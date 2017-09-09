package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.player;
import boldorf.eversector.ships.Ship;

/**
 * 
 */
public class AttackScreen extends MenuScreen<PopupMenu>
        implements WindowScreen<PopupWindow>
{
    public AttackScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        
        for (Ship ship: player.getLocation().getSector()
                .getShipsAt(player.getSectorLocation().getOrbit()))
        {
            if (!ship.isPlayer())
                getMenu().getWindow().getContents().add(ship.toColorString());
        }
    }

    @Override
    public PopupWindow getWindow()
        {return (PopupWindow) getMenu().getWindow();}
    
    @Override
    public Screen onConfirm()
    {
        Ship opponent = player.getLocation().getSector().getShip(
                getMenu().getSelection().toString());
        return new BattleScreen(getDisplay(), player.startBattle(opponent),
                true);
    }
}