package com.example.notification.application.dto.template.interfaces;
import com.example.notification.application.dto.request.EmailMessage;

public interface EmailTemplate<T> {

    EmailMessage build(T data);
}