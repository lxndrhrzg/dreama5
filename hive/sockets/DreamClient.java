package sockets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import others.BufferMessage;


public class DreamClient {
	
	public BufferMessage[] buffer;
	public boolean alive;
	private ChannelHandlerContext ctx;
	
	public DreamClient(String ip, int port) {
		DreamClient me = this;
		alive = true;
		buffer = new BufferMessage[DreamServer.LIST_SIZE / 2];
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (alive) { //attempt to reconnect if failed
					System.out.println("attempting connect to server.");
					EventLoopGroup group = new NioEventLoopGroup();
			        try {
			            Bootstrap b = new Bootstrap();
			            b.group(group)
			             .channel(NioSocketChannel.class)
			             .option(ChannelOption.SO_KEEPALIVE, true)
			             .handler(new ChannelInitializer<SocketChannel>() {
			                @Override
			                public void initChannel(SocketChannel ch) throws Exception {
			                    ChannelPipeline p = ch.pipeline();
			                    p.addLast(
			                            new ObjectEncoder(),
			                            new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
			                            new DreamClientHandler(me));
			                }
			             });
			            
			            // Start the connection attempt.
			            b.connect(ip, port).sync().channel().closeFuture().sync();
			        } catch (Exception e) {
			        	e.printStackTrace();
			        } finally {
			            group.shutdownGracefully();
			            if (ctx != null) {
			            	try {
			            		ctx.close();
			            	} catch (Exception e) {}
			            	ctx = null;
			            }
			    	}
			        if (alive) {  //wait between reconnection attempts
			        	try {
			        		Thread.sleep(20000);
			        	} catch (Exception e) {}
			        }
				}
			}
		});
		t1.start();
		
		//wait until connected to server so one-time-clients (servlets) dont immediately start work
		try {
			int waits = 0;
			while (ctx == null) {
				Thread.sleep((waits * waits) * 10); //sleep longer and longer
				if (waits < 20) waits++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setChannelHandlerContext(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}
	
	public void updateBuffer() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf1 = new SimpleDateFormat("HHmm");
		int time = Integer.parseInt(sdf1.format(cal.getTime()));
		for (int i = 0; i < buffer.length; i++) { //delete buffer messages after 10 minutes
			BufferMessage msg = buffer[i];
			if (msg != null && (time - msg.timeAdded > 10
					|| (msg.timeAdded >= 2310 && time < 2310))) {
				buffer[i] = null;
			}
		}
	}
	
	public Sendable getRegistration() {
		return null;
	}

	//basic receiving. not to be overridden by other classes
	public  Sendable receiveSendable(Sendable data) {
		Sendable result = null;
		updateBuffer();
		System.out.println("received" + (data.isAnswer ? " answer" : "") + ": " + data.cmd + " " + data.name);
		if (data.isAnswer) {
			for (int i = 0; i < buffer.length; i++) {
				if (buffer[i] == null) {
					buffer[i] = new BufferMessage(data);
					break;
				}
			}
		} else {
			result = processSendable(data);
		}
		return result;
	}
	
	//specific processing. may be overridden by other classes
	public Sendable processSendable(Sendable data) {
		return null;
	}
	
	public Sendable sendSendable(Sendable data) {
		return sendSendable(data, false);
	}
	
	public Sendable sendSendable(Sendable data, boolean waitResponse) {
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
				System.err.println("error: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void die() {
		alive = false;
		try {
			if (ctx != null) ctx.close();
		} catch (Exception e1) {
			System.err.println("error: " + e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		new DreamClient(DreamServer.HOST, DreamServer.TCP);
	}
}









