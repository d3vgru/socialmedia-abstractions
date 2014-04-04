package eu.socialsensor.framework.streams;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.InstagramRetriever;

/**
 * The stream that handles the configuration of the instagram wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */

public class InstagramStream extends Stream {
	
	private Logger logger = Logger.getLogger(InstagramStream.class);
	
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.Instagram;


	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#Instagram : Open stream");
		
		if (config == null) {
			logger.error("#Instagram : Config file is null.");
			return;
		}
		
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

