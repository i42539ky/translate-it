import com.amazonaws.services.polly.model.Voice;

public enum Language {
	AmEnglish("en-US"),
	German("de-DE"),
	French("fr-FR");
	
	private final String code;
	
	private Language(final String code) {
		this.code = code;
	}
	
	public String toCode() {
		return code;
	}
	
	public static Language toLanguage(String str) {
		for(Language x: Language.values()){
			if(str.equals(x.toCode())){
				return x;
			}
		}
		return null;
		
	}
}
