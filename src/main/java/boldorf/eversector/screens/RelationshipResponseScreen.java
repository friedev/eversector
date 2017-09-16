package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.faction.RelationshipChange;

import static boldorf.eversector.Main.pendingRelationships;
import static boldorf.eversector.Main.player;

/**
 *
 */
public class RelationshipResponseScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    private RelationshipChange change;

    public RelationshipResponseScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        change = pendingRelationships.poll();
        window.getContents().add(change.getMessage());
        window.getContents().add(new ColorString(change.getQuestion()));
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
        if (!change.negateAnswer()) { enactChange(); }
        return null;
    }

    @Override
    public Screen onCancel()
    {
        if (change.negateAnswer()) { enactChange(); }
        return null;
    }

    public void enactChange()
    {
        player.getFaction().setRelationship(change.getOtherFaction(), change.getRelationship());
    }
}