package com.ase.dms.services;

import com.ase.dms.entities.TagEntity;
import com.ase.dms.exceptions.DmsException;
import com.ase.dms.exceptions.TagNotFoundException;
import com.ase.dms.repositories.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
  private final TagRepository tagRepository;

  @Override
  public TagEntity createTag(String tagName) {
    if (tagRepository.existsByName(tagName)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag already exists");
    }
    return tagRepository.save(TagEntity.builder().name(tagName).build());
  }

  @Override
  public TagEntity updateTag(String id, TagEntity tag) {
    if (!tagRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
    }
    if (tagRepository.existsByName(tag.getName())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag already exists");
    }
    tag.setUuid(id);
    return tagRepository.save(tag);
  }

  @Override
  public void deleteTag(String id) {
    tagRepository.deleteById(id);
  }

  @Override
  public TagEntity getTag(String id) {
    return tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));
  }

  @Override
  public List<TagEntity> getAllTags() {
    return tagRepository.findAll();
  }
}
