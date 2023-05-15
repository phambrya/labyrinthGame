import com.wwu.graphics.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/* SubClass - BoardGUI
 *
 *    Binds the BoardGraphics interface to the IGameEventHandler interface.
 *    Handles display, sound, and input from the provided GUI
 *
 * Patterns:
 *    This is the 'View' in the Model-Controller-View pattern.
 */
public final class BoardGUI implements BoardGraphicsInf, IGameEventHandler {
    // Constants

    //   BASE_ICONS: Icon enum values for the Players' secret rooms.
    private static final GraphicImageTypes[] BASE_ICONS = {GraphicImageTypes.GOAL, GraphicImageTypes.BASE1.BASE1, GraphicImageTypes.BASE2};

    //   ACTOR_ICONS: Icon enum values for the different actors present in the game.
    private static final GraphicImageTypes[] ACTOR_ICONS = {GraphicImageTypes.ANTAGONIST, GraphicImageTypes.HERO1, GraphicImageTypes.HERO2};

    //   SOUND_TYPES: Map containing the conversions from the native SoundType to the external GraphicsSoundTypes.
    private static final Map<SoundType, GraphicsSoundTypes> SOUND_TYPES = new HashMap<SoundType, GraphicsSoundTypes>() {{
        put(SoundType.PLAYER_MOVE, GraphicsSoundTypes.PLAYER_MOVE);
        put(SoundType.PLAYER_ONE, GraphicsSoundTypes.PLAYER_ONE);
        put(SoundType.PLAYER_TWO, GraphicsSoundTypes.PLAYER_TWO);
        put(SoundType.PLAYER_ILLEGAL_MOVE, GraphicsSoundTypes.ILLEGAL_MOVE);
        put(SoundType.FOE_AWAKES, GraphicsSoundTypes.ON);
        put(SoundType.FOE_MOVE, GraphicsSoundTypes.FOE_MOVES);
        put(SoundType.FOE_ATTACK, GraphicsSoundTypes.FOE_ATTACKS);
        put(SoundType.WALL, GraphicsSoundTypes.WALL);
        put(SoundType.WIN, GraphicsSoundTypes.WINNER);
        put(SoundType.DEFEAT, GraphicsSoundTypes.DEFEAT);
        put(SoundType.GOAL, GraphicsSoundTypes.GOAL);
    }};

    // CLEAR_MESSAGE: Message used to clear the GUI console.
    private static final String CLEAR_MESSAGE = "\n\n\n\n\n\n\n\n\n";

    // gameGraphics: Private graphics driver used to affect the display.
    private GameGraphics gameGraphics;

    // subscriber: A subscribed input packet that gets signed once an input is detected.
    private InputPacket subscriber;

    // actorPositions: Cached Actor positions used for updating the GUI.
    private Map<Integer, Cell> actorPositions = new HashMap<Integer, Cell>();

    // roomPositions: Cached SecretRoom positions used for updating the GUI.
    private Map<Integer, Cell> roomPositions = new HashMap<Integer, Cell>();

    // treasureRoomPosition: Position of the treasure room.
    private Cell treasureRoomPosition = new Cell();

    //treasureRoomVisible: Whether the treasure room is visible.
    private boolean treasureRoomVisible = false;


    // Constructor: Creates a new GameGraphics and sets the initial wall layout.
    public BoardGUI() {
        gameGraphics = new GameGraphics(this);
        setWalls();
    }

