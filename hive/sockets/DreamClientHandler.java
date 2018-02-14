package sockets;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DreamClientHandler extends ChannelInboundHandlerAdapter {

	public DreamClient dc;
	
    public DreamClientHandler(DreamClient dc) {
    	this.dc = dc;
    }

    //happens once on connect with server
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    	System.out.println("connected to server.");
    	dc.setChannelHandlerContext(ctx);
		Thread t1 = new Thread(new Runnable() { //in case overridden getRegistration isnt available instantly, it retries
			@Override
			public void run() {
		    	Sendable registration = dc.getRegistration();
		    	int waits = 0;
		    	while (registration == null && waits < 20) {
		    		try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
		    		registration = dc.getRegistration();
		    		waits++;
		    	}
		    	if (registration != null) {
		    		ctx.writeAndFlush(registration);
		    	}
			}
		});
		t1.start();
    }
    
    //happens when object received from server
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	if (msg instanceof Sendable) {
    		Thread t1 = new Thread(new Runnable() {
    			@Override
    			public void run() {
		    		Sendable answer = dc.receiveSendable((Sendable) msg);
		    		if (answer != null) {
		    			ctx.writeAndFlush(answer);
		    		}
    			}
    		});
    		t1.start();
    	}
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
    	
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

