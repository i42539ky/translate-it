public class PronunciationCard extends Card{
	public String mode() {
		return "pronounciation";
	}
	
	public PronunciationCard(Entry entry) {
		super(entry);
	}

	public String front(){
		return entry.word;
	}
	
	public String back(){
		return entry.meaning;
	}
	
	public String comment(){
		return entry.comment;
	}
	
	public int getScore() {
		return entry.score3;
	}
	
	public void setScore(int score) {
		entry.score3 = score;
	}
	
	public boolean isAssignable() {
		return isPlayable() && super.isAssignable();
	}
}
