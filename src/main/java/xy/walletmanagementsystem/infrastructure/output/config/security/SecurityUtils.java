package xy.walletmanagementsystem.infrastructure.output.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;

import java.io.IOException;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Component
public class SecurityUtils {
  private final ObjectMapper objectMapper;

  public void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
    ApiResponse<Object> apiResponse =
        ApiResponse.builder()
            .message(message)
            .timestamp(ZonedDateTime.now())
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .data(null)
            .build();
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json");
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }

  public void sendForbiddenErrorResponse(HttpServletResponse response, String message)
      throws IOException {
    ApiResponse<Object> apiResponse =
        ApiResponse.builder()
            .message(message)
            .timestamp(ZonedDateTime.now())
            .statusCode(HttpStatus.FORBIDDEN.value())
            .isSuccessful(false)
            .data(null)
            .build();
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType("application/json");
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    response.getWriter().flush();
  }
}
