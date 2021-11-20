


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.Date;

import javazoom.jl.player.Player;

public abstract class Card {
	
	final static double DAY = 24 * 60 * 60 * 1000;
	
	protected Entry entry;
	
	public boolean isLocked;
	
	public Card(Entry entry){
		this.entry = entry;
		isLocked = false;
	}
	
	public abstract String front();
	public abstract String back();
	public abstract String comment();
	public abstract int getScore();
	public abstract void setScore(int score);
	public abstract String mode();

	public boolean isAssignable(){
		//if(getScore() == 0) {
		//	return true;
		//}
		//else {
		//	return false;
		//}	
		// if(getScore() == 0) {
		//   return true;
		// }
		double elapsed = (new Date().getTime() - entry.latest.getTime()) / DAY;
		//System.out.print(entry.word + ": ");
		//System.out.print(elapsed + " vs " + Math.pow(2, getScore()) + " -> ");
		//System.out.println(elapsed >= Math.pow(2, getScore()));
		return elapsed >= Math.pow(2, getScore() - 1);
	}
	
	public boolean isPlayable(){
		return entry.voice.toFile().isFile();
		//return Files.exists(entry.voice);
	}
	
	public void play(){
		System.out.println("DEBUG: play called");
		if(isPlayable()){
			new Thread(() -> {
			    try{
					new Player(new BufferedInputStream(new FileInputStream(entry.voice.toFile()))).play();
				} catch(Exception e){
					System.out.println(e);
				}
			}).start();
		}
	}
	
	public void increment() {
		if(!isLocked) {
			setScore(getScore() + 1);
		}
	}
	
	public void decrement() {
		if(!isLocked) {
			setScore(getScore() - 1);
			if(getScore() < 0) {
				setScore(0);
			}
		}
	}
	
	public void update() {
		entry.latest = new Date();
	}
}
