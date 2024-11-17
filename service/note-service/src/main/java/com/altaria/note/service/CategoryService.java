package com.altaria.note.service;

import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Category;

public interface CategoryService {
    Result<Category> addCategory(String categoryName, Long uid);

    Result updateCategory(String newCategoryName, Long id, Long uid);

    Result<PageResult<Category>> listCategories(Long uid);

    Result deleteCategory(Long cid, Long uid);
}
