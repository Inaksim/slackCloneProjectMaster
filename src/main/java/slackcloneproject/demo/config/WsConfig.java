package slackcloneproject.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

public class WsConfig implements WebSocketMessageBrokerConfigurer{

    @Autowired
    private HandShakeInterceptor handShakeInterceptor;

    public void registerStompEndPoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/messenger", "/call").addInterceptors(handShakeInterceptor).setAllowedOriginPatterns("http://localhost:3000").withSockJS();
    }

    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

}
