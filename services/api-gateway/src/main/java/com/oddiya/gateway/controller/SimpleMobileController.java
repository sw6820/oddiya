package com.oddiya.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class SimpleMobileController {

    @GetMapping(value = "/mobile", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> simpleMobileApp(
            org.springframework.http.server.reactive.ServerHttpResponse response
    ) {
        // 캐시 방지 헤더 추가
        response.getHeaders().setCacheControl("no-cache, no-store, must-revalidate");
        response.getHeaders().setPragma("no-cache");
        response.getHeaders().setExpires(0);
        
        return Mono.just("""
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Oddiya - 여행 계획</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: #f5f5f5;
            padding: 16px;
            min-height: 100vh;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 24px;
            border-radius: 16px;
            margin-bottom: 20px;
            text-align: center;
        }
        .card {
            background: white;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .button {
            background: #667eea;
            color: white;
            border: none;
            padding: 14px 24px;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            width: 100%;
            margin-top: 12px;
            cursor: pointer;
        }
        .button:active { background: #5568d3; }
        .button-secondary {
            background: white;
            color: #667eea;
            border: 2px solid #667eea;
        }
        .button-secondary:active { background: #f0f0f0; }
        .input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 8px;
            font-size: 16px;
            margin-top: 8px;
        }
        .label {
            display: block;
            font-weight: 600;
            color: #333;
            margin-top: 12px;
        }
        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            color: white;
            background: #4CAF50;
        }
        .hidden { display: none; }
        .error {
            color: #d32f2f;
            font-size: 14px;
            margin-top: 8px;
        }
        .success {
            color: #4CAF50;
            font-size: 14px;
            margin-top: 8px;
        }
        .text-center { text-align: center; }
        .link-button {
            background: none;
            border: none;
            color: #667eea;
            cursor: pointer;
            text-decoration: underline;
            font-size: 14px;
            padding: 8px;
        }
        .welcome-screen {
            display: flex;
            flex-direction: column;
            justify-content: center;
            min-height: 60vh;
        }
        .feature-list {
            list-style: none;
            margin: 20px 0;
        }
        .feature-list li {
            padding: 12px 0;
            color: #666;
        }
        .user-info {
            background: #f8f9fa;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 12px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .oauth-button {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            font-size: 16px;
        }
        .oauth-button.google {
            background: white;
            color: #333;
            border: 1px solid #ddd;
        }
        .oauth-button.google:active {
            background: #f5f5f5;
        }
        .oauth-button.apple {
            background: #000;
            color: white;
        }
        .oauth-button.apple:active {
            background: #333;
        }
    </style>
</head>
<body>
    <div class="header">
        <h1>🌏 Oddiya</h1>
        <p>AI 여행 플래너</p>
    </div>

    <!-- Welcome Screen with OAuth -->
    <div id="welcomeScreen" class="hidden">
        <div class="welcome-screen">
            <div class="card">
                <h2 class="text-center">환영합니다! 🎉</h2>
                <ul class="feature-list">
                    <li>🤖 AI가 맞춤 여행 계획을 생성해드려요</li>
                    <li>📸 여행 사진을 업로드하고 관리하세요</li>
                    <li>🎬 추억을 영상으로 만들어드려요</li>
                </ul>

                <div style="margin-top: 24px;">
                    <button class="button oauth-button google" onclick="loginWithGoogle()">
                        <span style="font-size: 20px;">🔵</span> Google로 시작하기
                    </button>
                    <button class="button oauth-button apple" onclick="loginWithApple()">
                        <span style="font-size: 20px;">🍎</span> Apple로 시작하기
                    </button>
                </div>

                <p class="text-center" style="margin-top: 16px; color: #999; font-size: 14px;">
                    로그인하면 <a href="#" style="color: #667eea;">이용약관</a> 및 <a href="#" style="color: #667eea;">개인정보처리방침</a>에 동의하게 됩니다
                </p>
            </div>
        </div>
    </div>

    <!-- Planning Screen (Authenticated) -->
    <div id="planningScreen" class="hidden">
        <div class="user-info">
            <div>
                <strong id="userName">사용자</strong>
                <br><small id="userEmail" style="color: #666;"></small>
            </div>
            <button class="button" onclick="handleLogout()" style="width: auto; padding: 8px 16px; margin: 0; font-size: 14px;">로그아웃</button>
        </div>

        <div class="card">
            <h2>여행 계획 만들기</h2>

            <label class="label">여행지</label>
            <input type="text" id="location" class="input" placeholder="예: 서울, 부산, 제주, 경주, 전주">
            
            <label class="label">여행 제목</label>
            <input type="text" id="title" class="input" placeholder="예: 힐링 여행">
            
            <label class="label">시작일</label>
            <input type="date" id="startDate" class="input">
            
            <label class="label">종료일</label>
            <input type="date" id="endDate" class="input">
            
            <button class="button" onclick="createPlan()">🤖 AI 여행 계획 생성</button>
        </div>

        <div id="plans"></div>
    </div>

    <script>
        const API = window.location.origin;
        const AUTH_API = 'http://localhost:8081'; // Auth Service

        let currentUser = null;
        let accessToken = null;

        // 페이지 로드 시 인증 확인
        window.onload = async function() {
            // OAuth 콜백 확인 (URL에 code가 있으면 콜백 처리)
            const isOAuthCallback = await handleOAuthCallback();

            if (!isOAuthCallback) {
                // 일반 페이지 로드 - 인증 확인
                checkAuth();
            }

            // 내일과 3일 후로 기본 날짜 설정
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            document.getElementById('startDate').value = tomorrow.toISOString().split('T')[0];

            const after3days = new Date();
            after3days.setDate(after3days.getDate() + 4);
            document.getElementById('endDate').value = after3days.toISOString().split('T')[0];
        };

        // 화면 전환 함수
        function hideAllScreens() {
            document.getElementById('welcomeScreen').classList.add('hidden');
            document.getElementById('planningScreen').classList.add('hidden');
        }

        function showWelcome() {
            hideAllScreens();
            document.getElementById('welcomeScreen').classList.remove('hidden');
        }

        function showPlanning() {
            hideAllScreens();
            document.getElementById('planningScreen').classList.remove('hidden');
            loadPlans();
        }

        // 인증 확인
        function checkAuth() {
            const token = localStorage.getItem('accessToken');
            const userId = localStorage.getItem('userId');
            const userName = localStorage.getItem('userName');
            const userEmail = localStorage.getItem('userEmail');

            if (token && userId) {
                accessToken = token;
                currentUser = { id: userId, name: userName, email: userEmail };
                document.getElementById('userName').textContent = userName || '사용자';
                document.getElementById('userEmail').textContent = userEmail || '';
                showPlanning();
            } else {
                showWelcome();
            }
        }

        // OAuth 로그인 - Google
        function loginWithGoogle() {
            // 현재 URL을 상태에 저장하여 콜백 후 복귀
            localStorage.setItem('oauth_return_url', window.location.href);

            // Google OAuth 흐름 시작
            window.location.href = AUTH_API + '/oauth2/authorize/google';
        }

        // OAuth 로그인 - Apple
        function loginWithApple() {
            alert('🍎 Apple 로그인은 곧 제공될 예정입니다!\\n현재는 Google 로그인을 이용해주세요.');
        }

        // OAuth 콜백 처리
        async function handleOAuthCallback() {
            const urlParams = new URLSearchParams(window.location.search);
            const code = urlParams.get('code');
            const state = urlParams.get('state');

            if (!code) {
                return false; // 콜백이 아님
            }

            try {
                // Auth 서비스에 code 전송하여 토큰 받기
                const response = await fetch(AUTH_API + '/api/auth/oauth2/callback/google', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ code, state })
                });

                if (!response.ok) {
                    throw new Error('OAuth 인증에 실패했습니다');
                }

                const data = await response.json();

                // 사용자 정보 가져오기
                const userResponse = await fetch(API + '/api/v1/users/me', {
                    headers: { 'X-User-Id': data.userId }
                });
                const userData = await userResponse.json();

                // 토큰 저장
                localStorage.setItem('accessToken', data.accessToken);
                localStorage.setItem('refreshToken', data.refreshToken);
                localStorage.setItem('userId', data.userId);
                localStorage.setItem('userName', userData.name);
                localStorage.setItem('userEmail', userData.email);

                alert('✅ 로그인 성공!');

                // URL에서 code/state 파라미터 제거
                window.history.replaceState({}, document.title, window.location.pathname);

                checkAuth();
                return true;

            } catch (error) {
                alert('❌ 로그인 실패: ' + error.message);
                window.history.replaceState({}, document.title, window.location.pathname);
                showWelcome();
                return false;
            }
        }

        // 로그아웃
        function handleLogout() {
            if (confirm('로그아웃 하시겠습니까?')) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('userId');
                localStorage.removeItem('userName');
                localStorage.removeItem('userEmail');
                accessToken = null;
                currentUser = null;
                showWelcome();
            }
        }

        // 계획 목록 로드
        async function loadPlans() {
            if (!currentUser) return;

            try {
                const response = await fetch(API + '/api/plans', {
                    headers: {'X-User-Id': currentUser.id}
                });
                const plans = await response.json();
                
                const container = document.getElementById('plans');
                if (plans.length === 0) {
                    container.innerHTML = '<div class="card"><p>아직 여행 계획이 없습니다</p></div>';
                    return;
                }
                
                container.innerHTML = plans.map(plan => `
                    <div class="card" onclick="showDetail(${plan.id})" style="cursor: pointer;">
                        <h3>${plan.title}</h3>
                        <p>${new Date(plan.startDate).toLocaleDateString('ko-KR')} - ${new Date(plan.endDate).toLocaleDateString('ko-KR')}</p>
                        ${plan.status ? '<span class="badge">' + plan.status + '</span>' : ''}
                    </div>
                `).join('');
            } catch (error) {
                document.getElementById('plans').innerHTML = '<div class="card"><p>로딩 실패</p></div>';
            }
        }

        // 계획 생성
        async function createPlan() {
            const location = document.getElementById('location').value;
            const title = document.getElementById('title').value;
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            if (!location || !startDate || !endDate) {
                alert('모든 항목을 입력해주세요');
                return;
            }

            const finalTitle = title.trim() ? (title.includes(location) ? title : location + ' ' + title) : location + ' 여행';

            document.getElementById('plans').innerHTML = '<div class="card"><p>AI가 계획 생성 중...</p></div>';

            try {
                await fetch(API + '/api/plans', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': currentUser.id
                    },
                    body: JSON.stringify({title: finalTitle, startDate, endDate})
                });

                alert('✅ 여행 계획이 생성되었습니다!');
                
                // 폼 초기화
                document.getElementById('location').value = '';
                document.getElementById('title').value = '';
                
                // 목록 새로고침
                loadPlans();
            } catch (error) {
                alert('❌ 생성에 실패했습니다');
                loadPlans();
            }
        }

        // 상세 보기
        let currentPlanId = null;
        
        async function showDetail(id) {
            currentPlanId = id;
            
            try {
                const response = await fetch(API + '/api/plans/' + id, {
                    headers: {'X-User-Id': currentUser.id}
                });
                const plan = await response.json();

                let html = `
                    <div class="card">
                        <h2>${plan.title}</h2>
                        <p>${new Date(plan.startDate).toLocaleDateString('ko-KR')} - ${new Date(plan.endDate).toLocaleDateString('ko-KR')}</p>
                        <button class="button" onclick="loadPlans()">← 목록으로</button>
                    </div>
                `;

                plan.details.forEach(detail => {
                    const activities = detail.activity.split(', ').map(act => {
                        const match = act.match(/(Morning|Afternoon|Evening):\\s*(.+?)\\s*\\((₩[^)]+|무료)\\)/);
                        if (match) {
                            const icons = {'Morning': '🌅 오전', 'Afternoon': '☀️ 오후', 'Evening': '🌙 저녁'};
                            return `
                                <div style="background: #f8f9fa; padding: 12px; border-radius: 8px; margin: 8px 0;">
                                    <strong style="color: #667eea;">${icons[match[1]]}</strong><br>
                                    ${match[2]}<br>
                                    <span style="color: #667eea; font-weight: 600;">${match[3]}</span>
                                </div>
                            `;
                        }
                        return '';
                    }).join('');

                    html += `
                        <div class="card">
                            <div style="background: #667eea; color: white; padding: 12px; margin: -16px -16px 12px -16px; border-radius: 12px 12px 0 0;">
                                <strong>Day ${detail.day}</strong><br>
                                📍 ${detail.location}
                            </div>
                            ${activities}
                        </div>
                    `;
                });
                
                // 기존 업로드된 사진 표시
                const photosResponse = await fetch(API + '/api/plans/' + id + '/photos', {
                    headers: {'X-User-Id': currentUser.id}
                });
                const existingPhotos = await photosResponse.json();
                
                if (existingPhotos && existingPhotos.length > 0) {
                    html += `
                        <div class="card">
                            <h3>📸 업로드된 사진 (${existingPhotos.length}장)</h3>
                            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 12px 0;">
                                ${existingPhotos.map(photo => `
                                    <img src="${photo.photoUrl}" style="width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 8px;">
                                `).join('')}
                            </div>
                        </div>
                    `;
                    
                    // 영상 생성 버튼
                    html += `
                        <div class="card">
                            <h3>🎬 여행 영상 만들기</h3>
                            <p style="color: #666; margin: 8px 0;">${existingPhotos.length}장의 사진으로 멋진 영상을 만들어드립니다!</p>
                            <button class="button" onclick="createVideo()">🎬 영상 생성 시작 (약 2-3분)</button>
                        </div>
                    `;
                }
                
                // 사진 업로드 섹션
                html += `
                    <div class="card">
                        <h3>📸 ${existingPhotos.length > 0 ? '사진 더 추가하기' : '여행 사진 추가하기'}</h3>
                        <p style="color: #666; margin: 8px 0;">여행의 추억을 사진으로 남겨보세요! (최대 10장)</p>
                        <input type="file" id="photos" multiple accept="image/*" style="margin: 12px 0;">
                        <div id="photo-preview" style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 12px 0;"></div>
                        <button class="button" onclick="uploadPhotos()">📤 사진 업로드</button>
                    </div>
                `;

                document.getElementById('plans').innerHTML = html;
                
                // 파일 선택 이벤트
                document.getElementById('photos').addEventListener('change', previewPhotos);
                
            } catch (error) {
                alert('상세 정보를 불러올 수 없습니다');
            }
        }
        
        // 사진 미리보기
        function previewPhotos(event) {
            const files = event.target.files;
            const preview = document.getElementById('photo-preview');
            preview.innerHTML = '';
            
            for (let i = 0; i < Math.min(files.length, 10); i++) {
                const file = files[i];
                const reader = new FileReader();
                reader.onload = (e) => {
                    preview.innerHTML += `
                        <img src="${e.target.result}" style="width: 100%; aspect-ratio: 1; object-fit: cover; border-radius: 8px;">
                    `;
                };
                reader.readAsDataURL(file);
            }
        }
        
        // 사진 업로드
        async function uploadPhotos() {
            const files = document.getElementById('photos').files;
            
            if (files.length === 0) {
                alert('사진을 선택해주세요');
                return;
            }
            
            alert('📤 ' + files.length + '장의 사진 업로드 중...');
            
            try {
                for (let i = 0; i < Math.min(files.length, 10); i++) {
                    // Mock upload (실제로는 S3 presigned URL 사용)
                    const photoUrl = 'https://picsum.photos/1080/1920?random=' + Date.now() + '_' + i;
                    
                    await fetch(API + '/api/plans/' + currentPlanId + '/photos', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'X-User-Id': currentUser.id
                        },
                        body: JSON.stringify({
                            photoUrl: photoUrl,
                            s3Key: 'photos/user' + currentUser.id + '/plan' + currentPlanId + '/photo' + i + '.jpg',
                            order: i + 1
                        })
                    });
                }
                
                alert('✅ ' + files.length + '장의 사진이 업로드되었습니다!');
                showDetail(currentPlanId); // 새로고침
                
            } catch (error) {
                alert('❌ 업로드에 실패했습니다');
            }
        }
        
        // 영상 생성
        async function createVideo() {
            if (!confirm('업로드한 사진으로 영상을 만드시겠습니까?\\n(약 2-3분 소요)')) {
                return;
            }
            
            alert('🎬 영상 생성을 시작합니다...');
            
            try {
                const response = await fetch(API + '/api/plans/' + currentPlanId + '/create-video', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-User-Id': currentUser.id,
                        'Idempotency-Key': crypto.randomUUID()
                    },
                    body: JSON.stringify({template: 'default'})
                });
                
                const video = await response.json();
                
                alert('✅ 영상 생성이 시작되었습니다!\\n완료되면 알려드립니다.');
                
                // 상태 확인 (5초마다)
                checkVideoStatus(video.id);
                
            } catch (error) {
                alert('❌ 영상 생성 요청에 실패했습니다');
            }
        }
        
        // 영상 상태 확인
        function checkVideoStatus(videoId) {
            const interval = setInterval(async () => {
                try {
                    const response = await fetch(API + '/api/videos/' + videoId, {
                        headers: {'X-User-Id': currentUser.id}
                    });
                    const video = await response.json();
                    
                    if (video.status === 'COMPLETED') {
                        clearInterval(interval);
                        alert('🎉 영상이 완성되었습니다!');
                        showDetail(currentPlanId);
                    } else if (video.status === 'FAILED') {
                        clearInterval(interval);
                        alert('❌ 영상 생성에 실패했습니다');
                    }
                } catch (error) {
                    clearInterval(interval);
                }
            }, 5000);
        }
    </script>
</body>
</html>
                """);
    }
}

