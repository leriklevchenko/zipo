package com.example.storage;

import com.example.model.License;
import com.example.model.Product;
import com.example.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseRepository extends JpaRepository<License, UUID> {
    Optional<License> findByCode(String code);
    boolean existsByCode(String code);

    @Query("""
            select l
            from License l
            join DeviceLicense dl on dl.license = l
            where dl.device.id = :deviceId
              and l.user = :user
              and l.product = :product
              and l.blocked = false
              and l.endingDate >= :now
            order by l.endingDate desc
            """)
    List<License> findActiveByDeviceUserAndProduct(@Param("deviceId") Long deviceId,
                                                   @Param("user") User user,
                                                   @Param("product") Product product,
                                                   @Param("now") Instant now);
}
