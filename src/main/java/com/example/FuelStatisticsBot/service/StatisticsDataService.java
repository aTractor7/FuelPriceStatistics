package com.example.FuelStatisticsBot.service;

import com.example.FuelStatisticsBot.model.StatisticsData;
import com.example.FuelStatisticsBot.repository.StatisticsDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class StatisticsDataService {

    private final StatisticsDataRepository statisticsDataRepository;

    public StatisticsDataService(StatisticsDataRepository statisticsDataRepository) {
        this.statisticsDataRepository = statisticsDataRepository;
    }

    public List<StatisticsData> findAll() {
        return statisticsDataRepository.findAll();
    }

    public Optional<StatisticsData> findOne(int id) {
        return statisticsDataRepository.findById(id);
    }

    @Transactional
    public void save(StatisticsData statisticsData) {
        statisticsDataRepository.save(statisticsData);
    }

    @Transactional
    public void update(int id, StatisticsData statisticsData) {
        statisticsData.setId(id);
        statisticsDataRepository.save(statisticsData);
    }

    @Transactional
    public void delete(int id) {
        statisticsDataRepository.deleteById(id);
    }
}
