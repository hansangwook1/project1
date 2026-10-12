package kopo.poly.foodallergy.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
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
@Slf4j
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

    // 요청 본문 JSON 형식이 잘못된 경우 (예: 찜 추가에 깨진 JSON 전송) → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "요청 형식(JSON)이 올바르지 않습니다."));
    }

    // 그 밖의 예상하지 못한 서버 오류 (DB 연결 실패 등) → 500 JSON
    // 이 처리가 없으면 오류가 /error 로 넘어가고, /error 는 로그인이 필요한 주소라 비로그인 사용자는 로그인 화면으로 이동해 원인이 가려짐
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        // 스프링이 상태 코드를 이미 알고 있는 예외(허용되지 않은 메서드 405, 필수 파라미터 누락 400 등)는 그 코드 그대로 응답
        if (e instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .body(Map.of("message", "요청을 처리할 수 없습니다."));
        }
        log.error("API 처리 중 오류", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 처리 중 오류가 발생했습니다."));
    }
}
