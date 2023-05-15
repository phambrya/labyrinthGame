/* Class - SecretRoom
 *
 * Holds the data of a secret room: an identification number and a Cell position.
 */
public final class SecretRoom {
    // position: The Cell position of this SecretRoom.
    private final Cell position;
    public Cell getPosition() { return new Cell(position); }

    // id: The identification number of this SecretRoom. It will be the same as the player it belongs too.
    private final int id;
    public int getId() {return id;}

    // Constructor(Cell, int): Create a new SecretRoom with the given position and ID.
    public SecretRoom(Cell position, int id)
    {
        this.position = new Cell(position);
        this.id = id;
    }
}

