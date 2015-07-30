package jp.cspiral.mosaica.util;

import org.fluentd.logger.FluentLogger;
/**
 * fluentdに投げる用シングルトンクラス
 * @author tktk
 *
 */
public class MosaicALogger {
	public static MosaicALogger instance = new MosaicALogger();

	/**
	 * Log type fluentd側のconfig <match hoge.huga> に依存
	 */
	protected static final String LOGGER = "mosaica";

	/**
	 * fluentdがインストールされているサーバのIP
	 */
	protected static final String FLUENTD_HOST = "52.69.19.81";

	/**
	 * fluentdが利用するポート番号
	 */
	protected static final int FLUENTD_PORT = 24224;

	/**
	 * ロガーの設定
	 */
	private FluentLogger LOG;

	/**
	 * コンストラクタ
	 */
	private MosaicALogger() {
		LOG = FluentLogger.getLogger(LOGGER, FLUENTD_HOST, FLUENTD_PORT);
	}

	public static MosaicALogger getInstance() {
		return MosaicALogger.instance;
	}

	public FluentLogger getLogger() {
		return LOG;
	}

}
