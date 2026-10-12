package kopo.poly.foodallergy.global.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * API(@RestController) 전용 예외 처리
 * - 기존 GlobalExceptionHandler 는 error.html 화면을 반환하므로 API 응답에는 맞지 않음
 * - API 에서 발생한 예외를 {"message": "..."} JSON + 상태 코드로 응답
 */
@RestControllerAdvice(annotations = RestController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    // 서비스에서 상태 코드를 정해 던진 예외 (400, 404, 409 등)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException e) {
        String message = (e.getReason() != null) ? e.getReason() : "요청을 처리할 수 없습니다.";
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", message));
    }

    // [기능2] 숫자 파라미터에 문자가 들어온 경우 (예: /api/search/popular?days=abc) → 400
    // 이 처리가 없으면 원인 예외(NumberFormatException)를 GlobalExceptionHandler 가 받아 error.html(200)로 응답함
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "'" + e.getName() + "' 값의 형식이 올바르지 않습니다."));
    }
}
