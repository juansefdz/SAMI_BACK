package main.java.com.sami.sami_app.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "The user is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 150 characters")
    private String email;
    @NotBlank(message = "The password is required")
    @Size(min = 3, max = 100, message = "Password must be between 3 and 150 characters")
    private String password;
}
