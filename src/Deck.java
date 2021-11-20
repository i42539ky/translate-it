import java.util.ArrayList;
import java.util.Random;

public class Deck extends ArrayList<Card>{
	public Deck() {
		super();
	}
	
	public void shuffle() {
		Random rand = new Random();
		for(int i = 0; i < size(); i++) {
			int j = rand.nextInt(size());
			Card tmp = get(i);
			set(i, get(j));
			set(j, tmp);
		}
	}
	
	public void sort() {
		sort((a, b) -> {
			int diff = b.getScore() - a.getScore();
			if( diff == 0) {
				diff = a.entry.level - b.entry.level;
			}
			return diff;
		});
	}
	
	public void head(int n) {
		while(size() > n) {
			remove(size() - 1);
		}
	}
}
