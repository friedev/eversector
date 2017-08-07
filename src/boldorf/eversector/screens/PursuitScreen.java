package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Battle;
import boldorf.eversector.entities.Ship;
import boldorf.eversector.storage.Actions;
import java.util.List;

/**
 * 
 */
public class PursuitScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private Battle battle;
    private List<Ship> pursuing;
    
    public PursuitScreen(Display display, Battle battle, List<Ship> pursuing)
    {
        super(display);
        this.battle = battle;
        this.pursuing = pursuing;
        window = new PopupWindow(display);
        window.getContents().add(new ColorString("Pursue ").add(pursuing.get(0))
                .add("?"));
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
        player.changeResourceBy(Actions.PURSUE);
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