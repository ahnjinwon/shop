package com.example.shop.member.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "회원 정보")
@Data
@Entity
@Table(name = "\"member\"", schema = "public")
public class Member {

    @Schema(description = "회원 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @Id
    private UUID id;

    @Schema(description = "이메일", example = "user@example.com")
    @Column(nullable = false, length = 50, unique = true)
    private String email;

    @Schema(description = "이름", example = "홍길동")
    @Column(name = "\"name\"", length = 20)
    private String name;

    @Schema(description = "비밀번호")
    @Column(name = "\"password\"", nullable = false, length = 100)
    private String password;

    @Schema(description = "휴대폰 번호", example = "01012345678")
    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Schema(description = "등록자 ID")
    @Column(name = "reg_id", nullable = false)
    private UUID regId;

    @Schema(description = "등록 일시")
    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Schema(description = "수정자 ID")
    @Column(name = "modify_id", nullable = false)
    private UUID modifyId;

    @Schema(description = "수정 일시")
    @Column(name = "modify_dt", nullable = false)
    private LocalDateTime modifyDt;

    @Schema(description = "비밀번호 salt", example = "Ab12Cd34Ef56Gh")
    @Column(name = "saltkey", nullable = false, length = 14)
    private String saltKey;

    @Schema(description = "회원 상태 플래그", example = "Y")
    @Column(name = "flag", length = 5)
    private String flag;

    public Member() {}

    private Member(UUID id,
                  String email,
                  String name,
                  String password,
                  String phone,
                  String saltKey,
                  String flag) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.saltKey = saltKey;
        this.flag = flag;
    }


    public static Member create(String email,
                                String name,
                                String password,
                                String phone,
                                String saltKey,
                                String flag){
        return new Member(UUID.randomUUID(), email, name, password, phone, saltKey, flag);
    }

    public void updateInformation(String email,
                                  String name,
                                  String password,
                                  String phone,
                                  String saltKey,
                                  String flag){
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
        this.saltKey = saltKey;
        this.flag = flag;
    }

    @PrePersist
    public void prePersist() {
        if (regId == null) {
            regId = id != null ? id : UUID.randomUUID();
        }
        if (modifyId == null) {
            modifyId = regId;
        }
        if (regDt == null) {
            regDt = LocalDateTime.now();
        }
        if (modifyDt == null) {
            modifyDt = LocalDateTime.now();
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @PreUpdate
    public void preUpdate() {
        modifyDt = LocalDateTime.now();
        if (modifyId == null) {
            modifyId = id;
        }
    }

}
