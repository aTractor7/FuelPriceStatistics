package com.example.FuelStatisticsBot.repository;

import com.example.FuelStatisticsBot.model.StatisticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatisticsDataRepository extends JpaRepository<StatisticsData, Integer> {
}
