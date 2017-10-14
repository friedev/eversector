package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.faction.RelationshipChange;

import static maugrift.eversector.Main.pendingRelationships;
import static maugrift.eversector.Main.player;

/**
 * The screen used to respond to relationship requests as a leader.
 *
 * @author Maugrift
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