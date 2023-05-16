# mazeGame

## Description 

This is an implementation of Maze Game.
Can be played with 1-2 players.

The objective is to find the treasure, though there are some obstacles in the players way:
   + Walls, which are revealed after being bumped into. They end a player's turn prematurely.
   + The Foe, which after being awakened by someone being too close, will pursue and attack the Player
   + The Other Player, who might get to the treasure first, or can steal it from another player.

If one can manage to bring the treasure back to their home base, they win the game.
If the Foe manages to kill every player, the game is lost.

## How To Run
Open the root folder of the project, and execute one of the below:

Mac/Linux
```python
Game.sh
```

Windows
```python
Game.bat
```

## Design Patterns
  + Model-View-Controller Pattern
    * Player (Model)
    * Foe (Model)
    * GameBoard (Model)
    * BoardGUI (View)
    * Session (Controller)
  + Adapter Pattern
    * IGameEventHandler (Target)
    * GameGraphics/BoardGraphics (Adaptee)
    * BoardGUI (Adapter)
  + Factory Pattern
    * ActorFactory (Factory)
    * Foe (Product)
    * Player (Product)
