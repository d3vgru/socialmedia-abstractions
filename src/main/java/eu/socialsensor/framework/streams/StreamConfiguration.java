package eu.socialsensor.framework.streams;

import eu.socialsensor.framework.Configuration;

/**
 * The configuration of the streams that are going to be used
 * for the retrieval process

 */
public class StreamConfiguration extends Configuration {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8712318419026334808L;

	public static final String CLASS_PATH = "Classpath";
	
	private StreamHandler handler;
	
	public StreamHandler getHandler() {
		return handler;
	}
	
}
