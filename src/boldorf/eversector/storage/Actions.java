package boldorf.eversector.storage;

import boldorf.eversector.map.Station;
import boldorf.eversector.items.Action;
import boldorf.eversector.items.Module;
import boldorf.eversector.items.Weapon;

/** A wrapper class for modules and actions ships can perform. */
public abstract class Actions
{
    public static final Action
    BURN     = new Action(Resources.FUEL,   4),
    ORBIT    = new Action(Resources.FUEL,   1),
    RELOCATE = new Action(Resources.FUEL,   1),
    ENTER    = new Action(Resources.FUEL,   1),
    ESCAPE   = new Action(Resources.FUEL,   1),
    LAND     = new Action(Resources.FUEL,   2),
    TAKEOFF  = new Action(Resources.FUEL,   2),
    MINE     = new Action(Resources.ENERGY, 3),
    FLEE     = new Action(Resources.FUEL,   1),
    PURSUE   = new Action(Resources.FUEL,   1);
    
    public static final Module
    SCAN   = Station.getBaseModule("Scanner"        ),
    REFINE = Station.getBaseModule("Refinery"       ),
    SOLAR  = Station.getBaseModule("Solar Array"    ),
    WARP   = Station.getBaseModule("Warp Drive"     ),
    SHIELD = Station.getBaseModule("Shield"         ),
    CLOAK  = Station.getBaseModule("Cloaking Device");
    
    public static final Weapon
    LASER   = Station.getBaseWeapon("Laser"       ),
    TORPEDO = Station.getBaseWeapon("Torpedo Tube"),
    PULSE   = Station.getBaseWeapon("Pulse Beam"  );
}