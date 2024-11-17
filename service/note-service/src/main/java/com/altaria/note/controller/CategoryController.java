package com.altaria.note.controller;

import com.altaria.common.constants.UserConstants;
import com.altaria.common.enums.StatusCodeEnum;
import com.altaria.common.pojos.common.PageResult;
import com.altaria.common.pojos.common.Result;
import com.altaria.common.pojos.note.entity.Category;
import com.altaria.note.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/note/category")
@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @GetMapping("/list")
    public Result<PageResult<Category>> listCategories(@RequestHeader(UserConstants.USER_ID) Long uid) {
        return categoryService.listCategories(uid);
    }


    @PostMapping("/add")
    public Result<Category> addCategory(@RequestBody Category category,
                              @RequestHeader(UserConstants.USER_ID) Long uid) {
        return categoryService.addCategory(category.getName(), uid);
    }

    @PutMapping("/update")
    public Result updateCategory(@RequestBody Category category,
                                @RequestHeader(UserConstants.USER_ID) Long uid) {
        return categoryService.updateCategory(category.getName(), category.getId(), uid);
    }

    @DeleteMapping("/delete/{category}")
    public Result deleteCategory(@PathVariable("category") Long category,
                                @RequestHeader(UserConstants.USER_ID) Long uid) {
        return categoryService.deleteCategory(category, uid);
    }
}
