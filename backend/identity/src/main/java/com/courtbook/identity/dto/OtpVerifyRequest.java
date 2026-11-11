package com.courtbook.identity.dto;

public record OtpVerifyRequest(String phone, String code) {
}
