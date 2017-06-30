package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.pendingElection;
import boldorf.eversector.entities.ReputationRange;
import boldorf.eversector.entities.Ship;
import java.util.List;

/**
 * 
 */
public class VotingScreen extends MenuScreen<PopupMenu>
        implements WindowScreen<PopupWindow>
{
    public VotingScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display, new Border(1),
                new Line(true, 1, 1)),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        pendingElection.gatherVotes();
        setUpMenu();
    }

    @Override
    public PopupWindow getWindow()
        {return (PopupWindow) getMenu().getWindow();}
    
    @Override
    public Screen onConfirm()
    {
        String shipString = getMenu().getSelection().toString();
        pendingElection.addVote(shipString.substring(0,
                shipString.indexOf(" (")));
        return onCancel();
    }
    
    @Override
    public Screen onCancel()
        {return new ElectionResultsScreen(getDisplay());}
    
    private void setUpMenu()
    {
        List<ColorString> contents = getWindow().getContents();
        contents.add(pendingElection.getDescription());
        contents.add(new ColorString("The candidates are listed below."));
        contents.add(new ColorString("Press ")
                .add(new ColorString("enter", COLOR_FIELD))
                .add(" on a candidate to vote or ")
                .add(new ColorString("escape", COLOR_FIELD))
                .add(" to abstain."));
        
        getWindow().addSeparator();
        for (int i = 0; i < pendingElection.getCandidates().size(); i++)
        {
            getMenu().getRestrictions().add(i + 4);
            Ship candidate = pendingElection.getCandidates().get(i);
            ReputationRange reputation = candidate
                    .getReputation(pendingElection.getFaction()).getRange();
            contents.add(new ColorString(candidate.toString()).add(" ")
                    .add(new ColorString("(" + reputation.getAdjective() + ")",
                            reputation.getColor())));
        }
        
        getMenu().setSelectionIndex(4);
    }
}