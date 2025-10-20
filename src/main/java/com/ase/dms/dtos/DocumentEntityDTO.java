package com.ase.dms.dtos;

import com.ase.dms.entities.DocumentEntity;
import com.ase.dms.services.MinIOService;

import lombok.RequiredArgsConstructor;
import lombok.Getter;

@RequiredArgsConstructor
@Getter
public class DocumentEntityDTO {

  private final DocumentEntity documentEntity;

  private final MinIOService minioService;

  public byte[] getData() {
    if (documentEntity.getId() == null) {
      throw new IllegalStateException("Document ID must be set before fetching data");
    }
    return minioService.getObjectData(documentEntity.getId());
  }

  public void setData(byte[] data) {
    if (documentEntity.getId() == null) {
      throw new IllegalStateException("Document ID must be set before storing data");
    }
    minioService.setObject(documentEntity.getId(), data);
    documentEntity.setSize(data.length);
  }
}
