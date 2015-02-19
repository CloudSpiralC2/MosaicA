package jp.cspiral.mosaica;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Date;

import javax.imageio.ImageIO;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GoogleController {
	// User-Agentを指定しないと変なページに飛ばされる
	public static final String USERAGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0";

	private static final String[] PROXY_LIST = {
		"localhost",
		"10.0.10.135",
		};
	private static final String PROXY_PORT = "3128";

	private int indexOfProxyList = 0;

	// 連続アクセス回数
	private static final int MAX_ACCESS_COUNT = 10;

	/**
	 * コンストラクタ
	 *
	 * プロキシの初期設定
	 */
	GoogleController() {
		System.setProperty("http.proxyHost", PROXY_LIST[indexOfProxyList]);
        System.setProperty("http.proxyPort", PROXY_PORT);
        System.out.println("proxy: " + PROXY_LIST[indexOfProxyList]);
	}

	/**
	 * 画像のURLをGoogleに送り、類似画像のURLを取得し、ChildImageを返す
	 *
	 * @param originalImage 元画像
	 * @param keyword キーワード
	 * @return ChildImage
	 * @author tomita
	 * @throws IOException
	 */
	public ChildImage sendGoogle(BufferedImage originalImage, String keyword) throws IOException {
		ChildImage childImage = new ChildImage();

		// サーバー上に保存
		String dirname = "/usr/share/tomcat8/webapps/images/";
		String filename = new Date().getTime() + ".jpg";
		File file = new File(dirname + filename);
		//System.out.println(filename);
		ImageIO.write(originalImage, "jpeg", file);
		String url = "http://ec2-54-92-0-242.ap-northeast-1.compute.amazonaws.com:8080/images/"
				+ filename;

		String resultImageUrl = sendGoogleByUrl(url, keyword);
		childImage.setUrl(resultImageUrl);

		file.delete();

		return childImage;
	}

	/**
	 * 画像のURLをGoogleに送り、類似画像のURLを取得する
	 *
	 * @param originalImageUrl 元画像のURL
	 * @param keyword キーワード
	 * @return 類似画像のURL
	 * @author tomita
	 * @throws IOException
	 */
	private String sendGoogleByUrl(String originalImageUrl, String keyword) throws IOException {
		// URLエンコード
		originalImageUrl = URLEncoder.encode(originalImageUrl, "UTF-8");

		String searchUrl = "http://www.google.co.jp/searchbyimage?image_url=" + originalImageUrl;
		if (!keyword.equals("")) {
			searchUrl += "&q=" + keyword;
		}
		//System.out.println("searchUrl: " + searchUrl);

		// 検索ページから類似画像のリンクを取得
		Document doc = getDocument(searchUrl);
		String href = doc.select("#imagebox_bigimages > div > a").attr("href");

		// うまくいかないとき
		if (href.equals("")) {
			System.out.println(doc.select("div.card-section").text());
			// ページをダンプ
			dumpHtml(doc.html());
		}

		searchUrl = "https://www.google.co.jp" + href;
		//System.out.println("searchUrl: " + searchUrl);

		// 類似画像のページから 1つ目のリンクのサムネイルを取得
		doc = getDocument(searchUrl);
		//System.out.println(page.select("a.rg_l img"));
		return doc.select("a.rg_l img[data-src]").attr("data-src");
	}

	/**
	 * urlからjsoupのDocumentを取得
	 *
	 * @param url
	 * @return jsoupのDocument
	 * @author tomita
	 */
	private Document getDocument(String url) {
		for (int i=0; i<MAX_ACCESS_COUNT; i++) {
			try {
				// 連続アクセスするとGoogleに怒られて繋がらなくなるので，
				// 500でエラーは起きなかった
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				return Jsoup.connect(url).userAgent(USERAGENT).get();
			} catch (SocketTimeoutException e) {
				// 何もせずにもう一度アクセス
			} catch (HttpStatusException e) {
				// Googleに怒られたらプロキシを変更
				if (e.getUrl().startsWith("http://ipv4.google.com/sorry/IndexRedirect?continue=")) {
					changeProxy();
				} else {
					System.out.println("url:" + e.getUrl());
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * htmlをダンプ
	 *
	 * @param html
	 * @author tomita
	 */
	private void dumpHtml(String html) {
		String filename = "/tmp/" + new Date().getTime() + ".html";
		System.out.println("error: dumpfile " + filename);
		File file = new File(filename);
		FileWriter filewriter;
		try {
			filewriter = new FileWriter(file);
			filewriter.write(html);
			filewriter.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * proxyを変更
	 *
	 * @author tomita
	 */
	private void changeProxy() {
		indexOfProxyList = (indexOfProxyList+1)%PROXY_LIST.length;
		System.setProperty("http.proxyHost", PROXY_LIST[indexOfProxyList]);
		System.out.println("proxy: " + PROXY_LIST[indexOfProxyList]);
	}
}
