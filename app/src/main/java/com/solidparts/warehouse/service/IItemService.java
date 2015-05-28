package com.solidparts.warehouse.service;

import com.solidparts.warehouse.dto.ItemDTO;

import java.util.List;

/**
 * Created by geidnert on 28/05/15.
 */
public interface IItemService {
    public List<ItemDTO> getItem(String searchString) throws Exception;
}
