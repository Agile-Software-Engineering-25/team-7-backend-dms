package com.ase.dms.services;

import com.ase.dms.entities.TagEntity;
import java.util.List;

public interface TagService {
  TagEntity createTag(String tagName);
  TagEntity updateTag(String id, TagEntity tag);
  void deleteTag(String id);
  TagEntity getTag(String id);
  List<TagEntity> getAllTags();
}
