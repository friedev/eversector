# EverSector

EverSector is a space-themed sandbox roguelike with an emphasis on simulation, set in a large yet finite universe.
Unlike similar roguelikes, EverSector puts you in control of a starship itself.
The universe you will navigate is divided into sectors, filled with planets, space stations, and other ships.
These ships are no different than yourself: competing for resources and waging war for their respective factions.
With enough hard work, any ship can rise to power over their faction, through noble or despicable means.

## Compiling

To build EverSector from source, clone the repository and run the included build script `./build.sh`.
This compiles EverSector and its dependencies into JAR files and also packages all files needed to run the game into a directory named `EverSector`.

To run the game, run the `bin/EverSector` script in the newly created `EverSector` directory (or use the `bin/EverSector.bat` batch file if you are on Windows).

To make a release build, run `./build.sh --release`.
This zips the `EverSector` directory into `EverSector.zip` for distribution.

## Guide

### How to Play

EverSector is played entirely with the keyboard.
Every action you can take is triggered by a keypress or combination of them.
At almost any time, you may press `?` to see all actions currently available to you.
This list may change based on your situation and items you may have. In popup windows and confirmation dialogs, `y` or Enter is equivalent to "yes", `n` means "no", and `q` or Escape means "cancel".
Sometimes "cancel" performs the same function as "no", though generally it can be used to leave the dialog without performing an action.
To navigate menus, use the arrow keys.
In some cases, `wasd`, the vi keys, and the number pad may also be used to navigate.
The current selection is often denoted by a blue highlight.

### Interface Overview

#### Galaxy

The galaxy map shows you the layout of nearby sectors in your galaxy.
Each tile is a sector, though only some of them have star systems you can enter.
Any tile other than a dot is a star system and highlighted sectors contain nebulae that block vision.
You can also switch between normal view and star view by pressing `v`.
Normal view colors sectors based on the faction that controls them and shows systems with stations in them.
Star view displays only star sizes and colors.
You can look around with `l` to what each sector contains.

#### Sector

The sector interface is primarily a list of orbits with a comprehensive list of their contents in the panel on the right.
The uppermost orbits are the closest to the star and lower ones are further away.
Traveling away from the star once at the furthest orbit will cause you to escape the sector.
The size of star determines the number of possible orbits around it.
You and other ships are displayed in the middle column, with stations on the left and planets on the right.
As with the galaxy map, `l` can be used to look around and reveal the contents of different orbits.

#### Planet

The planet interface is a display of all regions on a planet.
When traveling around the planet, note that traveling east and west will take you to the other side of the world.
Traveling north or south over a pole will take you to the opposite side of the planet, not to the other pole.
You can choose to show the colors of factions rather than default region colors by pressing `v`.
Also note that solar arrays won't work on planets as most light exposure will be inconsistent.

#### Station

The interface for stations lists items you can purchase on the left and items you can sell on the right.
To save time when mining, you can press `r` at a station to "restock," selling all ore and refilling other resources.
Docking with a station will automatically repair any damaged modules you may have for no cost.

#### Battle

The battle interface is a simple list of your allies and enemies.
Pressing one of the combat keys (as listed after pressing `?`) will perform its action on the currently selected ship.
You will be able to view the information of ships you've scanned on a panel to the right.
Note that the more modules a ship has, the more likely one is to get damaged.

### Goal

EverSector is, in some sense, a sandbox.
There are no goals imposed upon you, and you are given the freedom to play as you wish.
The most straightforward way to judge progress is by the credits you possess.
However, it can be more fun to impose challenges upon yourself.
Become the leader of your faction, assassinate an enemy leader, claim the entire galaxy for your faction, or upgrade your ship to its limits and crush your foes.
Now go forth, and shape the future of your galaxy!

## Links

- [itch.io](https://maugrift.itch.io/eversector)
- [GitHub](https://github.com/Maugrift/EverSector)

## Credits

Developer: [Aaron Friesen](https://maugrift.com)

Contributors:

- [Dale Campbell](https://oshuma.github.io)

Libraries:

- [AsciiPanel](https://github.com/trystan/AsciiPanel) by [Trystan Spangler](https://trystans.blogspot.com)
- [SquidLib](https://github.com/SquidPony/SquidLib) by [Eben Howard (SquidPony)](https://github.com/SquidPony)
- [APWT](https://github.com/Maugrift/APWT) by [Aaron Friesen](https://maugrift.com)

Fonts: All fonts are based on the following CP437 tilesets from the [Dwarf Fortress Tileset Repository](https://dwarffortresswiki.org/Tileset_repository), with custom tiles added by [Aaron Friesen](https://maugrift.com)

- [12x12 tileset](http://df.magmawiki.com/index.php/File:Alloy_curses_12x12.png) by [Alloy](https://dwarffortresswiki.org/index.php/User:Alloy)
- [16x16 tileset](https://dwarffortresswiki.org/index.php/File:Cooz_curses_square_16x16.png) by [Cooz](https://dwarffortresswiki.org/index.php?title=User:Cooz)
- [10x10 tileset](https://github.com/trystan/AsciiPanel/blob/master/src/main/resources/qbicfeet_10x10.png) by qbicfeet (included with AsciiPanel)

Title ASCII Art: [TAAG](http://patorjk.com/software/taag) by [Patrick Gillespie](http://patorjk.com)

All applicable licenses can be found in the `licenses` folder.

## Contributing

EverSector is no longer in development, and I do not intend add any new features.
However, I may still try to maintain it, so if something doesn't work, feel free to create an issue or pull request for it.

If you want to develop more features for EverSector, I would suggest making your own fork of the game!
Just make sure to abide by the terms of the source code license (found in `LICENSE.txt`).

**NOTE:** If you cloned the repo at or before commit 8dfcf5b ("Remove and disable audio"), you may need to rebase or re-clone the repository if you want to pull new changes.
This is because of a history rewrite to remove the large audio files from Git history.
Apologies for the inconvenience!
