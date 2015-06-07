package com.solidparts.warehouse.service;

import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public interface IItemService {
    public List<ItemDTO> getItems(String searchString, int searchType) throws Exception;

    public ItemDTO addItem(ItemDTO itemDTO) throws Exception;

    public ItemDTO updateItem(ItemDTO itemDTO) throws Exception;

    public void removeItem(long cacheId) throws Exception;

}
