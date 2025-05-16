package com.example.tomatomall.vo;

import com.example.tomatomall.po.Shop;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShopVO {
    private Integer shopId;
    private String name;
    private Integer ownerId;
    private String iconUrl;
    private String description;
    private Double rating;
    private Integer isValid;

    public Shop toPO() {
        Shop shop = new Shop();
        shop.setId(this.shopId);
        shop.setName(this.name);
        shop.setOwnerId(this.ownerId);
        shop.setIconUrl(this.iconUrl);
        shop.setDescription(this.description);
        shop.setRating(this.rating);
        shop.setIsValid(this.isValid);
        return shop;
    }
} 