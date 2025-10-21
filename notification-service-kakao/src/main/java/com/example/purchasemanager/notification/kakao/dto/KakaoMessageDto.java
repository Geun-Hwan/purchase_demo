package com.example.purchasemanager.notification.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class KakaoMessageDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Default {
        private String objectType;
        private String text;
        private Link link;
        private String buttonTitle;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        @JsonProperty("web_url")
        private String webUrl;
        @JsonProperty("mobile_web_url")
        private String mobileWebUrl;
    }
}