package com.example.shop.settlement.batch;

import com.example.shop.seller.domain.SellerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SellerSettlementScheduler {

    private static final Logger log = LoggerFactory.getLogger(SellerSettlementScheduler.class);

    private final SellerRepository sellerRepository;
    private final JobLauncher jobLauncher;
    private final Job sellerSettlementJob;
    private final ThreadPoolTaskExecutor settlementTaskExecutor;
    private final boolean settlementAsyncEnabled;

    public SellerSettlementScheduler(
            SellerRepository sellerRepository,
            JobLauncher jobLauncher,
            Job sellerSettlementJob,
            ThreadPoolTaskExecutor settlementTaskExecutor,
            @Value("${settlement.async.enabled:false}") boolean settlementAsyncEnabled
    ) {
        this.sellerRepository = sellerRepository;
        this.jobLauncher = jobLauncher;
        this.sellerSettlementJob = sellerSettlementJob;
        this.settlementTaskExecutor = settlementTaskExecutor;
        this.settlementAsyncEnabled = settlementAsyncEnabled;
    }

    @Scheduled(cron = "${spring.task.scheduling.cron.settlement}")
    public void runMindingSettlements(){
        Pageable pageable = Pageable.ofSize(100);
        Page<UUID> page;
        do{
            page = sellerRepository.findAll(pageable).map(seller -> seller.getId());
            List<UUID> sellerIds = page.getContent();
            if(sellerIds.isEmpty()){
                break;
            }
            log.info("settlement batch chunk for {} sellers (page {}/{})",
                    sellerIds.size(), page.getNumber() + 1, page.getTotalPages());
            sellerIds.forEach(this::runJobForSeller);
            pageable = page.hasNext() ? page.nextPageable() : Pageable.unpaged();
        }while (page.hasNext());
    }

    private void runJobForSeller(UUID sellerId){
        try{
            Runnable executeJob = () ->{
                try{
                    JobParameters params = new JobParametersBuilder()
                            .addLong("timestamp", System.currentTimeMillis())
                            .addString("sellerId", sellerId.toString())
                            .toJobParameters();
                    jobLauncher.run(sellerSettlementJob, params);
                    log.info("Settlement job triggered for seller {}", sellerId);
                }catch (Exception e){
                    log.error("Failed to run settlement job for seller {}", sellerId, e);
                }
            };

            if (settlementAsyncEnabled) {
                settlementTaskExecutor.execute(executeJob);
            } else {
                executeJob.run();
            }
        }catch (Exception e){
            log.error("Failed to run settlement job for seller {}", sellerId, e);
        }
    }
}

