import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/* SubClass - GameBoard
 *
 *     Holds the Maze layout and handles actor interactions with the GameBoard.
 *
 * Patterns:
 *     Part of the 'Model' in the Model-Controller-View pattern.
 */

public final class GameBoard extends Model<GameBoard>
{
    // Constants - Size: The size of the maze to be generated.
    public static final int SIZE = 8;

    // walls(4): Holds the walls in the labyrinth layout.
    private boolean[][] wallsVertical = new boolean[SIZE +1][SIZE +1];;
    
    private boolean[][] wallsHorizontal = new boolean[SIZE +1][SIZE +1];

    private boolean[][] revealedVertical = new boolean[SIZE +1][SIZE +1];

    private boolean[][] revealedHorizontal = new boolean[SIZE +1][SIZE +1];

    public boolean[][][] getWallData() {
        return new boolean[][][] {wallsVertical, wallsHorizontal, revealedVertical, revealedHorizontal};
    }

    // treasureReturned: Holds whether the treasure was returned by the Foe.
    private boolean treasureReturned = false;
    public boolean getTreasureReturned() { return treasureReturned; }


    // hasTreasure: Holds whether the treasure is within the treasure room.
    private boolean hasTreasure = true;
    public boolean getTreasure() { return hasTreasure; }
    public void setTreasure(boolean value) {
        if(!hasTreasure && value) {
            treasureReturned = true;
        }
        hasTreasure = value;
        triggerDataChange(this);
    }

    // treasureRoom: Holds the position of the treasure room.
    private Cell treasureRoom = new Cell();
    public Cell getTreasureRoom() { return new Cell(treasureRoom); }

    // secretRooms: Holds the secret rooms of all players.
    private ArrayList<SecretRoom> secretRooms = new ArrayList<SecretRoom>();
    public ArrayList<SecretRoom> getSecretRooms() {
        return secretRooms;
    }

    // random: Random instance used for this class.
    private Random random = new Random();

    // Constructor(): Creates a new GameBoard with a randomized layout.
    public GameBoard() {
        generateMaze();
    }

    // moveValid(Cell, Cell, boolean): Determines if an actor can move between two cells, and if there is a wall,
    //    that wall is revealed if 'reveal' is true.
    public boolean moveValid(Cell from, Cell to, boolean reveal) {
        boolean blocked = true;
        int dX = to.x - from.x;
        int dY = to.y - from.y;

        if(dX == -1) {
            blocked = wallsVertical[from.y][from.x];
            revealedVertical[from.y][from.x] = revealedVertical[from.y][from.x] || reveal;
        } else if (dX == 1) {
            blocked = wallsVertical[from.y][from.x + 1];
            revealedVertical[from.y][from.x + 1] = revealedVertical[from.y][from.x + 1] || reveal;
        } else if (dY == -1) {
            blocked = wallsHorizontal[from.y][from.x];
            revealedHorizontal[from.y][from.x] = revealedHorizontal[from.y][from.x] || reveal;
        } else if (dY == 1) {
            blocked = wallsHorizontal[from.y + 1][from.x];
            revealedHorizontal[from.y + 1][from.x] = revealedHorizontal[from.y + 1][from.x] || reveal;
        }
        triggerDataChange(this);
        return !blocked;
    }

    // selectTreasureRoom(): Randomly selects the treasure room, making sure it's at least 3 spaces from any secret room.
    public void selectTreasureRoom() {
        Cell candidate = null;
        boolean suitable = false;

        while(!suitable) {
            candidate = new Cell(random.nextInt(GameBoard.SIZE), random.nextInt(GameBoard.SIZE));

            double minDistance = Float.MAX_VALUE;

            for(SecretRoom secretRoom : secretRooms) {
                double distance = secretRoom.getPosition().squareDistance(candidate);
                if(distance < minDistance) {
                    minDistance = distance;
                }
            }

            if(minDistance > 3d) {
                suitable = true;
            }
        }
        treasureRoom = candidate;
    }

    // getSecretRoom(int): Returns the secret room associated with the player whose ID has been passed.
    public Cell getSecretRoom(int id) {
        for(SecretRoom secretRoom : secretRooms) {
            if(secretRoom.getId() == id){
                return secretRoom.getPosition();
            }
        }

        return null;
    }


    // setHomePosition(Cell, int): Creates a new SecretRoom at 'position' for player whose ID has been passed.
    public void setHomePosition(Cell position, int id) {
        SecretRoom home = new SecretRoom(position, id);
	    secretRooms.add(home);

        triggerDataChange(this);
    }

    // generateMaze(): Generates the labyrinth layout.
    private void generateMaze()
    {
        boolean[][] cells = new boolean[SIZE][SIZE];
        Random rn = new Random();

        int startX = rn.nextInt(SIZE);
        int startY = rn.nextInt(SIZE);

        for(int i = 0; i < SIZE +1; i++)
        {
            for(int j = 0; j < SIZE +1; j++)
            {
                wallsVertical[i][j] = true;
                wallsHorizontal[i][j] = true;
            }
        }

        generateMaze(startX, startY, cells);

        for(int i = 0; i < SIZE + 1; i++) {
            for (int j = 0; j < SIZE + 1; j++) {
                if (j == 0 || j == SIZE) {
                    revealedVertical[i][j] = true;
                }
            }

            if(i == SIZE) {
                for(int j = 0; j < SIZE; j++) {
                    revealedHorizontal[i][j] = true;
                }
            }
        }

        for(int i = 0; i < 2* SIZE; i++)
        {
            int randPosX = rn.nextInt(SIZE - 1) + 1;
            int randPosY = rn.nextInt(SIZE - 1) + 1;
            int wallType = rn.nextInt(2);

            if(wallType == 1)
                wallsVertical[randPosX][randPosY] = false;
            else
                wallsHorizontal[randPosX][randPosY] = false;
        }

        triggerDataChange(this);
    }

    // generateMaze(int int, boolean[][]): Recursive subroutine for generating the maze; uses the Recursive-Backtracking method.
    private void generateMaze(int x, int y, boolean[][] cells)
    {
        cells[x][y] = true;
        ArrayList<Character> optionPool;
        while(true) 
        {
            optionPool = new ArrayList<Character>();
            if(x + 1 < SIZE && !cells[x + 1][y])
            {
                optionPool.add('E');
            }
            if(x - 1 >= 0 && !cells[x - 1][y])
            {
                optionPool.add('W');
            }
            if(y + 1 < SIZE && !cells[x][y+1])
            {
                optionPool.add('N');
            }
            if(y - 1 >= 0 && !cells[x][y-1])
            {
                optionPool.add('S');
            }

            if(optionPool.size() > 0)
            {
                char option = optionPool.get(random.nextInt(optionPool.size()));

                if(option == 'N')
                {
                    wallsVertical[x][y + 1] = false;
                    generateMaze(x, y + 1, cells);
                }
                else if(option == 'S')
                {
                    wallsVertical[x][y] = false;
                    generateMaze(x, y - 1, cells);
                }
                else if(option == 'W')
                {
                    wallsHorizontal[x][y] = false;
                    generateMaze(x - 1, y, cells);
                }
                else if(option == 'E')
                {
                    wallsHorizontal[x + 1][y] = false;
                    generateMaze(x + 1, y, cells);
                }
            } else {
                break;
            }
        }
    }
}
