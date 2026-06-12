package com.ssemi.sampleorder.domain.service;

import com.ssemi.sampleorder.domain.model.Order;
import com.ssemi.sampleorder.domain.model.ProductionJob;
import com.ssemi.sampleorder.domain.model.Sample;
import com.ssemi.sampleorder.repository.OrderRepository;
import com.ssemi.sampleorder.repository.ProductionJobRepository;
import com.ssemi.sampleorder.repository.SampleRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public class DummyDataService {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2026, 6, 12, 9, 0);

    private final SampleRepository sampleRepository;
    private final OrderRepository orderRepository;
    private final ProductionJobRepository productionJobRepository;

    public DummyDataService(
            SampleRepository sampleRepository,
            OrderRepository orderRepository,
            ProductionJobRepository productionJobRepository
    ) {
        this.sampleRepository = sampleRepository;
        this.orderRepository = orderRepository;
        this.productionJobRepository = productionJobRepository;
    }

    public void generateIfMissing() {
        Set<String> sampleIds = sampleRepository.findAll().stream().map(Sample::id).collect(Collectors.toSet());
        saveSampleIfMissing(sampleIds, new Sample("S-001", "실리콘 웨이퍼-8인치", 0.5, 0.92, 480));
        saveSampleIfMissing(sampleIds, new Sample("S-002", "GaN 에피택셜-4인치", 0.3, 0.78, 220));
        saveSampleIfMissing(sampleIds, new Sample("S-003", "SiC 파워기판-6인치", 0.8, 0.92, 30));
        saveSampleIfMissing(sampleIds, new Sample("S-004", "포토레지스트-PR7", 0.2, 0.95, 910));
        saveSampleIfMissing(sampleIds, new Sample("S-005", "산화막 웨이퍼-SiO2", 0.6, 0.88, 0));

        Set<String> orderIds = orderRepository.findAll().stream().map(Order::id).collect(Collectors.toSet());
        saveOrderIfMissing(orderIds, Order.reserve("ORD-D001", "S-001", "AI Lab", 10, BASE_TIME));
        saveOrderIfMissing(orderIds, Order.reserve("ORD-D002", "S-002", "Fabless B", 20, BASE_TIME).toConfirmed(20, BASE_TIME));
        saveOrderIfMissing(orderIds, Order.reserve("ORD-D003", "S-003", "Power Lab", 40, BASE_TIME)
                .toConfirmed(40, BASE_TIME)
                .toRelease(BASE_TIME));
        saveOrderIfMissing(orderIds, Order.reserve("ORD-D004", "S-004", "University C", 5, BASE_TIME).reject(BASE_TIME));

        Set<String> jobIds = productionJobRepository.findAll().stream().map(ProductionJob::id).collect(Collectors.toSet());
        saveJobIfMissing(jobIds, ProductionJob.waiting("JOB-D001", "ORD-D003", "S-003", 10, 13, 0.8, BASE_TIME)
                .start(BASE_TIME)
                .complete(BASE_TIME.plusMinutes(11)));
        saveJobIfMissing(jobIds, ProductionJob.waiting("JOB-D002", "ORD-D005", "S-005", 8, 11, 0.6, BASE_TIME.plusMinutes(1))
                .start(BASE_TIME.plusMinutes(1))
                .complete(BASE_TIME.plusMinutes(8)));
    }

    private void saveSampleIfMissing(Set<String> existingIds, Sample sample) {
        if (existingIds.add(sample.id())) {
            sampleRepository.save(sample);
        }
    }

    private void saveOrderIfMissing(Set<String> existingIds, Order order) {
        if (existingIds.add(order.id())) {
            orderRepository.save(order);
        }
    }

    private void saveJobIfMissing(Set<String> existingIds, ProductionJob job) {
        if (existingIds.add(job.id())) {
            productionJobRepository.save(job);
        }
    }
}
