import java.util.ArrayList;

public enum Mode{
	Forward(0), //meaning to spelling
	Dictation(1), //voice to spelling
	Pronunciation(2); //spelling to voice
	
	private final int index;
	
	private Mode(final int index) {
		this.index = index;
	}
	
	public int toIndex() {
		return index;
	}
	
	public static Mode toMode(String str) {
		for(Mode m: Mode.values()){
			if(str.equals(m.name())){
				return m;
			}
		}
		return null;
	}
	
	public static String[] names() {
		ArrayList<String> values = new ArrayList<String>();
		for(Mode mode: values()) {
			values.add(mode.name());
		}
		return values.toArray(new String[0]);
	}
}