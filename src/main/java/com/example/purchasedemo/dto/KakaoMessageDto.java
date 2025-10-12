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
        private String objectType = "text";

        private String text;

        private Link link;

        @JsonProperty("button_title")
        private String buttonTitle = "주문내역 확인";
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        @JsonProperty("web_url")
        private String webUrl = "http://localhost:8080";

        @JsonProperty("mobile_web_url")
        private String mobileWebUrl = "http://localhost:8080";
    }
}
