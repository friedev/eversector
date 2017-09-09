package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.player;
import boldorf.eversector.faction.Faction;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;

/**
 * 
 */
public class JoinScreen extends MenuScreen<PopupMenu>
{
    public JoinScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        for (Faction faction: map.getFactions())
            getMenu().getWindow().getContents().add(faction.toColorString());
    }
    
    @Override
    public Screen onConfirm()
    {
        player.joinFaction(map.getFaction(getMenu().getSelectionIndex()));
        return null;
    }
}