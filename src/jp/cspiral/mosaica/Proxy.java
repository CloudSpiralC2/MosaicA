package jp.cspiral.mosaica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;

public class Proxy {
	private static AmazonEC2 ec2;
	private String instanceId;

	Proxy(String id) {
		ec2 = new AmazonEC2Client();
		ec2.setRegion(Region.getRegion(Regions.AP_NORTHEAST_1));
		instanceId = id;
	}
	
	public void start() {
	    List<String> instanceIds = new ArrayList<String>();
	    instanceIds.add(instanceId);

	    StartInstancesRequest startInstancesRequest = new StartInstancesRequest(instanceIds);

	    ec2.startInstances(startInstancesRequest);
	}
	
	public void stop() {
	    List<String> instanceIds = new ArrayList<String>();
	    instanceIds.add(instanceId);

	    StopInstancesRequest stopInstancesRequest = new StopInstancesRequest(instanceIds);

	    ec2.stopInstances(stopInstancesRequest);
	}
	
	public void restart() {
		stop();
		start();
	}
	/*
	public String getLocalIp() {
		return "";
	}
	*/
	public static void main(String[] args) throws IOException {
		Proxy proxy = new Proxy("i-deb5732b");
		proxy.restart();
	}
}
