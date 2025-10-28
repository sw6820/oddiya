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
                            <div class="empty-title">아직 여행 계획이 없습니다</div>
                            <div class="empty-text">첫 AI 여행 계획을 만들어보세요!</div>
                        </div>
                    `;
                } else {
                    container.innerHTML = plans.map(plan => {
                        const statusBadges = {
                            'DRAFT': {text: '📝 초안', color: '#999'},
                            'CONFIRMED': {text: '✅ 확정', color: '#4CAF50'},
                            'IN_PROGRESS': {text: '✈️ 진행중', color: '#2196F3'},
                            'COMPLETED': {text: '✨ 완료', color: '#9C27B0'},
                            'CANCELLED': {text: '❌ 취소', color: '#F44336'}
                        };
                        const badge = statusBadges[plan.status] || statusBadges['DRAFT'];
                        
                        return `
                        <div class="card" onclick="showPlanDetails(${plan.id})" style="cursor: pointer;">
                            <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;">
                                <div class="card-title">${plan.title}</div>
                                <span style="background: ${badge.color}; color: white; padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; white-space: nowrap;">
                                    ${badge.text}
                                </span>
                            </div>
                            <div class="card-subtitle">
                                ${new Date(plan.startDate).toLocaleDateString('ko-KR')} - 
                                ${new Date(plan.endDate).toLocaleDateString('ko-KR')}
                            </div>
                            ${plan.details && plan.details.length > 0 ? 
                                `<div class="card-meta">📍 ${plan.details.length}개 일정</div>` : ''}
                            ${plan.photos && plan.photos.length > 0 ?
                                `<div class="card-meta">📸 사진 ${plan.photos.length}장</div>` : ''}
                            <div style="margin-top: 12px; color: #667eea; font-size: 14px; font-weight: 600;">
                                👆 탭하여 상세보기
                            </div>
                        </div>
                    `;
                    }).join('');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to load plans</div>';
            }
        }
        
        // Show Plan Details
        async function showPlanDetails(planId) {
            const container = document.getElementById('plans-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Loading details...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/plans/${planId}`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const plan = await response.json();
                
                let detailsHTML = '';
                if (plan.details && plan.details.length > 0) {
                    detailsHTML = plan.details.map(detail => {
                        // Parse activity string into structured parts
                        const activities = detail.activity.split(', ').map(act => {
                            const match = act.match(/(Morning|Afternoon|Evening):\\s*(.+?)\\s*\\((₩[^)]+)\\)/);
                            if (match) {
                                return {
                                    period: match[1],
                                    description: match[2],
                                    cost: match[3]
                                };
                            }
                            return null;
                        }).filter(Boolean);
                        
                        const icons = {
                            'Morning': '🌅',
                            'Afternoon': '☀️',
                            'Evening': '🌙'
                        };
                        
                        const koreanPeriods = {
                            'Morning': '오전',
                            'Afternoon': '오후',
                            'Evening': '저녁'
                        };
                        
                        return `
                        <div class="card">
                            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 16px; margin: -16px -16px 16px -16px; border-radius: 12px 12px 0 0;">
                                <div style="color: white; font-size: 16px; font-weight: 700;">Day ${detail.day}</div>
                                <div style="color: rgba(255,255,255,0.9); font-size: 20px; font-weight: 700; margin-top: 4px;">
                                    📍 ${detail.location}
                                </div>
                            </div>
                            
                            ${activities.length > 0 ? activities.map(act => `
                                <div style="background: #f8f9fa; padding: 14px; border-radius: 10px; margin-bottom: 10px; border-left: 4px solid #667eea;">
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                                        <div style="font-size: 15px; font-weight: 700; color: #667eea;">
                                            ${icons[act.period]} ${koreanPeriods[act.period]}
                                        </div>
                                        <div style="background: #667eea; color: white; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: 600;">
                                            ${act.cost}
                                        </div>
                                    </div>
                                    <div style="color: #333; font-size: 14px; line-height: 1.5;">
                                        ${act.description}
                                    </div>
                                </div>
                            `).join('') : `
                                <div style="color: #666; font-size: 14px; line-height: 1.6;">
                                    ${detail.activity}
                                </div>
                            `}
                            
                            ${detail.weatherTip ? `
                                <div style="margin-top: 12px; padding: 12px; background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%); border-radius: 10px; border-left: 4px solid #2196F3;">
                                    <div style="font-size: 13px; font-weight: 600; color: #1976D2; margin-bottom: 4px;">
                                        🌤️ 날씨 정보
                                    </div>
                                    <div style="font-size: 13px; color: #333;">
                                        ${detail.weatherTip}
                                    </div>
                                </div>
                            ` : ''}
                        </div>
                    `;
                    }).join('');
                }
                
                // Calculate total budget from activities
                let totalBudget = 0;
                if (plan.details) {
                    plan.details.forEach(detail => {
                        const costs = detail.activity.match(/₩([0-9,]+)/g);
                        if (costs) {
                            costs.forEach(cost => {
                                totalBudget += parseInt(cost.replace(/[₩,]/g, ''));
                            });
                        }
                    });
                }
                
                container.innerHTML = `
                    <button class="button button-secondary" onclick="loadPlans()" style="margin-bottom: 16px;">
                        ← 목록으로
                    </button>
                    
                    <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 24px; border-radius: 16px; color: white; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);">
                        <div style="font-size: 24px; font-weight: 700; margin-bottom: 8px;">
                            ${plan.title}
                        </div>
                        <div style="font-size: 14px; opacity: 0.9; margin-bottom: 16px;">
                            📅 ${new Date(plan.startDate).toLocaleDateString('ko-KR', {year: 'numeric', month: 'long', day: 'numeric'})} - 
                            ${new Date(plan.endDate).toLocaleDateString('ko-KR', {month: 'long', day: 'numeric'})}
                        </div>
                        ${totalBudget > 0 ? `
                            <div style="background: rgba(255,255,255,0.2); padding: 12px; border-radius: 10px; display: inline-block;">
                                <div style="font-size: 13px; opacity: 0.9; margin-bottom: 4px;">예상 총 경비</div>
                                <div style="font-size: 28px; font-weight: 700;">₩${totalBudget.toLocaleString()}</div>
                            </div>
                        ` : ''}
                    </div>
                    
                    <div style="font-size: 18px; font-weight: 700; color: #333; margin-bottom: 16px;">
                        📋 일별 여행 일정
                    </div>
                    
                    ${detailsHTML}
                    
                    ${plan.tips && plan.tips.length > 0 ? `
                        <div class="card" style="background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%); border: 2px solid #ffc107;">
                            <div style="font-size: 18px; font-weight: 700; color: #f57c00; margin-bottom: 12px;">
                                💡 여행 팁
                            </div>
                            ${plan.tips.map(tip => `
                                <div style="padding: 8px 0; border-bottom: 1px dashed #ffe082; color: #333;">
                                    ${tip}
                                </div>
                            `).join('')}
                        </div>
                    ` : ''}
                    
                    <!-- Action buttons based on status -->
                    <div style="margin-top: 20px;">
                        ${plan.status === 'DRAFT' ? `
                            <button class="button" onclick="confirmPlan(${plan.id})">
                                ✅ 이 계획으로 확정하기
                            </button>
                        ` : ''}
                        
                        ${(plan.status === 'CONFIRMED' || plan.status === 'IN_PROGRESS') && isPastDate(plan.endDate) ? `
                            <button class="button" onclick="showPhotoUpload(${plan.id})">
                                📸 여행 사진 추가하기
                            </button>
                        ` : ''}
                        
                        ${plan.photos && plan.photos.length > 0 && !plan.videoId ? `
                            <button class="button" onclick="createVideoFromPlan(${plan.id})">
                                🎬 사진으로 영상 만들기 (${plan.photos.length}장)
                            </button>
                        ` : ''}
                        
                        ${plan.videoId ? `
                            <button class="button" onclick="playVideo(${plan.videoId})">
                                ▶️ 여행 영상 보기
                            </button>
                        ` : ''}
                        
                        <button class="button button-secondary" onclick="loadPlans()">
                            ← 목록으로 돌아가기
                        </button>
                    </div>
                `;
            } catch (error) {
                container.innerHTML = '<div class="error-message">계획을 불러오는데 실패했습니다</div>';
                setTimeout(loadPlans, 2000);
            }
        }
        
        // Helper function
        function isPastDate(dateString) {
            return new Date(dateString) < new Date();
        }
        
        // Confirm Plan
        async function confirmPlan(planId) {
            if (!confirm('이 계획을 확정하시겠습니까?')) return;
            
            try {
                await fetch(`${API_BASE}/api/plans/${planId}/confirm`, {
                    method: 'PATCH',
                    headers: {'X-User-Id': USER_ID}
                });
                
                showToast('✅ 여행 계획이 확정되었습니다!');
                showPlanDetails(planId);
            } catch (error) {
                showToast('❌ 확정에 실패했습니다');
            }
        }
        
        // Photo Upload
        function showPhotoUpload(planId) {
            const container = document.getElementById('plans-list');
            container.innerHTML = `
                <div class="card">
                    <h3 style="margin-bottom: 16px;">📸 여행 사진 추가</h3>
                    <p style="color: #666; margin-bottom: 16px;">
                        여행의 추억을 사진으로 남겨보세요!<br/>
                        최대 10장까지 업로드 가능합니다.
                    </p>
                    
                    <input type="file" 
                           id="photo-files" 
                           multiple 
                           accept="image/*"
                           style="margin-bottom: 16px;"
                           onchange="previewPhotos(event)">
                    
                    <div id="photo-previews" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(100px, 1fr)); gap: 10px; margin-bottom: 16px;"></div>
                    
                    <button class="button" onclick="uploadPhotos(${planId})" id="upload-btn" disabled>
                        ☁️ 사진 업로드
                    </button>
                    <button class="button button-secondary" onclick="showPlanDetails(${planId})">
                        취소
                    </button>
                </div>
            `;
        }
        
        function previewPhotos(event) {
            const files = event.target.files;
            const previews = document.getElementById('photo-previews');
            const uploadBtn = document.getElementById('upload-btn');
            
            if (files.length > 0) {
                uploadBtn.disabled = false;
            }
            
            previews.innerHTML = '';
            for (let i = 0; i < Math.min(files.length, 10); i++) {
                const file = files[i];
                const reader = new FileReader();
                reader.onload = (e) => {
                    previews.innerHTML += `
                        <img src="${e.target.result}" 
                             style="width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 8px;">
                    `;
                };
                reader.readAsDataURL(file);
            }
        }
        
        async function uploadPhotos(planId) {
            const files = document.getElementById('photo-files').files;
            const container = document.getElementById('plans-list');
            
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>사진 업로드 중...</p></div>';
            
            try {
                for (let i = 0; i < Math.min(files.length, 10); i++) {
                    // For now, use photo placeholder
                    // In production, would upload to S3
                    const photoUrl = `https://picsum.photos/1080/1920?random=${Date.now()}_${i}`;
                    
                    await fetch(`${API_BASE}/api/plans/${planId}/photos`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-User-Id': USER_ID
                        },
                        body: JSON.stringify({
                            photoUrl: photoUrl,
                            s3Key: `photos/user${USER_ID}/plan${planId}/photo${i}.jpg`,
                            order: i + 1
                        })
                    });
                }
                
                showToast(`✅ ${files.length}장의 사진이 업로드되었습니다!`);
                showPlanDetails(planId);
            } catch (error) {
                showToast('❌ 업로드에 실패했습니다');
                showPlanDetails(planId);
            }
        }
        
        // Create Video from Plan
        async function createVideoFromPlan(planId) {
            if (!confirm('업로드한 사진으로 영상을 만드시겠습니까?')) return;
            
            try {
                const response = await fetch(`${API_BASE}/api/plans/${planId}/create-video`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': USER_ID,
                        'Idempotency-Key': crypto.randomUUID()
                    },
                    body: JSON.stringify({template: 'default'})
                });
                
                const video = await response.json();
                
                showToast('🎬 영상 생성이 시작되었습니다! (2-3분 소요)');
                
                // Poll video status
                pollVideoStatus(video.id, planId);
                
            } catch (error) {
                showToast('❌ 영상 생성 요청에 실패했습니다');
            }
        }
        
        function pollVideoStatus(videoId, planId) {
            const checkInterval = setInterval(async () => {
                try {
                    const response = await fetch(`${API_BASE}/api/videos/${videoId}`, {
                        headers: {'X-User-Id': USER_ID}
                    });
                    const video = await response.json();
                    
                    if (video.status === 'COMPLETED') {
                        clearInterval(checkInterval);
                        showToast('🎉 영상이 완성되었습니다!');
                        showPlanDetails(planId);
                    } else if (video.status === 'FAILED') {
                        clearInterval(checkInterval);
                        showToast('❌ 영상 생성에 실패했습니다');
                    }
                } catch (error) {
                    clearInterval(checkInterval);
                }
            }, 5000);  // Check every 5 seconds
        }
        
        // Play Video (placeholder)
        function playVideo(videoId) {
            showToast('🎬 영상 재생 기능은 곧 추가됩니다!');
        }
        
        // Load Profile with Trip Collection
        async function loadProfile() {
            const container = document.getElementById('profile-info');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>프로필 로딩 중...</p></div>';
            
            try {
                // Get user profile
                const userResponse = await fetch(`${API_BASE}/api/users/me`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const user = await userResponse.json();
                
                // Get trip collection
                const tripsResponse = await fetch(`${API_BASE}/api/profile/trips`, {
                    headers: { 'X-User-Id': USER_ID }
                });
                const trips = await tripsResponse.json();
                
                container.innerHTML = `
                    <!-- User Info -->
                    <div class="card">
                        <div class="card-title">${user.name}</div>
                        <div class="card-subtitle">${user.email}</div>
                        <div class="card-meta">
                            <span>가입일: ${new Date(user.createdAt).toLocaleDateString('ko-KR')}</span>
                        </div>
                    </div>
                    
                    <!-- Statistics -->
                    <div class="card" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
                        <div style="font-size: 18px; font-weight: 700; margin-bottom: 16px;">
                            📊 여행 통계
                        </div>
                        <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px;">
                            <div style="text-align: center;">
                                <div style="font-size: 32px; font-weight: 700;">${trips.statistics?.totalTrips || 0}</div>
                                <div style="font-size: 13px; opacity: 0.9;">총 여행</div>
                            </div>
                            <div style="text-align: center;">
                                <div style="font-size: 32px; font-weight: 700;">${trips.statistics?.totalDays || 0}</div>
                                <div style="font-size: 13px; opacity: 0.9;">여행 일수</div>
                            </div>
                            <div style="text-align: center;">
                                <div style="font-size: 32px; font-weight: 700;">${trips.statistics?.citiesVisited?.length || 0}</div>
                                <div style="font-size: 13px; opacity: 0.9;">방문 도시</div>
                            </div>
                            <div style="text-align: center;">
                                <div style="font-size: 32px; font-weight: 700;">${trips.statistics?.totalVideos || 0}</div>
                                <div style="font-size: 13px; opacity: 0.9;">여행 영상</div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Upcoming Trips -->
                    ${trips.upcomingTrips && trips.upcomingTrips.length > 0 ? `
                        <h3 style="margin: 20px 0 12px 0;">🎒 다가오는 여행</h3>
                        ${trips.upcomingTrips.map(trip => `
                            <div class="card" onclick="showPlanDetails(${trip.id})" style="cursor: pointer; border-left: 4px solid #4CAF50;">
                                <div class="card-title">${trip.title}</div>
                                <div style="color: #4CAF50; font-size: 20px; font-weight: 700; margin-top: 8px;">
                                    D-${trip.daysUntil}일
                                </div>
                            </div>
                        `).join('')}
                    ` : ''}
                    
                    <!-- Completed Trips -->
                    ${trips.completedTrips && trips.completedTrips.length > 0 ? `
                        <h3 style="margin: 20px 0 12px 0;">✨ 완료된 여행</h3>
                        ${trips.completedTrips.map(trip => `
                            <div class="card" onclick="showPlanDetails(${trip.plan.id})" style="cursor: pointer;">
                                ${trip.photos && trip.photos.length > 0 ? `
                                    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 4px; margin-bottom: 12px; border-radius: 8px; overflow: hidden;">
                                        ${trip.photos.slice(0, 3).map(photo => `
                                            <img src="${photo.url}" style="width: 100%; aspect-ratio: 1; object-fit: cover;">
                                        `).join('')}
                                    </div>
                                ` : ''}
                                <div class="card-title">${trip.plan.title}</div>
                                <div class="card-meta">
                                    📅 ${new Date(trip.plan.startDate).toLocaleDateString('ko-KR')} - 
                                    ${new Date(trip.plan.endDate).toLocaleDateString('ko-KR')}
                                </div>
                                <div class="card-meta">
                                    📸 사진 ${trip.photos?.length || 0}장
                                    ${trip.video ? ' • 🎬 영상 완성' : ''}
                                </div>
                                ${trip.plan.totalCost ? `
                                    <div class="card-meta">
                                        💰 총 경비: ₩${trip.plan.totalCost.toLocaleString()}
                                    </div>
                                ` : ''}
                            </div>
                        `).join('')}
                    ` : ''}
                    
                    <button class="button button-secondary" onclick="loadPlans()" style="margin-top: 20px;">
                        ← 목록으로 돌아가기
                    </button>
                `;
            } catch (error) {
                container.innerHTML = '<div class="error-message">프로필을 불러오는데 실패했습니다</div>';
            }
        }
            } catch (error) {
                container.innerHTML = '<div class="error-message">Failed to load plan details</div>';
                setTimeout(loadPlans, 2000);
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
                    <h3 style="margin-bottom: 16px;">새 여행 계획 만들기</h3>
                    
                    <div class="form-group">
                        <label class="form-label">여행지 (자유 입력)</label>
                        <input type="text" class="form-input" id="plan-location" placeholder="예: 서울, 부산, 제주도, 경주, 전주, 속초 등" style="font-size: 16px;">
                        <div style="font-size: 12px; color: #666; margin-top: 4px;">
                            💡 한국의 모든 도시/지역 입력 가능 (AI가 해당 지역의 실제 명소를 찾아드립니다)
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">여행 제목 (선택)</label>
                        <input type="text" class="form-input" id="plan-title" placeholder="예: 힐링 여행, 미식 투어, 가족 여행 등">
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">시작일</label>
                        <input type="date" class="form-input" id="plan-start">
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">종료일</label>
                        <input type="date" class="form-input" id="plan-end">
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">예산 수준</label>
                        <select class="form-input" id="plan-budget" style="font-size: 16px;">
                            <option value="low">💰 저예산 (1일 ₩50,000)</option>
                            <option value="medium" selected>💰💰 중예산 (1일 ₩100,000)</option>
                            <option value="high">💰💰💰 고예산 (1일 ₩200,000)</option>
                        </select>
                    </div>
                    
                    <button class="button" onclick="createPlan()">🤖 AI 여행 계획 생성</button>
                    <button class="button button-secondary" onclick="loadPlans()">취소</button>
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
            const location = document.getElementById('plan-location').value;
            const title = document.getElementById('plan-title').value;
            const startDate = document.getElementById('plan-start').value;
            const endDate = document.getElementById('plan-end').value;
            const budget = document.getElementById('plan-budget').value;
            
            if (!title || !startDate || !endDate) {
                showToast('❌ 모든 항목을 입력해주세요');
                return;
            }
            
            // Create meaningful title
            const finalTitle = title.trim() 
                ? (title.includes(location) ? title : `${location} ${title}`)
                : `${location} 여행`;
            
            const container = document.getElementById('plans-list');
            container.innerHTML = '<div class="loading"><div class="spinner"></div><p>AI가 여행 계획을 생성하고 있습니다...</p></div>';
            
            try {
                const response = await fetch(`${API_BASE}/api/plans`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': USER_ID
                    },
                    body: JSON.stringify({ 
                        title: finalTitle,
                        startDate, 
                        endDate
                    })
                });
                
                if (response.ok) {
                    showToast('✅ 여행 계획이 생성되었습니다!');
                    loadPlans();
                } else {
                    throw new Error('Failed to create plan');
                }
            } catch (error) {
                container.innerHTML = '<div class="error-message">계획 생성에 실패했습니다. 다시 시도해주세요.</div>';
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

