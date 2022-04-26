package org.csu.mypetstore.api.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    // all from cartitem table
    private int cartItemId;
    private String username;
    private String itemId;
    // each quantity
    private int quantity;
    private BigDecimal unitPrice;
    private boolean inStock;

    // others from item table
    // pictures
    private String attribute1;
    private String attribute2;
    // Inventory quantity <=> ItemVO.quantity
    private long InventQty;

    private BigDecimal total = new BigDecimal("0");

    public void calculateTotal() {
        if (itemId != null && unitPrice != null)
            total = unitPrice.multiply(new BigDecimal(this.quantity));
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotal();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotal();
    }

    public void incrementQuantity() {
        this.quantity++;
        calculateTotal();
    }
}
