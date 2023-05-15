/* Player
 *
 * State of the player(s)
 * Child of ACTORS
 */

public final class Player extends Actor {

    // Constants

    //    FATIGUE_MAX: Maximum/Full fatigue value.
    private static final int FATIGUE_MAX = 50;

    //    MAXIMUM_MOVES: Maximum moves a player gets per turn
    private static final int MAXIMUM_MOVES = 8;

    //    MINIMUM_MOVES: Minimum moves a player gets per turn
    private static final int MINIMUM_MOVES = 4;

    //    FATAL_INJURY: Injury level that kills a player.
    public static final int FATAL_INJURY = 3;


    // Private Fields (+Getters/Setters)

    //    injuredLevel: Internal variable that holds how many times this player has been attacked.
    private int injuredLevel;

    //    fatigue: Current fatigue level of this player.
    private int fatigue = FATIGUE_MAX;
    public int getFatigue() { return fatigue; }
    public void resetFatigue() { fatigue = FATIGUE_MAX; }

    //    hasTreasure: Holds whether or not this player has the treasure.
    private boolean hasTreasure;
    public boolean getTreasure() { return hasTreasure; }
    public void setTreasure(boolean value) {
        hasTreasure = value;
        triggerDataChange(this);
    }

    //    safe: Holds whether or not this player is safe (in their secret room).
    public boolean safe;
    public boolean getSafe() { return safe; }
    public void setSafe(boolean value) {
        safe = value;
        triggerDataChange(this);
    }

    // Constructor(int): Creates a Player with the given ID.
    public Player(int id) {
        super(id);
        this.type = ActorType.PLAYER;
        this.setVisibility(true);
    }


    // getMaxMoves(): Computes this Player's maximum moves based on their injured level and treasure status.
    public int getMaxMoves() {
        if(hasTreasure) {
            return MINIMUM_MOVES;
        } else {
            int moves = MAXIMUM_MOVES - 2 * injuredLevel;

            if(moves < MINIMUM_MOVES) {
                moves = MINIMUM_MOVES;
            }

            return moves;
        }
    }

    // wound(): Wounds a player by one injured level.
    public void wound() {
        ++injuredLevel;
        triggerDataChange(this);
    }

    // heal(): Heals a player by one injured level.
    public boolean heal() {
        if(injuredLevel > 0) {
            --injuredLevel;
            triggerDataChange(this);
            return true;
        }
        triggerDataChange(this);
        return false;
    }

    // tire(): Tires a player depending on their injuredLevel.
    public void tire() {
        fatigue -= 1 + injuredLevel;

        if(fatigue < 0) {
            fatigue = 0;
        }
    }

    // isAlive(): Returns whether or not this Player is dead (injured level has exceeded FATAL_INJURY.)
    public boolean isAlive() {
        boolean alive =  (injuredLevel < FATAL_INJURY);
        this.setVisibility(alive);
        return alive;
    }
}
