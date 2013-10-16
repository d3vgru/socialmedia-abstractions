package eu.socialsensor.framework.abstractions.dailymotion;

import com.google.api.client.util.Key;

/** Represents a daily motion video. */
public class DailyMotionVideo {
	@Key
	public String id, title, url, embed_url, thumbnail_url;
	@Key
	public String[] tags;
	@Key
	public int rating, ratings_total, views_total, comments_total;
	@Key
	public long created_time;
	@Key
	public double[] geoloc;
}