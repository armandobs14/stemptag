package org.apache.lucene.index;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.store.GAEDirectory;

public class GAEIndexReader extends IndexReader {

  private static Logger log = Logger.getLogger(GAEIndexReader.class.getName());

  private static final String MESSAGE_DESTROIED = "Attempted to use GAEIndexReader after destroy() was called.";

  private long createdTime;

  private boolean isClosed;

  private boolean canClose;

  private int refCurCount;

  private int refTotalCount;

  private long lastUsed;

  private IndexReader in;

  public GAEIndexReader(IndexReader in) {
    this.in = in;
    this.createdTime = System.currentTimeMillis();
    this.isClosed = false;
    this.refCurCount = 0;
    this.refTotalCount = 0;
    this.lastUsed = 0;
  }

  public static GAEIndexReader getReader(GAEDirectory directory) throws IOException {
    IndexReader in = IndexReader.open(directory);
    return new GAEIndexReader(in);
  }

  private void assertOpen() throws IOException {
    if (isClosed) {
      throw new IOException(MESSAGE_DESTROIED);
    }
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public int getRefCurCount() {
    return this.refCurCount;
  }

  public int getRefTotalCount() {
    return this.refTotalCount;
  }

  public long getLastUsed() {
    return this.lastUsed;
  }

  public synchronized void borrow() throws IOException {
    assertOpen();
    this.refCurCount++;
    this.refTotalCount++;
    this.lastUsed = System.currentTimeMillis();
  }

  public synchronized void returnBack() throws IOException {
    this.refCurCount--;
    if (refCurCount <= 0 && canClose) {
      log.info("GAEIndexReader.close(): no thread hold the reader, trying to close it!");
      isClosed = true;
      this.in.close();
    }
  }

  protected synchronized void destory() throws IOException {
    if (refCurCount <= 0) {
      log.info("GAEIndexReader.destory(): no thread hold the reader, trying to close it!");
      isClosed = true;
      this.in.close();
    } else {
      log.info("GAEIndexReader.destory(): trying to close reader, but some thread hold the handle yet!");
      canClose = true;
    }
  }

  @Override
  protected void doClose() throws IOException {
    this.in.doClose();
  }

  @Override
  protected void doCommit() throws IOException {
    this.in.doCommit();
  }

  @Override
  protected void doDelete(int docNum) throws CorruptIndexException, IOException {
    this.in.doDelete(docNum);
  }

  @Override
  protected void doSetNorm(int doc, String field, byte value) throws CorruptIndexException, IOException {
    this.in.doSetNorm(doc, field, value);
  }

  @Override
  protected void doUndeleteAll() throws CorruptIndexException, IOException {
    this.in.doUndeleteAll();
  }

  @Override
  public int docFreq(Term t) throws IOException {
    return this.in.docFreq(t);
  }

  @Override
  public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    return this.in.document(n, fieldSelector);
  }

  @Override
  public Collection getFieldNames(FieldOption fldOption) {
    return this.in.getFieldNames(fldOption);
  }

  @Override
  public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException {
    return this.in.getTermFreqVector(docNumber, field);
  }

  @Override
  public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
    this.in.getTermFreqVector(docNumber, mapper);
  }

  @Override
  public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
    this.in.getTermFreqVector(docNumber, field, mapper);
  }

  @Override
  public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
    return this.in.getTermFreqVectors(docNumber);
  }

  @Override
  public boolean hasDeletions() {
    return this.in.hasDeletions();
  }

  @Override
  public boolean isDeleted(int n) {
    return this.in.isDeleted(n);
  }

  @Override
  public int maxDoc() {
    return this.in.maxDoc();
  }

  @Override
  public byte[] norms(String field) throws IOException {
    return this.in.norms(field);
  }

  @Override
  public void norms(String field, byte[] bytes, int offset) throws IOException {
    this.in.norms(field, bytes, offset);
  }

  @Override
  public int numDocs() {
    return this.in.numDocs();
  }

  @Override
  public TermDocs termDocs() throws IOException {
    return this.in.termDocs();
  }

  @Override
  public TermPositions termPositions() throws IOException {
    return this.in.termPositions();
  }

  @Override
  public TermEnum terms() throws IOException {
    return this.in.terms();
  }

  @Override
  public TermEnum terms(Term t) throws IOException {
    return this.in.terms(t);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GAEIndexReader) {
      GAEIndexReader another = (GAEIndexReader) obj;
      if (another.in.equals(this.in)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.in.hashCode();
  }

}