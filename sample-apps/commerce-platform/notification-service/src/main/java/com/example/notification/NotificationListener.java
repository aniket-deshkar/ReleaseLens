package com.example.notification;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
@Component public class NotificationListener { @KafkaListener(topics="order-events") public void onOrder(String event){} }
