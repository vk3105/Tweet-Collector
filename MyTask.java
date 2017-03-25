import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TimerTask;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;

class MyTask extends TimerTask {

	private int i = 0;
	private int totalTweetsCount = 0;
	private long maxID = -1;
	private int CUR_TOPIC = 1;
	private int CUR_LANG = 1;
	private int CUR_DAY = 1;
	private HashMap<Long, String> hmap = new HashMap<Long, String>();

	public MyTask() {
		// Some stuffs
	}

	@Override
	public void run() {

		readMasterFiles();
		System.out.println("--------------START------------");
		System.out.println("Topic : " + Utils.SEARCH_STRING[CUR_TOPIC - 1][0] + " Lang : " + Utils.LANG[CUR_LANG - 1]
				+ " Day : " + CUR_DAY);
		System.out.println("MaxID : " + maxID + " Total Tweets : " + totalTweetsCount);

		if (Utils.IS_STREAMING) {
			twitterStreamingApi();
		} else {
			twitterRESTApi();
		}

		System.out.println(Utils.SEARCH_STRING[CUR_TOPIC - 1][0] + " " + Utils.LANG[CUR_LANG - 1] + " " + CUR_DAY);
		System.out.println("MaxID : " + maxID + " Total Tweets : " + totalTweetsCount);
		System.out.println("--------------END-------------");

		writeMasterFiles();
	}

	private void readMasterFiles() {
		Scanner fileScanner = null;
		try {
			fileScanner = new Scanner(new File(Utils.HOME_DIR + Utils.MASTER_FILE));
			CUR_TOPIC = fileScanner.nextInt();
			CUR_LANG = fileScanner.nextInt();
			CUR_DAY = fileScanner.nextInt();
			fileScanner.close();

			fileScanner = new Scanner(new File(Utils.HOME_DIR + CUR_TOPIC + "/" + CUR_LANG + "/" + Utils.MASTER_FILE));
			maxID = fileScanner.nextLong();
			totalTweetsCount = fileScanner.nextInt();
			if (totalTweetsCount < 100) {
				maxID = -1;
			}
			fileScanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fileScanner.close();
		}
	}

	private void writeMasterFiles() {

		boolean success1 = (new File(Utils.HOME_DIR + Utils.MASTER_FILE)).delete();
		boolean success2 = (new File(Utils.HOME_DIR + CUR_TOPIC + "/" + CUR_LANG + "/" + Utils.MASTER_FILE)).delete();

		writeSubMasterFile();
		if (CUR_TOPIC < 5) {
			if (CUR_LANG < 4) {
				CUR_LANG++;
			} else {
				CUR_TOPIC++;
				CUR_LANG = 1;
			}
		} else {
			if (CUR_LANG < 4) {
				CUR_LANG++;
			} else {
				CUR_DAY++;
				CUR_TOPIC = 1;
				CUR_LANG = 1;
				writeHomeMasterFile();
				System.exit(0);
			}
		}
		writeHomeMasterFile();
		System.out.println("Writing Master Files Done");
	}

	private void writeSubMasterFile() {
		try {
			FileWriter fstream1 = new FileWriter(Utils.HOME_DIR + CUR_TOPIC + "/" + CUR_LANG + "/" + Utils.MASTER_FILE,
					true);
			BufferedWriter out1 = new BufferedWriter(fstream1);
			out1.write(String.valueOf(maxID));
			out1.newLine();
			out1.write(String.valueOf(totalTweetsCount));
			out1.close();
			fstream1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeHomeMasterFile() {
		FileWriter fstream;
		try {
			fstream = new FileWriter(Utils.HOME_DIR + Utils.MASTER_FILE, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(String.valueOf(CUR_TOPIC));
			out.newLine();
			out.write(String.valueOf(CUR_LANG));
			out.newLine();
			out.write(String.valueOf(CUR_DAY));
			out.close();
			fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void twitterRESTApi() {

		TwitterFactory tf = new TwitterFactory(Utils.getConfigurationBuilder().build());
		Twitter twitter = tf.getInstance();
		BufferedWriter output = null;

		try {

			Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("search");
			RateLimitStatus searchTweetsRateLimit = rateLimitStatus.get("/search/tweets");

			File file = new File(Utils.HOME_DIR + CUR_TOPIC + "/" + CUR_LANG + "/" + CUR_DAY + ".json");

			output = new BufferedWriter(new FileWriter(file));

			for (int queryNumber = 0; queryNumber < Utils.MAX_QUERIES; queryNumber++) {

				if (searchTweetsRateLimit.getRemaining() == 0) {
					System.out.println("Resets in : " + searchTweetsRateLimit.getResetTimeInSeconds());
					System.out.println("Remaining : " + searchTweetsRateLimit.getRemaining());
					Thread.sleep((searchTweetsRateLimit.getSecondsUntilReset() + 2) * 1000l);
				}

				String queryString = Utils.queryStringPreprocessing(Utils.SEARCH_STRING[CUR_TOPIC - 1]);

				// Query query = new Query(Utils.SEARCH_STRING_IPHONE[3]+" OR
				// "+Utils.SEARCH_STRING_IPHONE[1]+" +exclude:retweets");
				Query query = new Query(queryString);
				query.setCount(Utils.TWEETS_PER_QUERY);
				//query.setResultType(ResultType.recent);
				query.setLang(Utils.LANG[CUR_LANG - 1]);

				if (maxID != -1) {
					query.setMaxId(maxID - 1);
				}

				QueryResult queryResult = twitter.search(query);

				if (queryResult.getTweets().size() == 0) {
					break;
				}

				for (Status s : queryResult.getTweets()) {

					if (maxID == -1 || s.getId() < maxID) {
						maxID = s.getId();
					}

					try {
						long id = s.getId();
						if (hmap.containsKey((Long) id) == false) {
							hmap.put(id, "");
							String message = TwitterObjectFactory.getRawJSON(s);
							output.write(message);
							output.newLine();
							totalTweetsCount++;
							//System.out.println(s.getText());
						} else {
							System.out.println("Duplicate");
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void twitterStreamingApi() {
		TwitterStream stream = Utils.getStream();

		StatusListener listener = new StatusListener() {

			@Override
			public void onException(Exception e) {
				System.out.println("Exception occured:" + e.getMessage());
				e.printStackTrace();
			}

			@Override
			public void onTrackLimitationNotice(int n) {
				System.out.println("Track limitation notice for " + n);
				stream.shutdown();
			}

			@Override
			public void onStatus(Status status) {
				i++;
				String message = TwitterObjectFactory.getRawJSON(status);
				System.out.println(message);
				/*
				 * // gets Username String username =
				 * status.getUser().getScreenName();
				 * System.out.println(username); String profileLocation =
				 * user.getLocation(); System.out.println(profileLocation); long
				 * tweetId = status.getId(); System.out.println(tweetId); String
				 * content = status.getText(); System.out.println(content +
				 * "\n");
				 */
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
				System.out.println("Stall warning");
			}

			@Override
			public void onScrubGeo(long arg0, long arg1) {
				System.out.println("Scrub geo with:" + arg0 + ":" + arg1);
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice arg0) {
				System.out.println("Status deletion notice");
			}
		};

		FilterQuery qry = new FilterQuery();
		String[] keywords = Utils.SEARCH_STRING_SYRIA;

		qry.track(keywords);
		qry.language(Utils.LANG_ENGLISH);

		stream.addListener(listener);
		stream.filter(qry);
	}

}