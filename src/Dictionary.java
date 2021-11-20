import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class Dictionary {
	Table table;
	
	public Dictionary(Path path) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path.toFile()), "UTF-8"));
		table = new Table(reader);
		reader.close();
		
		if(table.getColCount() != 2) {
			throw new Exception("illegal format (col count is not 2)");
		}
		
		if(!table.get(0, 0).equals("key") || !table.get(0, 1).equals("value")){
			throw new Exception("illegal format (unexpected col names)");
		}
	}
	
	public String getValue(String key) throws Exception{
		for(int i = 0; i < table.getRowCount(); i++) {
			if(table.get(i, 0).equals(key)) {
				return table.get(i, 1);
			}
		}
		throw new Exception("key not found");
	}
}
