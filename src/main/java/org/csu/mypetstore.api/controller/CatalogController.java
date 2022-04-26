package org.csu.mypetstore.api.controller;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.Category;
import org.csu.mypetstore.api.entity.Product;
import org.csu.mypetstore.api.service.CatalogService;
import org.csu.mypetstore.api.vo.ItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/catalog")
@SessionAttributes("cart")
public class CatalogController {

    @Autowired
    private CatalogService catalogService;


    @GetMapping("/categories")
    @ResponseBody
    public CommonResponse<List<Category>> getCategoryList() {
        System.out.println(catalogService.getCategories()+"/categories: ");
        return catalogService.getCategories();
    }


    @GetMapping("/categories/{id}")
    @ResponseBody
    public CommonResponse<Category> getCategoryById(@PathVariable("id") String categoryId) {
        System.out.println(catalogService.getCategoryById(categoryId)+"/categories/{id}");
        return catalogService.getCategoryById(categoryId);
    }


    @GetMapping("/categories/{id}/products")
    @ResponseBody
    public CommonResponse<List<Product>> getProductListByCategoryId(@PathVariable("id") String categoryId) {
        System.out.println(catalogService.getProductsByCategoryId(categoryId)+"/categories/{id}/products");
        return catalogService.getProductsByCategoryId(categoryId);
    }

    // 获取单个Product小分类的信息
    @GetMapping("/products/{id}")
    @ResponseBody
    public CommonResponse<Product> getProductById(@PathVariable("id") String productId){
        System.out.println(catalogService.getProductById(productId)+"/products/{id}");
        return catalogService.getProductById(productId);
    }

    // 获取某个Product分类下的所有Item信息
    @GetMapping("/products/{id}/items")
    @ResponseBody
    public CommonResponse<List<ItemVO> > getItemListByProductId(@PathVariable("id") String productId){
        System.out.println(catalogService.getItemsByProductId(productId)+"/products/{id}/items");
        return catalogService.getItemsByProductId(productId);
    }

    // 获取某个分类下的所有Item
    @GetMapping("/categories/{id}/items")
    @ResponseBody
    public CommonResponse<List<ItemVO> > getItemListByCategoryId(@PathVariable("id") String categoryId){
        System.out.println( catalogService.getItemsByCategoryId(categoryId)+"/categories/{id}/items");
        return catalogService.getItemsByCategoryId(categoryId);
    }

    // 获取单个Item
    @GetMapping("/categories/items/{id}")
    @ResponseBody
    public CommonResponse<ItemVO> getItemById(@PathVariable("id") String itemId){
        System.out.println(catalogService.getItemByItemId(itemId)+"/categories/items/{id}");
        return catalogService.getItemByItemId(itemId);
    }

    // 获取所有Items
    @GetMapping("/categories/items")
    @ResponseBody
    public CommonResponse<List<ItemVO> > getItemById(){
        System.out.println(catalogService.getItems()+"/categories/items");
        return catalogService.getItems();
    }
}
