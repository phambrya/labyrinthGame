/* IGameEventHandler
 *
 *     Interface for an Input & GUI system.
 *
 * Patterns:
 *     This is the 'Target' in the Adapter pattern.
 */
public interface IGameEventHandler {
    public void displayText(String text);

    public void clearDisplay();
    public void playSound(SoundType soundType);

    public void subscribeInput(InputPacket inputPacket);

    public void actorDataUpdate(Actor actor);

    public void boardDataUpdate(GameBoard board);
}
