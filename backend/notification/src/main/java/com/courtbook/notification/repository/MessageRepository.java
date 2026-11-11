package com.courtbook.notification.repository;

import com.courtbook.notification.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
