

package eu.socialsensor.framework.streams.webfeeds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;

import com.ibm.hrl.syndicationHub.error.SyndErrorCodes;
import com.ibm.hrl.syndicationHub.hub.SyndicationHub;
import com.ibm.hrl.syndicationHub.hub.impl.SyndicationHubImpl;
import com.ibm.hrl.syndicationHub.monitor.schedule.Scheduler;
import com.ibm.hrl.syndicationHub.xml.transport.*;
import com.ibm.hrl.utils.logutils.LogUtils;

import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;


/**
 * The stream that handles the configuration for retrieving web feeds
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class WebFeedsStream extends Stream {

	public static final Source.Type SOURCE = Source.Type.WebFeeds;
	
	
	public static final String FEEDS_SEEDLIST = "FeedsSeedlist";
	public static final String MIN_SCHEDULE_PERIOD = "MinSchedulePeriod";
	
	public static final String HANDLER_HOST = "HandlerHost";
	public static final String HANDLER_PORT = "HandlerPort";
	
	public static final String DEFAULT_MIN_SCHEDULE_PERIOD = "60000";
	public static final String DEFAULT_HANDLER_HOST = "localhost";
	public static final String DEFAULT_HANDLER_PORT = "3281";
	
	private SyndicationHub proxy = null;
	private WebFeedsStreamNotificationClient client = null;
	
	public WebFeedsStream(){}


	@Override
	public synchronized void open(StreamConfiguration config) throws StreamException {
		
		if (proxy!= null){
			throw new StreamException("Stream is already open");
		}
		
		if (config == null) {
			return;
		}
	
		Properties props = new Properties();
		try{
			
			String minSchedulePeriod = config.getParameter(MIN_SCHEDULE_PERIOD, DEFAULT_MIN_SCHEDULE_PERIOD);
			String seedlist = config.getParameter(FEEDS_SEEDLIST);
			
			String handlerhost = config.getParameter(HANDLER_HOST, DEFAULT_HANDLER_HOST);
			int handlerPort = Integer.parseInt(config.getParameter(HANDLER_PORT, DEFAULT_HANDLER_PORT));

			client = new WebFeedsStreamNotificationClient(handlerPort, config.getHandler());
			client.open();
			
			//props.setProperty(SyndicationHub.DEFAULT_SUBSCRIBER_ID_PROP, "dummy");
			//props.setProperty(SyndicationHub.DEFAULT_SUBSCRIBER_DISK_DUMP_DIR_PROP, "feeds");
			
			props.setProperty(Scheduler.MIN_SCHEDULE_PERIOD_PROP, minSchedulePeriod);
			props.setProperty(LogUtils.LOG_LEVEL_PROP, Level.INFO.getName());
			
			proxy = new SyndicationHubImpl(props);
			
			if (seedlist == null || seedlist.trim().length() == 0){
				return;
			} 
			
			//register handler
			RegisterSubscriberRequest req1 = TransportFactory.eINSTANCE.createRegisterSubscriberRequest();
			req1.setHost(handlerhost);
			req1.setPort(handlerPort);
			req1.setSubscriberId("StreamHandler");
			
			RegisterSubscriberResponse res1 = proxy.registerSubscriber(req1);
			System.out.println("Got response:: "+res1+" error code:: "+SyndErrorCodes.getErrorMessage(res1.getErrorCode()));
			
			ArrayList<FeedDetails> feeds = readFeeds(seedlist);
			for (FeedDetails feed : feeds) {
				System.out.println("Registering feed:: "+feed.getFeedId());
				//Register feed
				RegisterFeedRequest req2 = TransportFactory.eINSTANCE.createRegisterFeedRequest();
				req2.setDescription("N/A");
				req2.setFeedId(feed.getFeedId());
				req2.setUrl(feed.getFeedUrl());
				
				RegisterFeedResponse res2 = proxy.registerFeed(req2);
				System.out.println("Got response:: "+res2+" error code:: "+SyndErrorCodes.getErrorMessage(res2.getErrorCode()));
				
				//Subscribe to feed
				SubscribeRequest req3 = TransportFactory.eINSTANCE.createSubscribeRequest();
				req3.setFeedId(feed.getFeedId());
				req3.setSubscriberId("dummy");
				
				SubscribeResponse res3 = proxy.subscribe(req3);
				System.out.println("Got response:: "+res3+" error code:: "+SyndErrorCodes.getErrorMessage(res3.getErrorCode()));
				
				//Subscribe handler to feed
				SubscribeRequest req4 = TransportFactory.eINSTANCE.createSubscribeRequest();
				req4.setFeedId(feed.getFeedId());
				req4.setSubscriberId("StreamHandler");
				
				SubscribeResponse res4 = proxy.subscribe(req4);
				System.out.println("Got response:: "+res4+" error code:: "+SyndErrorCodes.getErrorMessage(res4.getErrorCode()));
	
			}
		} catch(Exception e) {
			if (client != null) client.close();
			e.printStackTrace();
			System.out.println(e.getMessage());
			throw new StreamException("Error during stream open",e);
		}
		
	}
	
	
	@Override
	public synchronized void close() throws StreamException {
		if (proxy != null) {
			proxy.dispose();
			proxy = null;
		}
		if(client != null) {
			client.close();
			client = null;
		}
		
	}

	private ArrayList<FeedDetails> readFeeds(String filepath) throws IOException {
		BufferedReader in = null;
		ArrayList<FeedDetails> feeds = new ArrayList<FeedDetails>();
		StringTokenizer st;
		String nextLine;
		try {
			in = new BufferedReader(new FileReader(filepath));
			while ((nextLine = in.readLine()) != null){
				st = new StringTokenizer(nextLine+ ",", ",");
				feeds.add(new FeedDetails(st.nextToken(),st.nextToken()));
			}
			return feeds;
		} catch (IOException e) {
			throw e;
		}finally{
			if (in != null){
				in.close();
			}
		}
		
	}
	
	private class FeedDetails {
		private String feedId;
		private String feedUrl;
		
		
		/**
		 * @param feedId
		 * @param feedUrl
		 */
		public FeedDetails(String feedId, String feedUrl) {
			this.feedId = feedId;
			this.feedUrl = feedUrl;
		}
		
		
		/**
		 * @return Returns the feedId.
		 */
		public String getFeedId() {
			return feedId;
		}
		/**
		 * @param feedId The feedId to set.
		 */
//		public void setFeedId(String feedId) {
//			this.feedId = feedId;
//		}
		/**
		 * @return Returns the feedUrl.
		 */
		public String getFeedUrl() {
			return feedUrl;
		}
		/**
		 * @param feedUrl The feedUrl to set.
		 */
//		public void setFeedUrl(String feedUrl) {
//			this.feedUrl = feedUrl;
//		}
	}
	
	//test
	public static void main(String[] args) {
		StreamConfiguration config = new StreamConfiguration();
		config.setParameter(FEEDS_SEEDLIST, "conf/feeds.txt");
		
		Stream stream = new WebFeedsStream();
		try {
			stream.open(config);
		} catch (StreamException e) {
			e.printStackTrace();
		}
		
		Object wait = new Object();
		synchronized (wait) {
			try {
				wait.wait(600000);
			} catch (InterruptedException e) {}
		}
		
		try {
			stream.close();
		} catch (StreamException e) {
			e.printStackTrace();
		}
	}


	@Override
	public void search(Dysco dysco) throws StreamException {
		// Cannot Search Web Feeds
	}


}
