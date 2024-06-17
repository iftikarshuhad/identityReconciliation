package com.idendity.recon.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IdentityRequestDto {
    String email;
    String phoneNumber;
}
