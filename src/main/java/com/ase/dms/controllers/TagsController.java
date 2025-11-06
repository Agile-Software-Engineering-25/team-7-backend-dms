package com.ase.dms.controllers;

import com.ase.dms.entities.TagEntity;
import com.ase.dms.services.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/tags")
public class TagsController {

  private final TagService tagService;


  @GetMapping
  public ResponseEntity<Iterable<TagEntity>> getAllTags() {
    return ResponseEntity.ok(tagService.getAllTags());
  }

  @PostMapping("/{tagName}")
  public ResponseEntity<TagEntity> createTag(@PathVariable String tagName) {
    return ResponseEntity.ok(tagService.createTag(tagName));
  }

  @DeleteMapping("/{tagUuid}")
  public ResponseEntity<Void> deleteTag(@PathVariable String tagUuid) {
    tagService.deleteTag(tagUuid);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{tagUuid}")
  public ResponseEntity<TagEntity> updateTag(@PathVariable String tagUuid, @RequestBody String tagName) {
    return ResponseEntity.ok(tagService.updateTag(tagUuid, TagEntity.builder().name(tagName).build()));
  }

}
