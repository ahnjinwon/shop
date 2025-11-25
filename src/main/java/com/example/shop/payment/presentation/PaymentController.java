package com.example.shop.payment.presentation;

import com.example.shop.common.ResponseEntity;
import com.example.shop.payment.application.PaymentService;
import com.example.shop.payment.application.dto.PaymentFailureInfo;
import com.example.shop.payment.application.dto.PaymentInfo;
import com.example.shop.payment.presentation.dto.PaymentFailRequest;
import com.example.shop.payment.presentation.dto.PaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.v1}/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "결제 내역 조회", description = "확정된 결제 정보를 페이지 단위로 조회한다.")
    @GetMapping
    public ResponseEntity<List<PaymentInfo>> findAll(Pageable pageable) {
        return  paymentService.findAll(pageable);
    }

    @Operation(summary = "토스 결제 승인", description = "토스 결제 완료 후 paymentKey/orderId/amount를 전달받아 결제를 승인한다.")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentInfo> confirm(@RequestBody PaymentRequest request) {
        return paymentService.confirm(request.toCommand());
    }

    @Operation(summary = "결제 실패 기록", description = "토스 결제 실패 정보를 저장한다.")
    @PostMapping("/fail")
    public ResponseEntity<PaymentFailureInfo> fail(@RequestBody PaymentFailRequest request) {
        return paymentService.recordFailure(request.toCommand());
    }
}