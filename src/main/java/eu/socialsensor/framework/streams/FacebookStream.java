package eu.socialsensor.framework.streams;


import org.apache.log4j.Logger;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.FacebookRetriever;

/**
 * The stream that handles the configuration of the facebook wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FacebookStream extends Stream {
	
	public static SocialNetworkSource SOURCE = SocialNetworkSource.Facebook;
	
	public int maxFBRequests = 600;
	public long minInterval = 600000;
	
	private Logger  logger = Logger.getLogger(FacebookStream.class);
	
	private FacebookClient facebookClient;
	
	
	@Override
	public synchronized void open(StreamConfiguration config) throws StreamException {
		logger.info("#Facebook : Open stream");
		
		if (config == null) {
			logger.error("#Facebook : Config file is null.");
			return;
		}
		
		
		String access_token = config.getParameter(ACCESS_TOKEN);
		int maxResults = Integer.parseInt(config.getParameter(MAX_RESULTS));
		int maxRequests = Integer.parseInt(config.getParameter(MAX_REQUESTS));
		
		if(maxRequests > maxFBRequests)
			maxRequests = maxFBRequests;   
		
		if (access_token == null) {
			logger.error("#Facebook : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		facebookClient = new DefaultFacebookClient(access_token);
		retriever = new FacebookRetriever(facebookClient, maxRequests, minInterval,maxResults);	

	}

}
