package kopo.poly.foodallergy.domain.user.service;

import kopo.poly.foodallergy.domain.allergy.entity.AllergyItem;
import kopo.poly.foodallergy.domain.allergy.entity.UserAllergy;
import kopo.poly.foodallergy.domain.allergy.repository.AllergyItemRepository;
import kopo.poly.foodallergy.domain.allergy.repository.UserAllergyRepository;
import kopo.poly.foodallergy.domain.user.entity.User;
import kopo.poly.foodallergy.domain.user.repository.UserRepository;
import kopo.poly.foodallergy.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AllergyItemRepository allergyItemRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean isEmailDuplicated(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void join(JoinRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .build();
        userRepository.save(user);

        if (dto.getAllergyIds() != null) {
            for (Integer allergyId : dto.getAllergyIds()) {
                AllergyItem item = allergyItemRepository.findById(allergyId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알레르기 항목입니다."));
                userAllergyRepository.save(UserAllergy.builder()
                        .user(user)
                        .allergyItem(item)
                        .build());
            }
        }

        if (dto.getCustomAllergies() != null) {
            for (String customName : dto.getCustomAllergies()) {
                userAllergyRepository.save(UserAllergy.builder()
                        .user(user)
                        .customName(customName)
                        .build());
            }
        }
    }

    // 아이디(이메일) 찾기 - 닉네임으로 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 계정이 없습니다."));
        userRepository.save(user.withNewPassword(passwordEncoder.encode(newPassword)));
    }
}