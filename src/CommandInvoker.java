import java.util.Stack;

//undo, redo が可能なコマンドの実装
//詳細は
//http://d.hatena.ne.jp/Youchan/20081111/1226388917
//を参照

public class CommandInvoker {
	private Stack<Command> undoStack;
	private Stack<Command> redoStack;
	
	public CommandInvoker(){
		undoStack = new Stack<Command>();
		redoStack = new Stack<Command>();
	}
	
	public void invoke(Command command){
		redoStack.clear();
		undoStack.push(command);
		command.invoke();
	}
	
	public boolean isUndoable(){
		return !undoStack.isEmpty();
	}
	
	public boolean isRedoable(){
		return !redoStack.isEmpty();
	}
	
	public void undo(){
		if(!undoStack.isEmpty()){
			Command command = undoStack.pop();
			command.undo();
			redoStack.push(command);
		}
	}

	public void redo(){
		if(!redoStack.isEmpty()){
			Command command = redoStack.pop();
			command.redo();
			undoStack.push(command);	
		}
	}
}