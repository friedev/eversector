package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.addError;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.pendingElection;
import static boldorf.eversector.Main.pendingRelationships;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.ships.Reputation.ReputationRange;
import boldorf.eversector.ships.Ship;
import boldorf.eversector.faction.Faction;
import static boldorf.eversector.faction.Relationship.RelationshipType.WAR;
import boldorf.eversector.storage.Actions;
import static boldorf.eversector.storage.Paths.DISTRESS;
import static boldorf.eversector.storage.Paths.REFINE;
import boldorf.eversector.storage.Symbol;
import boldorf.util.Utility;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.Coord;

/**
 * The main screen on which gameplay will take place. This screen will process
 * global commands and host more specific screens based on the player's 
 * situation.
 */
public class GameScreen extends Screen implements WindowScreen<AlignedWindow>,
        PopupMaster, KeyScreen
{
    public static final int MESSAGE_LINES = 10;
    
    private AlignedWindow statusWindow;
    private AlignedWindow factionWindow;
    private List<Message> messages;
    private Screen subscreen;
    private Screen popup;
    private int messageOffset;
    
    public GameScreen(Display display)
    {
        super(display);
        statusWindow  = new AlignedWindow(display, Coord.get(1, 1));
        factionWindow = new AlignedWindow(display, Coord.get(1, 1));
        messages      = new LinkedList<>();
        subscreen     = new SectorScreen(display);
        messageOffset = -1;
    }
    
    @Override
    public void displayOutput()
    {
        setUpStatusWindow();
        statusWindow.display();
        int bottomY = statusWindow.getBottomRight().y;
        
        setUpFactionWindow();
        factionWindow.setLocation(
                Coord.get(statusWindow.getBottomRight().x + 3, 1));
        factionWindow.display();
        bottomY = Math.max(bottomY, factionWindow.getBottomRight().y);
        
        drawMessageWindow();
        
        if (subscreen != null)
        {
            if (subscreen instanceof WindowScreen &&
                    ((WindowScreen) subscreen).getWindow()
                    instanceof AlignedWindow)
            {
                ((AlignedWindow) ((WindowScreen) subscreen).getWindow())
                        .setLocation(Coord.get(1, bottomY + 3));
            }
            
            subscreen.displayOutput();
        }
        
        if (popup != null)
            popup.displayOutput();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            popup = popup.processInput(key);
            
            if (popup instanceof StartScreen)
                return popup;
            
            if (popup instanceof EndScreen)
                subscreen = null;
            
            return this;
        }
        
        if (viewingHistory())
        {
            if (key.getKeyCode() == KeyEvent.VK_H ||
                    key.getKeyCode() == KeyEvent.VK_ENTER ||
                    key.getKeyCode() == KeyEvent.VK_ESCAPE)
            {
                messageOffset = -1;
                return this;
            }
            
            Direction direction = Utility.keyToDirectionRestricted(key);
            
            if (direction == null)
                return this;
            
            if (direction.hasUp() && canScrollHistoryUp())
                messageOffset++;
            else if (direction.hasDown() && canScrollHistoryDown())
                messageOffset--;
            
            return this;
        }
        
        // This is necessary both here and below to avoid interruptions
        if (!pendingRelationships.isEmpty())
        {
            popup = new RelationshipResponseScreen(getDisplay());
            return this;
        }
        
        if (subscreen != null)
        {
            boolean subscreenHasPopup = subscreen instanceof PopupMaster &&
                    ((PopupMaster) subscreen).hasPopup();
            
            subscreen = subscreen.processInput(key);
            
            // Stop even if popup was closed to prevent keypresses performing
            // multiple functions
            if (subscreenHasPopup)
                return this;
        }
        
        boolean nextTurn = false;
        
        switch (key.getKeyCode())
        {
            // To be implemented upon expansion of the ore system
//            case KeyEvent.VK_G:
//                popup = new OreScreen(getDisplay());
//                break;
            case KeyEvent.VK_I:
                if (player.refine())
                {
                    nextTurn = true;
                    playSoundEffect(REFINE);
                }
                break;
            case KeyEvent.VK_J:
                popup = player.isAligned() ? new LeaveScreen(getDisplay(), true)
                        : new JoinScreen(getDisplay());
                break;
            case KeyEvent.VK_D:
                nextTurn = true;
                playSoundEffect(DISTRESS);
                Faction distressResponder = player.getDistressResponder();
                
                if (distressResponder == null)
                    break;
                
                if (player.getFaction() == distressResponder)
                {
                    player.distress(distressResponder);
                    break;
                }
                
                popup = new DistressConvertScreen(getDisplay(),
                        distressResponder);
                break;
            case KeyEvent.VK_N:
                if (player.isLeader() && map.getFactions().length > 2)
                    popup = new RelationshipRequestScreen(getDisplay());
                break;
            case KeyEvent.VK_M:
                if (player.hasActivationModules())
                    popup = new ToggleScreen(getDisplay());
                else
                    addError("The ship has no modules that can be activated.");
                break;
            case KeyEvent.VK_PERIOD: case KeyEvent.VK_SPACE:
                nextTurn = true;
                break;
            case KeyEvent.VK_H:
                if (messages.size() > MESSAGE_LINES)
                    messageOffset = 0;
                break;
            case KeyEvent.VK_B:
                List<ColorString> leaderboard =
                        LeaderboardScore.buildLeaderboard();
                if (!leaderboard.isEmpty())
                    popup = new LeaderboardScreen(getDisplay(), leaderboard);
                break;
            case KeyEvent.VK_O:
                popup = new OptionsScreen(getDisplay());
                break;
            case KeyEvent.VK_SLASH:
                if (key.isShiftDown())
                    popup = new HelpScreen(getDisplay(), getKeybindings());
                break;
            case KeyEvent.VK_Q:
                if (key.isShiftDown())
                    popup = new QuitScreen(getDisplay());
                break;
        }
        
        if (player.isDestroyed())
        {
            subscreen = null;
            popup = new EndScreen(getDisplay(),
                    new ColorString("You have been destroyed!"), true);
        }
        
        if (nextTurn)
            map.nextTurn();
        
        if (!pendingRelationships.isEmpty())
        {
            popup = new RelationshipResponseScreen(getDisplay());
            return this;
        }
        
        if (pendingElection != null)
        {
            pendingElection.findCandidates();
            if (player.getReputation(player.getFaction()).get() >=
                    pendingElection.getMinimumReputation())
            {
                popup = new PlayerCandidateScreen(getDisplay());
            }
            else
            {
                popup = new VotingScreen(getDisplay());
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
        keybindings.add(new Keybinding(player.isAligned() ?
                "join/leave faction" : "join faction", "j"));
        keybindings.add(new Keybinding("broadcast distress signal", "d"));
        if (player.isLeader() && map.getFactions().length > 2)
            keybindings.add(new Keybinding("negotiate relationship", "n"));
        if (player.hasActivationModules())
            keybindings.add(new Keybinding("toggle module activation", "m"));
        if (player.hasModule(Actions.REFINE))
            keybindings.add(new Keybinding("refine ore into fuel", "i"));
        keybindings.add(new Keybinding("wait one turn", ".", "space"));
        if (messages.size() > MESSAGE_LINES)
            keybindings.add(new Keybinding("message history", "h"));
        if (!LeaderboardScore.buildLeaderboard().isEmpty())
            keybindings.add(new Keybinding("leaderboard", "b"));
        keybindings.add(new Keybinding("options", "o"));
        keybindings.add(new Keybinding("keybindings", "?"));
        keybindings.add(new Keybinding("quit", "Q"));
        
        if (subscreen != null && subscreen instanceof KeyScreen)
        {
            keybindings.add(null);
            keybindings.addAll(((KeyScreen) subscreen).getKeybindings());
        }
        
        return keybindings;
    }
    
    @Override
    public AlignedWindow getWindow()
        {return statusWindow;}
    
    @Override
    public Screen getPopup()
        {return popup;}
    
    @Override
    public boolean hasPopup()
        {return popup != null;}
    
    private boolean viewingHistory()
        {return messageOffset != -1;}
    
    private boolean canScrollHistoryUp()
        {return messageOffset + MESSAGE_LINES < messages.size();}
    
    private boolean canScrollHistoryDown()
        {return messageOffset > 0;}
    
    private class Message
    {
        ColorString message;
        int counter;
        
        Message(ColorString message)
        {
            this.message = message;
            this.counter = 1;
        }
        
        ColorString getOutput()
        {
            if (counter == 1)
                return message;
            
            return new ColorString(message).add(new ColorString(
                    " (x" + Integer.toString(counter) + ")", COLOR_FIELD));
        }
    }
    
    public void addMessage(ColorString message)
    {
        if (messages.isEmpty())
        {
            messages.add(new Message(message));
            return;
        }
        
        Message previous = messages.get(messages.size() - 1);
        if (message.toString().equals(previous.message.toString()))
            previous.counter++;
        else
            messages.add(new Message(message));
    }
    
    private void setUpStatusWindow()
    {
        List<ColorString> contents = statusWindow.getContents();
        
        contents.clear();
        statusWindow.getSeparators().clear();
        List<ColorString> statusList = player.getStatusList();
        for (ColorString line: statusList)
        {
            if (line == null)
                statusWindow.addSeparator(new Line(true, 1, 1));
            else
                statusWindow.getContents().add(line);
        }
        
        statusWindow.addSeparator(new Line(true, 1, 1));
        contents.add(new ColorString("Turn ")
                .add(new ColorString(Integer.toString(Main.map.getTurn()),
                        COLOR_FIELD)));
    }
    
    private void setUpFactionWindow()
    {
        List<ColorString> contents = factionWindow.getContents();
        
        contents.clear();
        factionWindow.getSeparators().clear();
        
        Faction playerFaction = player.getFaction();
        
        for (Faction faction: map.getFactions())
            contents.add(faction.toColorString());

        if (playerFaction != null)
        {
            factionWindow.addSeparator(new Line(false, 1, 1));
            for (Faction faction: map.getFactions())
            {
                if (playerFaction == faction)
                    contents.add(new ColorString("You", COLOR_FIELD));
                else if (map.getFactions().length == 2)
                    contents.add(new ColorString("Enemy", WAR.getColor()));
                else
                    contents.add(playerFaction.getRelationship(faction)
                            .toColorString());
            }
        }
        
        factionWindow.addSeparator(new Line(false, 1, 1));
        for (Faction faction: map.getFactions())
            contents.add(new ColorString("Rank ").add(new ColorString("#" +
                    faction.getRank(), COLOR_FIELD)));
        
        factionWindow.addSeparator(new Line(false, 1, 1));
        for (Faction faction: map.getFactions())
        {
            ReputationRange reputation =
                    player.getReputation(faction).getRange();
            contents.add(new ColorString(reputation.getVerb() + " You",
                    reputation.getColor()));
        }
        
        if (playerFaction == null)
            return;
        
        factionWindow.addSeparator(new Line(true, 1, 1));
        ColorString leaderString = new ColorString("Leader: ");
        if (player.isLeader())
        {
            leaderString.add(new ColorString("You", COLOR_FIELD));
        }
        else
        {
            Ship leader = playerFaction.getLeader();
            ReputationRange reputation =
                    leader.getReputation(playerFaction).getRange();
            
            leaderString.add(new ColorString(leader.toString(),
                    COLOR_FIELD)).add(new ColorString(" ("
                            + reputation.getAdjective() + ")",
                            reputation.getColor()));
        }
        contents.add(leaderString);
        
        if (player.isLeader())
        {
            factionWindow.addSeparator(new Line(true, 1, 1));
            contents.add(new ColorString("Economy: ").add(new ColorString(
                    playerFaction.getEconomyCredits() + "" + Symbol.CREDITS,
                    COLOR_FIELD)));
            contents.add(new ColorString("Sectors: ").add(new ColorString(
                    Integer.toString(playerFaction.getSectorsControlled()),
                    COLOR_FIELD)));
            contents.add(new ColorString("Planets: ").add(new ColorString(
                    Integer.toString(playerFaction.getPlanetsControlled()),
                            COLOR_FIELD)));
            contents.add(new ColorString("Stations: ").add(new ColorString(
                    playerFaction.getStationTypes(), COLOR_FIELD)));
            contents.add(new ColorString("Ships: ").add(new ColorString(
                    playerFaction.getShipTypes(), COLOR_FIELD)));
        }
    }
    
    private void drawMessageWindow()
    {
        getDisplay().drawBorder(Coord.get(0, getDisplay().getCharHeight() -
                (MESSAGE_LINES + 2)), Coord.get(getDisplay().getCharWidth() - 1,
                        getDisplay().getCharHeight() - 1), new Border(1));
        
        int offset = Math.max(0, messageOffset);
        int lines = Math.min(messages.size(), MESSAGE_LINES);
        List<ColorString> messageOutput = new ArrayList<>(lines);
        List<Message> displayedMessages = messages.subList(
                messages.size() - lines - offset, messages.size() - offset);
        
        for (Message current: displayedMessages)
        {
            ColorString currentOutput = new ColorString(current.getOutput());
            
            if (currentOutput.length() >= getDisplay().getCharWidth() - 2)
            {
                int splitIndex = getDisplay().getCharWidth() - 3;
                while (currentOutput.charAt(splitIndex) != ' ')
                    splitIndex--;

                if (currentOutput.charAt(splitIndex) != ' ')
                    splitIndex = getDisplay().getCharWidth();
                else
                    currentOutput.getCharacters().remove(splitIndex);
                
                messageOutput.add(currentOutput.subSequence(0, splitIndex));
                messageOutput.add(currentOutput.subSequence(splitIndex,
                        currentOutput.length()));
            }
            else
            {
                messageOutput.add(current.getOutput());
            }
        }
        
        if (messageOutput.size() > MESSAGE_LINES)
        {
            messageOutput = messageOutput.subList(
                    messageOutput.size() - MESSAGE_LINES, messageOutput.size());
        }
        
        getDisplay().write(Coord.get(1, getDisplay().getCharHeight() -
                (MESSAGE_LINES + 1)),
                messageOutput.toArray(new ColorString[lines]));
        
        if (viewingHistory())
        {
            if (canScrollHistoryUp())
            {
                getDisplay().writeCenter(getDisplay().getCharHeight() -
                        MESSAGE_LINES - 2, new ColorString(Character.toString(
                                ExtChars.ARROW1_U), COLOR_FIELD));
            }
            
            if (canScrollHistoryDown())
            {
                getDisplay().writeCenter(getDisplay().getCharHeight() - 1,
                        new ColorString(Character.toString(ExtChars.ARROW1_D),
                                COLOR_FIELD));
            }
        }
    }
}