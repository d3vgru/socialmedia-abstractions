package eu.socialsensor.framework.subscribers;

import java.util.List;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.streams.StreamException;

public interface Subscriber {
	
	public void subscribe(List<Feed> feed) throws StreamException;
	
	public void stop();
	
}
