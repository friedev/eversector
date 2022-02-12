# EverSector

EverSector is a space-themed sandbox roguelike with an emphasis on simulation, set in a large yet finite universe.
Unlike similar roguelikes, EverSector puts you in control of a starship itself.
The universe you will navigate is divided into sectors, filled with planets, space stations, and other ships.
These ships are no different than yourself: competing for resources and waging war for their respective factions.
With enough hard work, any ship can rise to power over their faction, through noble or despicable means.

EverSector is written in Java and displayed with [AsciiPanel](https://github.com/trystan/AsciiPanel) by [Trystan Spangler](https://trystans.blogspot.com), as well as my own library, [APWT](https://github.com/Maugrift/APWT).
It uses [SquidLib](https://github.com/SquidPony/SquidLib) by [Eben Howard (Squidpony)](https://github.com/SquidPony) for random generation, FOV, and other calculations.

For more information about the game itself, see [README.html](https://github.com/Maugrift/EverSector/blob/master/bundle/README.html) in the bundle directory.

## Compiling

To compile EverSector as a jar file, simply clone the repository and run `gradle clean build jar`.
To run directly from the source, use `gradle run`.

No sound files are included in the repository for the sake of file size.
If you want to play with sound effects, download the `audio.zip` file from the latest release and unzip it to your assets folder.
If you use Windows and don't have Java installed, you can download the `windows.zip` file and unzip it to your EverSector directory.
This contains an executable launcher and a bundled JRE.

## Contributing

EverSector is no longer in development, and I do not intend to accept pull requests.
If you want to add features to EverSector, consider making your own fork!
Just make sure to abide by the terms of the source code license (found in `LICENSE.txt`).
