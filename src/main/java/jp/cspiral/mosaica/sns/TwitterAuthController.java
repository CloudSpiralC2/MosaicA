package jp.cspiral.mosaica.sns;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import jp.cspiral.mosaica.ImageController;
import jp.cspiral.mosaica.util.MosaicALogger;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.mongodb.MongoException;
/**
 * Twitterの認証，モザイク画像の投稿を行うクラス
 * @author tktk
 *
 */
public class TwitterAuthController {

	private Configuration conf;

	public TwitterAuthController() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
	 	  .setOAuthConsumerKey("GhTiizSWeNcsFIlLsGlm64url")
		  .setOAuthConsumerSecret("rdESnp71GYczKuQLKl3GEGjivRScbwktUs6onhWk5f7pS5IMJT");
		conf = cb.build(); // 設定の生成
	}

	/**
	 * リクエストトークンを生成し，認証画面のURLを返すメソッド
	 * @return
	 */
	public String requestToken(HttpSession session, String imageId) {
		try {
			OAuthAuthorization oauth = new OAuthAuthorization(conf); // 設定をもとにoauthインスタンスを作成
			String callbackURL = "http://52.68.162.198:8080/MosaicA/api/twitterUpdate"; // コールバック先
			RequestToken oAuthRequestToken = oauth
					.getOAuthRequestToken(callbackURL); // callbackURLをリクエストトークンを付加
			session.setAttribute("requestToken", oAuthRequestToken); // sessionにリクエストトークンを保存
			session.setAttribute("imageId", imageId); // sessionにImageIdを保存
			return oAuthRequestToken.getAuthenticationURL(); // 認証のためのURLを返す

		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * リクエストトークンを使用し，認証情報からアクセストークンを取得する
	 * @param session
	 * @param imageId
	 * @return
	 */
	public AccessToken getAccessToken(String oAuthToken, String oAuthVerifier, HttpSession session){
		try {
		RequestToken requestToken = (RequestToken) session.getAttribute("requestToken");

		// セッションからrequestTokenとrequestTokenSecretを取り出し
		AccessToken accessToken = new AccessToken(requestToken.getToken(), requestToken.getTokenSecret());

		// リクエスト時の設定まで復元
		OAuthAuthorization oauth = new OAuthAuthorization(conf);
		oauth.setOAuthAccessToken(accessToken);

		// 真のアクセストークンゲット
		accessToken = oauth.getOAuthAccessToken(oAuthVerifier);

		return accessToken;

		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * アクセストークンを使用してモザイク画像を投稿するメソッド
	 * @param accessToken
	 * @param imageId
	 * @return
	 */
	public String uploadImage(AccessToken accessToken, String imageId) {
		// ロギング用
		Map<String, Object> log = new HashMap<String, Object>();
		log.put("API", "twitterUpdate");
		log.put("imageid", imageId);
		try {
			// twitterつぶやきready
			TwitterFactory factory = new TwitterFactory(conf);
			Twitter twitter = factory.getInstance(accessToken);

			// 画像をInputStreamの形にする
			ImageController ic = new ImageController();
			String imageString = ic.saveImage(imageId);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(ic.decode(imageString), "jpg", baos);
			InputStream is = new ByteArrayInputStream(baos.toByteArray());

			// 画像とテキストををupdate
			StatusUpdate status = new StatusUpdate("MosaicAでモザイクアートを作りました！ http://hogehoge?" + imageId);
			status.setMedia(imageId, is);
			Status s = twitter.updateStatus(status);

			MosaicALogger.getInstance().getLogger().log("info", log);
			return "つぶやきました: " + s.getText();

		} catch (TwitterException e) {
			e.printStackTrace();
			log.put("message", e.toString());
			MosaicALogger.getInstance().getLogger().log("error", log);
			return "Twitter Error: " + e.toString();
		} catch(MongoException e) {
			log.put("message", e.toString());
			MosaicALogger.getInstance().getLogger().log("error", log);
			return "Image not found.";
		} catch (IOException e) {
			e.printStackTrace();
			log.put("message", e.toString());
			MosaicALogger.getInstance().getLogger().log("error", log);
			return "IO Exeption: " + e.toString();
		}
	}
}
