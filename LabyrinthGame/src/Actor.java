/* Abstract SubClass - Actor
 *
 * Provides base functionality for Player and Foe subclasses.
 * Represents an actor that moves on the GameBoard and interacts with other Actors.
 *
 *
 */
public abstract class Actor extends Model<Actor>
{
    // position: Cell position of this actor.
    private Cell position = new Cell();
    public Cell getPosition() { return new Cell(position); }

    // id: Identification number for this actor.
    private int id;
    public int getId() {return id;}

    // type: Type of this actor (Foe or Player)
    protected ActorType type;
    public ActorType getType() { return type; }

    // visible: Visibility status of this actor
    private boolean visible = false;
    public boolean getVisibility() { return visible; }
    public void setVisibility(boolean value) {
        visible = value;
        triggerDataChange(this);
    }

    // Constructor: Creates an actor with an ID.
    protected Actor(int id) {
        this.id = id;
    }

    // Move(Cell): Moves this actor to this cell position.
    public void move(Cell cell) {
        position = new Cell(cell);
        triggerDataChange(this);
    }
}
