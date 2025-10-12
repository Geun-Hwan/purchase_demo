package com.example.purchasedemo.service.notification;

public interface NotificationService {

    /**
     * 결제 성공 알림을 전송합니다.
     * @param orderId 주문 ID
     * @param message 알림 메시지
     */
    void sendPaymentSuccessNotification(Long orderId, String message);

    /**
     * 결제 실패 알림을 전송합니다.
     * @param orderId 주문 ID
     * @param message 알림 메시지
     */
    void sendPaymentFailureNotification(Long orderId, String message);
}
