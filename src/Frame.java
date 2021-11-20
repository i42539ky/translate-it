import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class Frame extends JFrame {
	public static void main(String args[]){
		new Frame();
	}
	
	//定数
	static final String HOME = System.getProperty("user.home") + "/translate_it";
	
	//大域クラス
	State state;
	CommandInvoker commandInvoker;
	
	//コンポーネント
	JTextArea question; //問題文
	JTextField field; //テキスト入力領域
	JTextPane answer; //回答文
	JTextArea comment; //コメント
	
	JButton play; //発音
	JButton back; //もどる
	JButton skip; //とばす
	JButton ok; //回答
	JButton good; //正解にして次へ
	JButton bad; //不正解にして次へ
	
	JMenuItem open; //ファイルを選ぶ
	JMenuItem select; //出題形式を選ぶ
	JMenuItem correct; //問題の修正
	JMenuItem redo; 
	JMenuItem undo;
	JMenuItem generate;
	
	JLabel statusBar;
	
	Frame(){
		//================概観の設定================//
		setTitle("単語帳 ver. 10");
		setBounds(200, 200, 750, 500); //ウィンドウの位置とサイズ
		
		//メニューバー
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(open = new JMenuItem("開く"));
		menuBar.add(select = new JMenuItem("出題形式の変更"));
		menuBar.add(correct = new JMenuItem("問題の修正"));
		menuBar.add(redo = new JMenuItem("やり直し"));
		menuBar.add(undo = new JMenuItem("元に戻す"));
		menuBar.add(generate = new JMenuItem("音声を生成"));
		setJMenuBar(menuBar);
		
		question = new JTextArea("問題文");
		question.setLineWrap(true);
		question.setWrapStyleWord(true);
		question.setPreferredSize(new Dimension(680, 65));
		question.setEditable(false); //編集不可
		question.setFocusable(true); //フォーカスしない
		question.setOpaque(false); //背景色
		
		field = new JTextField(57);
		
		answer = new JTextPane(new DefaultStyledDocument(new StyleContext()));
		answer.setText("正解文");
		answer.setPreferredSize(new Dimension(683, 20));
		answer.setEditable(false);
		answer.setFocusable(false);
		answer.setOpaque(false);
		
		comment = new JTextArea("コメント");
		comment.setLineWrap(true);
		comment.setWrapStyleWord(true);
		comment.setPreferredSize(new Dimension(680, 65));
		comment.setEditable(false); //編集不可
		comment.setFocusable(true); //フォーカスしない
		comment.setOpaque(false); //背景色
		
		play = new JButton("再生");
		
		ok = new JButton("回答");
		
		good = new JButton("正解にして次へ");
		good.setPreferredSize(new Dimension(150, 30));
		
		bad = new JButton("不正解にして次へ");
		bad.setPreferredSize(new Dimension(150, 30));
		
		back = new JButton("もどる");
		back.setPreferredSize(new Dimension(150, 30));
		
		skip = new JButton("とばす");
		skip.setPreferredSize(new Dimension(150, 30));
		
		//	JPanel 内に GridBagLayout で JPanel を積んでレイアウト
		//	それぞれの panel は FlowRayout
		//  	1行目: panel[0] << questionエリア
		//		2行目: panel[1] << field
		//		3行目: panel[2] << answerペーン
		//		4行目: panel[3] << commentラベル
		//		5行目: panel[4] << playボタン
		//		6行目: panel[5] << okボタン
		//		7行目: panel[6] << goodボタン, badボタン
		//		8行目: panel[7] << backボタン, skipボタン
		
		JPanel root = new JPanel();
		JPanel[] panel = new JPanel[9];
		for(int i = 0; i < panel.length; i++){
			panel[i] = new JPanel();
		}

		panel[0].add(question);

		panel[1].add(field);

		panel[2].add(answer);
		
		panel[3].add(comment);
		
		panel[4].add(play);

		panel[5].add(ok);

		panel[6].add(good);
		panel[6].add(bad);
		
		panel[7].add(back);
		panel[7].add(skip);
		
		GridBagLayout layout = new GridBagLayout();
		root.setLayout(layout);
		for(int i = 0; i < panel.length; i++){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i;
			layout.setConstraints(panel[i], gbc);
			root.add(panel[i]);
		}

		statusBar = new JLabel("status");
		statusBar.setPreferredSize(new Dimension(680, 20));
		
		getContentPane().add(root, BorderLayout.CENTER);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(ok);

		setVisible(true);
		
		
		//================動作設定================//
		//×ボタンで閉じる
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//プログラム終了時に行われる処理
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			state.closeWordbook();
		}));

		//ボタン、メニューアイテムの挙動
		correct.addActionListener(e -> {
			new CorrectionDialog(state.getCard(), this);
			refresh();
		});
		
		generate.addActionListener(e -> {
			String text = state.getCard().entry.word.replace("|", ""); //ドイツ語対応
			Path target = state.wordbook.getVoicePath().resolve(text.replace(" ",  "_") + "_polly.mp3");
			state.polly.synthesize(text, target);
			state.getCard().entry.voice = target;
			refresh();
		});
		
		play.addActionListener(e ->{
			state.getCard().play();
		});
		
		ok.addActionListener(e -> {
			state.answered = true;
			refresh();
		});
		
		back.addActionListener(e -> {
			commandInvoker.invoke(new BackCommand());
			refresh();
		});
		
		skip.addActionListener(e -> {
			commandInvoker.invoke(new SkipCommand());
			refresh();
		});
		
		good.addActionListener(e -> {
			commandInvoker.invoke(new GoodCommand());
			refresh();
		});
		
		bad.addActionListener(e -> {
			commandInvoker.invoke(new BadCommand());
			refresh();
		});
		
		open.addActionListener(e -> {
			commandInvoker.invoke(new OpenCommand(this));
			refresh();
		});
		
		select.addActionListener(e -> {
			commandInvoker.invoke(new SelectCommand(this));
			refresh();
		});
		
		redo.addActionListener(e -> {
			commandInvoker.redo();
			refresh();
		});
		
		undo.addActionListener(e -> {
			commandInvoker.undo();
			refresh();
		});
		
		
		//================変数の初期化================//
		state = new State();
		state.wordbook = null;
		state.polly = null;
		state.deck = null;
		state.counter = 0;
		state.good = 0;
		state.answered = false;
		
		commandInvoker = new CommandInvoker();
		
		Polly.init();
		
		refresh();
	}
	
	class BackCommand extends Command{
		public void invoke() {
			state.answered = false;
			state.push();
			state.counter--;
		}
		
		public void undo() {
			state.pop();
		}
		
		public void redo() {
			invoke();
		}
	}
	
	class SkipCommand extends Command{
		public void invoke() {
			state.answered = false;
			state.push();
			state.counter++;
		}
		
		public void undo() {
			state.pop();
		}
		
		public void redo() {
			invoke();
		}
	}

	class GoodCommand extends Command {
		Date latest_new;
		Date latest_old;
		boolean isLocked;
		
		public void invoke(){
			state.answered = false;
			state.push();
			
			//カードの情報をコピー
			latest_old = state.getCard().entry.latest;
			isLocked = state.getCard().isLocked;
			
			//更新
			state.getCard().increment();
			state.getCard().update();
			state.getCard().isLocked = true;
			
			//redo用
			latest_new = state.getCard().entry.latest;
			
			state.counter++;
			state.good++;
		}
		
		public void undo(){
			state.pop();
			state.getCard().isLocked = isLocked;
			state.getCard().entry.latest = latest_old;
			state.getCard().decrement();
		}
		
		public void redo() {
			state.answered = false;
			state.push();
			state.getCard().increment();
			state.getCard().entry.latest = latest_new;
			state.getCard().isLocked = true;
			state.counter++;
			state.good++;
		}
	}

	class BadCommand extends Command {	
		Date latest_new;
		Date latest_old;
		boolean isLocked;
		int score_old;
		
		public void invoke(){
			state.answered = false;
			state.push();
			
			//カードの情報をコピー
			latest_old = state.getCard().entry.latest;
			isLocked = state.getCard().isLocked;
			score_old = state.getCard().getScore();
			
			//更新
			state.getCard().decrement();
			state.getCard().update();
			state.getCard().isLocked = true;
			state.deck.add(state.getCard());
			
			//redo用
			latest_new = state.getCard().entry.latest;
			
			state.counter++;
		}
		
		public void undo(){
			state.pop();
			state.getCard().isLocked = isLocked;
			state.getCard().entry.latest = latest_old;
			state.getCard().setScore(score_old);
			state.deck.remove(state.deck.size() - 1);
		}
		
		public void redo() {
			state.answered = false;
			state.push();
			state.getCard().decrement();
			state.getCard().entry.latest = latest_new;
			state.getCard().isLocked = true;
			state.deck.add(state.getCard());
			state.counter++;
		}
	}
	
	class OpenCommand extends Command{
		JFrame parent;
		Wordbook wordbook;
		
		public OpenCommand(JFrame parent) {
			this.parent = parent;
		}
		
		public void invoke() {
			state.push();
			wordbook = state.wordbook;
			
			//ファイルを選択
			JFileChooser chooser = new JFileChooser(HOME);
			chooser.setFileFilter(new FileNameExtensionFilter("Translate It setting file (.tis)", "tis"));
			int result = chooser.showOpenDialog(parent);
			
			//ファイルが選ばれなかった
			if(result != JFileChooser.APPROVE_OPTION){
				return;
			}
			
			try {
				//設定ファイルの読み込み
				Path dictPath = chooser.getSelectedFile().toPath();
				Dictionary dict = new Dictionary(dictPath);
				
				//シグネチャの確認
				if(!dict.getValue("signature").equals("Translate It version 10")) {
					JOptionPane.showMessageDialog(null, "illegal signature");
					return;
				}
					
				Path wordbookPath = dictPath.resolveSibling(dict.getValue("wordbook"));
				Path voicePath = dictPath.resolveSibling(dict.getValue("voice"));
				int limit = Integer.parseInt(dict.getValue("limit"));
				Language language = Language.toLanguage(dict.getValue("language"));
			
				//すでにファイルを開いてる場合は閉じる
				if(state.wordbook != null){
					state.closeWordbook();
					
					state.wordbook = null;
					state.polly = null;
					state.deck = null;
					state.counter = 0;
					state.good = 0;
					state.answered = false;
				}
				
				state.wordbook = new Wordbook(wordbookPath, voicePath, limit);
				state.polly = new Polly(language);
				
				select.setEnabled(true);
				select.doClick();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e);
				return;
			}
			
			wordbook = state.wordbook;
		}
		
		public void undo() {
			state.pop();
		}
		
		public void redo() {
			state.push();
			
			state.wordbook = wordbook;
			state.deck = null;
			state.counter = 0;
			state.good = 0;
			state.answered = false;
		}
	}
	
	class SelectCommand extends Command{
		JFrame parent;
		Deck deck;
		
		public SelectCommand(JFrame parent) {
			this.parent = parent;
		}
		
		public void invoke() {
			state.push();
			deck = state.deck;
			
			SelectionDialog dialog = new SelectionDialog(parent, "出題形式を選択してください", Mode.names());
			Mode chosen = Mode.toMode(dialog.answer);
			
			if(chosen != null){
				state.deck = state.wordbook.complie(chosen);
				state.counter = 0;
				state.good = 0;
				state.answered = false;
				
				deck = state.deck;
			}
		}
		
		public void undo() {
			state.pop();
		}
		
		public void redo() {
			state.push();
			
			state.deck = deck;
			state.counter = 0;
			state.good = 0;
			state.answered = false;
		}
	}
	
	//画面の更新
	public void refresh(){
		//ボタン
		open.setEnabled(true);
		select.setEnabled(state.wordbook != null);
		correct.setEnabled(state.isOrdinal());
		redo.setEnabled(commandInvoker.isRedoable());
		undo.setEnabled(commandInvoker.isUndoable());
		generate.setEnabled(state.isOrdinal());
		
		ok.setEnabled(state.isOrdinal() && !state.answered);
		good.setEnabled(state.isOrdinal());
		bad.setEnabled(state.isOrdinal());
		skip.setEnabled(state.isOrdinal());
		back.setEnabled(state.counter > 0);
		play.setEnabled(state.isOrdinal() && state.getCard().isPlayable());
		
		//ステータスバー
		if(state.deck != null) {
			statusBar.setText(state.toString());
		}
		
		//問題文
		if(state.wordbook == null) {
			question.setText("ファイルを選択してください");
		}
		else if(state.deck == null) {
			question.setText("出題形式を選択してください");
		}
		else if(!state.isOrdinal()) {
			question.setText("すべての問題が終了しました");
		}
		else if(!state.answered){
			question.setText(state.getCard().front());
			if(state.getCard().mode() == "dictation") {
				state.getCard().play();
			}
		}
		else {
			//none
		}
		
		//その他のフィールド
		if(state.answered) {
			//answeredがtrueなのはokボタンによる遷移なので答え合わせをする
			Pattern pattern = Pattern.compile("([0-9a-zA-ZäüöÄÜÖßéàèùâêîôûëïüœç¥-]+|[.,;¥?¥!])");
			
			//回答文の正規化
			String tmp = field.getText();
			tmp = tmp.replaceAll(" +", " "); //連続したスペースをなくす
			tmp = tmp.replaceAll("^ ",  "" ); //文頭のスペースを削除
			tmp = tmp.replaceAll(" $",  ""); //文末のスペースを削除
			field.setText(tmp);
			
			//共通の処理
			answer.setText(state.getCard().back());
			comment.setText(state.getCard().comment());
			if(state.getCard().mode() != "dictation") {
				state.getCard().play();
			}
			
			//正解のときの処理
			if(field.getText().equals(state.getCard().back())){
				getRootPane().setDefaultButton(good);
				answer.setText(answer.getText() +  " ⇒ ◯");
			}
			
			//不正解のときの処理
			else{
				getRootPane().setDefaultButton(bad);
				answer.setText(answer.getText() + " ⇒ ×");
			}
			
			//解答欄がからのときは文字の装飾をしない
			if(field.getText().isEmpty()){
				return;
			}
			
			//文字の装飾
			StyledDocument doc = answer.getStyledDocument();
			MutableAttributeSet attr = new SimpleAttributeSet();
			StyleConstants.setForeground(attr, Color.red);
			
			//回答文をパース
			String words = " ";
			Matcher m = pattern.matcher(field.getText());
			while(m.find()){
				words += m.group(1) + " ";
			}
			
			//単語を取り出す正規表現
			m = pattern.matcher(state.getCard().back());
			while(m.find()){
				if(!words.contains(" " + m.group(1) + " ")){
					doc.setCharacterAttributes(m.start(1), m.end(1) - m.start(1), attr, true);
				}
			}
		}
		else {
			getRootPane().setDefaultButton(ok);
			answer.setText("");
			comment.setText("");
			field.setText("");
		}
	}
}