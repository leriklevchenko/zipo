package com.example.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "licenses")
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant firstActivationDate;
    private Instant endingDate;

    @Column(nullable = false)
    private int deviceCount;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status;

    @Column(nullable = false)
    private boolean blocked;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public LicenseType getType() { return type; }
    public void setType(LicenseType type) { this.type = type; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getFirstActivationDate() { return firstActivationDate; }
    public void setFirstActivationDate(Instant firstActivationDate) { this.firstActivationDate = firstActivationDate; }

    public Instant getEndingDate() { return endingDate; }
    public void setEndingDate(Instant endingDate) { this.endingDate = endingDate; }

    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LicenseStatus getStatus() { return status; }
    public void setStatus(LicenseStatus status) { this.status = status; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
