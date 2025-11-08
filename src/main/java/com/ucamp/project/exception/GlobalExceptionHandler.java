package com.ucamp.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 인증 정보가 없는 경우 발생 (예: SecurityContextHolder에 User 없음)
     * → 401 Unauthorized 반환
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthenticationCredentialsNotFoundException ex) {
        log.warn("인증 정보 없음: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "인증이 필요합니다.");
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * 유효하지 않은 요청 바디(@RequestBody 누락, DTO 필드 누락 등)
     * → 400 Bad Request 반환
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("요청 유효성 검증 실패: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "요청 데이터가 유효하지 않습니다.");
        body.put("code", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * NullPointerException (예: getCurrentUserId()에서 user == null)
     * → 500 Internal Server Error 반환
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointer(NullPointerException ex) {
        log.error("NullPointerException 발생: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "내부 서버 오류가 발생했습니다.");
        body.put("detail", "null 객체 접근 오류");
        body.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * IllegalArgumentException (예: 유효하지 않은 userId, 파일명 등)
     * → 400 Bad Request 반환
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("잘못된 요청 파라미터: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("code", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 서비스 단에서 발생한 RuntimeException 처리
     * (예: 비공개 질문셋 접근, 존재하지 않는 포스트 등)
     * → 400 Bad Request 반환
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.warn("RuntimeException 발생: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("code", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * 그 외 모든 예외 (정의되지 않은 예외)
     * → 500 Internal Server Error 반환
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("예상치 못한 예외 발생: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "서버 내부 오류가 발생했습니다.");
        body.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
