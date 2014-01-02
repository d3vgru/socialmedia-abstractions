package eu.socialsensor.framework.retrievers.socialmedia.twitpic;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import eu.socialsensor.framework.abstractions.socialmedia.twitpic.TwitPicImage;
import eu.socialsensor.framework.abstractions.socialmedia.twitpic.TwitPicMediaItem;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.retrievers.socialmedia.MediaRetriever;

/**
 * The retriever that implements the Twitpic simplified retriever
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitpicMediaRetriever implements MediaRetriever {

	private static String requestPrefix = "http://api.twitpic.com/2/media/show.json?id=";
	
	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private HttpRequestFactory requestFactory;

	public TwitpicMediaRetriever() {
		requestFactory = HTTP_TRANSPORT.createRequestFactory(
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});
	}
	
	public MediaItem getMediaItem(String shortId) {
		
		GenericUrl requestUrl = new GenericUrl(requestPrefix + shortId);
		
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(requestUrl);
			HttpResponse response = request.execute();
			TwitPicImage image = response.parseAs(TwitPicImage.class);
			if(image != null) {
				MediaItem mediaItem = new TwitPicMediaItem(image);
				return mediaItem;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return null;
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TwitpicMediaRetriever retriever = new TwitpicMediaRetriever();

		MediaItem mediaItem = retriever.getMediaItem("d255om");
		if(mediaItem != null) {
			System.out.println(mediaItem.toJSONString());
		}
		
	}

}
