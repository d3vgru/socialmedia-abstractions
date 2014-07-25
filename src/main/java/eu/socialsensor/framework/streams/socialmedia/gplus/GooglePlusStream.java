package eu.socialsensor.framework.streams.socialmedia.gplus;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.socialmedia.gplus.GooglePlusRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * Class responsible for setting up the connection to Google API
 * for retrieving relevant Google+ content.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class GooglePlusStream extends Stream {
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.GooglePlus;
	
	private Logger logger = Logger.getLogger(GooglePlusStream.class);
	
	private String key;

	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#GooglePlus : Open stream");
		
		if (config == null) {
			logger.error("#GooglePlus : Config file is null.");
			return;
		}
		
		key = config.getParameter(KEY);
		
		String maxResults = config.getParameter(MAX_RESULTS);
		String maxRequests = config.getParameter(MAX_REQUESTS);
		String maxRunningTime = config.getParameter(MAX_RUNNING_TIME);
		
		if (key == null) {
			logger.error("#GooglePlus : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		retriever = new GooglePlusRetriever(key,Integer.parseInt(maxResults),Integer.parseInt(maxRequests),Long.parseLong(maxRunningTime),this);
		
	}
	
	@Override
	public String getName() {
		return "GooglePlus";
	}
}
