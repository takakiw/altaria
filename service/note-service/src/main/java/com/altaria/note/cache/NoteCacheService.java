package com.altaria.note.cache;

import com.altaria.common.pojos.note.entity.Category;
import com.altaria.common.pojos.note.entity.Comment;
import com.altaria.common.pojos.note.entity.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class NoteCacheService {

    private static final String CATEGORY_CHILDREN_PREFIX = "category-children:";
    private static final long CATEGORY_CHILDREN_EXPIRE_TIME = 60 * 60; // 1 hour

    private static final String NOTE_PREFIX = "note:";
    private static final long NOTE_EXPIRE_TIME = 60 * 60; // 1 hour
    private static final String CATEGORY_PREFIX = "category:";
    private static final long CATEGORY_EXPIRE_TIME = 60 * 60 - 5 * 60;
    private static final String CATEGORY_PARENT_PREFIX = "category-parent:";
    private static final long CATEGORY_PARENT_EXPIRE_TIME = 60 * 60 - 5 * 60;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Async
    public void saveNote(Note note) {
        redisTemplate.opsForValue().set(NOTE_PREFIX + note.getId(), note, NOTE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Async
    public void deleteNote(Long id) {
        redisTemplate.delete(NOTE_PREFIX + id);
    }

    public Note getNote(Long id) {
        return (Note) redisTemplate.opsForValue().get(NOTE_PREFIX + id);
    }

    @Async
    public void saveNullNote(Long id) {
        redisTemplate.opsForValue().set(NOTE_PREFIX + id, new Note(), NOTE_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Async
    public void addCategoryChildren(Note note) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(CATEGORY_CHILDREN_PREFIX + note.getUid() + ":" + note.getCid()))){
            redisTemplate.opsForZSet().add(CATEGORY_CHILDREN_PREFIX + note.getUid() + ":" + note.getCid(), note.getId(), note.getUpdateTime().toEpochSecond(ZoneOffset.UTC));
        }
        saveNote(note);
    }

    @Async
    public void removeCategoryChildren(Note note) {
        deleteNote(note.getId());
        redisTemplate.opsForZSet().remove(CATEGORY_CHILDREN_PREFIX + note.getUid() + ":" + note.getCid(), note.getId());
    }

    @Async
    public void saveCategoryAllChildren(Long uid, Long cid, List<Note> notes) {
        redisTemplate.delete(CATEGORY_CHILDREN_PREFIX + uid + ":" + cid);
        notes.forEach(note -> {
            redisTemplate.opsForZSet().add(CATEGORY_CHILDREN_PREFIX + uid + ":" + cid, note.getId(), note.getUpdateTime().toEpochSecond(ZoneOffset.UTC));
            saveNote(note);
        });
        redisTemplate.expire(CATEGORY_CHILDREN_PREFIX + uid + ":" + cid, CATEGORY_CHILDREN_EXPIRE_TIME, TimeUnit.SECONDS);
    }
    public List<Note> getCategoryAllChildren(Long uid, Long cid) {
        Set<Object> range = redisTemplate.opsForZSet().range(CATEGORY_CHILDREN_PREFIX + uid + ":" + cid, 0, -1);
        if (range == null || range.isEmpty()) {
            return new ArrayList<>();
        }
        return range.stream().map(nid -> Long.parseLong(nid.toString())).map(id -> getNote(id)).toList();
    }

    public Category getCategory(Long uid, Long cid) {
        return (Category) redisTemplate.opsForValue().get(CATEGORY_PREFIX + uid + ":" + cid);
    }

    public void saveCategory(Category category) {
        redisTemplate.opsForValue().set(CATEGORY_PREFIX + category.getUid() + ":" + category.getId(), category, CATEGORY_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void delCategory(Long uid, Long cid) {
        redisTemplate.delete(CATEGORY_PREFIX + uid + ":" + cid);
    }


    public boolean isHasKeyCategoryChildren(Long uid, Long cid) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(CATEGORY_CHILDREN_PREFIX + uid + ":" + cid));
    }

    public Boolean isHasKeyCategoryParent(Long uid){
        return Boolean.TRUE.equals(redisTemplate.hasKey(CATEGORY_PARENT_PREFIX + uid));
    }


    public void categoryParentAddChild(Category dbCategory) {
        if(Boolean.TRUE.equals(redisTemplate.hasKey(CATEGORY_PARENT_PREFIX + dbCategory.getUid()))){
            redisTemplate.opsForZSet().add(CATEGORY_PARENT_PREFIX + dbCategory.getUid(), dbCategory.getId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        }
        saveCategory(dbCategory);
    }

    public void delParentCategoryChildren(Long cid, Long uid) {
        delCategory(uid, cid);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(CATEGORY_PARENT_PREFIX + uid))){
            redisTemplate.opsForZSet().remove(CATEGORY_PARENT_PREFIX + uid, cid);
        }
    }

    public List<Category> getCurrentParentAllCategory(Long uid) {
        Set<Object> range = redisTemplate.opsForZSet().range(CATEGORY_PARENT_PREFIX + uid, 0, -1);
        if (range == null || range.isEmpty()) {
            return new ArrayList<>();
        }
        List<Category> categories = range.stream().map(cid -> getCategory(uid, (Long) cid)).toList();
        return categories;
    }

    public void saveCurrentParentAllCategory(Long uid, List<Category> noteCategories) {
        if (noteCategories == null || noteCategories.isEmpty()){
            return;
        }
        redisTemplate.delete(CATEGORY_PARENT_PREFIX + uid);
        noteCategories.forEach(category -> {
            redisTemplate.opsForZSet().add(CATEGORY_PARENT_PREFIX + uid, category.getId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            saveCategory(category);
        });
        redisTemplate.expire(CATEGORY_PARENT_PREFIX + uid, CATEGORY_PARENT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public void incrNoteCommentCount(Long nid, int count) {
        Note note = getNote(nid);
        if (note == null) return;
        note.setCommentCount(note.getCommentCount() + count);
        saveNote(note);
    }
}
