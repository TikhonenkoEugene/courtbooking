package com.courtbook.identity.dto;

import com.courtbook.identity.entity.Role;

public record TokenResponse(Long userId, Role role, String token) {
}
