package com.github.abeatrizsc.financyx.repositories;

import com.github.abeatrizsc.financyx.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByIdAndUserId(String id, String userId);
    List<Category> findAllByUserId(String userId);
}