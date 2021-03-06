package org.deeplearning4j.scaleout.zookeeper;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.deeplearning4j.scaleout.conf.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves configuration data serialized by {@link org.deeplearning4j.sendalyzeit.textanalytics.mapreduce.hadoop.util.ZooKeeperConfigurationRegister}
 * @author Adam Gibson
 *
 */
public class ZookeeperConfigurationRetriever implements Watcher {

	private ZooKeeper keeper;
	private String host;
	private int port;
	private String id;
	private static Logger log = LoggerFactory.getLogger(ZookeeperConfigurationRetriever.class);

	public ZookeeperConfigurationRetriever(String id) {
		this("localhost",2181,id);
	}


	public ZookeeperConfigurationRetriever( String host,
			int port, String id) {
		super();
		this.keeper = new ZookeeperBuilder().setHost(host).setPort(port).build();
		this.host = host;
		this.port = port;
		this.id = id;
	}


	public Conf retrieve(Conf conf) throws Exception {
		Conf ret = retreive();
		return ret;
	}


	public Conf retreive(String host) throws Exception {
		Conf conf = new Conf();
		String path = new ZookeeperPathBuilder().addPaths(Arrays.asList("tmp",id)).setHost(host).setPort(port).build();
		Stat stat = keeper.exists(path, false);
		if(stat == null) {
			List<String> list = keeper.getChildren( new ZookeeperPathBuilder().setHost(host).setPort(port).addPath("tmp").build(), false);
			throw new IllegalStateException("Nothing found for " + path + " possible children include " + list);
		}
		byte[] data = keeper.getData(path, false, stat ) ;
		conf = (Conf) ZooKeeperConfigurationRegister.deserialize(data);


		return conf;
	}

	public Conf retreive() throws Exception {
		Conf c = null;
		String localhost = InetAddress.getLocalHost().getHostName();

		String[] hosts = { host,"127.0.0.1","localhost",localhost };

		for(int i = 0; i < hosts.length; i++) {
			try {
				log.info("Attempting to retreive conf from " + hosts[i]);
				c = retreive(hosts[i]);
			}catch(Exception e) {

			}
		}


		return c;
	}

	public void close() {
		try {
			keeper.close();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}


	@Override
	public void process(WatchedEvent event) {
		if(event.getState() == KeeperState.Expired) {
			keeper = new ZookeeperBuilder().setHost(host).setPort(port).setWatcher(this).build();

		}		
	}
}
