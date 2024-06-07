package com.idendity.recon.controller;

import com.idendity.recon.model.IdentityRequestDto;
import com.idendity.recon.model.IdentityResponseDto;
import com.idendity.recon.service.ReconService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReconController {
    @Autowired
    ReconService reconService;

    @PostMapping("/identity")
    public IdentityResponseDto saveIdentityRequest(@Valid @RequestBody IdentityRequestDto identityRequestDto) {
        return reconService.saveIdentityRequest(identityRequestDto);
    }
}
