package org.apache.lucene.store;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Blob;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GAEFileContent {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long cid;

  @Persistent
  private Long fileId;

  @Persistent
  private int segmentNo;

  @Persistent
  private Long segmentLength;

  @Persistent
  private Blob content;

  public Long getId() {
    return cid;
  }

  public void setId(Long id) {
    this.cid = id;
  }

  public Long getFileId() {
    return fileId;
  }

  public void setFileId(Long fileId) {
    this.fileId = fileId;
  }

  public int getSegmentNo() {
    return segmentNo;
  }

  public void setSegmentNo(int segmentNo) {
    this.segmentNo = segmentNo;
  }

  public Long getSegmentLength() {
    return segmentLength;
  }

  public void setSegmentLength(Long segmentLength) {
    this.segmentLength = segmentLength;
  }

  public Blob getContent() {
    return content;
  }

  public void setContent(Blob content) {
    this.content = content;
  }

}