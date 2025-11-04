package com.ucamp.project.service;

import com.ucamp.project.model.Interviewer;
import com.ucamp.project.repository.InterViewerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class InterViewerService {

    private final InterViewerRepository interViewerRepository;
    
    public List<Interviewer> findAll(){
        return interViewerRepository.findAll();
    }

}
