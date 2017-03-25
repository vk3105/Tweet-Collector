import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Utils {

	private static final String CONSUMER_KEY = "Put your CONSUMER_KEY";
	private static final String CONSUMER_KEY_SECRET = "Put your CONSUMER_KEY_SECRET";
	private static final String ACCESS_TOKEN = "Put your ACCESS_TOKEN";
	private static final String ACCESS_TOKEN_SECRET = "Put your ACCESS_TOKEN_SECRET";
	public static final String LANG_ENGLISH = "en";
	public static final String LANG_TURKISH = "tr";
	public static final String LANG_SPANISH = "es";
	public static final String LANG_KOREAN = "ko";
	public static final String MASTER_FILE = "master.txt";
	public static final String HOME_DIR = "/home/infinity/Data/Syria/";

	public static final String[] SEARCH_STRING_IPHONE = { "아이폰" };

	public static final String[] SEARCH_STRING_SYRIA = { "syria", "@cnnbrk" };
	/*
	 * public static final String[] SEARCH_STRING_SYRIA = { "syria", "Syria",
	 * "#syria", "#Syria", "Suriye", "Syrie", "#Syrie", "#syrie", "Assad",
	 * "Aleppo", "@ITV", "@BBC", "@SkyNews", "@Fox",
	 * "@ABC","@RT_com","@BBCBreaking","@nytimes" };
	 */

	public static final String[] SEARCH_STRING_USPRESIDENT = { "election2016", "#election2016", "Election2016",
			"#Election2016", "Presidential Election", "presidential election", "presidentialelection",
			"#presidentialelection", "#presidentialelection", "Hillary", "Trump", "DonaldTrumph", "#DonaldTrumph",
			"HillaryClinton", "#HillaryClinton", "#Election2016", "#PresidentialDebates", "#GOPDebate", "#DemDebate",
			"#Nonpartisan", "#2016EC" };

	public static final String[] SEARCH_STRING_USOPEN = { "usopen", "USOPEN", "US OPEN", "US open", "US Open", "USOpen",
			"#usopen", "#USOPEN", "#USOpen" };

	public static final String[] SEARCH_STRING_GOT = { "Game of Thrones" };

	public static final String[][] SEARCH_STRING = { SEARCH_STRING_SYRIA, SEARCH_STRING_IPHONE, SEARCH_STRING_GOT,
			SEARCH_STRING_USPRESIDENT, SEARCH_STRING_USOPEN };

	public static final String[] LANG = { LANG_ENGLISH, LANG_TURKISH, LANG_KOREAN, LANG_SPANISH };

	public static final boolean IS_STREAMING = false;

	public static final int TWEETS_PER_QUERY = 100;
	public static final int MAX_QUERIES = 180;

	public static ConfigurationBuilder getConfigurationBuilder() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(CONSUMER_KEY).setOAuthConsumerSecret(CONSUMER_KEY_SECRET)
				.setOAuthAccessToken(ACCESS_TOKEN).setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET)
				.setJSONStoreEnabled(true);
		return cb;
	}

	public static TwitterStream getStream() {

		return new TwitterStreamFactory(getConfigurationBuilder().build()).getInstance();
	}

	public static String URLEncodedString(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}

	public static String URLDecodedString(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}

	public static String cleanText(String text) {
		text = text.replace("\n", "\\n");
		text = text.replace("\t", "\\t");

		return text;
	}

	public static String queryStringPreprocessing(String[] strArr) {
		String queryString = "";
		for (int i = 0; i < strArr.length; i++) {
			if (i == 0) {
				queryString += strArr[i];
			} else {
				queryString += (" AND " + strArr[i]);
			}
		}
		queryString += " +exclude:retweets";
		return queryString;
		// return URLEncodedString(queryString);
	}

}
