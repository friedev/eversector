package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.MenuScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupMenu;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.actions.Toggle;
import maugrift.eversector.items.Module;

import static maugrift.eversector.Main.*;

/**
 * The screen used to toggle certain modules on or off.
 *
 * @author Maugrift
 */
public class ToggleScreen extends MenuScreen implements WindowScreen<PopupWindow>
{
    /**
     * Instantiates a new ToggleScreen.
     */
    public ToggleScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));

        for (Module module : player.getModules())
        {
            if (module != null && module.hasEffect())
            {
                getMenu().getWindow().getContents().add(new ColorString(module.toString()));
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
        String module = getMenu().getSelection().toString();
        boolean hadFlag = player.hasFlag(player.getModule(module).getEffect());

        String toggleExecution = new Toggle(getMenu().getSelection().toString()).execute(player);
        if (toggleExecution != null)
        {
            addError(toggleExecution);
        }

        return this;
    }
}