package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

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
     * The leader getting the vote.
     */
    private String leaderSelection;

    /**
     * Instantiates a new VotingConfirmScreen.
     */
    public VotingConfirmScreen(String selection)
    {
        super(Main.display);
        window = new PopupWindow(Main.display);
        leaderSelection = selection;

        window.getContents().add(new ColorString("Submit your vote for " + leaderSelection + "?"));
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
        Main.pendingElection.addVote(leaderSelection.substring(0, leaderSelection.indexOf(" (")));

        return new ElectionResultsScreen();
    }

    @Override
    public Screen onCancel()
    {
        return new VotingScreen();
    }
}
