import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

public class Polly {
	static AmazonPolly polly;
	
	List<Voice> voices;
	Random random;
	
	public static void init() {
		new Thread(() ->{
			System.setProperty("aws.accessKeyId", private_info.ACCESS_KEY);
			System.setProperty("aws.secretKey", private_info.SECRET_KEY);
			polly = AmazonPollyClientBuilder.standard().withCredentials(null).withRegion(Regions.US_WEST_1).build();
		}).start();
	}
	
	public Polly(Language language) {
		new Thread(() ->{
			String code = language.toCode();
			DescribeVoicesRequest request = new DescribeVoicesRequest().withLanguageCode(code);
			voices = polly.describeVoices(request).getVoices();
			random = new Random();
		}).start();
	}
	
	public void synthesize(String text, Path target) {
		System.out.println("synthesizing [" + text + "] with Polly...");
		SynthesizeSpeechRequest request = new SynthesizeSpeechRequest();
    	request.setText(text);
    	request.setVoiceId(voices.get(random.nextInt(voices.size())).getId());
    	request.setOutputFormat(OutputFormat.Mp3);
    	
    	SynthesizeSpeechResult result = polly.synthesizeSpeech( request );
    	
    	try {
			InputStream from = result.getAudioStream();
	        FileOutputStream to = new FileOutputStream(target.toFile());
	        byte[] buffer = new byte[ 1024 ];
	        int readBytes;
	        while(( readBytes = from.read( buffer )) > 0 ){
	        	to.write(buffer, 0,  readBytes);
	        }
	        from.close();
	        to.close();
	        System.out.println("done.");
    	}catch(Exception e) {
    		System.out.println(e);
    	}
	}
}
