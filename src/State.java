public class State {
	public Wordbook wordbook;
	public Deck deck;
	public int counter;
	public int good;
	public boolean answered;
	public Polly polly;
	
	public State prev;
	
	public State() {
		prev = null;
	}
	
	public void push() {
		State clone = new State();
		clone.wordbook = wordbook;
		clone.deck = deck;
		clone.counter = counter;
		clone.good = good;
		clone.answered = answered;
		clone.polly = polly;
		clone.prev = prev;
		prev = clone;
		
	}
	
	public void pop() {
		wordbook = prev.wordbook;
		deck = prev.deck;
		counter = prev.counter;
		good = prev.good;
		answered = prev.answered;
		polly = prev.polly;
		prev = prev.prev;
	}
	
	public Card getCard() {
		return deck.get(counter);
	}
	
	public void closeWordbook() {
		if(wordbook != null){
			wordbook.writeback();
			wordbook.close();
		}
	}
	
	public String toString() {
		int ratio = 0;
		if(counter >= 1) {
			ratio = (int)((float)good / counter * 100);
		}
		return counter + "/" + deck.size() + "    正答率: " + ratio + "%    " + wordbook.getWordbookPath();  
	}
	
	public boolean isOrdinal() {
		return deck != null && counter < deck.size();
	}
}
