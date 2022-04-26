package org.csu.mypetstore.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.CartItem;
import org.csu.mypetstore.api.entity.Item;
import org.csu.mypetstore.api.entity.ItemInventory;
import org.csu.mypetstore.api.entity.Product;
import org.csu.mypetstore.api.persistence.CartItemMapper;
import org.csu.mypetstore.api.persistence.InventoryMapper;
import org.csu.mypetstore.api.persistence.ItemMapper;
import org.csu.mypetstore.api.persistence.ProductMapper;
import org.csu.mypetstore.api.service.CartService;
import org.csu.mypetstore.api.vo.CartItemVO;
import org.csu.mypetstore.api.vo.CartVO;
import org.csu.mypetstore.api.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("cartService")
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public CartVO getCart(String username) {
        CartVO cartVO = new CartVO();
        if (username != null) {
            List<CartItemVO> cartItemVOS = this.getCartItemsByUsername(username);
            for (CartItemVO cartItemVO : cartItemVOS) {
                cartVO.getItemMap().put(cartItemVO.getItemId(), cartItemVO);
            }
            cartVO.setUsername(username);
        }
        // 传入username为null则默认新建一个新的临时购物车
        return cartVO;
    }

    @Override
    public CartVO mergeCarts(CartVO tempCart, CartVO persisCart) {
        // persisCart传入null
        if (persisCart == null) {
            if (tempCart != null) return tempCart;
            else return new CartVO();
        }
        // 当前没有临时购物车则直接返回持久化购物车
        if (tempCart == null) {
            persisCart.setMerged(true);
            return persisCart;
        }
        // tempCart、persisCart均不为null
        // 这里应使用Service层的MyBatis-Plus CRUD接口或者自定义接口批量插入以提高效率
        CartItem cartItem;
        String username = persisCart.getUsername();
        try {
            // 临时购物车持久化
            Map<String, CartItemVO> tempMap = tempCart.getItemMap(), persisMap = persisCart.getItemMap();
            for (Map.Entry<String, CartItemVO> entry : tempMap.entrySet()) {
                String tempKey = entry.getKey();
                CartItemVO tempVal = entry.getValue();
                if (persisCart.containsItemId(tempKey)){
                    // 如果已持久化的购物车中有这个商品，则改变persisCart中该商品的数量(加上tempCart的)、更新DB
                    plusQty(persisCart, tempKey, tempVal.getQuantity(), true);
                } else {
                    // 如果已持久化的购物车中没有这个商品，则相当于是新添加了一个商品tempVal
                    tempVal.setUsername(username);
                    cartItem = VOToCartItem(tempVal);
                    // 临时购物车没有userid(所以里面的商品也没有)，但是查出来的持久化购物车有userid
                    cartItemMapper.insert(cartItem);
                    // 主键cartItemId为物理自增
                    tempVal.setCartItemId(cartItem.getCartItemId());
                    // 最后加入到对象的Map当中
                    persisMap.put(tempKey, tempVal);
                }
            }
            persisCart.setMerged(true);
            return persisCart;
        } catch (Exception e) {
            log.error("购物车合并出错", e);
            return null;
        }
    }

    @Override
    public CommonResponse addCartItem(CartVO cart, String itemId) {
        // 成功返回1，失败返回0
        ItemVO item = this.getItemByItemId(itemId);
        // 查不到item，失败返回错误0
        if (item == null) return CommonResponse.createForError("数据库中没有这个item商品");
        Map<String, CartItemVO> itemMap = cart.getItemMap();
        CartItemVO cartItem = itemMap.get(itemId);
        try {
            if (!cart.isMerged()) {
                // 未登录，未合并，只需加入临时购物车
                if (cartItem == null) {
                    // 购物车中没有的
                    cartItem = itemToCartItemVO(item, null);
                    itemMap.put(item.getItemId(), cartItem);
                } else {
                    cartItem.incrementQuantity();
                }
            } else {
                // 已登录，已合并，加入购物车的同时需要持久化
                if (cartItem == null) {
                    // 购物车中没有的
                    cartItem = itemToCartItemVO(item, cart.getUsername());
                    // 持久化：插入数据库
                    CartItem persisItem = VOToCartItem(cartItem);
                    cartItemMapper.insert(persisItem);
                    // 持久化后设置自增得到的主键值
                    cartItem.setCartItemId(persisItem.getCartItemId());
                    itemMap.put(item.getItemId(), cartItem);
                } else {
                    CartItem persisItem = VOToCartItem(cartItem);
                    persisItem.setQuantity(persisItem.getQuantity()+1);
                    // 持久化：更新数据库
                    cartItemMapper.updateById(persisItem);
                    cartItem.incrementQuantity();
                }
            }
            return CommonResponse.createForSuccessMessage("商品"+ itemId +"添加成功");
        } catch (Exception e) {
            log.error("购物车持久化失败：添加时", e);
            return CommonResponse.createForError("购物车持久化失败：添加时");
        }
    }
    //获取已登录账号的购物车
    @Override
    public CommonResponse<CartVO> getCartRe(String username) {
        QueryWrapper<CartItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("buyername", username);
        List<CartItem> cartItemList = cartItemMapper.selectList(queryWrapper);
        if(cartItemList.isEmpty())
            return CommonResponse.createForSuccessMessage("购物车为空");

        CartVO cartVO = new CartVO();
        return CommonResponse.createForSuccess(cartVO);
    }
    @Override
    public CommonResponse addCartItem(CartVO cart, String itemId, int plusQty) {
        // 成功返回1，失败返回0
        ItemVO item = this.getItemByItemId(itemId);
        // 查不到item，失败返回错误0
        if (item == null) return CommonResponse.createForError("数据库中没有这个item商品");

        if (!cart.containsItemId(itemId)) return CommonResponse.createForError("购物车中无该商品");
        try {
            plusQty(cart, itemId, plusQty, cart.isMerged());
            return CommonResponse.createForSuccessMessage("商品"+itemId+"数量更新成功");
        } catch (Exception e) {
            log.error("数量更新过程出错", e);
            return CommonResponse.createForError("数量更新过程出错");
        }
    }

    @Override
    public CommonResponse deleteCartItem(CartVO cart, String itemId) {
        if (!cart.containsItemId(itemId)) {
            // 临时购物车中没有，说明数据库中肯定也没有
            return CommonResponse.createForError("购物车中没有这个item商品");
        }
        Map<String, CartItemVO> itemMap = cart.getItemMap();
        try {
            if (cart.isMerged()) {
                // 已登录，已合并，从临时购物车中删除的同时需要更新数据库
                CartItemVO cartItem = itemMap.get(itemId);
                CartItem persisItem = VOToCartItem(cartItem);
                // 持久化：删除数据库记录
                cartItemMapper.deleteById(persisItem.getCartItemId());
            }
            // 未登录，未合并则只需从临时购物车中删除
            cart.removeItemById(itemId);
            return CommonResponse.createForSuccessMessage("商品"+itemId+"删除成功", cart);
        } catch (Exception e) {
            log.error("删除过程出错", e);
            return CommonResponse.createForError("删除过程出错");
        }
    }

    @Override
    public CommonResponse updateCartItemQty(CartVO cart, int quantity, String itemId) {
        Map<String, CartItemVO> itemMap = cart.getItemMap();
        if (!cart.containsItemId(itemId)) {
            // 临时购物车中没有，说明数据库中肯定也没有
            return CommonResponse.createForError("购物车中没有这个item商品");
        }
        try {
            CartItemVO cartItem = itemMap.get(itemId);
            if (cart.isMerged()) {
                // 已登录，已合并，从临时购物车中更新的同时需要更新数据库
                // 持久化：构造更新器更新数据库quantity字段
                UpdateWrapper<CartItem> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("cartitemid", cartItem.getCartItemId()).set("quantity", quantity);
                cartItemMapper.update(null, updateWrapper);
            }
            // 未登录，未合并，只需从临时购物车中更新
            cartItem.setQuantity(quantity);
            return CommonResponse.createForSuccessMessage("商品"+itemId+"数量更新成功");
        } catch (Exception e) {
            log.error("数量更新过程出错", e);
            return CommonResponse.createForError("数量更新过程出错");
        }
    }

    @Override
    public CartItemVO getCartItem(int cartItemId) {
        CartItem cartItem = cartItemMapper.selectById(cartItemId);
        return this.cartItemToVO(cartItem);
    }

    @Override
    public List<CartItemVO> getCartItemsByUsername(String username) {
        QueryWrapper<CartItem> cartItemQW = new QueryWrapper<>();
        cartItemQW.eq("userid", username);
        List<CartItem> cartItems = cartItemMapper.selectList(cartItemQW);
        List<CartItemVO> res = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            CartItemVO cartItemVO = this.cartItemToVO(cartItem);
            res.add(cartItemVO);
        }
        return res;
    }

    private CartItemVO cartItemToVO(CartItem cartItem) {
        CartItemVO cartItemVO = new CartItemVO();

        cartItemVO.setCartItemId(cartItem.getCartItemId());
        cartItemVO.setUsername(cartItem.getUsername());
        cartItemVO.setItemId(cartItem.getItemId());
        cartItemVO.setInStock(cartItem.isInStock());
        cartItemVO.setQuantity(cartItem.getQuantity());
        cartItemVO.setUnitPrice(cartItem.getUnitPrice());
        cartItemVO.setAttribute1(cartItem.getAttribute1());
        cartItemVO.setAttribute2(cartItem.getAttribute2());
        cartItemVO.setInventQty(cartItem.getInventQty());

        cartItemVO.calculateTotal();
        return cartItemVO;
    }

    // 维护或者扩展时，将返回类型设为Map或者HashMap则可返回多个类
    private CartItem VOToCartItem(CartItemVO cartItemVO) {
        CartItem cartItem = new CartItem();
        cartItem.setCartItemId(cartItemVO.getCartItemId());
        cartItem.setUsername(cartItemVO.getUsername());
        cartItem.setItemId(cartItemVO.getItemId());
        cartItem.setQuantity(cartItemVO.getQuantity());
        cartItem.setUnitPrice(cartItemVO.getUnitPrice());
        cartItem.setInStock(cartItemVO.isInStock());
        cartItem.setAttribute1(cartItemVO.getAttribute1());
        cartItem.setAttribute2(cartItemVO.getAttribute2());
        cartItem.setInventQty(cartItemVO.getInventQty());

        return cartItem;
    }

    private ItemVO getItemByItemId(String itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) return null;

        Product product = productMapper.selectById(item.getProductId());
        if (product == null) return null;

        return this.itemToItemVO(item, product);
    }

    // 整合成为ItemVO
    private ItemVO itemToItemVO(Item item, Product product){

        ItemVO itemVO = new ItemVO();
        // item 字段注入
        itemVO.setItemId(item.getItemId());
        itemVO.setProductId(item.getProductId());
        itemVO.setListPrice(item.getListPrice());
        itemVO.setUnitCost(item.getUnitCost());
        itemVO.setSupplierId(item.getSupplierId());
        itemVO.setStatus(item.getStatus());
        itemVO.setAttribute1(item.getAttribute1());
        itemVO.setAttribute2(item.getAttribute2());
        itemVO.setAttribute3(item.getAttribute3());
        itemVO.setAttribute4(item.getAttribute4());
        itemVO.setAttribute5(item.getAttribute5());

        // product 字段注入
        itemVO.setCategoryId(product.getCategoryId());
        itemVO.setProductName(product.getName());
        itemVO.setProductDescription(product.getDescription());

        // inventory 字段注入
        ItemInventory itemInventory = inventoryMapper.selectById(item.getItemId());
        itemVO.setQuantity(itemInventory.getQuantity());

        return itemVO;
    }

    private CartItemVO itemToCartItemVO(ItemVO item, String userid) {
        CartItemVO cartItemVO = new CartItemVO();
        cartItemVO.setUsername(userid);
        cartItemVO.setItemId(item.getItemId());
        cartItemVO.setQuantity(1);
        cartItemVO.setUnitPrice(item.getListPrice());
        cartItemVO.setInStock(item.getQuantity() > 0);
        cartItemVO.setAttribute1(item.getAttribute1());
        cartItemVO.setAttribute2(item.getAttribute2());
        cartItemVO.setInventQty(item.getQuantity());
        cartItemVO.calculateTotal();

        return cartItemVO;
    }

    private void plusQty(CartVO cart, String itemId, int plusQty, boolean isPersis){
        Map<String, CartItemVO> itemMap = cart.getItemMap();
        CartItemVO cartItem = itemMap.get(itemId);
        int afterPlusQty = cartItem.getQuantity() + plusQty;
        if (isPersis) {
            // 需要持久化，从临时购物车中更新之前需要更新数据库
            CartItem persisItem = VOToCartItem(cartItem);
            persisItem.setQuantity(afterPlusQty);
            // 持久化：更新数据库记录
            cartItemMapper.updateById(persisItem);
        }
        // 不需要持久化，只需从临时购物车中更新
        cartItem.setQuantity(afterPlusQty);
    }
}
