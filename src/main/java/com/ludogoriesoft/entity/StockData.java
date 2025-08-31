package com.ludogoriesoft.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "stock_data")
public class StockData extends PanacheEntity {

    private Double marketCapitalization;

    private Double shareOutstanding;

    public Double getMarketCapitalization() {
        return marketCapitalization;
    }

    public void setMarketCapitalization(Double marketCapitalization) {
        this.marketCapitalization = marketCapitalization;
    }

    public Double getShareOutstanding() {
        return shareOutstanding;
    }

    public void setShareOutstanding(Double shareOutstanding) {
        this.shareOutstanding = shareOutstanding;
    }

    @CreationTimestamp
    @Column(nullable = false)
    public Instant fetchedAt;

    // This is the core of the relationship. Many StockData records can belong to one Company.
    // This will create a 'company_id' foreign key column in our 'stock_data' table.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    public Company company;
}