package com.example.order;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/orders")
public class OrderController {
  @GetMapping("/{id}") public OrderResponse get(@PathVariable String id){ return new OrderResponse(id,"PENDING"); }
  public record OrderResponse(String id,String status) {}
}
