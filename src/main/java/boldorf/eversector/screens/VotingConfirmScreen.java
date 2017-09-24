package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.faction.Election;

/**
 * The prompt when selecting a faction leader vote.
 */
public class VotingConfirmScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * The menu containing election candidates.
     */
    private PopupMenu menu;

    /**
     * The pending election candidates.
     */
    private Election pendingElection;

    /**
     * Instantiates a new VotingConfirmScreen.
     */
    public VotingConfirmScreen(PopupMenu electionMenu, Election election, String leaderSelection)
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        window.getContents().add(new ColorString("Submit your vote for " + leaderSelection + "?"));

        menu = electionMenu;
        pendingElection = election;
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
        String shipString = menu.getSelection().toString();
        pendingElection.addVote(shipString.substring(0, shipString.indexOf(" (")));

        return new ElectionResultsScreen();
    }

    @Override
    public Screen onCancel()
    {
        return new VotingScreen();
    }
}
