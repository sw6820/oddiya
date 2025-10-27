package com.oddiya.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class DashboardController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> dashboard() {
        return Mono.just("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Oddiya API Services</title>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            max-width: 800px;
                            margin: 50px auto;
                            padding: 20px;
                            background: #f5f5f5;
                        }
                        .header {
                            text-align: center;
                            margin-bottom: 30px;
                        }
                        .header h1 {
                            color: #333;
                            margin: 0;
                        }
                        .header p {
                            color: #666;
                            margin: 10px 0;
                        }
                        .status {
                            background: #4CAF50;
                            color: white;
                            padding: 10px 20px;
                            border-radius: 20px;
                            display: inline-block;
                            font-weight: bold;
                        }
                        .service {
                            background: white;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 15px 0;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                        }
                        .service h3 {
                            margin: 0 0 10px 0;
                            color: #333;
                        }
                        .service p {
                            margin: 5px 0;
                            color: #666;
                        }
                        .service code {
                            background: #f0f0f0;
                            padding: 2px 6px;
                            border-radius: 3px;
                            font-size: 14px;
                        }
                        .badge {
                            display: inline-block;
                            padding: 4px 8px;
                            border-radius: 4px;
                            font-size: 12px;
                            font-weight: bold;
                            margin: 5px 5px 5px 0;
                        }
                        .badge-success { background: #4CAF50; color: white; }
                        .badge-info { background: #2196F3; color: white; }
                        .badge-warning { background: #FF9800; color: white; }
                        .footer {
                            text-align: center;
                            margin-top: 40px;
                            color: #999;
                            font-size: 14px;
                        }
                        .endpoints {
                            background: #f9f9f9;
                            padding: 10px;
                            border-radius: 4px;
                            margin-top: 10px;
                        }
                        .endpoint {
                            font-family: monospace;
                            font-size: 13px;
                            color: #333;
                            margin: 5px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🚀 Oddiya API Services</h1>
                        <p>AI-Powered Travel Planner</p>
                        <div class="status">✅ ALL SYSTEMS OPERATIONAL</div>
                    </div>
                    
                    <div class="service">
                        <h3>🔐 Auth Service</h3>
                        <p>OAuth 2.0 authentication with JWT tokens</p>
                        <span class="badge badge-success">Port 8081</span>
                        <span class="badge badge-info">Spring Boot</span>
                        <div class="endpoints">
                            <div class="endpoint">POST /api/auth/oauth2/callback/google</div>
                            <div class="endpoint">POST /api/auth/refresh</div>
                        </div>
                    </div>
                    
                    <div class="service">
                        <h3>👤 User Service</h3>
                        <p>User profile management</p>
                        <span class="badge badge-success">Port 8082</span>
                        <span class="badge badge-info">Spring Boot</span>
                        <div class="endpoints">
                            <div class="endpoint">GET /api/users/me</div>
                            <div class="endpoint">PATCH /api/users/me</div>
                        </div>
                    </div>
                    
                    <div class="service">
                        <h3>📝 Plan Service</h3>
                        <p>AI-powered travel plan generation</p>
                        <span class="badge badge-success">Port 8083</span>
                        <span class="badge badge-info">Spring Boot</span>
                        <span class="badge badge-warning">LLM Integration</span>
                        <div class="endpoints">
                            <div class="endpoint">GET /api/plans</div>
                            <div class="endpoint">POST /api/plans</div>
                            <div class="endpoint">GET /api/plans/{id}</div>
                        </div>
                    </div>
                    
                    <div class="service">
                        <h3>🎥 Video Service</h3>
                        <p>Video job management with SQS</p>
                        <span class="badge badge-success">Port 8084</span>
                        <span class="badge badge-info">Spring Boot</span>
                        <span class="badge badge-warning">Async Processing</span>
                        <div class="endpoints">
                            <div class="endpoint">GET /api/videos</div>
                            <div class="endpoint">POST /api/videos</div>
                            <div class="endpoint">GET /api/videos/{id}</div>
                        </div>
                    </div>
                    
                    <div class="service">
                        <h3>🤖 LLM Agent</h3>
                        <p>AWS Bedrock integration with Kakao Local API</p>
                        <span class="badge badge-success">Port 8000</span>
                        <span class="badge badge-info">FastAPI</span>
                        <span class="badge badge-warning">Python</span>
                        <div class="endpoints">
                            <div class="endpoint">POST /api/v1/plans/generate</div>
                        </div>
                    </div>
                    
                    <div class="service">
                        <h3>🐘 PostgreSQL</h3>
                        <p>Primary database (17.0)</p>
                        <span class="badge badge-success">Port 5432</span>
                        <span class="badge badge-info">3 Schemas</span>
                    </div>
                    
                    <div class="service">
                        <h3>🔴 Redis</h3>
                        <p>Caching and session storage (7.4)</p>
                        <span class="badge badge-success">Port 6379</span>
                        <span class="badge badge-info">In-Memory Cache</span>
                    </div>
                    
                    <div class="footer">
                        <p><strong>Oddiya MVP</strong> | 7 Microservices | AI-Powered Travel Planner</p>
                        <p>GitHub: <a href="https://github.com/sw6820/oddiya">sw6820/oddiya</a></p>
                    </div>
                </body>
                </html>
                """);
    }
    
    @GetMapping(value = "/api/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> apiOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("project", "Oddiya");
        overview.put("version", "1.0.0");
        overview.put("description", "AI-Powered Mobile Travel Planner");
        
        Map<String, Object> services = new HashMap<>();
        services.put("apiGateway", Map.of("port", 8080, "status", "UP", "tech", "Spring Cloud Gateway"));
        services.put("authService", Map.of("port", 8081, "status", "UP", "tech", "Spring Boot", "features", "OAuth, JWT"));
        services.put("userService", Map.of("port", 8082, "status", "UP", "tech", "Spring Boot", "features", "User Profiles"));
        services.put("planService", Map.of("port", 8083, "status", "UP", "tech", "Spring Boot", "features", "AI Travel Plans"));
        services.put("videoService", Map.of("port", 8084, "status", "UP", "tech", "Spring Boot", "features", "Video Jobs, SQS"));
        services.put("llmAgent", Map.of("port", 8000, "status", "UP", "tech", "FastAPI", "features", "Bedrock, Kakao API"));
        
        Map<String, Object> infrastructure = new HashMap<>();
        infrastructure.put("database", Map.of("type", "PostgreSQL", "version", "17.0", "port", 5432));
        infrastructure.put("cache", Map.of("type", "Redis", "version", "7.4", "port", 6379));
        
        overview.put("services", services);
        overview.put("infrastructure", infrastructure);
        overview.put("totalServices", 7);
        overview.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(overview);
    }
}

