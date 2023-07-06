package com.sander.wrdcounter.repository;

import com.sander.wrdcounter.dto.WordData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepository extends JpaRepository<WordData, String> {
}
