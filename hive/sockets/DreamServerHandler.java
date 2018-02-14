package sockets;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DreamServerHandler extends ChannelInboundHandlerAdapter {
	
	public DreamServer ds;
	
    public DreamServerHandler(DreamServer ds) {
    	this.ds = ds;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    	if (msg instanceof Sendable) {
    		Thread t1 = new Thread(new Runnable() {
    			@Override
    			public void run() {
		    		Sendable answer = ds.receiveSendable((Sendable) msg, ctx);
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
        ds.removeContext(ctx);
        ctx.close();
    }
}