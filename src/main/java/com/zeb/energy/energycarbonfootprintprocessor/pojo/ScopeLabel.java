package com.zeb.energy.energycarbonfootprintprocessor.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ScopeLabel {
    private String id;
    private String name;
    private String label;
    List<ScopeLabel> subScopes;

    public ScopeLabel(String id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public ScopeLabel(String id, String name, String label, List<ScopeLabel> subScopes) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.subScopes = subScopes;
    }
}
