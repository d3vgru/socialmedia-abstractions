package eu.socialsensor.framework.retrievers;


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
import com.google.api.client.util.Key;

import eu.socialsensor.framework.abstractions.TwitPicMediaItem;
import eu.socialsensor.framework.abstractions.TwitPicStreamUser;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.StreamUser;
/**
 * The retriever that implements the Twitpic simplified retriever
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitpicMediaRetriever implements MediaRetriever {

	private static String requestPrefix = "http://api.twitpic.com/2/media/show.json?id=";
	private static String userRequestPrefix = "http://api.twitpic.com/2/users/show.json?username=";
	
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
				TwitPicUser user = image.user;
				//System.out.println(username);
				//TwitPicUser user = retrieveUser(username);
				
				MediaItem mediaItem = new TwitPicMediaItem(image);
				if(user!=null) {
					StreamUser streamUser = new TwitPicStreamUser(user);
					mediaItem.setUser(streamUser);
					mediaItem.setStreamId(streamUser.getId());
				}
				return mediaItem;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return null;
	}

	public TwitPicUser retrieveUser(String id) {
		GenericUrl requestUrl = new GenericUrl(userRequestPrefix + id);
		
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(requestUrl);
			HttpResponse response = request.execute();
			TwitPicUser user = response.parseAs(TwitPicUser.class);
			return user;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Class that holds the information regarding the twitpic image
	 * @author manosetro
	 * @email  manosetro@iti.gr
	 */
	public class TwitPicUser {
		@Key
		public String id, username, name, bio, avatar_url, timestamp, location;
		@Key
		public int photo_count;
	}
	
	/**
	 * Class that holds the information regarding the twitpic image
	 * @author manosetro
	 * @email  manosetro@iti.gr
	 */
	public class TwitPicImage {
		@Key
		public String id, message, tags, short_id, type;
		@Key
		public int views, number_of_comments, height, width;
		@Key
		public String timestamp;
		@Key
		public String user_id, location;
		
		@Key
		public TwitPicUser user;
		
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
