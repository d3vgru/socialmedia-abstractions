
package eu.socialsensor.framework.streams.socialmedia.twitter;

import org.apache.log4j.Logger;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.retrievers.socialmedia.twitter.TwitterRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;
import eu.socialsensor.framework.subscribers.socialmedia.twitter.TwitterSubscriber;

/**
 * The stream that handles the configuration of the twitter wrapper
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitterStream extends Stream {
	
	public static SocialNetworkSource SOURCE = SocialNetworkSource.Twitter;
	
	private Logger  logger = Logger.getLogger(TwitterStream.class);

	@Override
	public synchronized void open(StreamConfiguration config) throws StreamException {

		logger.info("#Twitter : Open stream");
		
		if (config == null) {
			logger.error("#Twitter : Config file is null.");
			return;
		}
		
		String oAuthConsumerKey 		= 	config.getParameter(KEY);
		String oAuthConsumerSecret 		= 	config.getParameter(SECRET);
		String oAuthAccessToken 		= 	config.getParameter(ACCESS_TOKEN);
		String oAuthAccessTokenSecret 	= 	config.getParameter(ACCESS_TOKEN_SECRET);
		
		if (oAuthConsumerKey == null || oAuthConsumerSecret == null ||
				oAuthAccessToken == null || oAuthAccessTokenSecret == null) {
			logger.error("#Twitter : Stream requires authentication");
			throw new StreamException("Stream requires authentication");
		}
		
		logger.info("Twitter Credentials: \n" + 
				"oAuthConsumerKey:  " + oAuthConsumerKey  + "\n" +
				"oAuthConsumerSecret:  " + oAuthConsumerSecret  + "\n" +
				"oAuthAccessToken:  " + oAuthAccessToken + "\n" +
				"oAuthAccessTokenSecret:  " + oAuthAccessTokenSecret);
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true)
			.setOAuthConsumerKey(oAuthConsumerKey)
			.setOAuthConsumerSecret(oAuthConsumerSecret)
			.setOAuthAccessToken(oAuthAccessToken)
			.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		Configuration conf = cb.build();
		
		if(isSubscriber) {
			logger.info("Initialize Twitter Subscriber");
			subscriber = new TwitterSubscriber(conf, this);
		}
		else {
			logger.info("Initialize Twitter Retriever for REST api");
			
			String maxRequests = config.getParameter(MAX_REQUESTS);
			String maxResults = config.getParameter(MAX_RESULTS);
			
			retriever = new TwitterRetriever(conf, this, Integer.parseInt(maxRequests), Integer.parseInt(maxResults));
		}
			

		/*logger.info("Start filtering.");
		filter(filter);
		
		List<Keyword> keywords = new ArrayList<Keyword>();
		for(String keyword : filter.keywords())
			keywords.add(new Keyword(keyword, 0));
		
		crawlerSpecsDAO.setKeywords(keywords , Source.Type.Twitter);
		Thread updater = new Thread(new CrawlerSpecsUpdate(crawlerSpecsDAO));
		updater.start();*/
		
	}



//	public class CrawlerSpecsUpdate implements Runnable {
//		
//		private Logger  LOG = Logger.getLogger(CrawlerSpecsUpdate.class);
//		
//		CrawlerSpecsDAO crawlerSpecs = null;
//		long update_period = 3000;
//		
//		public CrawlerSpecsUpdate(CrawlerSpecsDAO crawlerSpecs) {
//			this.crawlerSpecs = crawlerSpecs;
//		}
//		
//		@Override
//		public void run() {
//			while(true) {
//				try {
//					Thread.sleep(update_period);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					continue;
//				}
//				
//				List<Keyword> keywords = crawlerSpecs.getTopKeywords(10, SOURCE);
//				System.out.println("=======UPDATE CRAWLER SPECS=====");
//				for(Keyword keyword : keywords) {
//					System.out.println(keyword.toJSONString());
//				}
//				System.out.println("================================");
//
//				List<String> filter_keywords = new ArrayList<String>(keywords.size());
//				for(Keyword keyword : keywords) {
//					filter_keywords.add(keyword.getName());
//				}
////				
////				List<Long> follows = new ArrayList<Long>(topKeywords.size());
////				for(String source : topSources) {
////					follows.add(Long.parseLong(source));
////				}
//				
//			
//			}
//		}
//		
//	}

	
}

