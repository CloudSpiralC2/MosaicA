package jp.cspiral.mosaica;

import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.glassfish.jersey.internal.util.Base64;

import jp.cspiral.mosaica.util.DBUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class ImageController {
	/**
	 * 親イメージオブジェクト
	 */
	ParentImage parentImage = new ParentImage();
	/**
	 * 親イメージの画像
	 */
	BufferedImage image = null;

	/**
	 * GoogleController
	 */
	GoogleController googleController = new GoogleController();

	/**
	 * Mongoのコレクション名
	 */
	private final String DB_COLLECTION = "image";
	/**
	 * Loggerオブジェクト
	 */
	private Logger logger;
	/**
	 * DBオブジェクト
	 */
	private DB db;
	/**
	 * DBCollectionオブジェクト
	 */
	private DBCollection coll;

	// ストリーム処理用 どこを処理しているかを特定する
	private int position;

	ImageController() {

	}

	public String createParentImage(String img, String keyword, int userDivX,
			int userDivY) throws IOException, InterruptedException {
		// 時間測定 開始時間
		long start = System.currentTimeMillis();

		try {
			String status = new String("processing");

			// --divX, divY--
			int xxx = userDivX;
			int yyy = userDivY;

			// 元画像をデコード
			image = decode(img);

			// 元画像の情報をparentImageに入れる
			// parentImage.setImageId("test4");
			parentImage.setImageId(new Date().getTime() + "");
			parentImage.setSrc(img);
			parentImage.setDivX(xxx);
			parentImage.setDivY(yyy);
			parentImage.setSizeX(image.getWidth());
			parentImage.setSizeY(image.getHeight());
			parentImage.setStatus(status);

			// 分割
			BufferedImage[] splitedImages = splitImage();

			// それぞれの子イメージに対して，エンコードと類似画像検索を行う
			int divX = parentImage.getDivX();
			int divY = parentImage.getDivY();
			int sizeX = parentImage.getSizeX();
			int sizeY = parentImage.getSizeY();
			int width = (int)Math.ceil(sizeX / divX);
			int height = (int)Math.ceil(sizeY / divY);
			ChildImage[] children = new ChildImage[divX * divY];

			// 処理位置位置初期化
			position = 0;
			// stream処理結果入れる用
			List<ChildImage> childrenlist = new ArrayList<>();

			// splitedImagesを配列に変換し，そのストリームを作成する
			Stream<BufferedImage> stream = Arrays.stream(splitedImages);

			System.out.println("before stream process");

			// splitedImagesのstreamの各要素に対する類似画像検索の結果(ChildImage)でstreamを作成する
			Stream<ChildImage> ciStream = stream.parallel().map(splitedImage -> {
				String nameOfChildImage;
				ChildImage child;

				try {
					// 画像を文字列にエンコード
					nameOfChildImage = encode(splitedImage);
					// childImageをnew = googleに投げる この時点ではchildはURLのみ持つ
					child = googleController.sendGoogle(splitedImage, keyword);
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
					return null;
				}

				// childの各要素をセット
				child.setSrc(nameOfChildImage);
				child.setY((position / divX) % divY);
				child.setX(position % divX);

				System.out.println("Current Position:" + (position / divX) % divY + ","+ (position % divX));

				// 処理位置を進める stream外部の変数を更新するのはよろしくないらしいが…
				position++;

				return child;
			});

			// 順序通りにChildImageをリストに追加する
			ciStream.forEachOrdered(child -> childrenlist.add(child));;

			// streamを閉じる
			ciStream.close();
			stream.close();

			// ArrayList -> 配列
			children = (ChildImage[])childrenlist.toArray(new ChildImage[0]);

			// 子イメージリストをparentImageに追加する
			parentImage.setChildren(children);

			// ParentImageをDBに保存
			db = DBUtils.getInstance().getDb();
			coll = db.getCollection(DB_COLLECTION);
			// DBObjectの生成
			DBObject query = new BasicDBObject();
			DBObject childrenQuery = new BasicDBObject();
			// <Image>
			query.put("imageId", parentImage.getImageId());
			query.put("src", parentImage.getSrc());
			query.put("divX", divX);
			query.put("divY", divY);
			query.put("sizeX", sizeX);
			query.put("sizeY", sizeY);
			query.put("status", "done");
			// ---- <child>
			for (int i = 0; i < divX * divY; i++) {
				DBObject childQuery = new BasicDBObject();
				childQuery.put("src", children[i].getSrc());
				childQuery.put("x", children[i].getX());
				childQuery.put("y", children[i].getY());
				childQuery.put("url", children[i].getUrl());

				String key = "child" + Integer.toString(i);
				childrenQuery.put(key, childQuery);
			}
			// -- <children>
			query.put("children", childrenQuery);

			// DBに保存
			coll.insert(query);

			// 時間測定 終了時間
			long end = System.currentTimeMillis();
			System.out.println("変換時間" + (end - start)  + "ms");

			return parentImage.getImageId();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * 親イメージをx*yの子イメージ配列に分割する
	 *
	 * @return 子イメージ配列(ChildImage[] -> BufferedImage[])
	 * @author hayata
	 */
	public BufferedImage[] splitImage() throws RasterFormatException {
		int divX = parentImage.getDivX();
		int divY = parentImage.getDivY();
		int sizeX = parentImage.getSizeX();
		int sizeY = parentImage.getSizeY();
		int width = (int)Math.ceil(sizeX / divX);
		int height = (int)Math.ceil(sizeY / divY);
		BufferedImage[] child = new BufferedImage[divX * divY];

		try {
			for (int i = 0; i < divY; i++) {
				for (int j = 0; j < divX; j++) {
					if((width > 9) && (height > 9)){
					child[i * divX + j] = new BufferedImage(width, // 生成する画像の横サイズ
							height, // 生成する画像の縦サイズ
							BufferedImage.TYPE_INT_RGB); // イメージタイプ(RGB:int);
					child[i * divX + j] = image.getSubimage(j * width, i * height,
							width, height);
					}
					else{
						// 拡大倍率
						int mag;
						if(width < height)
							mag = (int)Math.ceil(10/width);
						else
							mag = (int)Math.ceil(10/height);

						// child生成
						BufferedImage preChild = new BufferedImage(width, // 生成する画像の横サイズ
								height, // 生成する画像の縦サイズ
								BufferedImage.TYPE_INT_RGB); // イメージタイプ(RGB:int);
						child[i * divX + j] = new BufferedImage(width * mag, // 生成する画像の横サイズ
								height * mag, // 生成する画像の縦サイズ
								BufferedImage.TYPE_INT_RGB); // イメージタイプ(RGB:int);

						// 画像生成
						preChild = image.getSubimage(j * width, i * height,
								width, height);
						for (int k = 0; k < height; k++){		// 行
							for (int l = 0; l < width; l++){	// 列
								// 座標(l, k)のRGB取得
								int c = preChild.getRGB(l, k);
								int r = 255 - (c>>16&0xff);
								int g = 255 - (c>>8&0xff);
								int b = 255 - (c&0xff);
								int rgb = 0xff000000 | r <<16 | g <<8 | b;
								// 各ピクセル値をm*m領域に拡大
								for (int m = 0; m < mag; m++){	// 倍率
									for (int n = 0; n < mag; n++){
									child[i * divX + j].setRGB(k*mag+m, l*mag+n, rgb);
									}
								}
							}
						}
					}
				}
			}
		} catch (RasterFormatException rfe) {
			rfe.printStackTrace();
		}

		return child;
	}


	/**
	 * 画像をMIMEエンコードし文字列に変換します
	 *
	 * @param image
	 *            画像オブジェクト
	 * @return MIMEエンコードされた文字列
	 * @author niki
	 */
	public String encode(BufferedImage image){
		// まずバイナリ表現に変換
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BufferedOutputStream os = new BufferedOutputStream(bos);
		image.flush();
		try {
			// osにimageを書き込み
			ImageIO.write(image, "jpg", os);
			os.flush();
			os.close();
			// 文字列に変換
			String encodedImage = new String(Base64.encode(bos.toByteArray()),
					"UTF-8");
			System.out.println("encoded length: " + encodedImage.length());
			StringBuilder sb = new StringBuilder();
			sb.append(encodedImage);
			 // 拡張子は一応jpgにしています．引数でセットするほうがいいかも？
			sb.insert(0, "data:image/jpg;base64,");
			return new String(sb);

		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} catch (ArrayIndexOutOfBoundsException e2){
			return "Base64.encode error";
		}

	}

	/**
	 * MIMEエンコードされた文字列を画像に変換します
	 *
	 * @param mime
	 *            MIMEエンコードされた文字列
	 * @return image 画像オブジェクト
	 * @author niki
	 */
	public BufferedImage decode(String mime) throws IOException {
		//System.out.println("mime_orig:" + mime);
		mime = mime.replaceAll("data:image/.*;base64,", "");// この文字列が先頭にくっついてるとダメみたいなので削除
		byte[] mimeByte = mime.getBytes("UTF-8");
		ByteArrayInputStream input = new ByteArrayInputStream(
				Base64.decode(mimeByte));

		// サーバー上に保存
		String dirname = "/usr/share/tomcat7/webapps/images/";
		String filename = "preParentImage.jpg";
		File file = new File(dirname + filename);
		System.out.println(filename);

		BufferedImage inputImage = ImageIO.read(input);
		ImageIO.write(inputImage, "jpg", file);

		BufferedImage image = ImageIO.read(file);

		return image;
	}

	/**
	 * DBに保存されたParentImageを読み出す
	 *
	 * @param imageId
	 *            画像に一意に設定されたID
	 * @return ParentImage 親画像オブジェクト
	 * @author niki
	 */
	public ParentImage getImage(String imageId) throws MongoException {
		ParentImage pImage = new ParentImage();

		pImage.setImageId(imageId);

		db = DBUtils.getInstance().getDb();
		coll = db.getCollection(DB_COLLECTION);

		DBObject query = new BasicDBObject("imageId", imageId);
		DBObject result = coll.findOne(query);

		if (result == null) { // 処理中の場合
			pImage.setStatus("processing"); // statusをprocessingにして返す
			return pImage;
		} else { // 処理が完了している場合
			pImage.setSrc((String) result.get("src"));
			pImage.setSizeX((int) result.get("sizeX"));
			pImage.setSizeY((int) result.get("sizeY"));
			pImage.setDivX((int) result.get("divX"));
			pImage.setDivY((int) result.get("divY"));
			pImage.setStatus((String) result.get("status")); // doneになってるはず

			ChildImage[] childImage = new ChildImage[pImage.getDivX()
					* pImage.getDivY()];

			DBObject children = (DBObject) result.get("children");
			for (int i = 0; i < pImage.getDivX() * pImage.getDivY(); i++) {
				ChildImage cImage = new ChildImage();
				String key = "child" + Integer.toString(i);
				DBObject child = (DBObject) children.get(key);
				cImage.setSrc((String) child.get("src"));
				cImage.setUrl((String) child.get("url"));
				cImage.setX((int) child.get("x"));
				cImage.setY((int) child.get("y"));

				childImage[i] = cImage;
			}

			pImage.setChildren(childImage);

			return pImage;
		}
	}

	/**
	 * DBに保存されたParentImageのimageIdを読み出す
	 *
	 * @return imageId 画像のimageIdト
	 * @author niki
	 */
	public String getImageIdList() throws MongoException{
		db = DBUtils.getInstance().getDb();
		coll = db.getCollection(DB_COLLECTION);

		String imageIdList = new String("");
		String crlf = System.getProperty("line.separator");

		DBCursor cursor = coll.find();
		for (DBObject o: cursor) {
			imageIdList = imageIdList.concat((String)o.get("imageId"));
			imageIdList = imageIdList.concat(crlf);
		}

		return imageIdList;
	}

	/**
	 * 保存用のイメージを作成するAPI
	 *
	 * @param imageId
	 * @return
	 * @throws MongoException
	 */
	public String saveImage(String imageId) throws IOException,
			MongoException {
		db = DBUtils.getInstance().getDb();
		coll = db.getCollection(DB_COLLECTION);

		DBObject query = new BasicDBObject("imageId", imageId);
		DBObject result = coll.findOne(query);

		int divX = (int) result.get("divX");
		int divY = (int) result.get("divY");
		int sizeX = (int) result.get("sizeX");
		int sizeY = (int) result.get("sizeY");
		int width = (int)Math.ceil(sizeX / divX);
		int height = (int)Math.ceil(sizeY / divY);

		// 保存用イメージ
		BufferedImage MergedImage = new BufferedImage(width*divX, // 生成する画像の横サイズ
				height*divY, // 生成する画像の縦サイズ
				BufferedImage.TYPE_INT_RGB); // イメージタイプ(RGB:int)
		BufferedImage ChildImage = new BufferedImage(width, // 生成する画像の横サイズ
				height, // 生成する画像の縦サイズ
				BufferedImage.TYPE_INT_RGB); // イメージタイプ(RGB:int)

		DBObject children = (DBObject) result.get("children");

		for (int i = 0; i < divY; i++) {
			for (int j = 0; j < divX; j++) {
				DBObject child = (DBObject) children.get("child" + Integer.toString(i*divX+j));

				String ChildImageSrc = (String) child.get("src");
				ChildImage = this.decode(ChildImageSrc);

				// 保存用のイメージ作成．
				// ChildImageの各ピクセルのRGB値をMergedImageの対応するピクセルに格納する.
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						MergedImage.setRGB(j * width + x, // ピクセルのx座標
								i * height + y, // ピクセルのy座標
								ChildImage.getRGB(x, y)); // ChildImageのRGB値
					}
				}
			}
		}

		return this.encode( MergedImage );
	}
}
