package kopo.poly.foodallergy.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 인증번호 임시 저장 (이메일 -> 인증번호)
    private final Map<String, String> codeStore = new ConcurrentHashMap<>();

    // 인증번호 발송
    public void sendCode(String email) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        codeStore.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[뭐먹을수있지?] 인증번호 안내");
        message.setText("인증번호: " + code + "\n\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    // 인증번호 검증
    public boolean verifyCode(String email, String code) {
        String stored = codeStore.get(email);
        if (stored != null && stored.equals(code)) {
            codeStore.remove(email);
            return true;
        }
        return false;
    }
}