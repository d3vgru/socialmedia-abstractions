
package eu.socialsensor.framework.streams;

import org.apache.log4j.Logger;

import eu.socialsensor.framework.common.domain.Item;

/**
 * Handles updates that come from a stream.
 * 
 */
public interface StreamHandler {

	public final Logger logger = Logger.getLogger(StreamHandler.class);
	
	/**
	 * Deliver a single update to handler
	 * @param update
	 *         Stream update
	 */
	public void update(Item update);
	

	/**
	 * Deliver a batch of updates to handler
	 * @param updates
	 *         Stream updates
	 */
	public void updates(Item[] updates);
	
	
	/**
	 * Deliver a single update deletion to handler
	 * @param update
	 *         Stream update to delete
	 */
	public void delete(Item update);
	

	/**
	 * Deliver error to handler
	 * @param err
	 *         Stream error
	 */
	public void error(StreamError error);
}
