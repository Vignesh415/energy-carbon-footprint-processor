package com.zeb.energy.energycarbonfootprintprocessor.pojo;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

@ToString
@Getter
public class EnergyUsage {
    @NotNull(message = "description is mandatory")
    private String description;
    @NotNull(message = "energySourceId is mandatory")
    private String energySourceId;
    @NotNull(message = "energyConsumption is mandatory")
    private Double energyConsumption;
    private Double emissionFactor;

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnergySourceId(String energySourceId) {
        this.energySourceId = energySourceId;
    }

    public void setEnergyConsumption(Double energyConsumption) {
        BigDecimal energyConsumptionDecimal = new BigDecimal(energyConsumption).setScale(5, RoundingMode.FLOOR);
        this.energyConsumption = energyConsumptionDecimal.doubleValue();
    }

    public void setEmissionFactor(Double emissionFactor) {
        BigDecimal emissionFactorDecimal = new BigDecimal(emissionFactor).setScale(5, RoundingMode.FLOOR);
        this.emissionFactor = emissionFactorDecimal.doubleValue();
    }
}
