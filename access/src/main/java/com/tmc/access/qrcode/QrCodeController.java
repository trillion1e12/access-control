package com.tmc.access.qrcode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qr-codes")
public class QrCodeController {

    @GetMapping("/test")
    public String hello() {
        return "Hello :D";
    }

}
