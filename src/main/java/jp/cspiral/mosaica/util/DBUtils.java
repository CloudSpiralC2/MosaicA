package jp.cspiral.mosaica.util;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Mongodbのコネクション管理クラス
 * DB接続へのシングルトン管理を行う．
 *
 * @author shin
 *
 */
@SuppressWarnings("unused")
public class DBUtils {
	public static DBUtils instance = new DBUtils();

	private DB db;

	private final String dbName = new String("mosaica");
	private final int PORT = 27017;
	private final String IP_LOCALHOST = "127.0.0.1";
	private final String IP_SERVER = "133.30.159.3";
	private final String USERNAME = "";
	private final String PASSWORD = "";

	private DBUtils() {
		connectLocalMongo();
		//connectServerMongo();
	}

	public static DBUtils getInstance() {
		return DBUtils.instance;
	}


	/**
	 * ローカル開発用
	 */
	private void connectLocalMongo() {
		db = null;
		try {
			Mongo m = new Mongo(IP_LOCALHOST, PORT);
			db = m.getDB(dbName);
		} catch (UnknownHostException | MongoException e) {
			e.printStackTrace();
		}
	}

	/**
	 * サーバ開発用
	 */
	private void connectServerMongo() {
		db = null;
		try {
			Mongo m = new Mongo(IP_SERVER, PORT);
			db = m.getDB(dbName);
			db.authenticate(USERNAME, PASSWORD.toCharArray());
		} catch (UnknownHostException | MongoException e) {
			e.printStackTrace();
		}
	}

	public DB getDb() {
		return this.db;
	}

}
