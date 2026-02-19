package org.example.service;

import org.example.dao.*;

public class MarketplaceService {
    ProductDAO productDAO=new ProductDAO();
    CartDAO cartDAO=new CartDAO();

    public void buy(int cartId,int productId,int qty){
        cartDAO.addToCart(cartId,productId,qty);
    }
}
