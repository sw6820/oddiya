package com.oddiya.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * UI Messages Configuration
 * Externalizes all UI strings to prevent hardcoding
 * Messages can be overridden via application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ui.messages")
public class UIMessages {

    private Map<String, String> ko = new HashMap<>();

    public UIMessages() {
        // Initialize Korean messages
        initializeKoreanMessages();
    }

    private void initializeKoreanMessages() {
        // App Header
        ko.put("app.title", "Oddiya");
        ko.put("app.subtitle", "AI 여행 플래너");

        // Form Labels
        ko.put("form.create.title", "여행 계획 만들기");
        ko.put("form.label.location", "여행지");
        ko.put("form.label.title", "여행 제목");
        ko.put("form.label.startDate", "시작일");
        ko.put("form.label.endDate", "종료일");

        // Placeholders
        ko.put("form.placeholder.location", "예: 서울, 부산, 제주, 경주, 전주");
        ko.put("form.placeholder.title", "예: 힐링 여행");

        // Buttons
        ko.put("button.createPlan", "🤖 AI 여행 계획 생성");
        ko.put("button.backToList", "← 목록으로");
        ko.put("button.uploadPhotos", "📤 사진 업로드");
        ko.put("button.createVideo", "🎬 영상 생성 시작 (약 2-3분)");

        // Messages
        ko.put("message.noPlan", "아직 여행 계획이 없습니다");
        ko.put("message.loading", "AI가 계획 생성 중...");
        ko.put("message.loadingFailed", "로딩 실패");
        ko.put("message.planCreated", "✅ 여행 계획이 생성되었습니다!");
        ko.put("message.planFailed", "❌ 생성에 실패했습니다");
        ko.put("message.selectPhotos", "사진을 선택해주세요");
        ko.put("message.photoUploading", "사진 업로드 중...");
        ko.put("message.photoUploaded", "장의 사진이 업로드되었습니다!");
        ko.put("message.photoUploadFailed", "업로드에 실패했습니다");
        ko.put("message.allFieldsRequired", "모든 항목을 입력해주세요");
        ko.put("message.detailLoadFailed", "상세 정보를 불러올 수 없습니다");

        // Video Section
        ko.put("video.title", "🎬 여행 영상 만들기");
        ko.put("video.description", "장의 사진으로 멋진 영상을 만들어드립니다!");
        ko.put("video.starting", "🎬 영상 생성을 시작합니다...");
        ko.put("video.confirm", "업로드한 사진으로 영상을 만드시겠습니까?\\n(약 2-3분 소요)");
        ko.put("video.completed", "🎉 영상이 완성되었습니다!");
        ko.put("video.failed", "❌ 영상 생성에 실패했습니다");
        ko.put("video.requestFailed", "영상 생성 요청에 실패했습니다");

        // Photo Section
        ko.put("photo.title", "여행 사진 추가하기");
        ko.put("photo.titleAdd", "사진 더 추가하기");
        ko.put("photo.description", "여행의 추억을 사진으로 남겨보세요! (최대 10장)");
        ko.put("photo.uploaded", "📸 업로드된 사진");
        ko.put("photo.count", "장");

        // Time Labels
        ko.put("time.morning", "🌅 오전");
        ko.put("time.afternoon", "☀️ 오후");
        ko.put("time.evening", "🌙 저녁");

        // Day Label
        ko.put("label.day", "Day");
    }

    public String get(String key) {
        return ko.getOrDefault(key, key);
    }

    public Map<String, String> getAllKorean() {
        return new HashMap<>(ko);
    }
}
