package com.zeb.energy.energycarbonfootprintprocessor.service;

import com.google.gson.JsonArray;
import com.zeb.energy.energycarbonfootprintprocessor.dto.CarbonFootprint;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.EnergySource;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.EnergyUsage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

public interface EnergyToCarbonFootPrintService {
    Map<String, CarbonFootprint> calculateCarbonFootPrint(List<EnergyUsage> energyUsagesList);
}
