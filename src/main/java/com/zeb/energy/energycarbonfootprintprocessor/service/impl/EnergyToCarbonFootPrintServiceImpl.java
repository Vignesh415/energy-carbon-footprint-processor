package com.zeb.energy.energycarbonfootprintprocessor.service.impl;

import com.google.gson.*;
import com.zeb.energy.energycarbonfootprintprocessor.dto.CarbonFootprint;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.EnergySource;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.EnergyUsage;
import com.zeb.energy.energycarbonfootprintprocessor.pojo.ScopeLabel;
import com.zeb.energy.energycarbonfootprintprocessor.service.EnergyToCarbonFootPrintService;
import com.zeb.energy.energycarbonfootprintprocessor.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnergyToCarbonFootPrintServiceImpl implements EnergyToCarbonFootPrintService {

    @Override
    public Map<String, CarbonFootprint> calculateCarbonFootPrint(List<EnergyUsage> energyUsageList) {

        Map<String, CarbonFootprint> carbonFootprintMap = new HashMap<>();
        try{
            //read the energy source data json file and convert to object
            JsonUtils jsonUtils = new JsonUtils();
            String energyDataSource = jsonUtils.readFile("D:\\MyWorkspace\\GitRepo\\energy-carbon-footprint-processor\\src\\main\\resources\\EnergyDataSource.json");
            JsonArray energySourceArray = JsonParser.parseString(energyDataSource).getAsJsonArray();
            final Map<String, EnergySource> energySourceMap = new HashMap<>();
            energySourceArray.asList().forEach(jsonElement-> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                energySourceMap.put(jsonObject.get("energySourceId").getAsString(), new Gson().fromJson(jsonObject, EnergySource.class));
            });

            log.info("EnergyDataSource: " + energySourceMap);
            //read the scopeLabel data source json file and convert to object
            String scopeLabelDataSource = jsonUtils.readFile("D:\\MyWorkspace\\GitRepo\\energy-carbon-footprint-processor\\src\\main\\resources\\ScopeLabelDataSource.json");
            JsonArray scopeLabelSourceArray = JsonParser.parseString(scopeLabelDataSource).getAsJsonArray();
            log.info("ScopeLabelSourceList: " + scopeLabelSourceArray);
            final Map<String, ScopeLabel> scopeLabelMap = new LinkedHashMap<>();
            scopeLabelSourceArray.asList().forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String key = jsonObject.get("id").getAsString();
                ScopeLabel scopeLabel = new Gson().fromJson(jsonObject, ScopeLabel.class);
                scopeLabelMap.put(key, new ScopeLabel(scopeLabel.getId(), scopeLabel.getName(), scopeLabel.getLabel()));
                if(scopeLabel.getSubScopes() !=null && !scopeLabel.getSubScopes().isEmpty()){
                    List<ScopeLabel> subScopes = scopeLabel.getSubScopes();
                    subScopes.forEach(subScope -> {
                        String key1 = subScope.getId();
                        scopeLabelMap.put(key1, new ScopeLabel(subScope.getId(), subScope.getName(), subScope.getLabel()));
                        if(subScope.getSubScopes() != null && !subScope.getSubScopes().isEmpty()){
                            List<ScopeLabel> subScopes1 = subScope.getSubScopes();
                            subScopes1.forEach(subScope1 ->{
                                String key2 = subScope1.getId();
                                scopeLabelMap.put(key2, new ScopeLabel(subScope1.getId(), subScope1.getName(), subScope1.getLabel()));
                            });
                        }
                    });
                }
            });

            log.info("scopeLabel: " + new Gson().toJson(scopeLabelMap));
            log.info("scopeLabelMap: " + scopeLabelMap);
            //For each energy data calculate the carbonFootPrint data
            Map<String, CarbonFootprint> carbonFootprintHashMap = new HashMap<>();
            for(EnergyUsage energyUsage : energyUsageList){
                //get energy source data based on the energy source id
                EnergySource energySource = energySourceMap.get(energyUsage.getEnergySourceId());
                //get scope label data based on the scope id
                log.info("Get the scope label of the energy source scope id: " + energySource.getScopeId());
                ScopeLabel scopeLabel = scopeLabelMap.get(energySource.getScopeId());

                log.info("EnergySourceId: " + energyUsage.getEnergySourceId() +
                        " energySource: " + energySource + " scopeLabel: " + scopeLabel);


                DecimalFormat df = new DecimalFormat("#.#####");
                double energy = energyUsage.getEnergyConsumption()*energySource.getConversionFactor();
                double co2 = (energy*energySource.getEmissionFactor())/1000;
                if(carbonFootprintHashMap.containsKey(scopeLabel.getName())){
                    CarbonFootprint carbonFootprint = carbonFootprintHashMap.get(scopeLabel.getName());
                    double existingEnergy = carbonFootprint.getEnergy();
                    carbonFootprint.setEnergy(existingEnergy+energy);
                    double existingCo2 = carbonFootprint.getCo2();
                    carbonFootprint.setCo2(existingCo2+co2);
                    carbonFootprintHashMap.put(scopeLabel.getName(), carbonFootprint);
                } else if(scopeLabel.getName().length() > 3){
                    log.info("Carbon Label: " + scopeLabel.getName().substring(0, scopeLabel.getName().length()-2));
                    CarbonFootprint carbonFootprint1 = carbonFootprintHashMap.get(scopeLabel.getName().substring(0,scopeLabel.getName().length()-2));
                    carbonFootprintHashMap.put(scopeLabel.getName(), new CarbonFootprint(scopeLabel.getName(),
                            scopeLabel.getLabel().concat(" (").concat(energyUsage.getDescription()).concat(")"),
                            Double.parseDouble(df.format(energy)),Double.parseDouble(df.format(co2))));
                    double existingEnergy = carbonFootprint1.getEnergy();
                    carbonFootprint1.setEnergy(existingEnergy+energy);
                    double existingCo2 = carbonFootprint1.getCo2();
                    carbonFootprint1.setCo2(existingCo2+co2);
                    carbonFootprintHashMap.put(scopeLabel.getName().substring(0, scopeLabel.getName().length()-2), carbonFootprint1);
                } else {
                    carbonFootprintHashMap.put(scopeLabel.getName(), new CarbonFootprint(scopeLabel.getName(),
                            scopeLabel.getLabel(),Double.parseDouble(df.format(energy)),Double.parseDouble(df.format(co2))));
                }
            }
            log.info("carbonFootprintHashMap: " + new Gson().toJson(carbonFootprintHashMap));

            //sort the linked hashmap by keys
            Map<String, CarbonFootprint> sortedMap = carbonFootprintHashMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


            log.info("SortedMap: " +new Gson().toJson(sortedMap));

            for (Map.Entry<String, CarbonFootprint> entry : sortedMap.entrySet()) {
                //check and add root elements

                if(entry.getKey().startsWith("1")){
                    CarbonFootprint carbonFootprint = entry.getValue();
                    if(carbonFootprintMap.containsKey("Scope 1")){
                        if(entry.getKey().length() > 3){
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 1");
                            carbonFootprint1Root.getChildren().forEach(carbonFootprint2Root -> {
                                if(carbonFootprint2Root.getName().equals(entry.getKey().substring(0, entry.getKey().length()-2))){
                                    double totalEnergy = carbonFootprint2Root.getEnergy()+carbonFootprint.getEnergy();
                                    carbonFootprint2Root.setEnergy(totalEnergy);
                                    double totalCo2 = carbonFootprint2Root.getCo2()+carbonFootprint.getCo2();
                                    carbonFootprint2Root.setCo2(totalCo2);
                                    List<CarbonFootprint> existingCarbonFoorPrint = carbonFootprint2Root.getChildren();
                                    existingCarbonFoorPrint.add(carbonFootprint);
                                    carbonFootprint2Root.setChildren(existingCarbonFoorPrint);
                                }
                            });
                        } else {
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 1");
                            double totalEnergy = carbonFootprint1Root.getEnergy()+carbonFootprint.getEnergy();
                            carbonFootprint1Root.setEnergy(totalEnergy);
                            double totalCo2 = carbonFootprint1Root.getCo2()+carbonFootprint.getCo2();
                            carbonFootprint1Root.setCo2(totalCo2);
                            List<CarbonFootprint> existingCarbonFootPrint = carbonFootprint1Root.getChildren();
                            existingCarbonFootPrint.add(carbonFootprint);
                            carbonFootprint1Root.setChildren(existingCarbonFootPrint);
                        }
                    } else {
                        //add root element for scope_1
                        List<CarbonFootprint> carbonFootprintList = new ArrayList<>();
                        carbonFootprintList.add(carbonFootprint);
                        ScopeLabel scopeLabel1 = scopeLabelMap.get("SCOPE_1");
                        carbonFootprintMap.put(scopeLabel1.getName(), new CarbonFootprint(scopeLabel1.getName(),
                                scopeLabel1.getLabel(), carbonFootprint.getEnergy(), carbonFootprint.getCo2(), carbonFootprintList));
                    }

                } else if (entry.getKey().startsWith("2")) {
                    CarbonFootprint carbonFootprint = entry.getValue();
                    if(carbonFootprintMap.containsKey("Scope 2")){
                        if(entry.getKey().length() > 3){
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 2");
                            carbonFootprint1Root.getChildren().forEach(carbonFootprint2Root -> {
                                if(carbonFootprint2Root.getName().equals(entry.getKey().substring(0, entry.getKey().length()-2))){
                                    double totalEnergy = carbonFootprint2Root.getEnergy()+carbonFootprint.getEnergy();
                                    carbonFootprint2Root.setEnergy(totalEnergy);
                                    double totalCo2 = carbonFootprint2Root.getCo2()+carbonFootprint.getCo2();
                                    carbonFootprint2Root.setCo2(totalCo2);
                                    List<CarbonFootprint> existingCarbonFoorPrint = carbonFootprint2Root.getChildren();
                                    existingCarbonFoorPrint.add(carbonFootprint);
                                    carbonFootprint2Root.setChildren(existingCarbonFoorPrint);
                                }
                            });
                        } else {
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 2");
                            double totalEnergy = carbonFootprint1Root.getEnergy()+carbonFootprint.getEnergy();
                            carbonFootprint1Root.setEnergy(totalEnergy);
                            double totalCo2 = carbonFootprint1Root.getCo2()+carbonFootprint.getCo2();
                            carbonFootprint1Root.setCo2(totalCo2);
                            List<CarbonFootprint> existingCarbonFoorPrint = carbonFootprint1Root.getChildren();
                            existingCarbonFoorPrint.add(carbonFootprint);
                            carbonFootprint1Root.setChildren(existingCarbonFoorPrint);
                        }
                    } else {
                        //add root element for scope_1
                        List<CarbonFootprint> carbonFootprintList = new ArrayList<>();
                        carbonFootprintList.add(carbonFootprint);
                        ScopeLabel scopeLabel1 = scopeLabelMap.get("SCOPE_2");
                        carbonFootprintMap.put(scopeLabel1.getName(), new CarbonFootprint(scopeLabel1.getName(),
                                scopeLabel1.getLabel(), carbonFootprint.getEnergy(), carbonFootprint.getCo2(), carbonFootprintList));
                    }
                } else if (entry.getKey().startsWith("3")) {
                    CarbonFootprint carbonFootprint = entry.getValue();
                    if(carbonFootprintMap.containsKey("Scope 3")){
                        if(entry.getKey().length() >3){
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 3");
                            carbonFootprint1Root.getChildren().forEach(carbonFootprint2Root -> {
                                if(carbonFootprint2Root.getName().equals(entry.getKey().substring(0, entry.getKey().length()-2))){
                                    double totalEnergy = carbonFootprint2Root.getEnergy()+carbonFootprint.getEnergy();
                                    carbonFootprint2Root.setEnergy(totalEnergy);
                                    double totalCo2 = carbonFootprint2Root.getCo2()+carbonFootprint.getCo2();
                                    carbonFootprint2Root.setCo2(totalCo2);
                                    List<CarbonFootprint> existingCarbonFootPrint = carbonFootprint2Root.getChildren();
                                    existingCarbonFootPrint.add(carbonFootprint);
                                    carbonFootprint2Root.setChildren(existingCarbonFootPrint);
                                }
                            });
                        } else {
                            CarbonFootprint carbonFootprint1Root = carbonFootprintMap.get("Scope 3");
                            double totalEnergy = carbonFootprint1Root.getEnergy()+carbonFootprint.getEnergy();
                            carbonFootprint1Root.setEnergy(totalEnergy);
                            double totalCo2 = carbonFootprint1Root.getCo2()+carbonFootprint.getCo2();
                            carbonFootprint1Root.setCo2(totalCo2);
                            List<CarbonFootprint> existingCarbonFootPrint = carbonFootprint1Root.getChildren();
                            existingCarbonFootPrint.add(carbonFootprint);
                            carbonFootprint1Root.setChildren(existingCarbonFootPrint);
                        }
                    } else {
                        //add root element for scope_1
                        List<CarbonFootprint> carbonFootprintList = new ArrayList<>();
                        carbonFootprintList.add(carbonFootprint);
                        ScopeLabel scopeLabel1 = scopeLabelMap.get("SCOPE_3");
                        carbonFootprintMap.put(scopeLabel1.getName(), new CarbonFootprint(scopeLabel1.getName(),
                                scopeLabel1.getLabel(), carbonFootprint.getEnergy(), carbonFootprint.getCo2(), carbonFootprintList));
                    }
                }
            }

            log.info("CarbonFoot Print: " + new Gson().toJson(carbonFootprintMap));

        } catch (Exception e) {
            log.info("Unable to calculate carbon footprint: " + e);
        }
        return carbonFootprintMap;
    }
}
