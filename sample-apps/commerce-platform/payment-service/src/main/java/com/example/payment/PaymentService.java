package com.example.payment;
import org.springframework.stereotype.Service;
@Service public class PaymentService { public boolean authorize(String orderId){ return !orderId.isBlank(); } }
