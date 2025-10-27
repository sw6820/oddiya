package com.oddiya.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class WebAppController {

    @GetMapping(value = "/app", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> mobileApp() {
        return Mono.just("""
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="default">
    <title>Oddiya - AI Travel Planner</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            color: #333;
        }
        
        .app {
            max-width: 600px;
            margin: 0 auto;
            background: #fff;
            min-height: 100vh;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            color: white;
            text-align: center;
        }
        
        .header h1 {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 5px;
        }
        
        .header p {
            font-size: 14px;
            opacity: 0.9;
        }
        
        .nav {
            display: flex;
            background: #fff;
            border-bottom: 1px solid #e0e0e0;
            position: sticky;
            top: 0;
            z-index: 100;
        }
        
        .nav-item {
            flex: 1;
            padding: 15px;
            text-align: center;
            cursor: pointer;
            border-bottom: 3px solid transparent;
            font-weight: 600;
            color: #666;
            transition: all 0.3s;
        }
        
        .nav-item.active {
            color: #667eea;
            border-bottom-color: #667eea;
        }
        
        .content {
            padding: 20px;
        }
        
        .page {
            display: none;
        }
        
        .page.active {
            display: block;
        }
        
        .card {
            background: #fff;
            border-radius: 12px;
            padding: 16px;
            margin-bottom: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border: 1px solid #e0e0e0;
        }
        
        .card-title {
            font-size: 18px;
            font-weight: 700;
            color: #333;
            margin-bottom: 8px;
        }
        
        .card-subtitle {
            font-size: 14px;
            color: #666;
            margin-bottom: 12px;
        }
        
        .card-meta {
            display: flex;
            gap: 12px;
            font-size: 13px;
            color: #999;
        }
        
        .button {
            background: #667eea;
            color: white;
            border: none;
            padding: 14px 24px;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            width: 100%;
            margin-top: 12px;
            transition: background 0.3s;
        }
        
        .button:active {
            background: #5568d3;
        }
        
        .button-secondary {
            background: #f0f0f0;
            color: #333;
        }
        
        .button-secondary:active {
            background: #e0e0e0;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }
        
        .empty-icon {
            font-size: 64px;
            margin-bottom: 16px;
        }
        
        .empty-title {
            font-size: 20px;
            font-weight: 700;
            color: #333;
            margin-bottom: 8px;
        }
        
        .empty-text {
            font-size: 14px;
            color: #666;
            margin-bottom: 24px;
        }
        
        .status-badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }
        
        .status-completed { background: #4CAF50; color: white; }
        .status-processing { background: #FF9800; color: white; }
        .status-pending { background: #2196F3; color: white; }
        .status-failed { background: #FF3B30; color: white; }
        
        .loading {
            text-align: center;
            padding: 40px;
        }
        
        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #667eea;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto 16px;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .form-group {
            margin-bottom: 16px;
        }
        
        .form-label {
            display: block;
            font-size: 14px;
            font-weight: 600;
            color: #333;
            margin-bottom: 6px;
        }
        
        .form-input {
            width: 100%;
            padding: 12px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
        }
        
        .error-message {
            background: #ffebee;
            color: #d32f2f;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 16px;
        }
        
        .toast {
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #323232;
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            z-index: 1000;
            display: none;
        }
        
        .toast.show {
            display: block;
            animation: slideDown 0.3s;
        }
        
        @keyframes slideDown {
            from { transform: translate(-50%, -100px); opacity: 0; }
            to { transform: translate(-50%, 0); opacity: 1; }
        }
    </style>
</head>
<body>
    <div class="app">
        <div class="header">
            <h1>🌏 Oddiya</h1>
            <p>AI-Powered Travel Planner</p>
        </div>
        
        <div class="nav">
            <div class="nav-item active" onclick="showPage('plans')">📝 Plans</div>
            <div class="nav-item" onclick="showPage('videos')">🎥 Videos</div>
            <div class="nav-item" onclick="showPage('profile')">👤 Profile</div>
        </div>
        
        <div class="content">
            <!-- Plans Page -->
            <div id="plans-page" class="page active">
                <h2 style="margin-bottom: 16px;">My Travel Plans</h2>
                <div id="plans-list"></div>
                <button class="button" onclick="showCreatePlanForm()">+ Create New Plan</button>
            </div>
            
            <!-- Videos Page -->
            <div id="videos-page" class="page">
                <h2 style="margin-bottom: 16px;">My Videos</h2>
                <div id="videos-list"></div>
                <button class="button" onclick="showCreateVideoForm()">+ Create Video</button>
            </div>
            
            <!-- Profile Page -->
            <div id="profile-page" class="page">
                <h2 style="margin-bottom: 16px;">Profile</h2>
                <div id="profile-info"></div>
            </div>
        </div>
    </div>
    
    <div id="toast" class="toast"></div>
    
    <script>
        const API_BASE = window.location.origin;
        const USER_ID = 1; // Demo user
        
        // Show page
        function showPage(pageName) {
            document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
            document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
            
            document.getElementById(pageName + '-page').classList.add('active');
            event.target.classList.add('active');
            
            if (pageName === 'plans') loadPlans();
            if (pageName === 'videos') loadVideos();
            if (pageName === 'profile') loadProfile();
        }
        
        // Toast notification
        function showToast(message) {
            const toast = document.getElementById('toast');
            toast.textContent = message;
            toast.classList.add('show');
            setTimeout(() => toast.classList.remove('show'), 3000);
        }
        
        // Load Plans
        async function loadPlans() {
            const container = document.getElementById('plans-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading plans...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/plans`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const plans = await response.json();
                
                if (plans.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-icon">📝</div>
                            <div class="empty-title">No travel plans yet</div>
                            <div class="empty-text">Create your first AI-powered travel plan!</div>
                        </div>
                    `;
                } else {
                    container.innerHTML = plans.map(plan => `
                        <div class="card">
                            <div class="card-title">${plan.title}</div>
                            <div class="card-subtitle">
                                ${new Date(plan.startDate).toLocaleDateString()} - 
                                ${new Date(plan.endDate).toLocaleDateString()}
                            </div>
                            ${plan.details && plan.details.length > 0 ? 
                                `<div class="card-meta">📍 ${plan.details.length} activities</div>` : ''}
                        </div>
                    `).join('');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to load plans</div>';
            }
        }
        
        // Load Videos
        async function loadVideos() {
            const container = document.getElementById('videos-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading videos...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/videos`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const videos = await response.json();
                
                if (videos.length === 0) {
                    container.innerHTML = `
                        <div class="empty-state">
                            <div class="empty-icon">🎥</div>
                            <div class="empty-title">No videos yet</div>
                            <div class="empty-text">Create amazing travel videos from your photos!</div>
                        </div>
                    `;
                } else {
                    container.innerHTML = videos.map(video => `
                        <div class="card">
                            <div class="card-title">📷 ${video.photoUrls.length} Photos</div>
                            <div class="card-subtitle">
                                <span class="status-badge status-${video.status.toLowerCase()}">${video.status}</span>
                            </div>
                            ${video.videoUrl ? `<button class="button" onclick="window.open('${video.videoUrl}')">▶️ Watch Video</button>` : ''}
                        </div>
                    `).join('');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to load videos</div>';
            }
        }
        
        // Load Profile
        async function loadProfile() {
            const container = document.getElementById('profile-info');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading profile...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/users/me`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const user = await response.json();
                
                container.innerHTML = `
                    <div class="card">
                        <div class="card-title">${user.name}</div>
                        <div class="card-subtitle">${user.email}</div>
                        <div class="card-meta">
                            <span>Provider: ${user.provider}</span>
                        </div>
                        <div class="card-meta">
                            <span>Member since: ${new Date(user.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                `;
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to load profile</div>';
            }
        }
        
        // Create Plan Form
        function showCreatePlanForm() {
            const container = document.getElementById('plans-list');
            container.innerHTML = `
                <div class="card">
                    <h3 style="margin-bottom: 16px;">Create New Travel Plan</h3>
                    <div class="form-group">
                        <label class="form-label">Title</label>
                        <input type="text" class="form-input" id="plan-title" placeholder="Seoul Weekend Trip">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Start Date</label>
                        <input type="date" class="form-input" id="plan-start">
                    </div>
                    <div class="form-group">
                        <label class="form-label">End Date</label>
                        <input type="date" class="form-input" id="plan-end">
                    </div>
                    <button class="button" onclick="createPlan()">🤖 Generate AI Plan</button>
                    <button class="button button-secondary" onclick="loadPlans()">Cancel</button>
                </div>
            `;
            
            // Set default dates
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            document.getElementById('plan-start').value = tomorrow.toISOString().split('T')[0];
            
            const nextWeek = new Date();
            nextWeek.setDate(nextWeek.getDate() + 3);
            document.getElementById('plan-end').value = nextWeek.toISOString().split('T')[0];
        }
        
        // Create Plan
        async function createPlan() {
            const title = document.getElementById('plan-title').value;
            const startDate = document.getElementById('plan-start').value;
            const endDate = document.getElementById('plan-end').value;
            
            if (!title || !startDate || !endDate) {
                showToast('❌ Please fill in all fields');
                return;
            }
            
            const container = document.getElementById('plans-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>AI is creating your plan...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/plans`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': USER_ID
                    },
                    body: JSON.stringify({ title, startDate, endDate })
                });
                
                if (response.ok) {
                    showToast('✅ Plan created successfully!');
                    loadPlans();
                } else {
                    throw new Error('Failed to create plan');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to create plan. Please try again.</div>';
                setTimeout(loadPlans, 2000);
            }
        }
        
        // Create Video Form
        function showCreateVideoForm() {
            const container = document.getElementById('videos-list');
            container.innerHTML = `
                <div class="card">
                    <h3 style="margin-bottom: 16px;">Create Travel Video</h3>
                    <p style="color: #666; margin-bottom: 16px;">
                        📷 Upload your travel photos and we'll create an amazing video!
                    </p>
                    <div class="form-group">
                        <label class="form-label">Photo URL 1</label>
                        <input type="text" class="form-input" id="photo-1" placeholder="https://example.com/photo1.jpg">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Photo URL 2</label>
                        <input type="text" class="form-input" id="photo-2" placeholder="https://example.com/photo2.jpg">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Photo URL 3</label>
                        <input type="text" class="form-input" id="photo-3" placeholder="https://example.com/photo3.jpg">
                    </div>
                    <button class="button" onclick="createVideo()">🎬 Create Video</button>
                    <button class="button button-secondary" onclick="loadVideos()">Cancel</button>
                </div>
            `;
        }
        
        // Create Video
        async function createVideo() {
            const photo1 = document.getElementById('photo-1').value;
            const photo2 = document.getElementById('photo-2').value;
            const photo3 = document.getElementById('photo-3').value;
            
            const photoUrls = [photo1, photo2, photo3].filter(url => url);
            
            if (photoUrls.length === 0) {
                showToast('❌ Please provide at least one photo URL');
                return;
            }
            
            const container = document.getElementById('videos-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Creating video job...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/videos`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': USER_ID,
                        'Idempotency-Key': crypto.randomUUID()
                    },
                    body: JSON.stringify({ photoUrls, template: 'default' })
                });
                
                if (response.ok) {
                    showToast('✅ Video job created! Processing...');
                    loadVideos();
                } else {
                    throw new Error('Failed to create video');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to create video. Please try again.</div>';
                setTimeout(loadVideos, 2000);
            }
        }
        
        // Initialize
        loadPlans();
    </script>
</body>
</html>
                """);
    }
}

