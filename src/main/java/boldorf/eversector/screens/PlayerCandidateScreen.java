package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;

import java.util.List;

import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.player;

/**
 * The screen presented to the player when they have been nominated for leader.
 *
 * @author Boldorf Smokebane
 */
public class PlayerCandidateScreen extends ConfirmationScreen
{
    /**
     * The window.
     */
    private PopupWindow window;

    /**
     * Instantiates a new PlayerCandidateScreen.
     */
    public PlayerCandidateScreen()
    {
        super(Main.display);
        window = new PopupWindow(Main.display);

        List<ColorString> contents = window.getContents();
        contents.add(pendingElection.getDescription());
        if (pendingElection.isReelected(player))
        {
            contents.add(new ColorString("You have performed well as leader and have been nominated again."));
            contents.add(new ColorString("Run for reelection?"));
        }
        else
        {
            contents.add(new ColorString("You have been recognized for your deeds and nominated."));
            contents.add(new ColorString("Run for office?"));
        }
    }

    @Override
    public void displayOutput()
    {
        window.display();
    }

    @Override
    public Screen onConfirm()
    {
        pendingElection.addPlayer();
        pendingElection.gatherVotes();
        return new ElectionResultsScreen();
    }

    @Override
    public Screen onCancel()
    {
        return new VotingScreen();
    }
}