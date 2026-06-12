package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.ProductionJobStatus;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionJobRepository;
import com.ssemi.sampleorder.repository.SampleRepository;
import com.ssemi.sampleorder.repository.SequenceRepository;
import com.ssemi.sampleorder.util.IdGenerator;
import com.ssemi.sampleorder.util.ProductionCalculator;
import com.ssemi.sampleorder.util.TimeProvider;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

public class ProductionService {
    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionJobRepository productionJobRepository;
    private final SequenceRepository sequenceRepository;
    private final TimeProvider timeProvider;

    public ProductionService(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            ProductionJobRepository productionJobRepository,
            SequenceRepository sequenceRepository,
            TimeProvider timeProvider
    ) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionJobRepository = productionJobRepository;
        this.sequenceRepository = sequenceRepository;
        this.timeProvider = timeProvider;
    }

    public ProductionJob enqueueProduction(Order order, Sample sample, int shortageQuantity) {
        int plannedQuantity = ProductionCalculator.plannedProductionQuantity(shortageQuantity, sample.yieldRate());
        ProductionJob job = ProductionJob.waiting(
                IdGenerator.productionJobId(sequenceRepository.nextProductionJobNumber()),
                order.id(),
                sample.id(),
                shortageQuantity,
                plannedQuantity,
                sample.averageProductionTimeMinutes(),
                timeProvider.now()
        );
        productionJobRepository.save(job);
        startNextWaitingJobIfIdle(timeProvider.now());
        return productionJobRepository.findById(job.id()).orElseThrow();
    }

    public void synchronizeProductionLine() {
        Optional<ProductionJob> runningJob = runningJob();
        while (runningJob.isPresent() && !runningJob.get().expectedEndAt().isAfter(timeProvider.now())) {
            LocalDateTime nextStartTime = runningJob.get().expectedEndAt();
            completeJob(runningJob.get());
            startNextWaitingJobIfIdle(nextStartTime);
            runningJob = runningJob();
        }
        if (runningJob.isEmpty()) {
            startNextWaitingJobIfIdle(timeProvider.now());
        }
    }

    private Optional<ProductionJob> runningJob() {
        return productionJobRepository.findAll().stream()
                .filter(job -> job.status() == ProductionJobStatus.RUNNING)
                .findFirst();
    }

    private void startNextWaitingJobIfIdle(LocalDateTime startTime) {
        if (runningJob().isPresent()) {
            return;
        }
        productionJobRepository.findAll().stream()
                .filter(job -> job.status() == ProductionJobStatus.WAITING)
                .min(Comparator.comparing(ProductionJob::createdAt))
                .map(job -> job.start(startTime))
                .ifPresent(productionJobRepository::update);
    }

    private void completeJob(ProductionJob job) {
        ProductionJob completed = job.complete(job.expectedEndAt());
        productionJobRepository.update(completed);

        Sample sample = sampleRepository.findById(job.sampleId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시료 ID입니다: " + job.sampleId()));
        sampleRepository.update(sample.withStockQuantity(sample.stockQuantity() + job.shortageQuantity()));

        Order order = orderRepository.findById(job.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 ID입니다: " + job.orderId()));
        orderRepository.update(order.toConfirmed(order.allocatedQuantity() + job.shortageQuantity(), job.expectedEndAt()));
    }
}
