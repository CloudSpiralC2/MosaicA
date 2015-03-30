package jp.cspiral.mosaica;

import java.util.ArrayList;
import java.util.List;

public class ProxyManager {
	private List<Proxy> proxies = new ArrayList<Proxy>();
	private int index;
	
	ProxyManager() {
		proxies.add(new Proxy("i-deb5732b"));
		proxies.add(new Proxy("i-bdab6d48"));
	}
	
	/*
	//プロキシのローカルIPを出力
	public String getLocalIp() {
		return proxies.get(index++).getLocalIp();
	}
	*/
	
}