package com.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "license_type")
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int defaultDurationInDays;

    @Column(length = 1000)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getDefaultDurationInDays() { return defaultDurationInDays; }
    public void setDefaultDurationInDays(int defaultDurationInDays) { this.defaultDurationInDays = defaultDurationInDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
