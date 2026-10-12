package kopo.poly.foodallergy.domain.allergy.repository;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyItemRepository extends JpaRepository<AllergyItem, Integer> {
}