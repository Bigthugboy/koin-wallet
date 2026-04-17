package xy.walletmanagementsystem.infrastructure.output.config.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WhiteList {
  static final String[] GET_PATTERNS = {
      "/v3/api-docs",
      "/v3/api-docs/**",
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/swagger-resources/**",
      "/webjars/**"
  };

  static final String[] POST_PATTERNS = {};

  static final String[] DELETE_PATTERNS = {};

  static final String[] PUT_PATTERNS = {};
}
