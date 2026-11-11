package com.courtbook.identity.event;

public record OtpRequestedEvent(String phone, String code) {
}
