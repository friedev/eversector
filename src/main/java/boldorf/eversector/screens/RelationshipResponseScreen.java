package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.faction.RelationshipChange;

import static boldorf.eversector.Main.pendingRelationships;
import static boldorf.eversector.Main.player;

/**
 * The screen used to respond to relationship requests as a leader.
 *
 * @author Boldorf Smokebane
 */
public class RelationshipResponseScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * The relationship change being requested.
     */
    private RelationshipChange change;

    /**
     * Instantiates a new RelationshipResponseScreen.
     */
    public RelationshipResponseScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        change = pendingRelationships.poll();
        window.getContents().add(change.getMessage());
        window.getContents().add(new ColorString(change.getQuestion()));
    }

    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public PopupWindow getWindow()
    {
        return window;
    }

    @Override
    public Screen onConfirm()
    {
        if (!change.negateAnswer())
        {
            enactChange();
        }
        return null;
    }

    @Override
    public Screen onCancel()
    {
        if (change.negateAnswer())
        {
            enactChange();
        }
        return null;
    }

    /**
     * Enacts the relationship change.
     */
    public void enactChange()
    {
        player.getFaction().setRelationship(change.getOtherFaction(), change.getRelationship());
    }
}