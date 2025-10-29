package com.ucamp.project.service;


import com.ucamp.project.model.Simulation;
import com.ucamp.project.repository.SimulationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SimulationService {

    private final SimulationRepository simulationRepository;
    
    public List<Simulation> findAll(){
        return simulationRepository.findAll();
    }

    public Simulation save(Simulation simulation) {
        return simulationRepository.save(simulation);
    }
}
