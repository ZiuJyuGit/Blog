package com.git.blog.common.socket;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.git.blog.entity.User;
import com.git.blog.service.IUserService;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@Component
@ServerEndpoint("/websocket/{username}/{password}")
public class WebSocket {

    public static IUserService iUserService;

    public static Session session = null;

    /**
     * WebSocket通过url占位传参，通过传送账号密码检验身份
     * @param session 记录连接
     * @param username 用户名
     * @param password 密码
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("password") String password) {
        User userFromDB = iUserService.getOne(new QueryWrapper<User>().eq("user_name", username));
        //身份校验失败拒绝连接
        if (userFromDB == null || !userFromDB.getUserPassword().equals(SecureUtil.md5(password))) {
            try {
                session.getBasicRemote().sendText("连接失败");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            WebSocket.session = session;
        }
    }

    @OnClose
    public void onClose() {
        WebSocket.session = null;
    }

    /**
     * 发送自定义消息，若博主在线则推送消息
     */
    public static void sendInfo(String msg) {
        if (WebSocket.session != null) {
            try {
                WebSocket.session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
