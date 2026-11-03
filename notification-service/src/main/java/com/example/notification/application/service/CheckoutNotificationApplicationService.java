package com.example.notification.application.service;

import com.example.notification.application.dto.event.CheckoutFailedNotification;
import com.example.notification.application.dto.event.CheckoutSucceededNotification;
import com.example.notification.application.dto.response.CheckoutFailedResponse;
import com.example.notification.application.dto.response.CheckoutSucceededResponse;
import com.example.notification.application.port.in.CheckCheckoutNotificationAccessUseCase;
import com.example.notification.application.port.out.NotificationStreamPort;
import com.example.notification.application.port.out.client.CheckoutAccessPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutNotificationApplicationService implements CheckCheckoutNotificationAccessUseCase {
    private final CheckoutAccessPort checkoutAccessPort;
    private final NotificationStreamPort notificationStreamPort;


    // check quyền sub see của check out
    @Override
    public void checkAccess(Long checkoutId) {
        if (checkoutId == null || checkoutId <= 0) {
            throw new IllegalArgumentException("checkoutId không hợp lệ");
        }
        checkoutAccessPort.checkAccess(checkoutId);
    }

    public void notifyCheckoutSucceeded(CheckoutSucceededNotification event) {
        CheckoutSucceededResponse response = CheckoutSucceededResponse.from(event);
        notificationStreamPort.publish("checkout:" + event.checkoutId(), event.eventId(),
                CheckoutSucceededNotification.EVENT_TYPE, response);
    }

    public void notifyCheckoutFailed(CheckoutFailedNotification event) {
        CheckoutFailedResponse response = CheckoutFailedResponse.from(event);
        notificationStreamPort.publish("checkout:" + event.checkoutId(), event.eventId(),
                CheckoutFailedNotification.EVENT_TYPE, response);
    }
}
