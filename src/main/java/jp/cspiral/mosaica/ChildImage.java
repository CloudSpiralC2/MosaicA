package jp.cspiral.mosaica;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author niki
 */
public class ChildImage {
	// 画像をmimeエンコードしたもの
	private String src;

	// 横座標
	private int x;

	// 縦座標
	private int y;

	// google類似画像の結果のURL
	private String url;

	// ---------以下ゲッター・セッター---------//

	@XmlElement(name = "src")
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@XmlElement(name = "x")
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	@XmlElement(name = "y")
	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@XmlElement(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
