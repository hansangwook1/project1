package kopo.poly.foodallergy.dto;

/**
 * 회원 탈퇴 요청 (DELETE /api/users/me 의 JSON 본문)
 * 예: {"password": "현재비밀번호"}
 */
public record WithdrawRequestDto(String password) {
}
