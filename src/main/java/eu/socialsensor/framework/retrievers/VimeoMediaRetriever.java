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

import eu.socialsensor.framework.abstractions.VimeoMediaItem;
import eu.socialsensor.framework.common.domain.MediaItem;
/**
 * The retriever that implements the Vimeo simplified retriever 
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class VimeoMediaRetriever implements MediaRetriever {

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	
	private HttpRequestFactory requestFactory;
	private String requestPrefix = "http://vimeo.com/api/v2/video/";
	
	public VimeoMediaRetriever() {
		requestFactory = HTTP_TRANSPORT.createRequestFactory(
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
					}
				});
	}
	
	public MediaItem getMediaItem(String id) {
	
		GenericUrl url = new GenericUrl(requestPrefix + id + ".json");
		
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(url);
			HttpResponse response = request.execute();
			VimeoVideo[] videos = response.parseAs(VimeoVideo[].class);
			if(videos != null && videos.length>0) {
				MediaItem mediaItem = new VimeoMediaItem(videos[0]);
				return mediaItem;
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
		 
		
	}
	
	/**
	 * Class that holds the information regarding the vimeo video
	 * @author manosetro
	 * @email  manosetro@iti.gr
	 */
	public class VimeoVideo {
		
		@Key
		public int id;
		@Key
		public String title, url, thumbnail_large, description, tags;
		@Key
		public int stats_number_of_comments, stats_number_of_likes, stats_number_of_plays;
		@Key
		public String upload_date;
		@Key
		public int user_id;
		@Key
		public int height, width;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VimeoMediaRetriever retriever = new VimeoMediaRetriever();

		MediaItem mediaItem = retriever.getMediaItem("13533846");
		System.out.println(mediaItem.toJSONString());
		
	}

}
