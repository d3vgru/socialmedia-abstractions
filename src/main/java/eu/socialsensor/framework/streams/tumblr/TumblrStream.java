package eu.socialsensor.framework.streams.tumblr;


import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.monitors.FeedsMonitor;
import eu.socialsensor.framework.retrievers.tumblr.TumblrRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;


/**
 * The stream that handles the configuration of the tumblr wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrStream extends Stream {
	
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.Tumblr;
	
	private FeedsMonitor monitor;
	private StreamConfiguration config;
	private String consumerKey;
	private String consumerSecret;
	
	private Logger logger = Logger.getLogger(TumblrStream.class);
	
	@Override
	public void close() throws StreamException {
		monitor.stopMonitor();
		logger.info("#Tumblr : Close stream");
	}
	
	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#Tumblr : Open stream");
		
		if (config == null) {
			logger.error("#Tumblr : Config file is null.");
			return;
		}
		
		this.config = config;
		
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
