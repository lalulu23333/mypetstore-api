package org.csu.mypetstore.api.service;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.Category;
import org.csu.mypetstore.api.entity.Product;
import org.csu.mypetstore.api.vo.ItemVO;
import org.csu.mypetstore.api.vo.LineItemVO;

import java.util.List;

public interface CatalogService {

    // 根据categoryId查询单个Category
    CommonResponse<Category> getCategoryById(String categoryId);

    // 查询所有Category列表项
    CommonResponse<List<Category> > getCategories();

    // 根据productId查询单个Product
    CommonResponse<Product> getProductById(String productId);

    // 查询所有Product列表项
    CommonResponse<List<Product> > getProducts();

    // 根据categoryId查询一系列Product列表项
    CommonResponse<List<Product> > getProductsByCategoryId(String categoryId);

    // 根据关键字keyword查询Product列表项
    CommonResponse<List<Product> > getProductsByKey(String keyword);

    // 根据productId查询一系列ItemVO列表项
    CommonResponse<List<ItemVO>> getItemsByProductId(String productId);

    // 根据itemId查询单个ItemVO
    CommonResponse<ItemVO> getItemByItemId(String itemId);

    // 查询所有ItemVO列表项
    CommonResponse<List<ItemVO>> getItems();

    // 根据categoryId查询一系列ItemVO列表项
    CommonResponse<List<ItemVO>> getItemsByCategoryId(String categoryId);

    // 判断某个item商品是否有库存
    CommonResponse isItemInStock(String itemId);

    // 获取热销item商品
    CommonResponse<List<ItemVO>> getMostBoughtItems();

}
