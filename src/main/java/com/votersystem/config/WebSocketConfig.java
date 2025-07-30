package com.votersystem.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.votersystem.util.JwtUtil;

/**
 * WebSocket configuration for real-time agent tracking
 * Enables STOMP messaging with JWT authentication
 */
@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback
        registry.addEndpoint("/ws/location")
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("https://api.expengo.com", "http://localhost:3000","http://localhost:5173")
                .withSockJS()
                .setWebSocketEnabled(true)
                .setHeartbeatTime(25000)
                .setDisconnectDelay(30000);
        
        // Register endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws/location")
                .setAllowedOriginPatterns("*")
                .setAllowedOrigins("https://api.expengo.com", "http://localhost:3000","http://localhost:5173");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from headers
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        String token = authToken.substring(7);
                        
                        try {
                            // Validate JWT token
                            if (jwtUtil.validateToken(token)) {
                                String username = jwtUtil.extractUsername(token);
                                String userType = jwtUtil.extractUserType(token);
                                String userId = jwtUtil.extractUserId(token); // Get actual agent ID

                                // Create authentication object
                                List<SimpleGrantedAuthority> authorities = List.of(
                                    new SimpleGrantedAuthority("ROLE_" + userType)
                                );

                                UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(username, null, authorities);

                                // Set authentication in accessor
                                accessor.setUser(authentication);

                                // Store user info in session attributes
                                accessor.getSessionAttributes().put("username", username); // Mobile number
                                accessor.getSessionAttributes().put("userId", userId);     // Actual agent ID
                                accessor.getSessionAttributes().put("userType", userType);
                                
                                System.out.println("WebSocket connection authenticated for user: " + username + " (" + userType + "), agentId: " + userId);
                            } else {
                                System.err.println("Invalid JWT token for WebSocket connection");
                                return null; // Reject connection
                            }
                        } catch (Exception e) {
                            System.err.println("Error validating JWT token for WebSocket: " + e.getMessage());
                            return null; // Reject connection
                        }
                    } else {
                        System.err.println("No Authorization header found for WebSocket connection");
                        return null; // Reject connection
                    }
                }
                
                return message;
            }
        });
    }
}
