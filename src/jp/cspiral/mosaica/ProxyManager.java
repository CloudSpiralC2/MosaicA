package jp.cspiral.mosaica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProxyManager extends Thread {
	//利用可能なプロキシのリスト
	private ArrayList<Proxy> proxies = new ArrayList<Proxy>();
	//利用可能なアドレスのリスト
	private ArrayList<String> addresses = new ArrayList<String>();
	//再起動中のプロキシのリスト
	private ArrayList<Proxy> restartingProxies = new ArrayList<Proxy>();
	private int index = 0;
	
	ProxyManager() {
		Proxy proxy;
		proxy = new Proxy("i-deb5732b");
		proxies.add(proxy);
		addresses.add(proxy.getPrivateIpAddress());
		
		proxy = new Proxy("i-bdab6d48");
		proxies.add(proxy);
		addresses.add(proxy.getPrivateIpAddress());
	}
	
	//プロキシのアドレスを返す
	public String getAddress() {
		return addresses.get(index++%addresses.size());
	}
	
	//プロキシのエラーを報告し新しいプロキシのアドレスを返す
	public String changeAddress(String oldAddress) {
		int index = addresses.indexOf(oldAddress);
		Proxy proxy = proxies.remove(index);
		addresses.remove(index);
		proxy.restart();
		restartingProxies.add(proxy);
		return getAddress();
	}
	
	//プロキシをチェックして起動していればリストを移動
	public void checkProxy() {
		for ( Proxy proxy: restartingProxies ) {
			if ( proxy.getStatus() == "" ) {
				restartingProxies.remove(proxy);
				proxies.add(proxy);
				addresses.add(proxy.getPrivateIpAddress());
			}
		}
	}
}