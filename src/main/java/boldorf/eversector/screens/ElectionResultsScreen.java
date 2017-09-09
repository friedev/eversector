package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.ConfirmationScreen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.pendingElection;
import boldorf.eversector.ships.Reputation.ReputationRange;
import boldorf.eversector.ships.Ship;
import java.util.List;

/**
 * 
 */
public class ElectionResultsScreen extends ConfirmationScreen
        implements WindowScreen<PopupWindow>
{
    private PopupWindow window;
    
    public ElectionResultsScreen(Display display)
    {
        super(display);
        window = new PopupWindow(display, new Border(1), new Line(true, 1, 1));
        setUpWindow();
    }

    @Override
    public void displayOutput()
        {window.display();}

    @Override
    public PopupWindow getWindow()
        {return window;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        
        Ship winner = pendingElection.getWinner();
        
        String messageSubject = winner.isPlayer() ?
                "You have" : winner + " has";
        String messageAction = pendingElection.isReelected(winner) ?
                "been reelected." : "won the election.";
        contents.add(new ColorString(messageSubject + " " + messageAction));
        window.addSeparator();
        
        for (int i = 0; i < pendingElection.getCandidates().size(); i++)
        {
            Ship candidate = pendingElection.getCandidates().get(i);
            String shipName = candidate.isPlayer() ? "You" :
                    candidate.toString();
            ReputationRange reputation = candidate
                    .getReputation(pendingElection.getFaction()).getRange();
            int votes = pendingElection.getVotes().get(i);
            contents.add(new ColorString(shipName).add(" ")
                    .add(new ColorString("(" + reputation.getAdjective() + ")",
                            reputation.getColor())).add(" - ")
                    .add(new ColorString(Integer.toString(votes), COLOR_FIELD))
                    .add(votes == 1 ? " Vote" : " Votes"));
        }
        
        pendingElection.lowerWinnerReputation(winner);
        pendingElection.getFaction().setLeader(winner);
        pendingElection = null;
    }
}