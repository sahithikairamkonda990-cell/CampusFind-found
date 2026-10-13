package com.campusfind.repository;

import com.campusfind.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findAllByOrderByCreatedAtDesc();
}
