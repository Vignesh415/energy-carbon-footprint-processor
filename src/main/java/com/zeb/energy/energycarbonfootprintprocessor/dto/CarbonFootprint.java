package com.zeb.energy.energycarbonfootprintprocessor.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@NoArgsConstructor
public class CarbonFootprint {
    public String name;
    public String label;
    public double energy;
    public double co2;
    private List<CarbonFootprint> children = new ArrayList<>();

    public CarbonFootprint(String name, String label, double energy, double co2) {
        this.name = name;
        this.label = label;
        this.energy = energy;
        this.co2 = co2;
    }

    public CarbonFootprint(String name, String label, double energy, double co2, List<CarbonFootprint> children) {
        this.name = name;
        this.label = label;
        this.energy = energy;
        this.co2 = co2;
        this.children = children;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setEnergy(double energy) {
        BigDecimal energyDecimal = new BigDecimal(energy).setScale(5, RoundingMode.FLOOR);
        this.energy = energyDecimal.doubleValue();
    }

    public void setCo2(double co2) {
        BigDecimal co2Decimal = new BigDecimal(co2).setScale(5, RoundingMode.FLOOR);
        this.co2 = co2Decimal.doubleValue();
    }

    public void setChildren(List<CarbonFootprint> children) {
        this.children = children;
    }
}
