/* Class - ActorFactory
 *
 * Creates a Player or Foe depending on the passed ActorType.
 */
public final class ActorFactory {

    // createActor(ActorType, int) - Creates an actor of the given type with the given ID.
    public Actor createActor(ActorType type, int id) {
        Actor result = null;

        switch(type) {
            case FOE    -> result = new Foe(id);
            case PLAYER -> result = new Player(id);
        }

        return result;
    }
}
