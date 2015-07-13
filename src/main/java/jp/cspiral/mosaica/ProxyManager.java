package jp.cspiral.mosaica;

import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 一定時間ごとにProxyを変更する
 *
 * @author tktk
 *
 */
public class ProxyManager extends TimerTask {

	private static final String[] PROXY_LIST = { "localhost",
		"10.0.10.229",
		"10.0.10.120",
		"10.0.10.75",
		"10.0.10.113",
		"10.0.10.114"
		};
	private static final String PROXY_PORT = "3128";
	private static final String HTTPS_PORT = "443";
	private long id;

	private int indexOfProxyList = 0;

	public ProxyManager() {
		Random rand = new Random();
		rand.setSeed(new Date().getTime());
		indexOfProxyList = rand.nextInt(PROXY_LIST.length);

		System.setProperty("http.proxyHost", PROXY_LIST[indexOfProxyList]);
		System.setProperty("http.proxyPort", PROXY_PORT);
		System.setProperty("https.proxyHost", PROXY_LIST[indexOfProxyList]);
		System.setProperty("https.proxyPort", HTTPS_PORT);
		System.out.println(id + " / proxy: " + PROXY_LIST[indexOfProxyList]);
	}

	/**
	 * 一定時間ごとに接続するプロキシを変更する
	 */
	@Override
	public void run() {
		changeProxy();
	}

	/**
	 * TimerTaskの開始
	 */
	public void timerTaskStart(int s) {
		if (s != 0) {
			Timer t = new Timer();
			t.schedule(this, s, s);
		}
	}

	/**
	 * TimerTaskの停止
	 */
	public void timerTaskStop() {
		this.cancel();
	}

	/**
	 * proxyを変更
	 *
	 * @author tomita
	 */
	private void changeProxy() {
		indexOfProxyList = (indexOfProxyList + 1) % PROXY_LIST.length;
		System.setProperty("http.proxyHost", PROXY_LIST[indexOfProxyList]);
		System.setProperty("https.proxyHost", PROXY_LIST[indexOfProxyList]);
		System.out.println(id + " / proxy: " + PROXY_LIST[indexOfProxyList]);
	}

	public void setId(long id) {
		this.id = id;
	}

}
