package maugrift.eversector.screens;

import asciiPanel.AsciiPanel;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.Option;
import maugrift.eversector.Paths;
import maugrift.eversector.ships.Reputation.ReputationRange;
import maugrift.eversector.ships.Ship;
import maugrift.apwt.util.FileManager;
import maugrift.apwt.util.Utility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static maugrift.eversector.Main.*;

/**
 * The screen displayed at the end of the game.
 *
 * @author Aaron Friesen
 */
public class EndScreen extends Screen implements WindowScreen<PopupWindow>
{
	/**
	 * The minimum number of turns that must pass before a score is logged.
	 */
	private static final int MIN_TURNS = 10;

	/**
	 * The color of the current score.
	 */
	public static final Color COLOR_SCORE = AsciiPanel.brightWhite;

	/**
	 * The color of the leaderboard header.
	 */
	public static final Color COLOR_HEADER = AsciiPanel.brightYellow;

	/**
	 * The window.
	 */
	private PopupWindow window;

	/**
	 * Instantiates a new EndScreen.
	 *
	 * @param message     the message to display as the reason for the game
	 *                    ending
	 * @param leaderboard if true, will show the leaderboard
	 * @param saved       true if the game was saved; will influence what files
	 *                    are saved
	 */
	public EndScreen(ColorString message, boolean leaderboard, boolean saved)
	{
		super(Main.display);
		window = new PopupWindow(
				Main.display,
				new Border(2),
				new Line(true, 2, 1)
		);
		if (leaderboard && Option.LEADERBOARD.toBoolean())
		{
			setUpLeaderboard();
		}
		setUpWindow(message);

		if (saved)
		{
			Properties save = player.toProperties();
			Option.SEED.setProperty(Long.toString(Main.seed));
			Option.KEEP_SEED.setProperty(true);

			try
			{
				FileManager.save(save, Paths.SAVE);
			}
			catch (IOException io)
			{
			}
		}
		else
		{
			Option.KEEP_SEED.setProperty(false);
			FileManager.delete(Paths.SAVE);
		}
	}

	/**
	 * Instantiates a new EndScreen with no message.
	 *
	 * @param leaderboard if true, will show the leaderboard
	 * @param saved       true if the game was saved; will influence what files
	 *                    are saved
	 */
	public EndScreen(boolean leaderboard, boolean saved)
	{
		this(null, leaderboard, saved);
	}

	@Override
	public void displayOutput()
	{
		ColorString[] titleArt = StartScreen.getTitleArt();
		getDisplay().writeCenter(
				getDisplay().getCenterY()
				- titleArt.length / 2
				- window.getContents().size() / 2
				- 1,
				titleArt
		);
		window.display();
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (key.getKeyCode() == KeyEvent.VK_R)
		{
			try
			{
				return new StartScreen(startGame());
			}
			catch (Exception e)
			{
				Thread.getDefaultUncaughtExceptionHandler().uncaughtException(
						Thread.currentThread(),
						e
				);
				System.exit(1);
			}

			return null;
		}

		if (key.getKeyCode() == KeyEvent.VK_Q && key.isShiftDown())
		{
			System.exit(0);
		}

		return this;
	}

	@Override
	public PopupWindow getWindow()
	{
		return window;
	}

	private void setUpWindow(ColorString message)
	{
		List<ColorString> contents = window.getContents();

		if (message != null)
		{
			contents.add(message);
		}

		int shipValue = player.calculateShipValue();

		// If the player hasn't improved their ship, don't print it
		if (shipValue > Ship.BASE_VALUE)
		{
			contents.add(
					new ColorString("You owned a ship worth ")
					.add(
						new ColorString(
							Integer.toString(shipValue),
							COLOR_FIELD
						)
					)
					.add(" credits.")
			);
		}

		// Print either leadership and reputation status
		if (player.isAligned() &&
				player.getReputation(player.getFaction()).get() != 0)
		{
			ReputationRange reputation = player
				.getReputation(player.getFaction()).
				getRange();

			String playerArticle = player.isLeader()
				? "the"
				: Utility.getArticle(reputation.getAdjective());
			String playerTitle = player.isLeader()
				? "leader"
				: "member";

			contents.add(new ColorString("You were " + playerArticle + " ")
					.add(
						new ColorString(
							reputation.getAdjective().toLowerCase(),
							reputation.getColor()
						)
					)
					.add(" " + playerTitle + " of the ")
					.add(player.getFaction())
					.add("."));
		}
		else if (player.isPirate())
		{
			contents.add(new ColorString("You were a despicable pirate."));
		}
		else if (!player.isAligned())
		{
			contents.add(new ColorString("You were an unaligned wanderer."));
		}

		if (galaxy.getTurn() == 0)
		{
			contents.add(new ColorString("Thanks for playing!"));
		}
		else
		{
			contents.add(new ColorString("Thanks for playing ")
					.add(new ColorString(Integer.toString(galaxy.getTurn()), COLOR_FIELD))
					.add(" " + Utility.makePlural("turn", galaxy.getTurn()) + "!"));
		}

		contents.add(new ColorString("Press ")
				.add(new ColorString("r", COLOR_FIELD))
				.add(" to play again or ")
				.add(new ColorString("Q", COLOR_FIELD))
				.add(" to quit."));
	}

