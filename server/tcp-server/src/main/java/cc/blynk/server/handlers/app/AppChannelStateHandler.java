package cc.blynk.server.handlers.app;

import cc.blynk.server.dao.SessionsHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/20/2015.
 *
 * Removes channel from session in case it became inactive (closed from client side).
 */
@ChannelHandler.Sharable
public class AppChannelStateHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LogManager.getLogger(AppChannelStateHandler.class);

    private final SessionsHolder sessionsHolder;

    public AppChannelStateHandler(SessionsHolder sessionsHolder) {
        this.sessionsHolder = sessionsHolder;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionsHolder.removeAppFromSession(ctx.channel());
        log.trace("Application channel disconnect.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof ReadTimeoutException) {
            //channel is already closed here by ReadTimeoutHandler
            log.trace("Application timeout disconnect.");
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
