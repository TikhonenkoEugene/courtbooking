package com.courtbook.notification.event;

public record OtpRequestedEvent(String phone, String code) {
}
