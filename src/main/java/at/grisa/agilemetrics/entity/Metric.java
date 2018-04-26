package at.grisa.agilemetrics.entity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class Metric {
    public final Double value;
    public final String name;
    public final Map meta;
    public final LocalDate date;

    public Metric(Double value, String name, HashMap<String, String> meta) {
        this.value = value;
        this.name = name;
        this.meta = meta;
        this.date = LocalDate.now();
    }

    public Metric(Double value, String name, HashMap<String, String> meta, LocalDate date) {
        this.value = value;
        this.name = name;
        this.meta = meta;
        this.date = date;
    }
}
