package org.csu.mypetstore.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.*;
import org.csu.mypetstore.api.persistence.*;
import org.csu.mypetstore.api.service.CatalogService;
import org.csu.mypetstore.api.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("catalogService")
@Slf4j
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private InventoryMapper inventoryMapper;
    @Autowired
    private LineItemMapper lineItemMapper;

    // 畅销商品选取的个数
    private static final int MOST_BUY_SIZE = 5;

    @Override
    public CommonResponse<Category> getCategoryById(String categoryId) {
        try {
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                return CommonResponse.createForSuccessMessage("没有该ID的category信息");
            }
            return CommonResponse.createForSuccess(category);
        } catch (Exception e) {
            log.error("categories查询出错", e);
            return CommonResponse.createForError("categories查询出错");
        }
    }

    @Override
    public CommonResponse<List<Category>> getCategories() {
        try {
            List<Category> categories = categoryMapper.selectList(null);
            if (categories.isEmpty()) {
                return CommonResponse.createForSuccessMessage("没有category分类信息");
            }
            return CommonResponse.createForSuccess(categories);
        } catch (Exception e) {
            log.error("categories查询出错", e);
            return CommonResponse.createForError("categories查询出错");
        }
    }

    @Override
    public CommonResponse<Product> getProductById(String productId) {
        try {
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return CommonResponse.createForSuccessMessage("该ID的product不存在");
            }
            return CommonResponse.createForSuccess(product);
        } catch (Exception e) {
            log.error("products查询出错", e);
            return CommonResponse.createForError("products查询出错");
        }
    }

    @Override
    public CommonResponse<List<Product>> getProducts() {
        try {
            List<Product> products = productMapper.selectList(null);
            if (products.isEmpty()) {
                return CommonResponse.createForSuccessMessage("没有product产品信息");
            }
            return CommonResponse.createForSuccess(products);
        } catch (Exception e) {
            log.error("products查询出错", e);
            return CommonResponse.createForError("products查询出错");
        }
    }

    @Override
    public CommonResponse<List<Product>> getProductsByCategoryId(String categoryId) {
        try {
            // 构造条件查询器
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            // 设置查询条件
            queryWrapper.eq("category", categoryId);
            List<Product> products = productMapper.selectList(queryWrapper);
            if (products.isEmpty()) {
                return CommonResponse.createForSuccessMessage("该category下没有相关product信息");
            }
            return CommonResponse.createForSuccess(products);
        } catch (Exception e) {
            log.error("products查询出错", e);
            return CommonResponse.createForError("products查询出错");
        }
    }

    @Override
    public CommonResponse<List<Product>> getProductsByKey(String keyword) {
        try {
            // 构造条件查询器
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            // 设置查询条件，模糊查询对象为name字段，关键字小写处理(数据库的name字段类型为utf8_genera_ci，大小写不敏感)
            queryWrapper.like("name", keyword.toLowerCase());
            List<Product> products = productMapper.selectList(queryWrapper);
            if (products.isEmpty()) {
                return CommonResponse.createForSuccessMessage("该关键字下无查询结果");
            }
            return CommonResponse.createForSuccess(products);
        } catch (Exception e) {
            log.error("products查询出错", e);
            return CommonResponse.createForError("products查询出错");
        }
    }

    @Override
    public CommonResponse<ItemVO> getItemByItemId(String itemId) {
        try {
            Item item = itemMapper.selectById(itemId);
            if (item == null) {
                return CommonResponse.createForSuccessMessage("该item不存在");
            }

            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                return CommonResponse.createForSuccessMessage("该item下的product信息不存在");
            }
            ItemVO itemVO = this.itemToItemVO(item, product);
            return CommonResponse.createForSuccess(itemVO);
        } catch (Exception e) {
            log.error("items查询出错", e);
            return CommonResponse.createForError("items查询出错");
        }
    }

    @Override
    public CommonResponse<List<ItemVO>> getItemsByProductId(String productId) {

        // 构造条件查询器
        QueryWrapper<Item> queryWrapper = new QueryWrapper<>();
        // 设置查询条件
        queryWrapper.eq("productid", productId);

        try {
            List<Item> items = itemMapper.selectList(queryWrapper);
            if (items.isEmpty()) {
                return CommonResponse.createForSuccessMessage("该product下没有item商品");
            }

            Product product = productMapper.selectById(productId);
            if (product == null) {
                return CommonResponse.createForSuccessMessage("该product不存在");
            }

            List<ItemVO> itemVOList = new ArrayList<>();
            for (Item item : items) {
                ItemVO itemVO = itemToItemVO(item, product);
                itemVOList.add(itemVO);
            }
            return CommonResponse.createForSuccess(itemVOList);
        } catch (Exception e) {
            log.error("items查询出错", e);
            return CommonResponse.createForError("items查询出错");
        }
    }

    @Override
    public CommonResponse<List<ItemVO>> getItems() {
        try {
            // 未作分页处理
            // 先查product表，一次性查出所有product减少数据库查询次数
            List<Product> products = productMapper.selectList(null);
            if (products.isEmpty()) {
                return CommonResponse.createForSuccessMessage("item商品查询无结果(product)");
            }
            // 查询结果放入HashMap方便之后的查找
            Map<String, Product> proMap = new HashMap<>();
            for (Product product : products) {
                proMap.put(product.getProductId(), product);
            }
            // 再查item表
            List<Item> items = itemMapper.selectList(null);
            if (items.isEmpty()) {
                return CommonResponse.createForSuccessMessage("item商品查询无结果(item)");
            }

            List<ItemVO> itemVOList = new ArrayList<>();
            for (Item item : items) {
                Product product = proMap.get(item.getProductId());
                ItemVO itemVO = itemToItemVO(item, product);
                itemVOList.add(itemVO);
            }
            return CommonResponse.createForSuccess(itemVOList);
        } catch (Exception e) {
            log.error("items查询出错", e);
            return CommonResponse.createForError("items查询出错");
        }
    }

    @Override
    public CommonResponse<List<ItemVO>> getItemsByCategoryId(String categoryId) {
        try {
            List<Product> products = this.getProductsByCategoryId(categoryId).getData();
            if (products == null) {
                return CommonResponse.createForSuccessMessage("该category下没有相关product信息");
            }
            List<ItemVO> res = new ArrayList<>();
            for (Product product : products) {
                List<ItemVO> itemVOList = this.getItemsByProductId(product.getProductId()).getData();
                if (itemVOList != null) {
                    res.addAll(itemVOList);
                }
            }
            return CommonResponse.createForSuccess(res);
        } catch (Exception e) {
            log.error("items查询出错", e);
            return CommonResponse.createForError("items查询出错");
        }
    }

    @Override
    public CommonResponse isItemInStock(String itemId) {
        ItemInventory itemInventory = inventoryMapper.selectById(itemId);
        if (itemInventory == null) {
            return CommonResponse.createForError("库存信息错误，查询无结果");
        }
        return CommonResponse.createForSuccess((itemInventory.getQuantity() > 0));
    }

    @Override
    public CommonResponse<List<ItemVO>> getMostBoughtItems() {
        // 不写sql的如下方法比写sql要慢，使用@Select注解写sql查询，三次平均538ms
        // 构造LineItem条件查询器
        QueryWrapper<LineItem> lineItemQW = new QueryWrapper<>();
        // 设置查询条件
        lineItemQW.select("itemid", "SUM(quantity) AS Q")
                .groupBy("itemid")
                .orderByDesc("Q")
                .last("LIMIT " + MOST_BUY_SIZE);
        // 先查lineitem表，将查出来的itemid字段放入List对象
        List<Object> itemIds = lineItemMapper.selectObjs(lineItemQW);

        // 构造Item条件查询器
        QueryWrapper<Item> itemQW = new QueryWrapper<>();
        // 使用in减少查询次数
        itemQW.in("itemid", itemIds);
        List<Item> items = itemMapper.selectList(itemQW);

        // 该方法相较遍历items时逐个去查询product的方法，平均快20ms(每个方法测试了三次，平均为596ms、576ms)
        // 将items的productId取出成为List对象
        List<String> productIds = items.stream().map(Item::getProductId).collect(Collectors.toList());
        // 构造product条件查询器
        QueryWrapper<Product> productQW = new QueryWrapper<>();
        // 使用in减少查询次数
        productQW.in("productid", productIds);
        List<Product> products = productMapper.selectList(productQW);
        Map<String, Product> proMap = new HashMap<>();
        for (Product product : products) {
            proMap.put(product.getProductId(), product);
        }

        List<ItemVO> itemVOList = new ArrayList<>();
        for (Item item : items) {
//            Product product = productMapper.selectById(item.getProductId());
            Product product = proMap.get(item.getProductId());
            ItemVO itemVO = itemToItemVO(item, product);
            itemVOList.add(itemVO);
        }
        return CommonResponse.createForSuccess(itemVOList);
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

}
