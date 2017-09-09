package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.player;
import java.util.List;

/**
 * 
 */
public class PlayerCandidateScreen extends ConfirmationScreen
{
    private PopupWindow window;
    
    public PlayerCandidateScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display);
        
        List<ColorString> contents = window.getContents();
        contents.add(pendingElection.getDescription());
        if (pendingElection.isReelected(player))
        {
            contents.add(new ColorString("You have performed well as leader "
                    + "and have been nominated again."));
            contents.add(new ColorString("Run for reelection?"));
        }
        else
        {
            contents.add(new ColorString(
                    "You have been recognized for your deeds and nominated."));
            contents.add(new ColorString("Run for office?"));
        }
    }

    @Override
    public void displayOutput()
        {window.display();}

    @Override
    public Screen onConfirm()
    {
        pendingElection.addPlayer();
        pendingElection.gatherVotes();
        return new ElectionResultsScreen(getDisplay());
    }
    
    @Override
    public Screen onCancel()
        {return new VotingScreen(getDisplay());}
}