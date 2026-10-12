package kopo.poly.foodallergy.domain.allergy.service;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.repository.AllergyItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllergyService {

    private final AllergyItemRepository allergyItemRepository;

    public List<AllergyItem> findAll() {
        return allergyItemRepository.findAll();
    }
}