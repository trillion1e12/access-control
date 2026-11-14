package com.tmc.access.qrcode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

@Component
public class ZxingQrImageEncoder implements QrImageEncoder {
    private static final int SIZE_PX = 320;

    @Override
    public byte[] encode(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("Content cannot be encoded as a QR code", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
