package com.courtbook.identity.repository;

import com.courtbook.identity.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);

    List<OtpCode> findByPhoneAndUsedFalse(String phone);
}
