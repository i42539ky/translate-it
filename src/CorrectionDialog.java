import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import javazoom.jl.player.Player;

public class CorrectionDialog extends JDialog {
	JTextField spelling;
	JTextField meaning;
	JTextField voice;
	JTextField comment;
	JTextField rank;
	
	JButton play;
	JButton correct;
	JButton cancel;
	
	Card card;
	
	public CorrectionDialog(Card card, Frame parent){
		this.card = card;
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		setModal(true);
		setTitle("問題文の修正");
		setBounds(100, 100, 750, 300);
		
		spelling = new JTextField(50);
		spelling.setText(card.entry.word);
		
		meaning = new JTextField(50);
		meaning.setText(card.entry.meaning);
		
		comment = new JTextField(50);
		comment.setText(card.entry.comment);
		
		rank = new JTextField(10);
		rank.setText(String.valueOf(card.entry.level));
		
		voice = new JTextField(50);
		voice.setText(card.entry.voice.toString());
		//ファイルが存在しない場合は背景色をピンクに
		voice.addActionListener(e -> {
			Path path = Paths.get(voice.getText());
			if(path.toFile().exists()){
				voice.setBackground(Color.white);
				play.setEnabled(true);
			}
			else{
				voice.setBackground(Color.pink);
				play.setEnabled(false);
			}
		});
		
		play = new JButton("再生");
		play.setEnabled(card.isPlayable());
		play.addActionListener(e -> {
			new Thread(() -> {
			    try{
					Path path = Paths.get(voice.getText());
					new Player(new BufferedInputStream(new FileInputStream(path.toFile()))).play();
				} catch(Exception e1){
					System.out.println(e1);
				}
			}).start();
		});
		
		correct = new JButton("OK");
		correct.addActionListener(e -> {
			card.entry.word = spelling.getText();
			card.entry.meaning = meaning.getText();
			card.entry.comment = comment.getText();
			card.entry.level = Integer.parseInt(rank.getText());
			if(play.isEnabled()){
				card.entry.voice = Paths.get(voice.getText());
			}
			parent.refresh();
			dispose();
		});
		
		cancel = new JButton("CANCEL");
		cancel.addActionListener(e -> {
			dispose();
		});
		
		JPanel[] panel = new JPanel[6];
		panel[0] = new JPanel();
		panel[0].add(new JLabel("　　　     word"));
		panel[0].add(spelling);
		
		panel[1] = new JPanel();
		panel[1].add(new JLabel("　　  meaning"));
		panel[1].add(meaning);
		
		panel[2] = new JPanel();
		panel[2].add(new JLabel("　　comment"));
		panel[2].add(comment);
		
		panel[3] = new JPanel();
		panel[3].add(new JLabel("level"));
		panel[3].add(rank);
		
		panel[4] = new JPanel();
		panel[4].add(new JLabel("voice"));
		panel[4].add(voice);
		panel[4].add(play);
		
		panel[5] = new JPanel();
		panel[5].add(cancel);
		panel[5].add(correct);

		GridBagLayout layout = new GridBagLayout();
		getContentPane().setLayout(layout);
		for(int i = 0; i < panel.length; i++){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = i;
			layout.setConstraints(panel[i], gbc);
			getContentPane().add(panel[i]);
		}
		setVisible(true);
	}
}