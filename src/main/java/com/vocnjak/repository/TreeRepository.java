package com.vocnjak.repository;

import com.vocnjak.domain.Tree;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TreeRepository extends JpaRepository<Tree, Long> {

    List<Tree> findByOrchardId(Long orchardId);

    Optional<Tree> findByOrchardIdAndRowIndexAndColIndex(Long orchardId, int rowIndex, int colIndex);

    boolean existsByOrchardIdAndRowIndexAndColIndex(Long orchardId, int rowIndex, int colIndex);
}