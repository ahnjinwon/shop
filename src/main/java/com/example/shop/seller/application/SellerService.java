package com.example.shop.seller.application;

import com.example.shop.common.ResponseEntity;
import com.example.shop.seller.application.dto.SellerCommand;
import com.example.shop.seller.application.dto.SellerInfo;
import com.example.shop.seller.domain.Seller;
import com.example.shop.seller.domain.SellerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SellerService {
    @Autowired
    private SellerRepository sellerRepository;

    public ResponseEntity<List<SellerInfo>> findAll(Pageable pageable) {
        Page<Seller> page = sellerRepository.findAll(pageable);
        List<SellerInfo> sellers = page.stream().map(SellerInfo::from).toList();
        long count = sellers.size();
        return new ResponseEntity<>(HttpStatus.OK.value(), sellers, count);
    }

    public ResponseEntity<SellerInfo> create(SellerCommand command) {
        Seller seller = Seller.register(
                command.companyName(),
                command.representativeName(),
                command.email(),
                command.phone(),
                command.businessNumber(),
                command.address(),
                command.status()
        );

        Seller created = sellerRepository.save(seller);
        return new ResponseEntity<>(HttpStatus.OK.value(), SellerInfo.from(created), 1);
    }

    public ResponseEntity<SellerInfo> update(UUID id, SellerCommand command) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + id));

        seller.update(
                command.companyName(),
                command.representativeName(),
                command.email(),
                command.phone(),
                command.businessNumber(),
                command.address(),
                command.status()
        );

        Seller updated = sellerRepository.save(seller);
        return new ResponseEntity<>(HttpStatus.OK.value(), SellerInfo.from(updated), 1);
    }

    public ResponseEntity<Void> delete(UUID id) {
        sellerRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT.value(), null, 0);
    }
}
