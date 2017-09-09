package boldorf.eversector.screens;

import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.COLOR_SELECTION_FOREGROUND;
import static boldorf.eversector.Main.player;
import boldorf.eversector.faction.Faction;
import boldorf.eversector.faction.Relationship;
import boldorf.eversector.faction.Relationship.RelationshipType;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class RelationshipRequestScreen extends MenuScreen<PopupMenu>
        implements WindowScreen<PopupWindow>
{
    private List<Faction> factions;
    private List<RelationshipType> changes;
    
    public RelationshipRequestScreen(Display display)
    {
        super(new PopupMenu(new PopupWindow(display),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        factions = new ArrayList<>();
        changes = new ArrayList<>();
        setUpMenu();
    }

    @Override
    public PopupWindow getWindow()
        {return (PopupWindow) getMenu().getWindow();}
    
    @Override
    public Screen onConfirm()
    {
        int index = getMenu().getSelectionIndex();
        player.getFaction().requestRelationship(factions.get(index),
                changes.get(index));
        return null;
    }
    
    private void setUpMenu()
    {
        List<ColorString> contents = getWindow().getContents();
        
        for (Relationship relationship: player.getFaction().getRelationships())
        {
            Faction otherFaction =
                    relationship.getOtherFaction(player.getFaction());
            RelationshipType type = relationship.getType();
            ColorString base = otherFaction.toColorString().add(": ").add(type)
                    .add(new ColorChar(ExtChars.ARROW1_R, COLOR_FIELD));
            
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