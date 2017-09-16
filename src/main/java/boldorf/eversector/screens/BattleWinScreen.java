package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.ships.Reputation;
import boldorf.eversector.ships.Ship;

import static boldorf.eversector.Main.kills;
import static boldorf.eversector.Main.player;

/**
 *
 */
public class BattleWinScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private Ship looting;

    public BattleWinScreen(Display display, Ship looting, ColorString message)
    {
        super(display);
        window = new PopupWindow(display);
        window.getContents().add(message);
        window.getContents().add(new ColorString("Loot them?"));
        this.looting = looting;
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
        player.loot(looting);
        return onCancel();
    }

    @Override
    public Screen onCancel()
    {
        if (looting.isLeader())
        {
            if (player.getFaction() == looting.getFaction())
            {
                looting.getFaction().addNews(player + " has destroyed our " + "leader, " + looting.toString() + ".");
            }
            else
            {
                looting.getFaction().addNews(
                        player + " of the " + player.getFaction() + " has destroyed our " + "leader, " +
                        looting.toString() + ".");
            }
        }

        player.changeReputation(player.getFaction(),
                player.isPassive(looting) ? Reputation.KILL_ALLY : Reputation.KILL_ENEMY);
        player.changeReputation(looting.getFaction(), Reputation.KILL_ALLY);

        looting.destroy(false);
        kills++;
        return new SectorScreen(getDisplay());
    }
}