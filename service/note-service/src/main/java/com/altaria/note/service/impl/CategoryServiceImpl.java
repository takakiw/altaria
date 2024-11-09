package com.altaria.note.service.impl;

import cn.hutool.core.util.IdUtil;
import com.altaria.common.constants.NoteConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Category;
import com.altaria.note.mapper.CategoryMapper;
import com.altaria.note.mapper.NoteMapper;
import com.altaria.note.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result addCategory(String categoryName, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        if (categoryName == null || !categoryName.matches(NoteConstants.CATEGORY_NAME_REGEX)){
            return Result.error(StatusCodeEnum.CATEGORY_NAME_INVALID);
        }
        Category categoryByNames = categoryMapper.getCategoryByNames(categoryName, uid);
        if (categoryByNames!= null){
            return Result.error(StatusCodeEnum.CATEGORY_ALREADY_EXISTS);
        }
        Category dbCategory = new Category();
        dbCategory.setUid(uid);
        dbCategory.setName(categoryName);
        dbCategory.setId(IdUtil.getSnowflake().nextId());
        int i = categoryMapper.insertCategory(dbCategory);
        if (i > 0){
            return Result.success();
        }
        return Result.error(StatusCodeEnum.CATEGORY_ADD_FAILED);
    }

    @Override
    public Result updateCategory(String newCategoryName, Long id, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        if (newCategoryName == null || !newCategoryName.matches(NoteConstants.CATEGORY_NAME_REGEX)){
            return Result.error(StatusCodeEnum.CATEGORY_NAME_INVALID);
        }
        Category dbCategory = new Category();
        dbCategory.setId(id);
        dbCategory.setName(newCategoryName);
        dbCategory.setUid(uid);
        int i = categoryMapper.updateCategory(dbCategory);
        if (i > 0){
            return Result.success();
        }
        return Result.error(StatusCodeEnum.CATEGORY_UPDATE_FAILED);
    }

    @Override
    @Transactional
    public Result<PageResult<Category>> listCategories(Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        List<Category> noteCategories = categoryMapper.listCategories(uid);
        return Result.success(new PageResult<>(noteCategories.size(), noteCategories));
    }

    @Override
    public Result deleteCategory(String category, Long uid) {
        if (uid == null){
            return Result.error(StatusCodeEnum.TOKEN_INVALID);
        }
        Category categoryByNames = categoryMapper.getCategoryByNames(category, uid);
        if (categoryByNames == null){
            return Result.error(StatusCodeEnum.CATEGORY_NOT_FOUND);
        }
        noteMapper.deleteNoteByCategory(categoryByNames.getId(), uid);
        int i = categoryMapper.deleteCategory(categoryByNames.getId());
        if (i > 0){
            return Result.success();
        }
        return Result.error(StatusCodeEnum.CATEGORY_DELETE_FAILED);
    }
}
