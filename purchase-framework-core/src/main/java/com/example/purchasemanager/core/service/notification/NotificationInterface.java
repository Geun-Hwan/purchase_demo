package com.example.purchasemanager.core.service.notification;

public interface NotificationInterface {
    void sendPaymentSuccessNotification(Long orderId, String message);
    void sendPaymentFailureNotification(Long orderId, String message);
}
