///////////////////////////////////////////////////////////////////////////
// 
// IBM Confidential
// OCO Source Materials
// (c) Copyright IBM Corp. 2012
// 
// The source code for this program is not published or otherwise divested of 
// its trade secrets, irrespective of what has been deposited with
// the U.S. Copyright Office.
// 
///////////////////////////////////////////////////////////////////////////

package eu.socialsensor.framework.streams.webfeeds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.hrl.syndicationHub.io.StringInputStream;
import com.ibm.hrl.syndicationHub.net.notification.Notifiable;
import com.ibm.hrl.syndicationHub.net.notification.NotificationServiceClient;
import com.ibm.hrl.syndicationHub.net.notification.NotificationWrapper;
import com.ibm.hrl.syndicationHub.xml.beans.Notification;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.streams.StreamError;
import eu.socialsensor.framework.streams.StreamHandler;
import eu.socialsensor.framework.abstractions.webfeeds.WebFeedStreamItem;
/*
 * A client that listens on socket for feed notifications
 */
class WebFeedsStreamNotificationClient implements Notifiable{
	
	private NotificationServiceClient client = null;
	private StreamHandler handler = null;
	
	public WebFeedsStreamNotificationClient(int port, StreamHandler handler) throws IOException{
		client = new NotificationServiceClient(this, port);
		this.handler = handler;
	}
	
	public void open(){
		client.start();
	}
	
	public void close(){
		client.dispose();
	}

	@SuppressWarnings("all")
	@Override
	public void getNotified(NotificationWrapper wrapper) {
		try {
			if (handler != null) {
				
				Notification notification = wrapper.getNotification();
				String content = notification.getFeedContent();
				StringInputStream ins = new StringInputStream(content);
				SyndFeedInput syndIn = new SyndFeedInput();
				SyndFeed feeds = null;
				XmlReader reader = null;
				reader = new XmlReader(ins);
				feeds = syndIn.build(reader);
				reader.close();
				
				List<SyndEntry> entries = feeds.getEntries();
				
				if (entries != null) {
					List<Item> updates = new ArrayList<Item>();
					Iterator<SyndEntry> it = entries.iterator();
					while (it.hasNext()) {				
						Item item = new WebFeedStreamItem(it.next());						
						updates.add(item);
					}
					handler.updates(updates.toArray(new Item[updates.size()]));	
				}
				
			}
		} catch (Exception e) {
			if (handler != null){
				handler.error(new StreamError("Error during feed notification",e));
			}
		} 
	}
              
}
