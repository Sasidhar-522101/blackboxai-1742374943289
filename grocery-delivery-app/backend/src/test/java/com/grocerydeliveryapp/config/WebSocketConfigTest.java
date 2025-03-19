package com.grocerydeliveryapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void configureMessageBroker() {
        // Arrange
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        // Act
        webSocketConfig.configureMessageBroker(registry);

        // Assert
        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");
    }

    @Test
    void registerStompEndpoints() {
        // Arrange
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        when(registry.addEndpoint(anyString())).thenReturn(
            mock(org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration.class)
        );

        // Act
        webSocketConfig.registerStompEndpoints(registry);

        // Assert
        verify(registry).addEndpoint("/ws");
        verify(registry).addEndpoint("/ws").setAllowedOrigins("${cors.allowed-origins}").withSockJS();
    }

    @Test
    void configureWebSocketTransport() {
        // Arrange
        WebSocketTransportRegistration registration = mock(WebSocketTransportRegistration.class);

        // Act
        webSocketConfig.configureWebSocketTransport(registration);

        // Assert
        verify(registration).setMessageSizeLimit(8192); // 8KB
        verify(registration).setSendBufferSizeLimit(8192); // 8KB
        verify(registration).setSendTimeLimit(20000); // 20 seconds
    }

    @Test
    void implementsWebSocketMessageBrokerConfigurer() {
        // Assert
        assertTrue(webSocketConfig instanceof WebSocketMessageBrokerConfigurer);
    }

    @Test
    void testWebSocketEndpointConfiguration() {
        // Arrange
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration registration = 
            mock(org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOrigins(anyString())).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(registration);

        // Act
        webSocketConfig.registerStompEndpoints(registry);

        // Assert
        verify(registry).addEndpoint("/ws");
        verify(registration).setAllowedOrigins("${cors.allowed-origins}");
        verify(registration).withSockJS();
    }

    @Test
    void testMessageBrokerConfiguration() {
        // Arrange
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        // Act
        webSocketConfig.configureMessageBroker(registry);

        // Verify broker configuration
        verify(registry).enableSimpleBroker("/topic", "/queue");
        verify(registry).setApplicationDestinationPrefixes("/app");
        verify(registry).setUserDestinationPrefix("/user");

        // Verify no other interactions
        verifyNoMoreInteractions(registry);
    }

    @Test
    void testWebSocketTransportConfiguration() {
        // Arrange
        WebSocketTransportRegistration registration = mock(WebSocketTransportRegistration.class);

        // Act
        webSocketConfig.configureWebSocketTransport(registration);

        // Verify transport settings
        verify(registration).setMessageSizeLimit(8192);
        verify(registration).setSendBufferSizeLimit(8192);
        verify(registration).setSendTimeLimit(20000);

        // Verify no other interactions
        verifyNoMoreInteractions(registration);
    }

    @Test
    void testEndpointHandshakeHandler() {
        // Arrange
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration registration = 
            mock(org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws")).thenReturn(registration);
        when(registration.setAllowedOrigins(anyString())).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(registration);
        when(registration.setHandshakeHandler(any())).thenReturn(registration);

        // Act
        webSocketConfig.registerStompEndpoints(registry);

        // Assert
        verify(registry).addEndpoint("/ws");
        verify(registration).setAllowedOrigins("${cors.allowed-origins}");
        verify(registration).withSockJS();
    }

    @Test
    void testClientInboundChannel() {
        // Arrange
        org.springframework.messaging.simp.config.ChannelRegistration registration = 
            mock(org.springframework.messaging.simp.config.ChannelRegistration.class);

        // Act
        webSocketConfig.configureClientInboundChannel(registration);

        // Assert
        verify(registration).taskExecutor()
            .thenReturn(mock(org.springframework.messaging.simp.config.TaskExecutorRegistration.class));
    }

    @Test
    void testClientOutboundChannel() {
        // Arrange
        org.springframework.messaging.simp.config.ChannelRegistration registration = 
            mock(org.springframework.messaging.simp.config.ChannelRegistration.class);

        // Act
        webSocketConfig.configureClientOutboundChannel(registration);

        // Assert
        verify(registration).taskExecutor()
            .thenReturn(mock(org.springframework.messaging.simp.config.TaskExecutorRegistration.class));
    }
}
