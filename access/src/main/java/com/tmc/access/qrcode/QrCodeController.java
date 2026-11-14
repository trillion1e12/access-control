package com.tmc.access.qrcode;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmc.access.decision.AccessDecisionService;
import com.tmc.access.qrcode.dto.VerifyQrCodeRequest;

@RestController
@RequestMapping("/qr-codes")
public class QrCodeController {
    private final QrCodeService qrCodeService;
    private final AccessDecisionService accessDecisionService;

    public QrCodeController(QrCodeService qrCodeService, AccessDecisionService accessDecisionService) {
        this.qrCodeService = qrCodeService;
        this.accessDecisionService = accessDecisionService;
    }

    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getQrCode(@AuthenticationPrincipal Jwt jwt) {
        return qrCodeService.getQrCode(jwt.getSubject());
    }

    @PostMapping
    public String verifyQrCode(@RequestBody @Valid VerifyQrCodeRequest request) {
        return accessDecisionService.verifyQrCode(request.content(), request.event());
    }

}
