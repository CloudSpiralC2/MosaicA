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

	private long id;

	// 連続アクセス回数
	private static final int MAX_ACCESS_COUNT = 3;

	/**
	 * コンストラクタ
	 *
	 * プロキシの初期設定
	 */
	GoogleController() {
	}

	/**
	 * 画像のURLをGoogleに送り、類似画像のURLを取得し、ChildImageを返す
	 *
	 * @param originalImage
	 *            元画像
	 * @param keyword
	 *            キーワード
	 * @return ChildImage
	 * @author tomita
	 * @throws IOException
	 */
	public ChildImage sendGoogle(BufferedImage originalImage, String keyword)
			throws IOException {
		ChildImage childImage = new ChildImage();

		// サーバー上に保存
		String dirname = "/usr/share/tomcat7/webapps/images/";
		String filename = new Date().getTime() + ".jpg";
		File file = new File(dirname + filename);
		ImageIO.write(originalImage, "jpeg", file);
		String url = "http://52.68.162.198:8080/images/" + filename;

		String resultImageUrl = sendGoogleByUrl(url, keyword);
		childImage.setUrl(resultImageUrl);

		file.delete();

		return childImage;
	}

	/**
	 * 画像のURLをGoogleに送り、類似画像のURLを取得する
	 *
	 * @param originalImageUrl
	 *            元画像のURL
	 * @param keyword
	 *            キーワード
	 * @return 類似画像のURL
	 * @author tomita
	 * @throws IOException
	 */
	private String sendGoogleByUrl(String originalImageUrl, String keyword)
			throws IOException {
		// URLエンコード
		originalImageUrl = URLEncoder.encode(originalImageUrl, "UTF-8");

		String searchUrl = "http://www.google.co.jp/searchbyimage?image_url="
				+ originalImageUrl;
		if (!keyword.equals("")) {
			searchUrl += "&q=" + keyword;
		}

		// 検索ページから類似画像のリンクを取得
		Document doc = getDocument(searchUrl);
		String href;
		try {
			href = doc.select("#imagebox_bigimages > div > a").attr("href");
			if("".equals(href)){
				// ページをダンプ
				dumpHtml(doc.html());
//				System.out.println(id + " / This html body is null url: " + doc.baseUri());
				// もう一度トライ
				doc = getDocument(searchUrl);
				href = doc.select("#imagebox_bigimages > div > a").attr("href");
			}
		} catch (NullPointerException e) {
			// getDocumentがnullのとき
			System.out.println(id + " / Document is null, so this position is empty: " + searchUrl);
			href = "";
		}

		searchUrl = "https://www.google.co.jp" + href;

		// 類似画像のページから 1つ目のリンクのサムネイルを取得
		doc = getDocument(searchUrl);
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
		for (int i = 0; i < MAX_ACCESS_COUNT; i++) {
			try {
				// 連続アクセスするとGoogleに怒られて繋がらなくなるので，
				// 500でエラーは起きなかった
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return Jsoup.connect(url).userAgent(USERAGENT).get();
			} catch (SocketTimeoutException e) {
				// 何もせずにもう一度アクセス
				System.out.println(id + " / Timeout, retry: " + i);
			} catch (HttpStatusException e) {
				if (e.getUrl().startsWith("http://ipv4.google.com/sorry/IndexRedirect?continue=")) {
					System.out.println(id + " / Google angry: " + e.getUrl());
				} else {
					System.out.println(id + " / HttpStatusException code:" + e.getStatusCode() );//+ ", url:" + e.getUrl());
				}
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
		System.out.println(id + " / Return null");
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
		System.out.println(id + " / Error: dumpfile " + filename);
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

	public void setId(long id) {
		this.id = id;
	}
}
