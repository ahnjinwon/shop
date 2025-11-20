package com.example.shop.seller.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "\"seller\"", schema = "public")
public class Seller {

    @Id
    private UUID id;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "representative_name", nullable = false, length = 50)
    private String representativeName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "business_number", nullable = false, unique = true, length = 20)
    private String businessNumber;

    @Column(length = 200)
    private String address;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Seller() {
    }

    private Seller(String companyName,
                   String representativeName,
                   String email,
                   String phone,
                   String businessNumber,
                   String address,
                   String status) {
        this.id = UUID.randomUUID();
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.email = email;
        this.phone = phone;
        this.businessNumber = businessNumber;
        this.address = address;
        this.status = status;
    }

    public static Seller register(String companyName,
                                  String representativeName,
                                  String email,
                                  String phone,
                                  String businessNumber,
                                  String address,
                                  String status) {
        return new Seller(companyName, representativeName, email, phone, businessNumber, address, status);
    }

    public void update(String companyName,
                       String representativeName,
                       String email,
                       String phone,
                       String businessNumber,
                       String address,
                       String status) {
        this.companyName = companyName;
        this.representativeName = representativeName;
        this.email = email;
        this.phone = phone;
        this.businessNumber = businessNumber;
        this.address = address;
        this.status = status;
    }

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = "ACTIVE";
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getBusinessNumber() {
        return businessNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}