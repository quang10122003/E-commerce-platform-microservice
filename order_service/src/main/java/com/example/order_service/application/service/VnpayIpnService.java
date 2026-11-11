package com.example.order_service.application.service;

import com.example.order_service.application.dto.response.VnpayIpnResponse;
import com.example.order_service.application.port.in.HandleVnpayIpnUseCase;
import com.example.order_service.application.port.out.client.VnpaySignaturePort;
import com.example.order_service.application.port.out.repo.PaymentRepoPort;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.Payment;
import com.example.order_service.domain.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class VnpayIpnService implements HandleVnpayIpnUseCase {

    private static final String SUCCESS_CODE = "00";
    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnpaySignaturePort vnpaySignaturePort;
    private final PaymentRepoPort paymentRepoPort;
    private final Clock clock;
    private final PaymentOutcomeService paymentOutcomeService;

    // Khóa dòng payment để các lần VNPay gọi lại cùng giao dịch không xử lý song song.
    @Override
    @Transactional
    public VnpayIpnResponse handleIpn(Map<String, String> parameters) {
        String transactionReference = parameters.get("vnp_TxnRef");
        // check chữ ký
        if (!vnpaySignaturePort.verifySignature(parameters)) {
            log.warn("IPN VNPay sai chữ ký: vnp_TxnRef={}", transactionReference);
            return new VnpayIpnResponse("97", "Invalid Checksum");
        }

        Optional<Payment> foundPayment = transactionReference == null || transactionReference.isBlank()
                ? Optional.empty()
                : paymentRepoPort.findByTransactionReferenceForUpdate(transactionReference);
        // k tìm thấy giao dịch
        if (foundPayment.isEmpty()) {
            log.warn("IPN VNPay không tìm thấy payment: vnp_TxnRef={}", transactionReference);
            return new VnpayIpnResponse("01", "Order not Found");
        }
        Payment payment = foundPayment.get();

        BigDecimal receivedAmount = parseVnpayAmount(parameters.get("vnp_Amount"));
        // chekc tiền
        if (receivedAmount == null || payment.getAmount() == null
                || payment.getAmount().compareTo(receivedAmount) != 0) {
            log.warn("IPN VNPay sai số tiền: vnp_TxnRef={}, expected={}, received={}",
                    transactionReference, payment.getAmount(), receivedAmount);
            return new VnpayIpnResponse("04", "Invalid Amount");
        }


        String responseCode = parameters.get("vnp_ResponseCode");
        String transactionStatus = parameters.get("vnp_TransactionStatus");
        boolean paid = SUCCESS_CODE.equals(responseCode) && SUCCESS_CODE.equals(transactionStatus);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            // Không tự khôi phục đơn vì tồn kho đã hoàn; cần người kiểm tra giao dịch này.
            if (paid && payment.getStatus() == PaymentStatus.CANCEL) {
                log.error("IPN VNPay thành công cho payment đã hủy, cần kiểm tra xử lý: vnp_TxnRef={}, "
                        + "vnp_TransactionNo={}, amount={}, payment_id={}", transactionReference,
                        parameters.get("vnp_TransactionNo"), receivedAmount, payment.getId());
            } else {
                log.info("IPN VNPay cho payment đã xử lý: vnp_TxnRef={}, status={}",
                        transactionReference, payment.getStatus());
            }
            return new VnpayIpnResponse("02", "Order already confirmed");
        }

        // thành toán k thành công
        if (!paid) {
            log.info("IPN VNPay thanh toán không thành công: vnp_TxnRef={}, vnp_ResponseCode={}, "
                    + "vnp_TransactionStatus={}", transactionReference, responseCode, transactionStatus);
            paymentOutcomeService.cancelPaymentAndOrders(payment,
                    "Thanh toán VNPay không thành công, mã " + responseCode, OrderCancelledBy.USER, LocalDateTime.now(clock));
            return new VnpayIpnResponse("00", "Confirm Success");
        }

        LocalDateTime paidAt = parseVnpayDate(parameters.get("vnp_PayDate"));
        if (paidAt == null) {
            log.warn("IPN VNPay sai vnp_PayDate: vnp_TxnRef={}", transactionReference);
            return new VnpayIpnResponse("99", "Invalid PayDate");
        }

        // Tiền đã bị trừ nên vẫn xác nhận; chỉ cảnh báo để đối soát khi giờ thanh toán vượt hạn.
        if (payment.isExpiredAt(paidAt)) {
            log.warn("IPN VNPay thanh toán sau hạn payment: vnp_TxnRef={}, paid_at={}, expires_at={}",
                    transactionReference, paidAt, payment.getExpiresAt());
        }

        LocalDateTime now = LocalDateTime.now(clock);
        payment.markPaid(receivedAmount, parameters.get("vnp_TransactionNo"), parameters.get("vnp_CardType"),
                parameters.get("vnp_BankCode"), parameters.get("vnp_BankTranNo"), responseCode,
                transactionStatus, paidAt, now);
        paymentOutcomeService.savePaidAndConfirmOrders(payment, now);
        log.info("IPN VNPay xác nhận thanh toán thành công: vnp_TxnRef={}, payment_id={}",
                transactionReference, payment.getId());
        return new VnpayIpnResponse("00", "Confirm Success");
    }

    // VNPay gửi số tiền đã nhân 100 nên phải chia lại trước khi so với tiền lưu trong payment.
    private BigDecimal parseVnpayAmount(String value) {
        if (value == null || !value.matches("\\d{1,15}")) {
            return null;
        }
        return new BigDecimal(value).movePointLeft(2);
    }

    // VNPay trả thời gian theo GMT+7, cùng múi giờ với Clock và expires_at lưu trong DB.
    private LocalDateTime parseVnpayDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, VNPAY_DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
