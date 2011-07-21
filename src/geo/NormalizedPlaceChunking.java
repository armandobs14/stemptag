package geo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingImpl;
import com.aliasi.util.Iterators;
import com.aliasi.util.Strings;

public class NormalizedPlaceChunking implements Chunking, Iterable<Chunk> {

	private final String mString;
    private final Set<NormalizedPLACEChunk> mChunkSet = new LinkedHashSet<NormalizedPLACEChunk>();

    /**
     * Constructs a chunking implementation to hold chunks over the
     * specified character sequence.  The sequence is stored immutably
     * in this implementation, so later changes to the sequence
     * provided to this constructor will not affect the constructed
     * chunking implementation.  All chunks added must be within this
     * character sequence's bounds.
     *
     * @param cSeq Character sequence underlying the chunking.
     */
    public NormalizedPlaceChunking(CharSequence cSeq) {
        mString = cSeq.toString(); // no copy if already string
    }

    /**
     * Construct a chunking implementation to hold chunks over the
     * specified character slice.  The slice is copied, so later
     * changes to it do not affect the constructed chunking.  All
     * chunks added to this chunking must be within this character
     * slice's (relative) bounds.  The chunks themselves will have
     * indices relative to the start parameter of this constructor,
     * rather than absolute offsets into this character slice.
     *
     * @param cs Character array.
     * @param start Index in array of first element in chunk.
     * @param end Index in array of one past the last element in chunk.
     */
    public NormalizedPlaceChunking(char[] cs, int start, int end) {
        this(new String(cs,start,end-start));
    }

    /**
     * Adds all of the chunks in the specified collection to this
     * chunking.  If any of the chunks do not implement the
     * <code>Chunk</code> interface, an illegal argument exception is
     * thrown.
     *
     * @param chunks Chunks to add to this chunking.
     * @throws IllegalArgumentException If the collection contains an
     * object that does not implement <code>Chunk</code>.
     */
    public void addAll(Collection<NormalizedPLACEChunk> chunks) {
        for (NormalizedPLACEChunk next : chunks)
            add(next);
    }

    /**
     * Returns an unmodifiable iterator over the chunk set underlying
     * this chunking implementation.  The chunks will be iterated in
     * the order in which they were added to this implementation.
     *
     * @return Unmodifiable iterator over the set of chunks.
     */
    public Iterator<Chunk> iterator() {
        return Iterators.unmodifiable(chunkSet().iterator());
    }

    /**
     * Add a chunk this this chunking.  The chunk must have start
     * and end points within the bounds provided by the character
     * sequence underlying this chunking.
     *
     * @param chunk Chunk to add to this chunking.
     * @throws IllegalArgumentException If the end point is beyond the
     * underlying character sequence.
     */
    public void add(NormalizedPLACEChunk chunk) {
        if (chunk.end() > mString.length()) {
            String msg = "End point of chunk beyond end of char sequence."
                + "Char sequence length=" + mString.length()
                + " chunk.end()=" + chunk.end();
            throw new IllegalArgumentException(msg);
        }
        mChunkSet.add(chunk);
    }

    /**
     * Returns the character sequence underlying this chunking.
     *
     * @return The character sequence underlying this chunking.
     */
    public CharSequence charSequence() {
        return mString;
    }

    /**
     * Return an unmodifiable view of the set of chunks for this
     * chunking.  The chunk set will iterate elements in the order in
     * which they were added to the chunking.
     *
     * @return The set of chunks for this chunking.
     */
    public Set<Chunk> chunkSet() {
        return Collections.<Chunk>unmodifiableSet(mChunkSet);
    }



    @Override
    public boolean equals(Object that) {
        return (that instanceof NormalizedPlaceChunking)
            ? equal(this,(NormalizedPlaceChunking)that)
            : false;
    }

    @Override
    public int hashCode() {
        return hashCode(this);
    }

    /**
     * Returns a string-based representation of this chunking.  This
     * representation includes the character sequence and each
     * chunk in the chunk set.
     *
     * @return String-based representation of this chunking.
     */
    @Override
    public String toString() {
        return charSequence()
            + " : " + chunkSet();
    }

    static final NormalizedPLACEChunk[] EMPTY_CHUNK_ARRAY = new NormalizedPLACEChunk[0];

    /**
     * Returns <code>true</code> if the specified chunkings are equal.
     * Chunking equality is defined in {@link Chunking#equals(Object)}
     * to be equality of character sequence yields and equality of
     * chunk sets.
     *
     * <P><i>Warning:</i> Equality is unstable if the chunkings
     * change.
     *
     * @param chunking1 First chunking.
     * @param chunking2 Second chunking.
     * @return <code>true</code> if the chunkings are equal.
     */
    public static boolean equal(NormalizedPlaceChunking chunking1, NormalizedPlaceChunking chunking2) {
        return Strings.equalCharSequence(chunking1.charSequence(),
                                         chunking2.charSequence())
            && chunking1.chunkSet().equals(chunking2.chunkSet());
    }

