package com.idendity.recon.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IdentityRequestDto {
    @Email(message = "Invalid email format")
    String email;
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    String phoneNumber;
}
