import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.swing.Timer;

public class Wordbook {

	//定数
	final private static int WRITEBACK_INTERVAL = 60 * 1000; //1分ごとに書き戻す
	private static final String DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss";
	
	//Settings
	private Path wordbookPath; //csvファイルのパス
	private Path voicePath; //音声ファイルのルートのパス
	private int limit; //出題数の上限
	
	private ArrayList<Entry> entries; //全エントリ
	
	private Timer writebackTimer;
	private Path locked;
	
	private int counter = 0;
	
	//Note: wordbookPath must be absolute!
	public Wordbook(Path wordbookPath, Path voicePath, int limit) throws Exception{
		this.wordbookPath = wordbookPath;
		this.voicePath = wordbookPath.resolve(voicePath);
		this.limit = limit;

		//ファイルロックがうまくいかないので、自分でやる
		//　".lock_" + "ファイル名" というファイルが存在していたらロックされていると判定
		locked = wordbookPath.resolveSibling(".lock_" + wordbookPath.getFileName());
		if(locked.toFile().exists()){
			throw new Exception("File is locked.");
		}
		locked.toFile().createNewFile();
		locked.toFile().deleteOnExit(); //念のため
		
		//wordbookの読み込み
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(wordbookPath.toFile()), "UTF-8"));
		Table table = new Table(reader);
		reader.close();
		
		entries = new ArrayList<Entry>();
		
		for(int i = 1; i < table.getRowCount(); i++){
			Entry entry = new Entry();
			
			entry.word = table.get(i, 0);
			entry.meaning = table.get(i, 1);
			entry.comment = table.get(i, 2);
			entry.level = myParseInt(table.get(i, 3));
			entry.voice = voicePath.resolve(table.get(i, 4));
			entry.latest = myParseDate(table.get(i, 5));
			entry.score1 = myParseInt(table.get(i, 6));
			entry.score2 = myParseInt(table.get(i, 7));
			entry.score3 = myParseInt(table.get(i, 8));
			entry.id = table.get(i,  9);
			
			entries.add(entry);
			
			//音声ファイルの自動検索
			//	voiceに指定されたファイルが存在する
			//		=> そのまま
			//  それ以外で、"[voicePath]/[word].mp3"というファイルが存在する
			//		=> そのファイルをvoiceとして使用
			//	それ以外で、wordに含まれるスペースをアンダースコアで置き換えたファイルが存在する
			//		=> そのファイルをvoiceとして使用
			//	それ以外
			//		=> voice = voicePathにしておく
			if(table.get(i, 4).equals("no voice")) {
				entry.no_voice = true;
			}
			else {
				Path candidate1 = voicePath.resolve(entry.word + ".mp3");
				Path candidate2 = voicePath.resolve(entry.word.replace(' ', '_') + ".mp3");
				if(Files.exists(entry.voice) & entry.voice.toFile().isFile()) {
				}
				else if(Files.exists(candidate1)){
					entry.voice = candidate1;
				}
				else if(Files.exists(candidate2)) {
					entry.voice = candidate2;
				}
				else {
					entry.voice = voicePath;
				}
			}
			
			// id が空なら現在のエポック秒から生成
			if(entry.id.equals("")) {
				entry.id = encode(new Date().getTime());
			}
		}
		
		//定期書き出し処理をセット
		writebackTimer = new Timer(WRITEBACK_INTERVAL, e -> {
			new Thread(() -> {
				writeback();
			}).start();
		});
		writebackTimer.start();
	}
	
	private String encode(long x) {
		String letters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int length = letters.length();
		String id = "";
		
		counter++;
		long remainder = x * 100 + counter;
		for( int i = 0; i < 8; i++) {
			int residual = (int) (remainder % length);
			remainder = (remainder - residual) / length;
			id = letters.charAt(residual) + id;
		}
		return id;
	}
	
	public void close(){
		writebackTimer.stop();
		locked.toFile().delete();
	}
	
	//出題の基準は
	//	1. scoreの高いものを優先
	//	2. scoreが同じであればlevelが低いものを優先
	//  3. score, levelが同じものについては優先順位にランダム性を持たせる
	public Deck complie(Mode mode){
		Deck deck = new Deck();
		
		for(Entry entry: entries){
			Card card = null;
			
			switch(mode) {
				case Forward:
					card = new ForwardCard(entry);
					break;
				case Dictation:
					card = new DictationCard(entry);
					break;
				case Pronunciation:
					card = new PronunciationCard(entry);
					break;
			}
			
			if(card.isAssignable()){
				deck.add(card);
			}
		}
		
		deck.shuffle();
		deck.sort();
		deck.head(limit);
		deck.shuffle();
		
		return deck;
	}
	
	public void writeback() {
		writeback(wordbookPath);
	}
	
	public void writeback(Path target){
		Table table = new Table(entries.size() + 1, 10);
		
		table.set(0, 0, "word");
		table.set(0, 1, "meaning");
		table.set(0, 2, "comment");
		table.set(0, 3, "level");
		table.set(0, 4, "voice");
		table.set(0, 5, "latest");
		table.set(0, 6, "score1");
		table.set(0, 7, "score2");
		table.set(0, 8, "score3");
		table.set(0, 9, "ID");
		
		for(int i = 0; i < entries.size(); i++){
			table.set(i + 1, 0, entries.get(i).word);
			table.set(i + 1, 1, entries.get(i).meaning);
			table.set(i + 1, 2, entries.get(i).comment);
			table.set(i + 1, 3, entries.get(i).level + "");
			if(entries.get(i).no_voice) {
				table.set(i + 1,  4,  "no voice");
			}
			else {
				table.set(i + 1, 4, voicePath.relativize(entries.get(i).voice).toString());
			}
			table.set(i + 1, 5, myFormatDate(entries.get(i).latest));
			table.set(i + 1, 6, entries.get(i).score1 + "");
			table.set(i + 1, 7, entries.get(i).score2 + "");
			table.set(i + 1, 8, entries.get(i).score3 + "");
			table.set(i + 1, 9, entries.get(i).id);
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target.toFile()), "UTF-8"));
			table.write(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		System.out.println(target.toString() + " is written back.");
	}
	
	public Path getWordbookPath(){
		return wordbookPath;
	}
	
	public Path getVoicePath() {
		return voicePath;
	}
	
	private static int myParseInt(String str) {
		try{
			return Integer.parseInt(str);
		}
		catch(Exception e) {
			return 0;
		}
	}
	
	private static Date myParseDate(String str) {
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(str);
		}
		catch(Exception e) {
			return new Date();
		}
	}
	
	private static String myFormatDate(Date date) {
		return new SimpleDateFormat(DATE_FORMAT).format(date);
	}
}