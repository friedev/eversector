# EverSector

EverSector is a space simulation roguelike where you pilot a starship through a procedurally generated galaxy.
Mine resources on planets and asteroid belts, trade with space stations, and claim territory for your faction.
Work alongside each individually simulated ship, or blast them apart and salvage their remains.
Go forth, and shape the future of your galaxy!

## Usage

Dependencies:

- Java 8 or newer

To run the game: `./bin/EverSector`

Or, on Windows: `./bin/EverSector.bat`

## Compiling

Dependencies:

- Java 8 or newer
- Gradle (last tested with Gradle 7.5.1)

Clone the repository and run the build script `./build.sh`.
This creates the directory `EverSector`, containing all files needed to run the game.

To make a release build, run `./build.sh --release`.
This creates an `EverSector.zip` package for distribution.

## How to Play

EverSector is played entirely with the keyboard.
Every action you can take is triggered by a keypress or combination of them.
At almost any time, you may press `?` to see all actions currently available to you.
This list may change based on your situation and items you may have. In popup windows and confirmation dialogs, `y` or Enter is equivalent to "yes", `n` means "no", and `q` or Escape means "cancel".
Sometimes "cancel" performs the same function as "no", though generally it can be used to leave the dialog without performing an action.
To navigate menus, use the arrow keys.
In some cases, `wasd`, the vi keys, and the number pad may also be used to navigate.
The current selection is often denoted by a blue highlight.

### Galaxy Screen

The galaxy map shows you the layout of nearby sectors in your galaxy.
Each tile is a sector, though only some of them have star systems you can enter.
Any tile other than a dot is a star system and highlighted sectors contain nebulae that block vision.
You can also switch between normal view and star view by pressing `v`.
Normal view colors sectors based on the faction that controls them and shows systems with stations in them.
Star view displays only star sizes and colors.
You can look around with `l` to what each sector contains.

### Sector Screen

The sector screen is primarily a list of orbits with a comprehensive list of their contents in the panel on the right.
The uppermost orbits are the closest to the star and lower ones are further away.
Traveling away from the star once at the furthest orbit will cause you to escape the sector.
The size of star determines the number of possible orbits around it.
You and other ships are displayed in the middle column, with stations on the left and planets on the right.
As with the galaxy map, `l` can be used to look around and reveal the contents of different orbits.

### Planet Screen

The planet screen displays all regions on a planet.
When traveling around the planet, note that traveling east and west will take you to the other side of the world.
Traveling north or south over a pole will take you to the opposite side of the planet, not to the other pole.
You can choose to show the colors of factions rather than default region colors by pressing `v`.
Also note that solar arrays won't work on planets as most light exposure will be inconsistent.

### Station Screen

The interface for stations lists items you can purchase on the left and items you can sell on the right.
To save time when mining, you can press `r` at a station to "restock," selling all ore and refilling other resources.
Docking with a station will automatically repair any damaged modules you may have for no cost.

### Battle Screen

The battle screen is a simple list of your allies and enemies.
Pressing one of the combat keys (as listed after pressing `?`) will perform its action on the currently selected ship.
You will be able to view the information of ships you've scanned on a panel to the right.
Note that the more modules a ship has, the more likely one is to get damaged.

### Goal

EverSector is, in some sense, a sandbox.
There are no goals imposed upon you, and you are given the freedom to play as you wish.
The most straightforward way to judge progress is by the credits you possess.
However, it can be more fun to impose challenges upon yourself.
Become the leader of your faction, assassinate an enemy leader, claim the entire galaxy for your faction, or upgrade your ship to its limits and crush your foes.
Go forth, and shape the future of your galaxy!

## Credits

EverSector was created by [Aaron Friesen](https://frie.dev).

Contributors:

- [Dale Campbell](https://oshuma.github.io)

Libraries:

- [AsciiPanel](https://github.com/trystan/AsciiPanel) by [Trystan Spangler](https://trystans.blogspot.com)
- [SquidLib](https://github.com/SquidPony/SquidLib) by [Eben Howard (SquidPony)](https://github.com/SquidPony)
- [APWT](https://frie.dev/apwt) by [Aaron Friesen](https://frie.dev)

Fonts: All fonts are based on the following CP437 tilesets from the [Dwarf Fortress Tileset Repository](https://dwarffortresswiki.org/Tileset_repository), with custom tiles added by [Aaron Friesen](https://frie.dev)

- [12x12 tileset](http://df.magmawiki.com/index.php/File:Alloy_curses_12x12.png) by [Alloy](https://dwarffortresswiki.org/index.php/User:Alloy)
- [16x16 tileset](https://dwarffortresswiki.org/index.php/File:Cooz_curses_square_16x16.png) by [Cooz](https://dwarffortresswiki.org/index.php?title=User:Cooz)
- [10x10 tileset](https://github.com/trystan/AsciiPanel/blob/master/src/main/resources/qbicfeet_10x10.png) by qbicfeet (included with AsciiPanel)

Title ASCII Art: [TAAG](http://patorjk.com/software/taag) by [Patrick Gillespie](http://patorjk.com)

All applicable licenses can be found in the `licenses` folder.

## Contributing

EverSector is no longer in development.
If you want to expand on the game, make your own fork.
