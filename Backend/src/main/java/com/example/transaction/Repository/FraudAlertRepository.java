package com.example.transaction.Repository;


import com.example.transaction.entity.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    List<FraudAlert> findByReviewedFalse();
}
