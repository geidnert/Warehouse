package com.solidparts.warehouse.service;

import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

public interface IItemService {
    public List<ItemDTO> getItems(String searchString, int searchType) throws Exception;

    public void addItem(ItemDTO itemDTO) throws Exception;

    public void updateItem(ItemDTO itemDTO) throws Exception;

    public void removeItem(ItemDTO itemDTO) throws Exception;

    public int syncToOnlineDB();

    public int syncFromOnlineDB();

}
