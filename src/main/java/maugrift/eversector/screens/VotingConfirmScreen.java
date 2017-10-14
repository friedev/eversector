package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;

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

        window.getContents().add(
                new ColorString("Submit your vote for ").add(
                    new ColorString(leaderSelection, Main.player.getFaction().getColor()).add(
                        new ColorString("?")
                    )
                )
            );
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
