package org.apache.lucene.store;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class GAEIndexCategory {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private String cat;

  @Persistent
  private Long ver;

  @Persistent
  private Long lastModified;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public Long getLastModified() {
    return lastModified;
  }

  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  public boolean lessThan(GAEIndexCategory obj) {
    if (this.ver.longValue() < obj.ver.longValue()) {
      return true;
    }
    if (this.lastModified.longValue() < obj.lastModified.longValue()) {
      return true;
    }
    return false;
  }

}