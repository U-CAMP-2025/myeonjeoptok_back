package com.ucamp.project.service;

import com.ucamp.project.dto.JobResponse;
import com.ucamp.project.model.Job;
import com.ucamp.project.repository.JobRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    public List<JobResponse> findAll(){
        List<Job> jobs = jobRepository.findAll();

        return jobs.stream()
                .map(job -> JobResponse.builder()
                        .jobId(job.getJobId())
                        .jobName(job.getJobName())
                        .build())
                        .collect(Collectors.toList());
    }

}
