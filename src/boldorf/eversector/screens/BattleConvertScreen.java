package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Ship;

/**
 * 
 */
public class BattleConvertScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private Ship converting;
    
    public BattleConvertScreen(Display display, Ship converting)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(converting.toColorString()
                .add(" offers to spare you if you join the ")
                .add(converting.getFaction()).add("."));
        window.getContents().add(new ColorString("Accept the offer?"));
        this.converting = converting;
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
        converting.convert(player);
        return new SectorScreen(getDisplay());
    }
}