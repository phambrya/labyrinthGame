/* SubClass - Foe
 *
 *    Sub class of Actor. Represents the Foe on the GameBoard
 *
 * Patterns:
 *    Part of the 'Model' in the Model-Controller-View pattern.
 */
public final class Foe extends Actor {
    // Constant - AWAKEN_RANGE: Distance that when encroached upon awakens the Foe.
    public static final int AWAKEN_RANGE = 3;

    // awake: Holds whether the Foe has been awakened or not.
    private boolean awake = false;
    public boolean getAwake() { return awake; }
    public void setAwake(boolean value) {
        awake = value;
        triggerDataChange(this);
    }

    // Constructor: Creates a Foe with the given ID.
    public Foe(int id) {
        super(id);
        this.type = ActorType.FOE;
    }
}
