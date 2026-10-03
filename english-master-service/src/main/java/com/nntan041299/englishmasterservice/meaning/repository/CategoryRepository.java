package com.nntan041299.englishmasterservice.meaning.repository;

import com.nntan041299.englishmasterservice.meaning.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCaseAndUserId(String name, Long userId);

    List<Category> findAllByUserIdOrderByNameAsc(Long userId);
}
