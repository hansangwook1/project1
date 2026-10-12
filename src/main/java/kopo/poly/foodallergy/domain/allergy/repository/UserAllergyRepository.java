package kopo.poly.foodallergy.domain.allergy.repository;

import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
    List<UserAllergy> findByUser(User user);
    void deleteByUser(User user);
}