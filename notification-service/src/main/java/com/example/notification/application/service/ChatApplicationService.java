package com.example.notification.application.service;

import com.example.notification.application.port.in.CreateChatConversationsUseCase;
import com.example.notification.application.port.out.client.CurrentUserPort;
import com.example.notification.application.port.out.client.ProductClientPort;
import com.example.notification.domain.model.ChatConversation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ChatApplicationService implements CreateChatConversationsUseCase {

    ProductClientPort productClientPort;
    CurrentUserPort currentUserPort;
    Clock clock;

    // taọ 1 conversion chat mới
    @Override
    public void createRoomChatConversations(Long productId) {
        boolean excitProduct = productClientPort.existsById(productId);

        if(!excitProduct){
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        Long shopid = productClientPort.getShopId(productId);
        Long custommerId =  currentUserPort.getUserInfo().userId();
        ChatConversation chatConversation =  ChatConversation.create(custommerId,shopid,productId,now);
    }

}
