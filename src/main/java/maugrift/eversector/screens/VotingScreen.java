package maugrift.eversector.screens;

import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.MenuScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.apwt.windows.PopupMenu;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.ships.Reputation.ReputationRange;
import maugrift.eversector.ships.Ship;

import java.awt.event.KeyEvent;
import java.util.List;

import static maugrift.eversector.Main.*;

/**
 * The screen used to vote on faction leaders.
 *
 * @author Aaron Friesen
 */
public class VotingScreen
	extends MenuScreen<PopupMenu>
	implements WindowScreen<PopupWindow>, PopupMaster
{

	/**
	 * Popup screen for the confirmation prompt.
	 */
	private Screen popup;

	/**
	 * Instantiates a new VotingScreen.
	 */
	public VotingScreen()
	{
		super(
			new PopupMenu(
				new PopupWindow(
					Main.display,
					new Border(1),
					new Line(true, 1, 1)
				),
				COLOR_SELECTION_FOREGROUND,
				COLOR_SELECTION_BACKGROUND
			)
		);
		pendingElection.gatherVotes();
		setUpMenu();
	}

	@Override
	public void displayOutput()
	{
		super.displayOutput();

		if (popup != null) {
			popup.displayOutput();
		}
	}

	@Override
	public PopupWindow getWindow()
	{
		return (PopupWindow) getMenu().getWindow();
	}

	@Override
	public Screen getPopup()
	{
		return popup;
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (popup != null) {
			popup = popup.processInput(key);

			if (popup instanceof ElectionResultsScreen) {
				return popup;
			}

			return this;
		}

		return super.processInput(key);
	}

	@Override
	public Screen onConfirm()
	{
		String selection = getMenu().getSelection().toString();
		popup = new VotingConfirmScreen(selection);
		return this;
	}

	@Override
	public Screen onCancel()
	{
		return new ElectionResultsScreen();
	}

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
		for (int i = 0; i < pendingElection.getCandidates().size(); i++) {
			getMenu().getRestrictions().add(i + 4);
			Ship candidate = pendingElection.getCandidates().get(i);
			ReputationRange reputation = candidate.getReputation(
					pendingElection.getFaction()
				).getRange();
			contents.add(
				new ColorString(candidate.toString())
				.add(" ")
				.add(
					new ColorString(
						"("
						+ reputation.getAdjective()
						+ ")",
						reputation.getColor()
					)
				)
			);
		}

		getMenu().setSelectionIndex(4);
	}
}
