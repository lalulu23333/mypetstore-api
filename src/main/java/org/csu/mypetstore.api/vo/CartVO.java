package org.csu.mypetstore.api.vo;

import lombok.Data;
import org.csu.mypetstore.api.common.CommonResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
public class CartVO {
    // String：ItemId作为Key使用
    private final Map<String, CartItemVO> itemMap = Collections.synchronizedMap(new HashMap<String, CartItemVO>());
    public String getItemMap;
    // 是否已经合并
    private boolean merged = true;
    // 每个CartItem有一个userid，这里的id是当购物车内没有CartItem时发挥作用
    private String username = null;

    public Map<String, CartItemVO> getItemMap() {
        return itemMap;
    }

    public boolean containsItemId(String itemId) {
        return itemMap.containsKey(itemId);
    }

    public CartItemVO removeItemById(String itemId) {
        // 删除失败返回null，否则返回被删除的CartItemVO
        return itemMap.remove(itemId);
    }

    public CartItemVO incrementQuantityByItemId(String itemId) {
        CartItemVO cartItem = itemMap.get(itemId);
        cartItem.incrementQuantity();
        return cartItem;
    }

    public CartItemVO setQuantityByItemId(String itemId, int quantity) {
        CartItemVO cartItem = itemMap.get(itemId);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    public BigDecimal getSubTotal() {
        BigDecimal subTotal = new BigDecimal("0");
        for (CartItemVO cartItem : itemMap.values()) {
            BigDecimal qty = new BigDecimal(cartItem.getQuantity());
            subTotal = subTotal.add(cartItem.getUnitPrice().multiply(qty));
        }
        return subTotal;
    }
}
