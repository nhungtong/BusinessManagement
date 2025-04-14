package com.example.businessmanagement.service;

import com.example.businessmanagement.config.VnPayConfig;
import com.example.businessmanagement.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Service
public class VNPayService {
    private final VnPayConfig vnPayConfig;
    private final OrderRepository orderRepository;

    public VNPayService(VnPayConfig vnPayConfig, OrderRepository orderRepository) {
        this.vnPayConfig = vnPayConfig;
        this.orderRepository = orderRepository;
    }

    public String createPaymentUrl(String orderInfo, String amount, String orderId, HttpServletRequest request) {
        String vnp_ExpireDate = getVnpExpireDate();

        // Tạo tham số đầu vào
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(Integer.parseInt(amount) * 100));
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_CreateDate", getCurrentDateTime());
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_OrderInfo", URLEncoder.encode(orderInfo, StandardCharsets.UTF_8));
        vnpParams.put("vnp_OrderType", vnPayConfig.getOrderType());
        vnpParams.put("vnp_ReturnUrl", URLEncoder.encode(vnPayConfig.getVnp_ReturnUrl(), StandardCharsets.UTF_8));
        vnpParams.put("vnp_TxnRef", orderId);
        // Lấy IP của client
        String clientIp = getClientIp(request);

        // Mã hóa IP trước khi đưa vào vnpParams
        vnpParams.put("vnp_IpAddr", URLEncoder.encode(clientIp, StandardCharsets.UTF_8));

        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ExpireDate", vnp_ExpireDate);

        // Log tất cả các tham số vnpParams
        System.out.println("vnpParams:");
        vnpParams.forEach((key, value) -> System.out.println(key + " = " + value));

        // Tạo chuỗi dữ liệu để ký (sắp xếp theo thứ tự, loại bỏ vnp_SecureHash)
        StringBuilder queryString = new StringBuilder();
        vnpParams.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sắp xếp theo key
                .forEach(entry -> {
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        queryString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    }
                });

        // Loại bỏ dấu '&' cuối cùng
        if (queryString.length() > 0) {
            queryString.deleteCharAt(queryString.length() - 1); // Xóa ký tự & cuối cùng
        }
        System.out.println("Query String before HMAC: " + queryString.toString());

        // Tạo chữ ký SHA-512 bằng HMAC
        String secureHash = generateHMACSHA512(queryString.toString(), vnPayConfig.getSecretKey());

        // Log chữ ký tạo ra
        System.out.println("Generated Secure Hash: " + secureHash);

        // Thêm chữ ký vào tham số (vnp_SecureHash luôn phải đứng cuối)
        queryString.append("&vnp_SecureHash=").append(secureHash);

        // Log URL thanh toán
        StringBuilder paymentUrl = new StringBuilder(vnPayConfig.getVnp_PayUrl());
        paymentUrl.append("?").append(queryString.toString());
        System.out.println("Generated Payment URL: " + paymentUrl.toString());

        // Trả về URL thanh toán
        return paymentUrl.toString();
    }

    private String generateHMACSHA512(String data, String secretKey) {
        try {
            // Tạo đối tượng HMAC với thuật toán SHA-512
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);

            // Thực hiện băm dữ liệu
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Chuyển byte thành chuỗi Hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase(); // Trả về hash dạng chữ hoa
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi mã hóa HMAC-SHA512", e);
        }
    }
    private String getCurrentDateTime() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.ofHours(7)));
    }

    public String getVnpExpireDate() {
        // Cộng 15 phút cho thời gian hiện tại và định dạng theo mẫu "yyyyMMddHHmmss"
        return java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(java.time.ZonedDateTime.now(java.time.ZoneOffset.ofHours(7)).plusMinutes(15));
    }


    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-Forwarded-For");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
