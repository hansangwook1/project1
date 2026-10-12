package kopo.poly.foodallergy.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kopo.poly.foodallergy.domain.user.service.UserService;
import kopo.poly.foodallergy.dto.WithdrawRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 API
 * [기능5] DELETE /api/users/me  회원 탈퇴 (본문: {"password": "..."}) → 204 / 400(비밀번호 불일치)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserApiController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@RequestBody WithdrawRequestDto request,
                                         Authentication authentication,
                                         HttpServletRequest httpRequest,
                                         HttpServletResponse httpResponse) {
        userService.withdraw(authentication.getName(), request.password());

        // 탈퇴 후 로그인 상태 정리 (세션 무효화 + 인증 정보 삭제)
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
        return ResponseEntity.noContent().build();
    }
}
