import java.io.FileInputStream;

/* Class - InputPacket
 *
 *     Used to wait on an input from an IGameEventHandler.
 *     Once the handler signs a packet, the waiting stops, and input data can be extracted from the packet.
 */
public final class InputPacket {
    // Constants

    //    FRAMES_PER_SECOND: How many times a second this packet should check if it has been signed.
    private static final long FRAMES_PER_SECOND = 30;

    //    MILLISECONDS: How many milliseconds occur in a second.
    private static final long MILLISECONDS = 1000;

    // [volatile] signed: Whether or not this packet has been signed. Volatile as it's value is set by another thread.
    private volatile boolean signed = false;
    public void sign() { signed = true; }

    // type: The type of input issued.
    private EventType type;
    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }

    // cell: Cell returned by a 'CELL' input.
    private Cell cell;
    public Cell getCell() { return cell;}
    public void setCell(Cell cell) { this.cell = cell; }

    // awaitSigned(): Sleeps the main thread until this input packet is signed.
    public void awaitSigned() {
        long millisecondsToWait = MILLISECONDS/FRAMES_PER_SECOND;
        try {
            while (!signed) {
                Thread.sleep(millisecondsToWait, 0);
            }
        } catch (InterruptedException interruptedException) {
            System.out.println("Gameplay thread interrupted.");
            Thread.currentThread().interrupt();
        }
    }
}
