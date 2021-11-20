public class DictationCard extends Card{
	public String mode() {
		return "dictation";
	}
			
	public DictationCard(Entry entry) {
		super(entry);
	}

	public String front(){
		return "Sound Only";
	}
	
	public String back(){
		return entry.word;
	}
	
	public String comment(){
		return entry.meaning + "(" + entry.comment + ")";
	}
	
	public int getScore() {
		return entry.score2;
	}
	
	public void setScore(int score) {
		entry.score2 = score;
	}
	
	public boolean isAssignable() {
		return isPlayable() && super.isAssignable();
	}
}