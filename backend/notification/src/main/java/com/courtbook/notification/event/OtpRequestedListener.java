package com.courtbook.notification.event;

import com.courtbook.notification.entity.Message;
import com.courtbook.notification.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpRequestedListener {

    private final MessageRepository messageRepository;

    @KafkaListener(topics = "otp-requested")
    public void handle(OtpRequestedEvent event) {
        Message message = new Message();
        message.setPhone(event.phone());
        message.setType("OTP");
        message.setText("Ваш код для входа в CourtBook: " + event.code());
        messageRepository.save(message);
    }
}