	private void setUpLeaderboard()
	{
		List<ColorString> contents = window.getContents();

		if (galaxy.getTurn() <= MIN_TURNS)
		{
			window.getContents().addAll(LeaderboardScore.buildLeaderboard());
			contents.add(
					new ColorString(
						"This game has been too short to log a score.",
						COLOR_SCORE
					)
			);
		}
		else if (player.calculateShipValue() <= Ship.BASE_VALUE)
		{
			window.getContents().addAll(LeaderboardScore.buildLeaderboard());
			contents.add(
					new ColorString(
						"You have not scored enough for a leaderboard entry.",
						COLOR_SCORE
					)
			);
		}
		else
		{
			String reputationAdjective;
			if (player.isAligned())
			{
				reputationAdjective = player
					.getReputation(player.getFaction())
					.getRange()
					.getAdjective();
			}
			else if (player.isPirate())
			{
				reputationAdjective = "Pirate";
			}
			else
			{
				reputationAdjective = "Wanderer";
			}

			String name = Option.CAPTAIN_NAME.getProperty();

			LeaderboardScore playerScore;
			if (Option.CAPTAIN_NAME.getDefault().equals(name))
			{
				playerScore = new LeaderboardScore(
						player.calculateShipValue(),
						galaxy.getTurn(),
						kills,
						reputationAdjective,
						player.isLeader()
				);
			}
			else
			{
				playerScore = new LeaderboardScore(
						name,
						player.calculateShipValue(),
						galaxy.getTurn(),
						kills,
						reputationAdjective,
						player.isLeader()
				);
			}

			// Current player's score must be added after the leaderboard print
			// so that it isn't duplicated in the displayed list
			int nScores = printLeaderboard(playerScore);
			try
			{
				FileManager.save(
						playerScore.toProperties(),
						Paths.LEADERBOARD
						+ "score_"
						+ nScores
						+ ".properties"
				);
			}
			catch (IOException e)
			{
			}
		}

		window.addSeparator();
	}

	/**
	 * Prints the leaderboard as well as the given score, if separate.
	 *
	 * @param playerScore the score to add to the leaderboard and print
	 *                    regardless of position
	 * @return the number of scores
	 */
	private int printLeaderboard(LeaderboardScore playerScore)
	{
		List<ColorString> contents = window.getContents();
		List<LeaderboardScore> scores = LeaderboardScore.getLeaderboardScores();

		if (scores == null || scores.isEmpty())
		{
			contents.add(new ColorString("LEADERBOARD", COLOR_HEADER));
			contents.add(new ColorString(playerScore.toString(), COLOR_SCORE));
			return 1;
		}

		scores.add(playerScore);
		scores.sort(Comparator.reverseOrder());

		boolean playerScoreNoted = false;
		contents.add(LeaderboardScore.buildLeaderboardHeader(scores.size()));

		for (int i = 0;
				i < Math.min(scores.size(), LeaderboardScore.DISPLAYED_SCORES);
				i++)
		{
			ColorString scoreString = new ColorString(scores.get(i).toString());

			if (!playerScoreNoted && scores.get(i) == playerScore)
			{
				scoreString.setForeground(COLOR_SCORE);
				playerScoreNoted = true;
			}

			contents.add(scoreString);
		}

		int playerRank = scores.indexOf(playerScore);
		if (!playerScoreNoted)
		{
			if (playerRank > LeaderboardScore.DISPLAYED_SCORES)
			{
				contents.add(
						new ColorString(
							"("
							+ (playerRank - LeaderboardScore.DISPLAYED_SCORES)
							+ " more)",
							AsciiPanel.brightBlack
						)
				);
			}

			contents.add(new ColorString(playerScore.toString(), COLOR_SCORE));
		}

		return scores.size();
	}
}
