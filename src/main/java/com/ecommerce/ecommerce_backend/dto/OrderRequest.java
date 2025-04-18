package com.ecommerce.ecommerce_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    private List<Item> items;


    @Data
    public static class Item {
        private Long productId;
        private int quantity;
    }

}
