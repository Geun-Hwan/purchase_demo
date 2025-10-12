package com.example.purchasedemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoMessageDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Default {
        @JsonProperty("object_type")
        private String objectType;

        private String text;

        private Link link;

        @JsonProperty("button_title")
        private String buttonTitle;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        @JsonProperty("web_url")
        private String webUrl;

        @JsonProperty("mobile_web_url")
        private String mobileWebUrl;
    }
}
