package eu.socialsensor.framework.streams.facebook;


import org.apache.log4j.Logger;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.retrievers.facebook.FacebookRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * The stream that handles the configuration of the facebook wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FacebookStream extends Stream {
	
	public static SocialNetworkSource SOURCE = SocialNetworkSource.Facebook;
	
	public int maxRequests = 600;
	public long minInterval = 600000;
	
	private Logger  logger = Logger.getLogger(FacebookStream.class);
	
	private FacebookClient facebookClient;
	
	private StreamConfiguration config;
	
	
	@Override
	public synchronized void open(StreamConfiguration config) throws StreamException {
		logger.info("#Facebook : Open stream");
		
		if (config == null) {
			logger.error("#Facebook : Config file is null.");
			return;
		}
		
		this.config = config;
		
		String access_token = config.getParameter(ACCESS_TOKEN);
		String maxResults = config.getParameter(MAX_RESULTS);
		
		if (access_token == null) {
			logger.error("#Facebook : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		facebookClient = new DefaultFacebookClient(access_token);
		retriever = new FacebookRetriever(facebookClient, maxRequests, minInterval,Integer.parseInt(maxResults));	

	}
	
	@Override
	public synchronized void close() {
		monitor.stopMonitor();
		logger.info("#Facebook : Close stream");
	}

}
