package boldorf.eversector.screens;

import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.faction.Faction;

import static boldorf.eversector.Main.*;

/**
 * The menu for choosing a faction to join.
 *
 * @author Maugrift
 */
public class JoinScreen extends MenuScreen<PopupMenu>
{
    /**
     * Instantiates a new JoinScreen.
     */
    public JoinScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        for (Faction faction : galaxy.getFactions())
        {
            getMenu().getWindow().getContents().add(faction.toColorString());
        }
    }

    @Override
    public Screen onConfirm()
    {
        player.joinFaction(galaxy.getFactions()[getMenu().getSelectionIndex()]);
        return null;
    }
}