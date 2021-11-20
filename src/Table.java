import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
 *コンストラクタ
 * 	Table(): 0行0列のテーブルを宣言。
 * 	Table(int row, int col): 行数と列数を指定してテーブルを宣言。
 * 	Table(File file): ファイルを指定してテーブルを宣言。
 * 
 *テーブルの操作
 * 	String get(int row, int col): 指定した要素を取得。行、列の番号は0から。
 * 	void set(int row, int col, String str): 指定した要素を上書き。
 * 	
 *	int getRowCount(): 行数を取得。
 * 	int getColCount(): 列数を取得。
 * 
 * 	void setRowCount(int rowCount): 行数を変更。指定された列数が現在よりも大きい場合は空文字 "" で埋める。
 * 	void setColCount(int colCount):　列数を変更。同上。
 * 
 *ファイルへの書き出し
 *  void write(BufferedWriter writer): ストリームを指定して書き込む。
 */

//2014.06.18
//	・csvファイルの読み書き機能付きテーブル。文字コードはUTF-8。

//2016.11.04 更新
//	・read関数、write関数の引数を、ファイル名を表す文字列からFileクラスに変更
//	・添字のエラー時に投げる例外をExceptionからArrayIndexOutOfBoundsExceptionに変更
//	・行数、列数を操作する関数名をget/setCol, get/setRowからget/setcolCount, get/setRowCountに変更

//2017.06.25 更新
//	・read, write関数の引数をBufferedReader/Writerに変更
//	・コンストラクタの処理を若干変更。仕様はそのまま。

//2019.10.24 更新
//	・read関数を廃止; ストリームを指定する場合はコンストラクタを使用する
//	・setCol/RowCount関数に引数チェックを追加 (行/列数は0以上でなければならない）
//	・一部の関数名を変更

class Table{
	private ArrayList<ArrayList<String>> data;
	private int rowCount, colCount; //直接いじらない
	
	//空のテーブルを宣言
	public Table(){
		this(0, 0);
	}
	
	//行数と列数を指定して宣言
	public Table(int rowCount, int colCount){
		data = new ArrayList<ArrayList<String>>();
		setRowCount(rowCount);
		setColCount(colCount);
	}
	
	//ストリームを指定して宣言
	public Table(BufferedReader reader) throws IOException {
		data = new ArrayList<ArrayList<String>>();
		
		//読み込み
		String line; //読んだ行
		int maxColCount = 0; //各エントリのうち列数が最大のものの値
		
		//行単位で読み込み
		while((line = reader.readLine()) != null){
			ArrayList<String> entry = new ArrayList<String>();
			
			//有限オートマトンにより行をパース
			int state = 0; //状態変数 (0:初期状態orノンエスケープ 1:エスケープ 2:どっちか分からない)
			String buffer = ""; //書き出し用のバッファ
			line += ","; //番兵
			for(int i = 0; i < line.length(); i++){
				char c = line.charAt(i);
				if((state == 0 || state == 2) && c == ','){ //bufferを書き出し + クリア + 0に遷移
					entry.add(buffer);
					buffer = "";
					state = 0;
				}
				else if(state == 0 && c == '"'){ //1に遷移(エスケープ)
					state = 1;
				}
				else if(state == 1 && c == '"'){ //2に遷移
					state = 2;
				}
				else if(state == 2 && c == '"'){ //1に遷移(エスケープ) + bufferに追記
					buffer += c;
					state = 1;
				}
				else{ //bufferに追記
					buffer += c;
				}
			}
			data.add(entry);
			if(maxColCount < entry.size()){
				maxColCount = entry.size();
			}
		}
		setColCount(maxColCount);
		setRowCount(data.size());
	}
	
	//セルの読み書き
	public String get(int row, int col){
		if(row < 0 || row >= getRowCount()){
			throw new ArrayIndexOutOfBoundsException("The row number is out of the bound.");
		}
		if(col < 0 || col >= getColCount()){
			throw new ArrayIndexOutOfBoundsException("The col number is out of the bound.");
		}
		return data.get(row).get(col);
	}
	
	public void set(int row, int col, String str){
		if(row < 0 || row >= getRowCount()){
			throw new ArrayIndexOutOfBoundsException("The row number is out of the bound.");
		}
		if(col < 0 || col >= getColCount()){
			throw new ArrayIndexOutOfBoundsException("The col number is out of the bound.");
		}
		data.get(row).set(col, str);
	}
	
	//行数の取得、変更
	public int getRowCount(){
		return rowCount;
	}
	
	public void setRowCount(int rowCount){
		if(rowCount < 0){
			throw new ArrayIndexOutOfBoundsException("Number of rows must be 0 or positive integer.");
		}
		
		this.rowCount = rowCount;
		
		//dataの要素数がrowより大きい場合はクリアする
		while (data.size() > rowCount){
			data.remove(data.size() - 1);
		}
		
		//dataの要素数がrowより小さい場合は拡張を行う
		while(data.size() < rowCount){
			data.add(new ArrayList<String>());
			for(int j = 0; j < getColCount(); j++){
				data.get(data.size() - 1).add("");
			}
		}
	}
	
	//列数の取得、変更
	public int getColCount(){
		return colCount;
	}
	
	public void setColCount(int colCount){
		if(colCount < 0){
			throw new ArrayIndexOutOfBoundsException("Number of columns must be 0 or positive integer.");
		}
		
		this.colCount = colCount;
		
		for(int i = 0; i < getRowCount(); i++){
			//dataの各エントリの要素数がcolより小さい場合はクリアする
			while(data.get(i).size() > colCount){
				data.get(i).remove(data.get(i).size() - 1);
			}
			
			//dataの各エントリの要素数がcolより小さければ拡張を行う
			while(data.get(i).size() < colCount){
				data.get(i).add("");
			}
		}
	}
	
	//ストリームへ書き出し
	public void write(BufferedWriter writer) throws IOException{
		String dquote = "\"";
		
		for(int i = 0; i < getRowCount(); i++){
			for(int j = 0; j < getColCount(); j++){
				//コンマを含む -> ダブルクォートで括って書き出し
				if(get(i, j).contains(",") || get(i, j).contains(dquote)){
					writer.write(dquote + get(i, j).replaceAll(dquote, dquote + dquote) + dquote);
				}
				//そのまま書き出し
				else{
					writer.write(get(i, j).replaceAll(dquote, dquote + dquote));
				}
				//最後のデータ以外はコンマを出力
				if(j < getColCount() - 1){
					writer.write(",");
				}
			}
			writer.newLine();		
		}
	}
}