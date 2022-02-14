package maugrift.eversector.screens;

import maugrift.apwt.screens.MenuScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.windows.PopupMenu;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.faction.Faction;

import static maugrift.eversector.Main.*;

/**
 * The menu for choosing a faction to join.
 *
 * @author Aaron Friesen
 */
public class JoinScreen extends MenuScreen<PopupMenu>
{
	/**
	 * Instantiates a new JoinScreen.
	 */
	public JoinScreen()
	{
		super(
				new PopupMenu(
					new PopupWindow(Main.display),
					COLOR_SELECTION_FOREGROUND,
					COLOR_SELECTION_BACKGROUND
				)
		);
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
