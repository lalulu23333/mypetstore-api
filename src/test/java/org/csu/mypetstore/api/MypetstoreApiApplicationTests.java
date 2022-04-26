package org.csu.mypetstore.api;


import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.*;
import org.csu.mypetstore.api.persistence.*;

import org.csu.mypetstore.api.service.AccountService;
import org.csu.mypetstore.api.service.CartService;
import org.csu.mypetstore.api.service.CatalogService;
import org.csu.mypetstore.api.service.OrderService;
import org.csu.mypetstore.api.vo.AccountVO;
import org.csu.mypetstore.api.vo.ItemVO;
import org.csu.mypetstore.api.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class MypetstoreApplicationTests {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private SignOnMapper signOnMapper;
    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private LineItemMapper lineItemMapper;
    @Autowired
    private BannerDataMapper bannerDataMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private LogMapper logMapper;
    @Autowired
    private SequenceMapper sequenceMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private AccountService accountService;
    @Autowired
    private CatalogService catalogService;
    @Autowired
    private OrderService orderService;
   @Autowired
    private CartService cartService;

    @Test
    void contextLoads() {
    }

    @Test
    void apitest() throws NoSuchFieldException {
//        CommonResponse cs = CommonResponse.createForSuccessMessage("hello");
//        System.out.println(cs.getMsg());

//        List<Category> categoryList = categoryMapper.selectList(null);
//        System.out.println(categoryList);

//        Category category = categoryMapper.selectById("BIRDS");
//        System.out.println(category);

//        List<Profile> profiles = profileMapper.selectList(null);
//        System.out.println(profiles);

//        CartItem cartItem = new CartItem();
//        cartItem.setUsername("1");
//        cartItem.setItemId("EST-2");
//        cartItem.setQuantity(2);
//        cartItem.setUnitPrice(new BigDecimal("16.50"));
//        cartItem.setInStock(true);
//        cartItemMapper.insert(cartItem);
//        System.out.println(cartItem.getCartItemId());
//        cartItemMapper.deleteById(2);

 /*       Map<String, String> test = Collections.synchronizedMap(new HashMap<>());;
        test.put("1", "a");
        System.out.println(test.get("2"));*/
    }

    @Test
    void funcTest() {
/*        CommonResponse<List<Category>> test = catalogService.getCategories();
        CommonResponse a = accountService.insertAccount(test.getData());
        System.out.println(a.getData());
       CommonResponse test = cartService;
        System.out.println(test.getData());*/
    }

    @Test
    void InterceptorTest() {
//        accountService.updatePassword("123", "456");
    }
    @Test
    void test() {
        Category category = categoryMapper.selectById("CATS");
        System.out.println(category);
    }
}

