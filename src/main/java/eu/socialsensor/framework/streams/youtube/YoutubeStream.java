package eu.socialsensor.framework.streams.youtube;



import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.retrievers.youtube.YoutubeRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;
/**
 * The stream that handles the configuration of the youtube wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */

public class YoutubeStream extends Stream {

	public static Source.Type SOURCE = Source.Type.Youtube;
	
	private Logger logger = Logger.getLogger(YoutubeStream.class);
	
	private String clientId;
	private String developerKey;
	private StreamConfiguration config;
	
	@Override
	public void close() throws StreamException {
		monitor.stopMonitor();
		logger.info("#YouTube : Close stream");
	}
	
	@Override
	public void open(StreamConfiguration config) throws StreamException {
		logger.info("#YouTube : Open stream");
		
		if (config == null) {
			logger.error("#YouTube : Config file is null.");
			return;
		}
		
		this.config = config;
		
		this.clientId = config.getParameter(CLIENT_ID);
		this.developerKey = config.getParameter(KEY);
		String maxResults = config.getParameter(MAX_RESULTS);
		String maxRequests = config.getParameter(MAX_REQUESTS);
		
		if (clientId == null || developerKey == null) {
			logger.error("#YouTube : Stream requires authentication.");
			throw new StreamException("Stream requires authentication");
		}

		this.retriever = new YoutubeRetriever(clientId, developerKey,Integer.parseInt(maxResults),Integer.parseInt(maxRequests));

	}
}
