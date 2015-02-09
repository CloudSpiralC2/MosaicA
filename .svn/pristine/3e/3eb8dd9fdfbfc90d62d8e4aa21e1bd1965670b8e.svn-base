package jp.cspiral.mosaica;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author niki
 */

@XmlRootElement(name = "parentimage")
public class ParentImage {
	// 画像を一意にするID
	private String imageId;

	// 画像をmimeエンコードしたもの
	private String src;

	// 横サイズ
	private int sizeX;

	// 縦サイズ
	private int sizeY;

	// 横分割数
	private int divX;

	// 縦分割数
	private int divY;

	// 状態(done, processing)
	private String status;

	// 子画像の配列
	private ChildImage[] children;

	// ---------以下ゲッター・セッター---------//
	@XmlElement(name = "imageid")
	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	@XmlElement(name = "src")
	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	@XmlElement(name = "sizex")
	public int getSizeX() {
		return sizeX;
	}

	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}

	@XmlElement(name = "sizey")
	public int getSizeY() {
		return sizeY;
	}

	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}

	@XmlElement(name = "divx")
	public int getDivX() {
		return divX;
	}

	public void setDivX(int divX) {
		this.divX = divX;
	}

	@XmlElement(name = "divy")
	public int getDivY() {
		return divY;
	}

	public void setDivY(int divY) {
		this.divY = divY;
	}

	@XmlElement(name = "status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlElement(name = "child")
	public ChildImage[] getChildren() {
		return children;
	}

	public void setChildren(ChildImage[] children) {
		this.children = children;
	}

}
