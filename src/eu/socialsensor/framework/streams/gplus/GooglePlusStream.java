package eu.socialsensor.framework.streams.gplus;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.monitors.FeedsMonitor;
import eu.socialsensor.framework.retrievers.gplus.GooglePlusRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * The stream that handles the configuration of the google plus wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */

public class GooglePlusStream extends Stream {
	public static final Source.Type SOURCE = Source.Type.GooglePlus;

	
	private Logger logger = Logger.getLogger(GooglePlusStream.class);
	private FeedsMonitor monitor;
	private StreamConfiguration config;
	
	private String key;
	
	
	@Override
	public void close() throws StreamException {
		monitor.stopMonitor();
		logger.info("#GooglePlus : Close stream");
	}
	

	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#GooglePlus : Open stream");
		
		if (config == null) {
			logger.error("#GooglePlus : Config file is null.");
			return;
		}
		
		this.config = config;
		
		key = config.getParameter(KEY);
		String maxResults = config.getParameter(MAX_RESULTS);
		String maxRequests = config.getParameter(MAX_REQUESTS);
		
		if (key == null) {
			logger.error("#GooglePlus : Stream requires authentication.");
			throw new StreamException("Stream requires authentication.");
		}
		
		this.retriever = new GooglePlusRetriever(key,Integer.parseInt(maxResults),Integer.parseInt(maxRequests));
		
	}

}
