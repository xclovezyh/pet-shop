package com.petshop.repository;

import com.petshop.model.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage, Long> {
    List<PrivateMessage> findByThreadIdOrderByCreatedAtAscIdAsc(Long threadId);

    long countByThreadIdAndSenderNotAndReadByRecipientFalse(Long threadId, String sender);

    long countByThreadIdAndSenderUserIdNotAndReadByRecipientFalse(Long threadId, Long senderUserId);
}
