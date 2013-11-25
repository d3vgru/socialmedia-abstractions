package eu.socialsensor.framework.streams.instagram;



import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.monitors.FeedsMonitor;
import eu.socialsensor.framework.retrievers.instagram.InstagramRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * The stream that handles the configuration of the instagram wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */

public class InstagramStream extends Stream {
	
	private Logger logger = Logger.getLogger(InstagramStream.class);
	
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.Instagram;

	private FeedsMonitor monitor;
	
	private StreamConfiguration config;
	
	@Override
	public void close() throws StreamException {
		monitor.stopMonitor();
		logger.info("#Instagram : Close stream");
	}

	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#Instagram : Open stream");
		
		if (config == null) {
			logger.error("#Instagram : Config file is null.");
			return;
		}
		
		this.config = config;
		
		String key = config.getParameter(KEY);
		String secret = config.getParameter(SECRET);
		String token = config.getParameter(ACCESS_TOKEN);
		String maxResults = config.getParameter(MAX_RESULTS);
		String maxRequests = config.getParameter(MAX_REQUESTS);
		
		if (key == null || secret == null || token == null) {
			logger.error("#Instagram : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		retriever = new InstagramRetriever(secret, token,Integer.parseInt(maxResults),Integer.parseInt(maxRequests));
	
	}
	
}

