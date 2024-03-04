package com.zeb.energy.energycarbonfootprintprocessor.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.zeb.energy.energycarbonfootprintprocessor.dto.CarbonFootprint;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.EnergyUsage;
import com.zeb.energy.energycarbonfootprintprocessor.service.EnergyToCarbonFootPrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api")
public class EnergyToCarbonFootPrintController {

    private final EnergyToCarbonFootPrintService energyToCarbonFootPrintService;

    public EnergyToCarbonFootPrintController(EnergyToCarbonFootPrintService energyToCarbonFootPrintService) {
        this.energyToCarbonFootPrintService = energyToCarbonFootPrintService;
    }

    @PostMapping(value = "/getCarbonFootprintForEnergySource")
    public String getCarbonFootprintForEnergySource(@Valid @RequestBody List<EnergyUsage> energyUsages){
        log.info("Energy Sources: " + energyUsages);
        Map<String , CarbonFootprint> carbonFootprintMap = energyToCarbonFootPrintService.calculateCarbonFootPrint(energyUsages);
        JsonArray jsonArray = new JsonArray();
        for (Map.Entry<String, CarbonFootprint> entry : carbonFootprintMap.entrySet()) {
            JsonElement jsonElement = new Gson().toJsonTree(entry.getValue());
            log.info("jsonElement: " + jsonElement);
            jsonArray.add(jsonElement);
        }

        log.info("jsonArray: " + new Gson().toJson(jsonArray));
        return new Gson().toJson(jsonArray);
    }
}
