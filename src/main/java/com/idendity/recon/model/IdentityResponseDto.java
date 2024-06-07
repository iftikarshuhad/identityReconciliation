package com.idendity.recon.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class IdentityResponseDto {
    ContactResponse contact;
    @Data
    public static class ContactResponse {
        Integer primaryContactId;
        List<String> emails;
        List<String> phoneNumbers;
        List<Integer> secondaryContactIds;
    }
}
