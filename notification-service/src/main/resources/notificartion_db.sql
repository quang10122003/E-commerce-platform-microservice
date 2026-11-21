CREATE DATABASE notifiservice_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE notifiservice_db;

CREATE TABLE banners (
                         id BIGINT NOT NULL AUTO_INCREMENT,
                         name_banner VARCHAR(255) NOT NULL,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         html LONGTEXT NOT NULL,
                         created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                         PRIMARY KEY (id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS chat_conversations (
                                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                                  customer_id BIGINT NOT NULL,
                                                  shop_id BIGINT NOT NULL,
                                                  product_id BIGINT NOT NULL,
                                                  last_message_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    UNIQUE KEY uk_conversation_customer_shop_product
(customer_id, shop_id, product_id),
    INDEX idx_chat_conversations_customer_updated (customer_id, updated_at),
    INDEX idx_chat_conversations_shop_updated (shop_id, updated_at),
    INDEX idx_chat_conversations_product (product_id)
    ) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS chat_messages (
                                             id BIGINT NOT NULL AUTO_INCREMENT,
                                             conversation_id BIGINT NOT NULL,
                                             sender_id BIGINT NOT NULL,
                                             content TEXT NOT NULL,
                                             is_read BOOLEAN NOT NULL DEFAULT FALSE,

                                             created_at DATETIME(6) NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6),

    updated_at DATETIME(6) NOT NULL
    DEFAULT CURRENT_TIMESTAMP(6)
    ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_chat_message_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES chat_conversations(id)
    ON DELETE CASCADE,
    INDEX idx_chat_messages_conversation_created (conversation_id, created_at),
    INDEX idx_chat_messages_conversation_read (conversation_id, is_read)
    ) ENGINE = InnoDB;
