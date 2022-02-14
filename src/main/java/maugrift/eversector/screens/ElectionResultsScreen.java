package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.ConfirmationScreen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.ships.Reputation.ReputationRange;
import maugrift.eversector.ships.Ship;

import java.util.List;

import static maugrift.eversector.Main.COLOR_FIELD;
import static maugrift.eversector.Main.pendingElection;

/**
 * The screen displaying the results of an election.
 *
 * @author Maugrift
 */
public class ElectionResultsScreen extends ConfirmationScreen implements WindowScreen<PopupWindow>
{
	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new ElectionResultsScreen.
	 */
	public ElectionResultsScreen()
	{
		super(Main.display);
		window = new PopupWindow(Main.display, new Border(1), new Line(true, 1, 1));
		setUpWindow();
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

	/**
	 * Sets up the window and its contents.
	 */
	private void setUpWindow()
	{
		List<ColorString> contents = window.getContents();

		Ship winner = pendingElection.getWinner();

		String messageSubject = winner.isPlayer() ? "You have" : winner + " has";
		String messageAction = pendingElection.isReelected(winner) ? "been reelected." : "won the election.";
		contents.add(new ColorString(messageSubject + " " + messageAction));
		window.addSeparator();

		for (int i = 0; i < pendingElection.getCandidates().size(); i++)
		{
			Ship candidate = pendingElection.getCandidates().get(i);
			String shipName = candidate.isPlayer() ? "You" : candidate.toString();
			ReputationRange reputation = candidate.getReputation(pendingElection.getFaction()).getRange();
			int votes = pendingElection.getVotes().get(i);
			contents.add(new ColorString(shipName)
					.add(" ")
					.add(new ColorString("(" + reputation.getAdjective() + ")", reputation.getColor()))
					.add(" - ")
					.add(new ColorString(Integer.toString(votes), COLOR_FIELD))
					.add(votes == 1 ? " Vote" : " Votes"));
		}

		pendingElection.lowerWinnerReputation(winner);
		pendingElection.getFaction().setLeader(winner);
		pendingElection = null;
	}
}
