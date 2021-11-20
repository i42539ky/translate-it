public class ForwardCard extends Card{
	public String mode() {
		return "forward";
	}
	
	public ForwardCard(Entry entry) {
		super(entry);
	}

	public String front(){
		return entry.meaning;
	}
	
	public String back(){
		return entry.word;
	}
	
	public String comment(){
		return entry.comment;
	}
	
	public int getScore() {
		return entry.score1;
	}
	
	public void setScore(int score) {
		entry.score1 = score;
	}
}
