package com.example.notification.application.dto.template;

import java.util.List;


import com.example.notification.application.dto.event.UserRegisteredEvent;
import com.example.notification.application.dto.request.EmailMessage;
import com.example.notification.application.dto.template.interfaces.EmailTemplate;
// tempale tạo EmailMessage  Dto cho case UserRegistered
public class UserRegisteredEmailTemplate
        implements EmailTemplate<UserRegisteredEvent> {

    private final String welcomeSender;

    public UserRegisteredEmailTemplate(String welcomeSender) {
        this.welcomeSender = welcomeSender;
    }

    @Override
    public EmailMessage build(UserRegisteredEvent event) {
        String html = """
                <html>
                    <body>
                        <h2>Chao mung %s!</h2>
                        <p>Tai khoan cua ban da duoc tao thanh cong.</p>
                    </body>
                </html>
                """.formatted(event.fullName());

        return new EmailMessage(
                welcomeSender,
                List.of(event.email()),
                "Chao mung ban",
                html);
    }
}
