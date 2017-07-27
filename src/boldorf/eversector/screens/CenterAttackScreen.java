package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.player;
import boldorf.eversector.map.Sector;

/**
 * 
 */
public class CenterAttackScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public CenterAttackScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(new ColorString(
                "Combat is strictly prohibited in the central sector."));
        window.getContents().add(new ColorString("Attack anyway?"));
    }

    @Override
    public void displayOutput()
        {window.display();}

    @Override
    public PopupWindow getWindow()
        {return window;}
    
    @Override
    public Screen onConfirm()
    {
        Sector sector = player.getLocation().getSector();
        if (sector.getShipsAt(player.getSectorLocation().getOrbit()).size()
                == 2)
        {
            return new BattleScreen(getDisplay(),
                    player.startBattle(sector.getFirstOtherShip(player)), true);
        }
        
        return new AttackScreen(getDisplay());
    }
}