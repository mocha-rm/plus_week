package com.example.demo.service;

import com.example.demo.entity.RentalLog;
import com.example.demo.repository.RentalLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RentalLogService {
    private final RentalLogRepository rentalLogRepository;

    public RentalLogService(RentalLogRepository rentalLogRepository) {
        this.rentalLogRepository = rentalLogRepository;
    }

    @Transactional
    public void save(RentalLog rentalLog) {
        if (rentalLog == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        rentalLogRepository.save(rentalLog);
    }
}
