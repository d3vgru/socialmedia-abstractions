package eu.socialsensor.framework.streams;


import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.TumblrRetriever;


/**
 * The stream that handles the configuration of the tumblr wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrStream extends Stream {
	
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.Tumblr;
	
	private String consumerKey;
	private String consumerSecret;
	
	private Logger logger = Logger.getLogger(TumblrStream.class);

	
	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#Tumblr : Open stream");
		
		if (config == null) {
			logger.error("#Tumblr : Config file is null.");
			return;
		}
		
		consumerKey = config.getParameter(KEY);
		consumerSecret = config.getParameter(SECRET);
		
		String maxResults = config.getParameter(MAX_RESULTS);
		String maxRequests = config.getParameter(MAX_REQUESTS);
		
		if (consumerKey == null || consumerSecret==null) {
			logger.error("#Tumblr : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		retriever = new TumblrRetriever(consumerKey,consumerSecret,Integer.parseInt(maxResults),Integer.parseInt(maxRequests));
		
	}

}
