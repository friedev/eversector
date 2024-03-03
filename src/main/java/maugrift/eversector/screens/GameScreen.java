package maugrift.eversector.screens;

import maugrift.apwt.ExtChars;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.KeyScreen;
import maugrift.apwt.screens.Keybinding;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.AlignedWindow;
import maugrift.apwt.windows.Border;
import maugrift.apwt.windows.Line;
import maugrift.eversector.Main;
import maugrift.eversector.Option;
import maugrift.eversector.Symbol;
import maugrift.eversector.actions.Distress;
import maugrift.eversector.actions.Refine;
import maugrift.eversector.faction.Faction;
import maugrift.eversector.ships.Reputation.ReputationRange;
import maugrift.eversector.ships.Ship;
import maugrift.apwt.util.Utility;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static maugrift.eversector.Main.*;
import static maugrift.eversector.faction.Relationship.RelationshipType.WAR;

/**
 * The main screen on which gameplay will take place. This screen will process
 * global commands and host more specific screens based on the player's
 * situation.
 *
 * @author Aaron Friesen
 */
public class GameScreen
	extends Screen
	implements WindowScreen<AlignedWindow>, PopupMaster, KeyScreen
{
	/**
	 * The number of lines in the message window.
	 */
	private static final int MESSAGE_LINES = 10;

	/**
	 * The window displaying the player's status.
	 */
	private AlignedWindow statusWindow;

	/**
	 * The window displaying information about factions.
	 */
	private AlignedWindow factionWindow;

	/**
	 * The messages in the message log.
	 */
	private List<Message> messages;

	/**
	 * The screen displayed within this one.
	 */
	private Screen subscreen;

	/**
	 * The screen temporarily displayed over and overriding all others.
	 */
	private Screen popup;

	/**
	 * The offset of messages displayed when scrolling through message history.
	 * -1 if not viewing history.
	 */
	private int messageOffset;

	/**
	 * Instantiates a new GameScreen.
	 */
	public GameScreen()
	{
		super(Main.display);
		statusWindow = new AlignedWindow(Main.display, 1, 1);
		factionWindow = new AlignedWindow(Main.display, 1, 1);
		messages = new LinkedList<>();
		if (player.isOrbital()) {
			subscreen = new SectorScreen();
		} else if (player.isLanded()) {
			subscreen = new PlanetScreen();
		} else if (player.isDocked()) {
			subscreen = new StationScreen();
		} else if (player.isInBattle()) {
			subscreen = new BattleScreen(
				player.getBattleLocation().getBattle(),
				false
			);
		} else {
			subscreen = new MapScreen();
		}
		messageOffset = -1;
	}

	@Override
	public void displayOutput()
	{
		setUpStatusWindow();
		statusWindow.display();
		int bottomY = statusWindow.getBottom();

		setUpFactionWindow();
		factionWindow.setLocation(statusWindow.getRight() + 3, 1);
		factionWindow.display();
		bottomY = Math.max(bottomY, factionWindow.getBottom());

		drawMessageWindow();

		if (subscreen != null) {
			if (
				subscreen instanceof WindowScreen
				&& ((WindowScreen) subscreen).getWindow() instanceof AlignedWindow
			) {
				((AlignedWindow)((WindowScreen) subscreen).getWindow()).setLocation(1, bottomY + 3);
			}

			subscreen.displayOutput();
		}

		if (popup != null) {
			popup.displayOutput();
		}
	}

	@Override
	public Screen processInput(KeyEvent key)
	{
		if (popup != null) {
			popup = popup.processInput(key);

			if (popup instanceof StartScreen) {
				return popup;
			}

			if (popup instanceof EndScreen) {
				subscreen = null;
			}

			return this;
		}

		if (viewingHistory()) {
			if (
				key.getKeyCode() == KeyEvent.VK_H
				|| key.getKeyCode() == KeyEvent.VK_ENTER
				|| key.getKeyCode() == KeyEvent.VK_ESCAPE
			) {
				messageOffset = -1;
				return this;
			}

			Direction direction = Utility.keyToDirectionRestricted(key);

			if (direction == null) {
				return this;
			}

			if (direction.hasUp() && canScrollHistoryUp()) {
				messageOffset++;
			} else if (direction.hasDown() && canScrollHistoryDown()) {
				messageOffset--;
			}

			return this;
		}

		// This is necessary both here and below to avoid interruptions
		if (!pendingRelationships.isEmpty()) {
			popup = new RelationshipResponseScreen();
			return this;
		}

		if (subscreen != null) {
			boolean subscreenHasPopup = (
				subscreen instanceof PopupMaster
				&& ((PopupMaster) subscreen).hasPopup()
			);

			subscreen = subscreen.processInput(key);

			// Stop even if popup was closed to prevent keypresses performing
			// multiple functions
			if (subscreenHasPopup) {
				return this;
			}
		}

		boolean nextTurn = false;

		switch (key.getKeyCode()) {
		// To be implemented upon expansion of the ore system
		//            case KeyEvent.VK_G:
		//                popup = new OreScreen();
		//                break;
		case KeyEvent.VK_I:
			String refineExecution = new Refine().execute(player);
			if (refineExecution == null) {
				break;
			}

			addError(refineExecution);
			break;
		case KeyEvent.VK_J:
			popup = player.isAligned() ? new LeaveScreen(true) : new JoinScreen();
			break;
		case KeyEvent.VK_D:
			nextTurn = true;
			Faction distressResponder = player.getDistressResponder();

			if (distressResponder == null) {
				break;
			}

			if (player.getFaction() == distressResponder) {
				String distressExecution = new Distress(distressResponder).execute(player);
				if (distressExecution != null) {
					addError(distressExecution);
					break;
				}
				break;
			}

			popup = new DistressConvertScreen(distressResponder);
			break;
		case KeyEvent.VK_N:
			if (player.isLeader() && galaxy.getFactions().length > 2) {
				popup = new RelationshipRequestScreen();
			}
			break;
		case KeyEvent.VK_M:
			if (player.hasActivationModules()) {
				popup = new ToggleScreen();
			} else {
				addError("The ship has no modules that can be activated.");
			}
			break;
		case KeyEvent.VK_PERIOD:
		case KeyEvent.VK_SPACE:
			nextTurn = true;
			break;
		case KeyEvent.VK_H:
			if (messages.size() > MESSAGE_LINES) {
				messageOffset = 0;
			}
			break;
		case KeyEvent.VK_B:
			if (!Option.LEADERBOARD.toBoolean()) {
				break;
			}

			List<ColorString> leaderboard = LeaderboardScore.buildLeaderboard();
			if (!leaderboard.isEmpty()) {
				popup = new LeaderboardScreen(leaderboard);
			}
			break;
		case KeyEvent.VK_O:
			popup = new OptionsScreen();
			break;
		case KeyEvent.VK_SLASH:
			if (key.isShiftDown()) {
				popup = new HelpScreen(getKeybindings());
			}
			break;
		case KeyEvent.VK_Q:
			if (key.isShiftDown()) {
				popup = new QuitScreen();
			}
			break;
		}

		if (player.isDestroyed()) {
			subscreen = null;
			popup = new EndScreen(
				new ColorString("You have been destroyed!"),
				true,
				false
			);
		}

		if (nextTurn) {
			galaxy.nextTurn();
		}

		if (!pendingRelationships.isEmpty()) {
			popup = new RelationshipResponseScreen();
			return this;
		}

		if (pendingElection != null) {
			pendingElection.findCandidates();
			if (player.getReputation(player.getFaction()).get() >= pendingElection.getMinimumReputation()) {
				popup = new PlayerCandidateScreen();
			} else {
				popup = new VotingScreen();
			}
		}

		return this;
	}

	@Override
	public List<Keybinding> getKeybindings()
	{
		List<Keybinding> keybindings = new ArrayList<>();

		keybindings.add(new Keybinding("confirm", "y", "enter"));
		keybindings.add(new Keybinding("deny", "n"));
		keybindings.add(new Keybinding("cancel", "q", "escape"));
		keybindings.add(null);
		keybindings.add(
			new Keybinding(
				player.isAligned()
				? "join/leave faction"
				: "join faction",
				"j"
			)
		);
		keybindings.add(new Keybinding("broadcast distress signal", "d"));
		if (player.isLeader() && galaxy.getFactions().length > 2) {
			keybindings.add(new Keybinding("negotiate relationship", "n"));
		}
		if (player.hasActivationModules()) {
			keybindings.add(new Keybinding("toggle module activation", "m"));
		}
		if (player.hasModule(Refine.MODULE)) {
			keybindings.add(new Keybinding("refine ore into fuel", "i"));
		}
		keybindings.add(new Keybinding("wait one turn", ".", "space"));
		if (messages.size() > MESSAGE_LINES) {
			keybindings.add(new Keybinding("message history", "h"));
		}
		if (
			Option.LEADERBOARD.toBoolean()
			&& !LeaderboardScore.buildLeaderboard().isEmpty()
		) {
			keybindings.add(new Keybinding("leaderboard", "b"));
		}
		keybindings.add(new Keybinding("options", "o"));
		keybindings.add(new Keybinding("keybindings", "?"));
		keybindings.add(new Keybinding("quit", "Q"));

		if (subscreen != null && subscreen instanceof KeyScreen) {
			keybindings.add(null);
			keybindings.addAll(((KeyScreen) subscreen).getKeybindings());
		}

		return keybindings;
	}

	@Override
	public AlignedWindow getWindow()
	{
		return statusWindow;
	}

	@Override
	public Screen getPopup()
	{
		return popup;
	}

	/**
	 * Returns true if the player is currently viewing message history.
	 *
	 * @return true if the player is currently viewing message history
	 */
	private boolean viewingHistory()
	{
		return messageOffset != -1;
	}

	/**
	 * Returns true if the history can be scrolled up by one line.
	 *
	 * @return true if the history can be scrolled up by one line
	 */
	private boolean canScrollHistoryUp()
	{
		return messageOffset + MESSAGE_LINES < messages.size();
	}

	/**
	 * Returns true if the history can be scrolled down by one line.
	 *
	 * @return true if the history can be scrolled down by one line
	 */
	private boolean canScrollHistoryDown()
	{
		return messageOffset > 0;
	}

	/**
	 * A message on the message list, storing the message itself and the number
	 * of times it has been received.
	 */
	private class Message
	{
		public ColorString message;
		public int counter;

		public Message(ColorString message)
		{
			this.message = message;
			this.counter = 1;
		}

		public ColorString getOutput()
		{
			if (counter == 1) {
				return message;
			}

			return new ColorString(message).add(
					new ColorString(
						" (x"
						+ Integer.toString(counter)
						+ ")",
						COLOR_FIELD
					)
				);
		}
	}

	/**
	 * Adds the given message to the message list.
	 *
	 * @param message the message to add
	 */
	public void addMessage(ColorString message)
	{
		if (messages.isEmpty()) {
			messages.add(new Message(message));
			return;
		}

		Message previous = messages.get(messages.size() - 1);
		if (message.toString().equals(previous.message.toString())) {
			previous.counter++;
		} else {
			messages.add(new Message(message));
		}
	}

	/**
	 * Sets up the status window and its contents.
	 */
	private void setUpStatusWindow()
	{
		List<ColorString> contents = statusWindow.getContents();

		contents.clear();
		statusWindow.getSeparators().clear();
		List<ColorString> statusList = player.getStatusList();
		for (ColorString line : statusList) {
			if (line == null) {
				statusWindow.addSeparator(new Line(false, 1, 1));
			} else {
				statusWindow.getContents().add(line);
			}
		}

		statusWindow.addSeparator(new Line(true, 1, 1));
		contents.add(
			new ColorString("Turn ")
			.add(
				new ColorString(
					Integer.toString(Main.galaxy.getTurn()),
					COLOR_FIELD
				)
			)
		);
	}

	/**
	 * Sets up the faction window and its contents.
	 */
	private void setUpFactionWindow()
	{
		List<ColorString> contents = factionWindow.getContents();

		contents.clear();
		factionWindow.getSeparators().clear();

		Faction playerFaction = player.getFaction();

		for (Faction faction : galaxy.getFactions()) {
			contents.add(faction.toColorString());
		}

		if (playerFaction != null) {
			factionWindow.addSeparator(new Line(false, 1, 1));
			for (Faction faction : galaxy.getFactions()) {
				if (playerFaction == faction) {
					contents.add(new ColorString("You", COLOR_FIELD));
				} else if (galaxy.getFactions().length == 2) {
					contents.add(new ColorString("Enemy", WAR.getColor()));
				} else {
					contents.add(playerFaction.getRelationship(faction).toColorString());
				}
			}
		}

		factionWindow.addSeparator(new Line(false, 1, 1));
		for (Faction faction : galaxy.getFactions()) {
			contents.add(
				new ColorString("Rank ")
				.add(
					new ColorString(
						"#" + faction.getRank(), COLOR_FIELD
					)
				)
			);
		}

		factionWindow.addSeparator(new Line(false, 1, 1));
		for (Faction faction : galaxy.getFactions()) {
			ReputationRange reputation = player.getReputation(faction).getRange();
			contents.add(
				new ColorString(
					reputation.getVerb() + " You",
					reputation.getColor()
				)
			);
		}

		if (playerFaction == null) {
			return;
		}

		factionWindow.addSeparator(new Line(true, 1, 1));
		ColorString leaderString = new ColorString("Leader: ");
		if (player.isLeader()) {
			leaderString.add(new ColorString("You", COLOR_FIELD));
		} else {
			Ship leader = playerFaction.getLeader();

			if (leader == null) {
				leaderString.add("None");
			}

			ReputationRange reputation = leader.getReputation(playerFaction).getRange();

			leaderString.add(new ColorString(leader.toString(), COLOR_FIELD))
			.add(
				new ColorString(
					" (" + reputation.getAdjective() + ")",
					reputation.getColor()
				)
			);
		}
		contents.add(leaderString);

		if (player.isLeader()) {
			factionWindow.addSeparator(new Line(true, 1, 1));
			contents.add(
				new ColorString("Economy: ")
				.add(
					new ColorString(
						playerFaction.getEconomyCredits()
						+ ""
						+ Symbol.CREDITS,
						COLOR_FIELD
					)
				)
			);
			contents.add(
				new ColorString("Sectors: ")
				.add(
					new ColorString(
						Integer.toString(
							playerFaction.getSectorsControlled()
						),
						COLOR_FIELD
					)
				)
			);
			contents.add(
				new ColorString("Planets: ")
				.add(
					new ColorString(
						Integer.toString(
							playerFaction.getPlanetsControlled()
						),
						COLOR_FIELD
					)
				)
			);
			contents.add(
				new ColorString("Stations: ")
				.add(
					new ColorString(
						playerFaction.getStationTypes(),
						COLOR_FIELD
					)
				)
			);
			contents.add(
				new ColorString("Ships: ")
				.add(
					new ColorString(
						playerFaction.getShipTypes(),
						COLOR_FIELD
					)
				)
			);
		}
	}

	/**
	 * Draws the message window and its contents.
	 */
	private void drawMessageWindow()
	{
		getDisplay().drawBorder(
			0,
			getDisplay().getHeightInCharacters() - (MESSAGE_LINES + 2),
			getDisplay().getWidthInCharacters() - 1,
			getDisplay().getHeightInCharacters() - 1,
			new Border(1)
		);

		int offset = Math.max(0, messageOffset);
		int lines = Math.min(messages.size(), MESSAGE_LINES);
		List<ColorString> messageOutput = new ArrayList<>(lines);
		List<Message> displayedMessages = messages.subList(
				messages.size() - lines - offset,
				messages.size() - offset
			);

		for (Message current : displayedMessages) {
			ColorString currentOutput = new ColorString(current.getOutput());

			if (currentOutput.length() >= getDisplay().getWidthInCharacters() - 2) {
				int splitIndex = getDisplay().getWidthInCharacters() - 3;
				while (currentOutput.charAt(splitIndex) != ' ') {
					splitIndex--;
				}

				if (currentOutput.charAt(splitIndex) != ' ') {
					splitIndex = getDisplay().getWidthInCharacters();
				} else {
					currentOutput.getCharacters().remove(splitIndex);
				}

				messageOutput.add(currentOutput.subSequence(0, splitIndex));
				messageOutput.add(
					currentOutput.subSequence(
						splitIndex,
						currentOutput.length()
					)
				);
			} else {
				messageOutput.add(current.getOutput());
			}
		}

		if (messageOutput.size() > MESSAGE_LINES) {
			messageOutput = messageOutput.subList(
					messageOutput.size() - MESSAGE_LINES,
					messageOutput.size()
				);
		}

		getDisplay().write(
			1,
			getDisplay().getHeightInCharacters() - (MESSAGE_LINES + 1),
			messageOutput.toArray(new ColorString[lines])
		);

		if (viewingHistory()) {
			if (canScrollHistoryUp()) {
				getDisplay().writeCenter(
					getDisplay().getHeightInCharacters() - MESSAGE_LINES - 2,
					new ColorString(
						Character.toString(ExtChars.ARROW1_U),
						COLOR_FIELD
					)
				);
			}

			if (canScrollHistoryDown()) {
				getDisplay().writeCenter(
					getDisplay().getHeightInCharacters() - 1,
					new ColorString(
						Character.toString(ExtChars.ARROW1_D),
						COLOR_FIELD
					)
				);
			}
		}
	}
}
