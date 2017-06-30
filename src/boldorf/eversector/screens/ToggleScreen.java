package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.items.Module;
import static boldorf.eversector.storage.Paths.OFF;
import static boldorf.eversector.storage.Paths.ON;

/**
 * 
 */
public class ToggleScreen extends MenuScreen
        implements WindowScreen<PopupWindow>
{
    public ToggleScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        
        for (Module module: player.getModules())
            if (module != null && module.hasEffect())
                getMenu().getWindow().getContents()
                        .add(new ColorString(module.toString()));
    }
    
    @Override
    public PopupWindow getWindow()
        {return (PopupWindow) getMenu().getWindow();}
    
    @Override
    public Screen onConfirm()
    {
        String module = getMenu().getSelection().toString();
        boolean hadFlag = player.hasFlag(player.getModule(module).getEffect());
        player.toggleActivation(getMenu().getSelection().toString());
        boolean hasFlag = player.hasFlag(player.getModule(module).getEffect());
        if (hasFlag && !hadFlag)
            playSoundEffect(ON);
        else if (!hasFlag && hadFlag)
            playSoundEffect(OFF);
        return null;
    }
}