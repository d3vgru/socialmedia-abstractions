package eu.socialsensor.framework.subscribers.socialmedia;

import java.util.List;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.streams.StreamException;
/**
 * The interface for retrieving content by subscribing to a social network channel.
 * Currently the only API that supports subscribing is Twitter API.
 * @author ailiakop
 *
 */
public interface Subscriber {
	/**
	 * Retrieves and stores relevant real-time content to a list of feeds by subscribing
	 * to a social network channel. 
	 * @param feed
	 * @throws StreamException
	 */
	public void subscribe(List<Feed> feed) throws StreamException;
	
	/**
	 * Stops the subscriber and all the processes affiliated with it. 
	 */
	public void stop();
	
}
