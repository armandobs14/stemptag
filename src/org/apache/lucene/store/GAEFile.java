package org.apache.lucene.store;

import java.util.logging.Logger;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GAEFile {

  private static Logger log = Logger.getLogger(GAEFile.class.getName());

  public static final int SEGMENT_LENGTH = 512000;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String cat;

  @Persistent
  private Long ver;

  @Persistent
  private String name;

  @Persistent
  private Long length;

  @Persistent
  private Long lastModified = System.currentTimeMillis();

  @Persistent
  private Integer segmentCount = 0;

  @Persistent
  private Boolean deleted;

  @NotPersistent
  private GAEFileContent[] segments;

  public Long getId() {
    return id;
  }

  public String getCat() {
    return cat;
  }

  public void setCat(String cat) {
    this.cat = cat;
  }

  public Long getVer() {
    return ver;
  }

  public void setVer(Long ver) {
    this.ver = ver;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getLength() {
    return length;
  }

  public void setLength(Long length) {
    this.length = length;
  }

  public Long getLastModified() {
    return lastModified;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public Integer getSegmentCount() {
    return segmentCount;
  }

  public void setSegmentCount(Integer segmentCount) {
    this.segmentCount = segmentCount;
  }

  public Boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
  }

  public byte[] getSegment(int segmentNo) {
    if (this.segments == null) {
      this.segments = new GAEFileContent[this.segmentCount];
    }
    if (this.segments[segmentNo] == null) {
      this.segments[segmentNo] = GAEFileContentJDO.get(id, segmentNo);
      log.info("got segment '" + this.name + "-" + segmentNo + "' from gdb("
          + this.segments[segmentNo].getId() + "), segment-size="
          + this.segments[segmentNo].getContent().getBytes().length);
    }
    return this.segments[segmentNo].getContent().getBytes();
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("id:").append(this.id);
    result.append("\nname:").append(this.name);
    result.append("\ncategory:").append(this.cat);
    result.append("\nversion:").append(this.ver);
    result.append("\nlength:").append(this.length);
    result.append("\nlastModified:").append(this.lastModified);
    result.append("\nsegmentCount:").append(this.segmentCount);
    return result.toString();
  }

}