package org.gb.stellarplayer.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.gb.stellarplayer.Service.OrderService;
import org.gb.stellarplayer.Service.VNPAYService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class VNPAYController {
    private final VNPAYService vnPayService;
    private final OrderService orderService;

    @GetMapping({"", "/"})
    public String home() {
        return "createOrder";
    }

    // Chuyển hướng người dùng đến cổng thanh toán VNPAY
    @PostMapping("/submitOrder")
    public String submidOrder(@RequestParam("amount") int orderTotal,
                              @RequestParam("orderInfo") String orderInfo,
                              HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String vnpayUrl = vnPayService.createOrder(request, orderTotal, orderInfo, baseUrl);
        return "redirect:" + vnpayUrl;
    }

    // Sau khi hoàn tất thanh toán, VNPAY sẽ chuyển hướng trình duyệt về URL này
    @GetMapping("/vnpay-payment-return")
    public RedirectView paymentCompleted(HttpServletRequest request, Model model) {
        // Check if this is a direct access (not from VNPAY)
        if (request.getParameter("vnp_ResponseCode") == null) {
            // This is likely a direct access or reload - redirect to home page
            return new RedirectView("http://localhost:3000");
        }
        int paymentStatus = vnPayService.orderReturn(request);

        // Build the API URL with all VNPAY parameters
        String apiUrl = "http://localhost:8080/api/v1/payments/vnpay-payment-return";
        apiUrl += buildQueryString(request);

        try {
            // Call the API to process the payment
            ResponseEntity<Map> response = new RestTemplate().getForEntity(apiUrl, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Check if the API call was successful
            if (responseBody != null && responseBody.containsKey("redirectUrl")) {
                // Redirect to the URL provided by the API
                String redirectUrl = (String) responseBody.get("redirectUrl");
                return new RedirectView(redirectUrl);
            }
        } catch (Exception e) {
            // Log the error
            System.err.println("Error calling payment API: " + e.getMessage());
        }

        // Fallback redirect if API call fails
        if (paymentStatus == 1) {
            // Default success redirect
            return new RedirectView("http://localhost:3000/userdetail/1");
        } else {
            // Default failure redirect
            return new RedirectView("http://localhost:3000/payment-failed");
        }
    }

    /**
     * Helper method to build query string from request parameters
     */
    private String buildQueryString(HttpServletRequest request) {
        StringBuilder queryString = new StringBuilder("?");
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) {
                if (queryString.length() > 1) {
                    queryString.append("&");
                }
                queryString.append(key).append("=").append(value);
            }
        });
        return queryString.toString();
    }
}
