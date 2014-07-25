package eu.socialsensor.framework.streams.socialmedia.topsy;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.socialmedia.topsy.TopsyRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * Class responsible for setting up the connection to Topsy API
 * for retrieving relevant Tospy content.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TopsyStream extends Stream{
	private Logger logger = Logger.getLogger(TopsyStream.class);
	
	public static final SocialNetworkSource SOURCE = SocialNetworkSource.Topsy;
	
	private String key;
	
	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#Topsy : Open stream");
		
		if (config == null) {
			logger.error("#Topsy : Config file is null.");
			return;
		}
		
		key = config.getParameter(KEY);
		
		if (key == null) {
			logger.error("#Topsy : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		retriever = new TopsyRetriever(key,this);
		
	}
	
	@Override
	public String getName() {
		return "Topsy";
	}
}
