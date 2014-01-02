package eu.socialsensor.framework.retrievers.socialmedia;

import eu.socialsensor.framework.common.domain.MediaItem;

/**
 * The interface that represents the simplified 
 * media retriever for some of the wrappers
 * @author manosetro
 * @email  manosetro@iti.gr
 *
 */
public interface MediaRetriever {

	public MediaItem getMediaItem(String id);

}
