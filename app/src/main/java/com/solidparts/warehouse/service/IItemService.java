package com.solidparts.warehouse.service;

import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public interface IItemService {
    public ItemDTO getItem(String searchString) throws Exception;

    public void addItem(ItemDTO itemDTO) throws Exception;

    public boolean removeItem(String searchString) throws Exception;
}
