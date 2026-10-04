package com.vocnjak.service;

import com.vocnjak.domain.Orchard;
import com.vocnjak.domain.Tree;
import com.vocnjak.repository.OrchardRepository;
import com.vocnjak.repository.TreeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class OrchardCascadeDeleteTest {

    @Autowired
    private OrchardRepository orchardRepository;

    @Autowired
    private TreeRepository treeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void deleteOrchard_cascadesTrees() {
        Orchard orchard = orchardRepository.save(new Orchard("Backyard", 4, 5));
        Tree first = new Tree(orchard, 0, 0);
        first.setSpecies("Apple");
        Tree second = new Tree(orchard, 1, 1);
        second.setSpecies("Pear");
        treeRepository.save(first);
        treeRepository.save(second);
        Long orchardId = orchard.getId();

        entityManager.flush();
        entityManager.clear();

        orchardRepository.deleteById(orchardId);
        orchardRepository.flush();

        assertThat(treeRepository.findByOrchardId(orchardId)).isEmpty();
    }
}