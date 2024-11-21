package ru.Darvin.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EquipmentNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleEquipmentNotFound(EquipmentNotFoundException ex){
        Map<String,String> response = createErrorResponse(ex.getMessage());
        logError(ex);

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> handleRuntimeException (RuntimeException ex, WebRequest request){
        Map<String,String> errorResponse = createErrorResponse(ex.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    // Метод для создания стандартной структуры ответа об ошибке
    private Map<String,String> createErrorResponse (String errorMessage){
        Map<String,String> response = new HashMap<>();
        response.put("error",errorMessage);
        return response;
    }

    //метод для логирования ошибок
    private void logError(Exception ex){
        System.out.println("Ошибка: "+ex.getMessage());
    }
}
