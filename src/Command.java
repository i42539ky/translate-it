
public abstract class Command {
	public abstract void invoke();
	public abstract void undo();
	public abstract void redo();
}