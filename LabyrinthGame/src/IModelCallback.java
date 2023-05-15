@FunctionalInterface
public interface IModelCallback<T> {
    public void execute(T object, IGameEventHandler handler);
}
