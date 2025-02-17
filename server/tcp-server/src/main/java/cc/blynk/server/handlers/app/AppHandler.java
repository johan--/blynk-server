package cc.blynk.server.handlers.app;

import cc.blynk.common.model.messages.Message;
import cc.blynk.common.utils.ServerProperties;
import cc.blynk.server.dao.SessionsHolder;
import cc.blynk.server.dao.UserRegistry;
import cc.blynk.server.handlers.BaseSimpleChannelInboundHandler;
import cc.blynk.server.handlers.app.logic.*;
import cc.blynk.server.handlers.common.PingLogic;
import cc.blynk.server.model.auth.User;
import cc.blynk.server.storage.StorageDao;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import static cc.blynk.common.enums.Command.*;

/**
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 2/1/2015.
 *
 */
@ChannelHandler.Sharable
public class AppHandler extends BaseSimpleChannelInboundHandler<Message> {

    private final SaveProfileLogic saveProfile;
    private final GetTokenLogic token;
    private final HardwareAppLogic hardwareApp;
    private final RefreshTokenLogic refreshToken;
    private final GetGraphDataLogic graphData;

    public AppHandler(ServerProperties props, UserRegistry userRegistry, SessionsHolder sessionsHolder, StorageDao storageDao) {
        super(props);
        this.saveProfile = new SaveProfileLogic(props);
        this.token = new GetTokenLogic(userRegistry);
        this.hardwareApp = new HardwareAppLogic(sessionsHolder);
        this.refreshToken = new RefreshTokenLogic(userRegistry);
        this.graphData = new GetGraphDataLogic(storageDao);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, User user, Message msg) {
        switch (msg.command) {
            case HARDWARE:
                hardwareApp.messageReceived(ctx, user, msg);
                break;
            case SAVE_PROFILE :
                saveProfile.messageReceived(ctx, user, msg);
                break;
            case ACTIVATE_DASHBOARD :
                ActivateDashboardLogic.messageReceived(ctx, user, msg);
                break;
            case DEACTIVATE_DASHBOARD :
                DeActivateDashboardLogic.messageReceived(ctx, user, msg);
                break;
            case LOAD_PROFILE :
                LoadProfileLogic.messageReceived(ctx, user, msg);
                break;
            case GET_TOKEN :
                token.messageReceived(ctx, user, msg);
                break;
            case REFRESH_TOKEN :
                refreshToken.messageReceived(ctx, user, msg);
                break;
            case GET_GRAPH_DATA :
                graphData.messageReceived(ctx, user, msg);
                break;
            case PING :
                PingLogic.messageReceived(ctx, msg.id);
                break;
        }
    }

}