    // Private Methods
    private void clearActors() {
        int size = GameBoard.SIZE;
        for(int r = 0; r < size; r++) {
            for(int c = 0; c < size; c++) {
                gameGraphics.changeTileImage(c, r, GraphicImageTypes.TILE);
            }
        }
    }
    private void clearWalls() {
        int size = GameBoard.SIZE;

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                gameGraphics.wallGraphicSetVisible(j, i, GraphicsWallDirections.NORTH, false);
                gameGraphics.wallGraphicSetVisible(j, i, GraphicsWallDirections.WEST, false);
            }
        }
    }

    private void setWalls() {
        int size = GameBoard.SIZE;
        for(int i = 0; i < size; i++) {
            gameGraphics.wallGraphicSetVisible(i, 0, GraphicsWallDirections.NORTH, true);
            gameGraphics.wallGraphicSetVisible(size - 1, i, GraphicsWallDirections.EAST, true);
        }

        for(int i = 0; i < size; i++) {
            gameGraphics.wallGraphicSetVisible(i, size - 1, GraphicsWallDirections.SOUTH, true);
            gameGraphics.wallGraphicSetVisible(0, i, GraphicsWallDirections.WEST, true);
        }
    }


    // Interface Fulfillment - BoardGraphicsInf

    /* @Override buttonPressed(GraphicsClickType):
     *
     * Callback for when a button is pressed.
     * Finds the corresponding EventType and hands it off to the current subscriber. The subscriber is then signed.
     */
    @Override
    public void buttonPressed(GraphicsClickTypes graphicsClickTypes) {
        if(this.subscriber != null) {
            switch(graphicsClickTypes) {
                case NEXT -> subscriber.setType(EventType.NEXT);
                case RESET -> subscriber.setType(EventType.RESET);
                case START -> subscriber.setType(EventType.START);
                default -> System.out.println("INVALID GRAPHICS CLICK TYPE");
            }
            subscriber.sign();

            subscriber = null;
        }
    }

    // Unused but necessary.
    @Override
    public void tilePressed(int i) {}


    /* @Override tilePressed(int, int):
     *
     * Triggers when a tile is pressed on the GUI.
     * Hands the tile to the current subscriber and signs said subscriber.
     */
    @Override
    public void tilePressed(int x, int y) {
        if(this.subscriber != null) {
            subscriber.setType(EventType.CELL);
            subscriber.setCell(new Cell(x, y));
            subscriber.sign();

            subscriber = null;
        }
    }


    // Interface Fulfillment - IGameEventHandler

    // @Override displayText(String): Displays text to the GUI.
    @Override
    public void displayText(String text) {
        gameGraphics.addTextToInfoArea(text);
    }


    // @Override subscribeInput(InputPacket): Subscribes an InputPacked for later signing.
    @Override
    public void subscribeInput(InputPacket inputPacket) {
        if(this.subscriber != null) {
            subscriber.setType(EventType.ORPHAN);
            subscriber.sign();
        }

        subscriber = inputPacket;
    }


    // @Override actorDataUpdate(Actor): Callback for when an Actor gets updated. Displays actors in their new positions.
    @Override
    public void actorDataUpdate(Actor actor) {
        if(actor.getVisibility()) {
            actorPositions.put(actor.getId(), actor.getPosition());
        } else {
            actorPositions.remove(actor.getId());
        }

        clearActors();

        Set<Integer> roomKeys = roomPositions.keySet();
        for(Integer key : roomKeys) {
            Cell roomPosition = roomPositions.get(key);
            gameGraphics.changeTileImage(roomPosition.x, roomPosition.y, BASE_ICONS[key]);
        }

        if(treasureRoomVisible) {
            gameGraphics.changeTileImage(treasureRoomPosition.x, treasureRoomPosition.y, BASE_ICONS[0]);
        } else {
            gameGraphics.changeTileImage(treasureRoomPosition.x, treasureRoomPosition.y, GraphicImageTypes.TILE);
        }

        Set<Integer> actorKeys = actorPositions.keySet();
        for(Integer key : actorKeys) {
            Cell position = actorPositions.get(key);
            gameGraphics.changeTileImage(position.x, position.y, ACTOR_ICONS[key]);
        }
    }

    // @Override actorDataUpdate(GameBoard): Callback for when the GameBoard gets updated. Updates revelations of the layout.
    @Override
    public void boardDataUpdate(GameBoard board) {
        int size = GameBoard.SIZE;
        boolean[][][] wallData = board.getWallData();
        boolean[][] wallsVertical = wallData[0];
        boolean[][] wallsHorizontal = wallData[1];
        boolean[][] revealedVertical = wallData[2];
        boolean[][] revealedHorizontal = wallData[3];
        List<SecretRoom> secretRooms = board.getSecretRooms();

        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                gameGraphics.wallGraphicSetVisible(j, i, GraphicsWallDirections.NORTH, wallsHorizontal[i][j] && revealedHorizontal[i][j]);
                gameGraphics.wallGraphicSetVisible(j, i, GraphicsWallDirections.WEST, wallsVertical[i][j] && revealedVertical[i][j]);
            }
        }

        setWalls();

        for(SecretRoom secretRoom : secretRooms) {
            roomPositions.put(secretRoom.getId(), secretRoom.getPosition());
        }

        treasureRoomPosition = board.getTreasureRoom();
        treasureRoomVisible = board.getTreasureReturned();
    }

    // @Override clearDisplay(): Clears the GUI completely.
    @Override
    public void clearDisplay() {
        int size = GameBoard.SIZE;

        clearActors();

        actorPositions.clear();
        roomPositions.clear();

        treasureRoomPosition = new Cell();
        treasureRoomVisible = false;

        clearWalls();
        setWalls();

        gameGraphics.addTextToInfoArea(CLEAR_MESSAGE);
    }

    // @Override playSound(SoundType): Plays the corresponding sound to the provided SoundType.
    @Override
    public void playSound(SoundType soundType) {
        GraphicsSoundTypes soundToPlay = SOUND_TYPES.get(soundType);
        gameGraphics.playSound(soundToPlay);
    }
}
