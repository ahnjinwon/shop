package com.example.shop.settlement.infrastructure;

import com.example.shop.settlement.domain.SellerSettlement;
import com.example.shop.settlement.domain.SellerSettlementRepository;
import com.example.shop.settlement.domain.SettlementStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SellerSettlementRepositoryAdapter implements SellerSettlementRepository {

    private final SellerSettlementJpaRepository sellerSettlementJpaRepository;

    public SellerSettlementRepositoryAdapter(SellerSettlementJpaRepository sellerSettlementJpaRepository) {
        this.sellerSettlementJpaRepository = sellerSettlementJpaRepository;
    }

    @Override
    public SellerSettlement save(SellerSettlement settlement) {
        return sellerSettlementJpaRepository.save(settlement);
    }

    @Override
    public List<SellerSettlement> findByStatus(SettlementStatus status) {
        return sellerSettlementJpaRepository.findByStatus(status);
    }

    @Override
    public List<SellerSettlement> findByStatusAndSeller(SettlementStatus status, UUID sellerId) {
        return sellerSettlementJpaRepository.findByStatusAndSellerId(status, sellerId);
    }

    @Override
    public void saveAll(List<SellerSettlement> settlements) {
        sellerSettlementJpaRepository.saveAll(settlements);
    }
}
