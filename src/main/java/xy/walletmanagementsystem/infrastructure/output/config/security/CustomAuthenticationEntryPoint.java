package xy.walletmanagementsystem.infrastructure.output.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import xy.walletmanagementsystem.infrastructure.input.rest.data.response.ApiResponse;

import java.io.IOException;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    ApiResponse<Object> apiResponse =
        ApiResponse.builder()
            .data(null)
            .message("Authentication failed: " + authException.getMessage())
            .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
            .isSuccessful(false)
            .timestamp(ZonedDateTime.now())
            .build();
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }
}
