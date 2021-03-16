package com.git.blog.config;

import com.git.blog.common.socket.WebSocket;
import com.git.blog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 因SpringBoot WebSocket对每个客户端连接都会创建一个WebSocketServer（@ServerEndpoint 注解对应的）对象
     * Bean注入操作会被直接略过，因而手动注入一个全局变量
     * @param iUserService 手动注入iUserService对象
     */
    @Autowired
    public void setIUserService(IUserService iUserService) {
        WebSocket.iUserService = iUserService;
    }
}
