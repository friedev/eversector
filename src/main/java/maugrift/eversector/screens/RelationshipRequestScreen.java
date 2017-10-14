package maugrift.eversector.screens;

import maugrift.apwt.ExtChars;
import maugrift.apwt.glyphs.ColorChar;
import maugrift.apwt.glyphs.ColorString;
import maugrift.apwt.screens.MenuScreen;
import maugrift.apwt.screens.Screen;
import maugrift.apwt.screens.WindowScreen;
import maugrift.apwt.windows.PopupMenu;
import maugrift.apwt.windows.PopupWindow;
import maugrift.eversector.Main;
import maugrift.eversector.faction.Faction;
import maugrift.eversector.faction.Relationship;
import maugrift.eversector.faction.Relationship.RelationshipType;

import java.util.ArrayList;
import java.util.List;

import static maugrift.eversector.Main.*;

/**
 * The menu for requesting relationships with other factions as a leader.
 *
 * @author Maugrift
 */
public class RelationshipRequestScreen extends MenuScreen<PopupMenu> implements WindowScreen<PopupWindow>
{
    /**
     * The factions the player can request a relationship with.
     */
    private List<Faction> factions;

    /**
     * The relationship changes the player can request.
     */
    private List<RelationshipType> changes;

    /**
     * Instantiates a new RelationshipRequestScreen.
     */
    public RelationshipRequestScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display), COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        factions = new ArrayList<>();
        changes = new ArrayList<>();
        setUpMenu();
    }

    @Override
    public PopupWindow getWindow()
    {
        return (PopupWindow) getMenu().getWindow();
    }

    @Override
    public Screen onConfirm()
    {
        int index = getMenu().getSelectionIndex();
        player.getFaction().requestRelationship(factions.get(index), changes.get(index));
        return null;
    }

    /**
     * Sets up the menu and its contents.
     */
    private void setUpMenu()
    {
        List<ColorString> contents = getWindow().getContents();

        for (Relationship relationship : player.getFaction().getRelationships())
        {
            Faction otherFaction = relationship.getOtherFaction(player.getFaction());
            RelationshipType type = relationship.getType();
            ColorString base = otherFaction.toColorString().add(": ").add(type).add(
                    new ColorChar(ExtChars.ARROW1_R, COLOR_FIELD));

            factions.add(otherFaction);

            if (type == RelationshipType.PEACE)
            {
                factions.add(otherFaction);
                changes.add(RelationshipType.WAR);
                changes.add(RelationshipType.ALLIANCE);
                contents.add(new ColorString(base).add(RelationshipType.WAR));
                contents.add(base.add(RelationshipType.ALLIANCE));
                continue;
            }

            changes.add(RelationshipType.PEACE);
            contents.add(base.add(RelationshipType.PEACE));
        }
    }
}