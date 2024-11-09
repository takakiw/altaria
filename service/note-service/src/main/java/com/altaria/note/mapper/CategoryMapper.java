package com.altaria.note.mapper;

import com.altaria.common.pojos.note.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT * FROM category WHERE id = #{cid}")
    Category getCategoryById(@Param("cid") Long cid, @Param("uid") Long uid);

    @Select("SELECT * FROM category WHERE name = #{category} AND uid = #{uid}")
    Category getCategoryByNames(@Param("category") String category, @Param("uid") Long uid);

    int insertCategory(Category category);

    @Update("UPDATE category SET name = #{name} WHERE id = #{id} AND uid = #{uid}")
    int updateCategory(Category dbCategory);

    @Select("SELECT * FROM category WHERE uid = #{uid}")
    List<Category> listCategories(@Param("uid") Long uid);

    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteCategory(@Param("id") Long id);
}
