import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;


/* Abstract Class - Model
 *
 * Abstract SuperClass for Classes that need to notify an IGameEventHandler that their data has changed.
 */
public abstract class Model<T> {
    // gameEventHandler: Reference to the handler that will be notified of changes
    private IGameEventHandler gameEventHandler;

    // callback: The method on the handler that should be used to send the updated data.
    private IModelCallback<T> callback;


    // setCallback(IGameEventHandler, Method): Sets the callback handler and method.
    public void setCallback(IModelCallback<T> callback, IGameEventHandler gameEventHandler) {
        this.callback = callback;
        this.gameEventHandler = gameEventHandler;
    }

    // triggerDataChange(Object): Passes the updated data to the handler using the callback.
    public void triggerDataChange(T data) {
        if(callback != null && gameEventHandler != null) {
            callback.execute(data, gameEventHandler);
        }
    }
}
