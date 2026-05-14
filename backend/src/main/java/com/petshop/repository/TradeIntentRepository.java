package com.petshop.repository;

import com.petshop.model.TradeIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeIntentRepository extends JpaRepository<TradeIntent, Long> {
    List<TradeIntent> findByRequesterOrderByUpdatedAtDesc(String requester);

    List<TradeIntent> findByOwnerOrderByUpdatedAtDesc(String owner);

    boolean existsByPostIdAndRequester(Long postId, String requester);

    List<TradeIntent> findByRequesterUserIdOrderByUpdatedAtDesc(Long requesterUserId);

    List<TradeIntent> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);

    boolean existsByPostIdAndRequesterUserId(Long postId, Long requesterUserId);
}
