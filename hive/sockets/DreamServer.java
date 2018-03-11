package sockets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import others.Account;
import others.BufferMessage;

public class DreamServer {

	public static final String HOST = "a2r3dmn96f8sf3n62.bounceme.net";
	public static final int TCP = 64144;
	public static final int LIST_SIZE = 100;
	public static final String ACCOUNTS_FILE = "accounts.bin";
	public static final String VALUE_SEPARATOR = "~";
	protected volatile Connector[] connections; //has to be fixed sized array for thread safety?
	protected volatile BufferMessage[] buffer;
	protected volatile Map<String, Map<String, Account>> dreamlings;

	public class Connector {
		public ChannelHandlerContext ctx;
		public String name;
		public Connector(String name, ChannelHandlerContext ctx) {
			this.name = name;
			this.ctx = ctx;
		}
	}
	
	public DreamServer(int port) {
		DreamServer me = this;
		
		connections = new Connector[LIST_SIZE];
		buffer = new BufferMessage[LIST_SIZE * 5];
		dreamlings = new LinkedHashMap<String, Map<String, Account>>();
		loadAccounts();
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
		        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		        EventLoopGroup workerGroup = new NioEventLoopGroup();
		        try {
		            ServerBootstrap b = new ServerBootstrap();
		            b.group(bossGroup, workerGroup)
		             .channel(NioServerSocketChannel.class)
		             .handler(new LoggingHandler(LogLevel.INFO))
		             .childOption(ChannelOption.SO_KEEPALIVE, true)
		             .childHandler(new ChannelInitializer<SocketChannel>() {
		                @Override
		                public void initChannel(SocketChannel ch) throws Exception {
		                    ChannelPipeline p = ch.pipeline();
		                    p.addLast(
		                            new ObjectEncoder(),
		                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
		                            new DreamServerHandler(me));
		                }
		             });
		            
		            // Bind and start to accept incoming connections.
		            b.bind(port).sync().channel().closeFuture().sync();
		        } catch (Exception e) {
		        	e.printStackTrace();
		        } finally {
		            bossGroup.shutdownGracefully();
		            workerGroup.shutdownGracefully();
		        }
			}
		});
		t1.start();
	}
	
	public void saveAccounts() {
		if (!new File(ACCOUNTS_FILE).exists()) {
			try {
				new File(ACCOUNTS_FILE).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(ACCOUNTS_FILE));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(dreamlings);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadAccounts() {
		if (new File(ACCOUNTS_FILE).exists()) {
			try {
				FileInputStream fis = new FileInputStream(new File(ACCOUNTS_FILE));
				ObjectInputStream ois = new ObjectInputStream(fis);
				dreamlings = (Map<String, Map<String, Account>>) ois.readObject();
				ois.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void removeContext(ChannelHandlerContext ctx) {
		for (int i = 0; i < connections.length; i++) {
			Connector d = connections[i];
			if (d != null && d.ctx.equals(ctx)) {
				System.out.println("removing client: " + d.name);
				connections[i] = null;
			}
		}
	}
	
	public void checkSockets() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat("HHmm");
		int time = Integer.parseInt(sdf1.format(cal.getTime()));
		
		for (int i = 0; i < buffer.length; i++) { //delete buffer messages after 10 minutes
			BufferMessage msg = buffer[i];
			if (msg != null && (time - msg.timeAdded > 10 || (msg.timeAdded >= 2350 && time < 2350))) {
				buffer[i] = null;
			}
		}
		for (int i = 0; i < connections.length; i++) {
			Connector d = connections[i];
			if (d != null && (d.ctx == null || d.ctx.isRemoved())) {
				System.out.println("removing client: " + d.name);
				connections[i] = null;
			}
		}
	}
	
	//basic receiving. not to be overridden by other classes
	public Sendable receiveSendable(Sendable data, ChannelHandlerContext ctx) {
		Sendable result = null;
		checkSockets();
		System.out.println("received" + (data.isAnswer ? " answer" : "") + ": " + data.cmd + " " + data.name);
		if (data.isAnswer) {
			for (int i = 0; i < buffer.length; i++) {
				if (buffer[i] == null) {
					buffer[i] = new BufferMessage(data);
					break;
				}
			}
		} else {
			result = processSendable(data, ctx);
		}
		return result;
	}
	
	//specific processing. may be overridden by other classes
	public Sendable processSendable(Sendable data, ChannelHandlerContext ctx) {
		Sendable result = null;
		if (data.cmd.equals("dreamling")) {
			addIfNotAlreadyHave(data, ctx);
		}
		if (data.cmd.equals("rename")) { //data: oldName, newName
			if (data.data.size() >= 2) {
				final String oldName = data.data.get(0);
				final String newName = data.data.get(1);
				for (Connector c : connections) {
					if (c != null && c.name.equalsIgnoreCase(oldName)) {
						c.name = newName;
					}
				}
				dreamlings.put(newName, dreamlings.get(oldName));
				dreamlings.remove(oldName);
			}
		}
		if (data.cmd.equals("requestDreamling")) { //data: dreamling
			if (data.data.size() >= 1) {
				final String targetDreamling = data.data.get(0);
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				
				//fill result.data with all account values
				Map<String, Account> accs = dreamlings.get(targetDreamling);
				if (accs != null) {
					Set<String> titles = null; //will be set by first account
					for (String accId : accs.keySet()) {
						Account acc = accs.get(accId);
						if (titles == null) {
							titles = acc.getTitles();
							//set titles as first line in result
							String allTitles = "";
							for (String title : titles) {
								allTitles += title + VALUE_SEPARATOR;
							}
							allTitles = allTitles.substring(0, Math.max(allTitles.length() - VALUE_SEPARATOR.length(), 0)); //remove last separator
							if (!allTitles.isEmpty()) result.data.add(allTitles);
						}
						
						String allValues = "";
						List<String> values = acc.getValuesForTitles(titles);
						for (String value : values) {
							allValues += value + VALUE_SEPARATOR;
						}
						allValues = allValues.substring(0, Math.max(allValues.length() - VALUE_SEPARATOR.length(), 0)); //remove last separator
						if (!allValues.isEmpty()) result.data.add(allValues);
					}
					
				}
			}
		}
		if (data.cmd.equals("tabs")) {
			result = new Sendable(data.name);
			result.isAnswer = true;
			result.cmd = data.cmd;
			ArrayList<String> names = new ArrayList<String>();
			
			for (int i = 0; i < connections.length; i++) {
				Connector d = connections[i];
				if (d != null) {
					String name = d.name;
					if (name != null) {
						names.add(name);
					}
				}
			}
			String[] namesArray = names.toArray(new String[names.size()]);
			Arrays.sort(namesArray, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					String name1 = "";
					String name2 = "";
					String version1 = "";
					String version2 = "";
					if (s1.lastIndexOf(" ") == -1) {
						name1 = s1;
					} else {
						name1 = s1.substring(0, s1.lastIndexOf(" "));
						version1 = s1.substring(s1.lastIndexOf(" "), s1.length());
					}
					if (s2.lastIndexOf(" ") == -1) {
						name2 = s2;
					} else {
						name2 = s2.substring(0, s2.lastIndexOf(" "));
						version2 = s2.substring(s2.lastIndexOf(" "), s2.length());
					}
					
					int result = name1.compareTo(name2);
					if (result != 0) {
						return result;
					} else {
						if (version1.length() < version2.length()) {
							return -1;
						} else if (version1.length() > version2.length()) {
							return 1;
						} else {
							return version1.compareTo(version2);
						}
					}
				}
			});
			for (int i = 0; i < namesArray.length; i++) {
				result.data.add(namesArray[i]);
			}
		}
		//multiple actions and values may be split with VALUE_SEPARATOR
		if (data.cmd.equals("action")) { //data: dreamling, accId, attrNames, values
			if (data.data.size() >= 3) {
				final String targetDreamling = data.data.get(0);
				final String accId = data.data.get(1);
				final String[] attrNames = data.data.get(2).split(VALUE_SEPARATOR);
				final String[] values = data.data.get(3).split(VALUE_SEPARATOR);

				result = new Sendable(data.name);
				result.cmd = data.cmd;
				result.isAnswer = true;
				result.data.add("true");
				
				Map<String, Account> accs = dreamlings.get(targetDreamling);
				if (accs != null) {
					if (!accId.equals("0")) {
						Account acc = accs.get(accId);
						if (acc != null) {
							for (int i = 0; i < attrNames.length; i++) {
								final String attrName = attrNames[i];
								final String value = values[i];
								if (attrName.equals("action")) {
									switch (value) {
									case "start":
										for (int j = 0; j < connections.length; j++) {
											Connector c = connections[j];
											if (c != null && c.name.equalsIgnoreCase(targetDreamling)) {
												Sendable s1 = new Sendable();
												s1.cmd = "startBot";
												s1.data.add(accId);
												s1.data.add(acc.get("name"));
												s1.data.add(acc.generateCmdParams());
												sendSendable(s1, c.ctx);
											}
										}
										break;
									case "kill":
										Sendable s2 = new Sendable();
										s2.cmd = "killBot";
										s2.data.add(accId);
										for (int j = 0; j < connections.length; j++) {
											Connector c = connections[j];
											if (c != null && c.name.equalsIgnoreCase(targetDreamling)) {
												sendSendable(s2, c.ctx);
											}
										}
										break;
									case "delete":
										accs.remove(accId);
										break;
									default:
									}
								} else {
									acc.put(attrName, value);
								}
							}
						}
					} else { //accId is 0 (means functionality for all accounts)
						for (int i = 0; i < attrNames.length; i++) {
							final String action = attrNames[i];
							final String value = values[i];
							if (action.equals("action")) {
								switch (value) {
								case "startAll":
									for (int j = 0; j < connections.length; j++) {
										Connector c = connections[j];
										if (c != null && c.name.equalsIgnoreCase(targetDreamling)) {
											for (Account acc : accs.values()) {
												Sendable s1 = new Sendable();
												s1.cmd = "startBot";
												s1.data.add(acc.get("id"));
												s1.data.add(acc.get("name"));
												s1.data.add(acc.generateCmdParams());
												sendSendable(s1, c.ctx);
											}
										}
									}
									break;
								case "killAll":
									for (int j = 0; j < connections.length; j++) {
										Connector c = connections[j];
										if (c != null && c.name.equalsIgnoreCase(targetDreamling)) {
											Sendable s1 = new Sendable();
											s1.cmd = "killAll";
											sendSendable(s1, c.ctx);
										}
									}
									break;
								case "addOne":
									System.out.println("received addOne");
									Account acc = new Account();
									accs.put(acc.get("id"), acc);
									break;
								default:
								}
							}
						}
					}
				}
			}
		}
		if (data.cmd.equals("killBot")) { //data: dreamling, botName
			if (data.data.size() >= 2) {
				final String targetDreamling = data.data.get(0);
				final String botName = data.data.get(1);
				Map<String, Account> accs = dreamlings.get(targetDreamling);
				if (accs != null) {
					for (String accId : accs.keySet()) {
						Account acc = accs.get(accId);
						if (acc.get("name").equalsIgnoreCase(botName)) {
							for (int i = 0; i < connections.length; i++) {
								Connector d = connections[i];
								if (d != null && d.name.equals(targetDreamling)) {
									Sendable request = new Sendable();
									request.cmd = "killBot";
									request.data.add(accId);
									sendSendable(request, d.ctx);
								}
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	//returns whether added
	public boolean addIfNotAlreadyHave(Sendable registration, ChannelHandlerContext ctx) {
		if (registration.cmd.equals("dreamling")) {
			boolean alreadyConnected = false;
			for (int i = 0; i < connections.length; i++) {
				Connector d = connections[i];
				if (d != null && d.name.equalsIgnoreCase(registration.name)) {
					alreadyConnected = true;
				}
			}
			if (!alreadyConnected) {
				System.out.println("dreamling connected: " + registration.name);
				for (int i = 0; i < connections.length; i++) {
					if (connections[i] == null) {
						connections[i] = new Connector(registration.name, ctx);
						break;
					}
				}
				boolean alreadySeenInThePast = false;
				for (String dreamling : dreamlings.keySet()) {
					if (dreamling.equalsIgnoreCase(registration.name)) {
						alreadySeenInThePast = true;
						break;
					}
				}
				if (!alreadySeenInThePast) {
					dreamlings.put(registration.name, new LinkedHashMap<String, Account>());
				}
				return true;
			}
		}
		return false;
	}
	
	public Sendable sendSendable(Sendable data, ChannelHandlerContext ctx) {
		return sendSendable(data, ctx, false);
	}
	
	public Sendable sendSendable(Sendable data, ChannelHandlerContext ctx, boolean waitResponse) {
		if (data != null) {
			try {
				ctx.writeAndFlush(data);
				int waits = 0;
				while (waitResponse && waits < 30) {
					BufferMessage msg = null;
					for (int i = 0; i < buffer.length; i++) {
						msg = buffer[i];
						if (msg != null && msg.sendable.cmd.equals(data.cmd)
								&& msg.sendable.name.equals(data.name)) {
							buffer[i] = null;
							break;
						}
					}
					if (msg != null) {
						return msg.sendable;
					} else {
						Thread.sleep((waits * waits) * 10); //sleep longer and longer
						waits++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		new DreamServer(TCP);
	}
}
