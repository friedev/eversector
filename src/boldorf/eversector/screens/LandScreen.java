package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Region;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.storage.Paths.ENGINE;
import java.awt.event.KeyEvent;

/**
 * 
 */
public class LandScreen extends MenuScreen<PopupMenu>
{
    public LandScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        getConfirmCodes().add(KeyEvent.VK_RIGHT);
        getCancelCodes().add(KeyEvent.VK_LEFT);
        
        for (Region region:
                player.getSector().getPlanetAt(player.getOrbit()).getRegions())
            getMenu().getWindow().getContents().add(region.toColorString());
    }
    
    @Override
    public Screen onConfirm()
    {
        if (player.land(getMenu().getSelectionIndex()))
        {
            playSoundEffect(ENGINE);
            player.getMap().nextTurn();
            return new PlanetScreen(getDisplay());
        }
        return null;
    }
}