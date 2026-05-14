package com.petshop.repository;

import com.petshop.model.PrivateMessageThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateMessageThreadRepository extends JpaRepository<PrivateMessageThread, Long> {
    List<PrivateMessageThread> findByStarterOrRecipientOrderByUpdatedAtDesc(String starter, String recipient);

    Optional<PrivateMessageThread> findFirstByPostIdAndStarterAndRecipient(Long postId, String starter, String recipient);

    List<PrivateMessageThread> findByStarterUserIdOrRecipientUserIdOrderByUpdatedAtDesc(Long starterUserId, Long recipientUserId);

    Optional<PrivateMessageThread> findFirstByPostIdAndStarterUserIdAndRecipientUserId(Long postId, Long starterUserId, Long recipientUserId);
}
