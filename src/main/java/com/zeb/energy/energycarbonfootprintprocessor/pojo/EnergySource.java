package com.zeb.energy.energycarbonfootprintprocessor.pojo;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ToString
@Getter
public class EnergySource {
    private String energySourceId;
    private String scopeId;
    private String name;
    private Double conversionFactor;
    private Double emissionFactor;

    public void setEnergySourceId(String energySourceId) {
        this.energySourceId = energySourceId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConversionFactor(Double conversionFactor) {
        BigDecimal conversionFactorDecimal = new BigDecimal(conversionFactor).setScale(5, RoundingMode.FLOOR);
        this.conversionFactor = conversionFactorDecimal.doubleValue();
    }

    public void setEmissionFactor(Double emissionFactor) {
        BigDecimal emissionFactorDecimal = new BigDecimal(emissionFactor).setScale(5, RoundingMode.FLOOR);
        this.emissionFactor = emissionFactorDecimal.doubleValue();
    }
}
