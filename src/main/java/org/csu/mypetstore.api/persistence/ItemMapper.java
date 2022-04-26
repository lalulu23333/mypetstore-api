package org.csu.mypetstore.api.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.csu.mypetstore.api.entity.Item;
import org.csu.mypetstore.api.vo.ItemVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemMapper extends BaseMapper<Item> {
/*
    @Select("SELECT " +
                "I.ITEMID, " +
                "LISTPRICE, " +
                "UNITCOST, " +
                "SUPPLIER AS supplierId, " +
                "I.PRODUCTID AS productId, " +
                "NAME AS productName, " +
                "DESCN AS productDescription, " +
                "CATEGORY AS categoryId, " +
                "STATUS, " +
                "ATTR1 AS attribute1, " +
                "ATTR2 AS attribute2, " +
                "ATTR3 AS attribute3, " +
                "ATTR4 AS attribute4, " +
                "ATTR5 AS attribute5, " +
                "qty AS quantity " +
            "FROM ITEM I, PRODUCT P, inventory V, ( SELECT itemid,SUM(quantity) AS Q FROM lineitem GROUP BY itemid ORDER BY Q DESC LIMIT 5 ) T " +
            "WHERE P.PRODUCTID = I.PRODUCTID AND I.ITEMID = T.itemid AND V.itemid = I.ITEMID")
    List<ItemVO> getMostBoughtItems();
    */
}