    /**
     * Returns the hash code for the specified chunking.  The hash
     * code for a chunking is defined by {@link Chunking#hashCode()}.
     *
     * <P><i>Warning:</i> Hash codes are unstable if the chunkings change.
     *
     * @param chunking Chunking whose hash code is returned.
     * @return The hash code for the specified chunking.
     */
    public static int hashCode(NormalizedPlaceChunking chunking) {
        return Strings.hashCode(chunking.charSequence())
            + 31 * chunking.chunkSet().hashCode();
    }

    /**
     * Returns {@code true} if the chunks overlap at least one
     * character position.
     *
     * <p>Chunks {@code chunk1} and {@code chunk2} overlap if
     *
     * <blockquote><pre>
     * chunk1.start() <= chunk2.start() < chunk1.end()</pre></blockquote>
     *
     * or
     * <blockquote><pre>
     * chunk2.start() <= chunk1.start() < chunk2.end()</pre></blockquote>
     *
     * @param chunk1 First chunk to test.
     * @param chunk2 Second chunk to test.
     * @return {@code true} if the chunks overlap at least one character
     * position.
     */
    public static boolean overlap(NormalizedPLACEChunk chunk1, NormalizedPLACEChunk chunk2) {
        return overlapOneWay(chunk1,chunk2)
            || overlapOneWay(chunk2,chunk1);
    }

    /**
     * Return the result of combining two chunkings into a single
     * non-overlapping chunking.  Chunks in the first chunking are
     * sorted based on a {@link Chunk#TEXT_ORDER_COMPARATOR}, and then
     * visited left to right, keeping chunks that don't overlap chunks
     * appearing earlier in the order.  Next, chunks are added from the
     * second chunking in the same way, first by sorting, then by
     * adding in order, all the chunks that are consistent with existing
     * chunks.
     *
     * <p>The returned chunking has a string as a character sequence
     * rather than copying one of the input chunking's character
     * sequence.
     *
     * <p>Overall, this is an O(n log n) operation because of the
     * sorting.  It also allocates arrays for each of the input
     * chunking's chunks, and the string and the chunk set for the
     * result.
     *
     * @param chunking1 First chunking to combine.
     * @param chunking2 Second chunking to combine.
     * @return Combination of the two chunkings.
     * @throws IllegalArgumentException If the chunkings are not over the same
     * character sequence.
     */
    public static NormalizedPlaceChunking merge(NormalizedPlaceChunking chunking1, NormalizedPlaceChunking chunking2) {
        if (!Strings.equalCharSequence(chunking1.charSequence(),
                                       chunking2.charSequence())) {
            String msg = "Chunkings must be over same character sequence."
                + " Found chunking1.charSequence()=" + chunking1.charSequence()
                + " chunking2.charSequence()=" + chunking2.charSequence();
            throw new IllegalArgumentException(msg);
        }
        NormalizedPlaceChunking chunking = new NormalizedPlaceChunking(chunking1.charSequence().toString());
        NormalizedPLACEChunk[] chunks1 = sortedChunks(chunking1);
        NormalizedPLACEChunk[] chunks2 = sortedChunks(chunking2);
        int pos1 = 0;
        NormalizedPLACEChunk lastChunk = null;
        for (int pos2 = 0; pos2 < chunks2.length; ++pos2) {
            for (; isBefore(chunks1,pos1,chunks2,pos2) || overlap(chunks1,pos1,lastChunk); ++pos1) {
                if (!overlap(chunks1,pos1,lastChunk)) {
                    lastChunk = chunks1[pos1];
                    chunking.add(lastChunk);
                }
            }
            if ((pos1 >= chunks1.length || !overlap(chunks1[pos1],chunks2[pos2]))
                && !overlap(chunks2,pos2,lastChunk)) {
                lastChunk = chunks2[pos2];
                chunking.add(lastChunk);
            }
        }
        for (; pos1 < chunks1.length; ++pos1) {
            if (!overlap(chunks1,pos1,lastChunk)) {
                lastChunk = chunks1[pos1];
                chunking.add(lastChunk);
            }
        }
        return chunking;
    }

    static boolean overlap(NormalizedPLACEChunk[] chunks, int pos, NormalizedPLACEChunk lastChunk) {
        return lastChunk != null
            && pos < chunks.length
            && overlap(chunks[pos],lastChunk);
    }

    // +pos2
    static boolean isBefore(NormalizedPLACEChunk[] chunks1, int pos1, NormalizedPLACEChunk[] chunks2, int pos2) {
        return pos1 < chunks1.length
            && chunks1[pos1].end() <= chunks2[pos2].start();
    }

    static boolean overlapOneWay(NormalizedPLACEChunk chunk1, NormalizedPLACEChunk chunk2) {
        return chunk1.start() <= chunk2.start()
            && chunk2.start() < chunk1.end();
    }

    static final NormalizedPLACEChunk[] sortedChunks(NormalizedPlaceChunking chunking) {
        Set<Chunk> chunkSet = chunking.chunkSet();
        NormalizedPLACEChunk[] chunks = chunkSet.toArray(EMPTY_CHUNK_ARRAY);
        Arrays.sort(chunks,Chunk.TEXT_ORDER_COMPARATOR);
        return chunks;
    }

}
