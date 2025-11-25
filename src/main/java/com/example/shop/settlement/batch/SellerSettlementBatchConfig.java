package com.example.shop.settlement.batch;

import com.example.shop.settlement.domain.SellerSettlement;
import com.example.shop.settlement.domain.SellerSettlementRepository;
import com.example.shop.settlement.domain.SettlementStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class SellerSettlementBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(SellerSettlementBatchConfig.class);

    /**
     * 모든 (또는 특정) 판매자의 PENDING 정산을 읽어 상태를 COMPLETED로 바꾸는 Batch Job.
     * settlementStep 하나로 구성된 간단한 Job이다.
     */
    @Bean
    public Job sellerSettlementJob(JobRepository jobRepository,
                                   Step settlementStep) {
        return new JobBuilder("sellerSettlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    /**
     * 정산 Step: Tasklet 기반으로 PENDING 상태의 정산 레코드를 조회하고
     * 셀러별 금액 합계를 계산한 뒤 상태를 COMPLETED로 업데이트한다.
     * JobParameters에 sellerId가 있으면 해당 셀러만 처리한다.
     */
    @Bean
    public Step settlementStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               SellerSettlementRepository sellerSettlementRepository) {
        return new StepBuilder("settlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String sellerParam = (String) chunkContext.getStepContext()
                            .getJobParameters()
                            .get("sellerId");
                    List<SellerSettlement> pending;
                    if (sellerParam != null && !sellerParam.isBlank()) {
                        pending = sellerSettlementRepository.findByStatusAndSeller(
                                SettlementStatus.PENDING,
                                UUID.fromString(sellerParam)
                        );
                    } else {
                        pending = sellerSettlementRepository.findByStatus(SettlementStatus.PENDING);
                    }
                    if (pending.isEmpty()) {
                        log.info("No pending settlements to process.");
                        return RepeatStatus.FINISHED;
                    }

                    Map<UUID, BigDecimal> totals = pending.stream()
                            .collect(Collectors.groupingBy(
                                    SellerSettlement::getSellerId,
                                    Collectors.reducing(BigDecimal.ZERO, SellerSettlement::getAmount, BigDecimal::add)
                            ));

                    pending.forEach(SellerSettlement::markCompleted);
                    sellerSettlementRepository.saveAll(pending);

                    totals.forEach((sellerId, total) ->
                            log.info("Settled seller {} amount {}", sellerId, total));

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}