package temporal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collection;

/**
 * Implementation of a Bloom-filter, as described in http://en.wikipedia.org/wiki/Bloom_filter
 *
 * Inspired by the SimpleBloomFilter-class written by Ian Clarke. This
 * implementation provides a more evenly distributed Hash-function by
 * using a proper digest instead of the Java RNG. Many of the changes
 * were proposed in comments in his blog:
 * http://blog.locut.us/2008/01/12/a-decent-stand-alone-java-bloom-filter-implementation/
 *
 * @param <E> Object type that is to be inserted into the Bloom filter, e.g. String or Integer.
 */
public class BloomFilter<E> {
	
    private BitSet bitset;
    
    private int bitSetSize;
    
    private int expectedNumberOfFilterElements; // expected (maximum) number of elements to be added
    
    private int numberOfAddedElements; // number of elements actually added to the Bloom filter
    
    private int k;
    
    private static int SIZEOF_INT = 10;
        
    static final MessageDigest digestFunction;
   
    static {
        MessageDigest tmp;
        try { tmp = java.security.MessageDigest.getInstance("MD5"); } catch (NoSuchAlgorithmException e) { tmp = null; }
        digestFunction = tmp;
    }
    
    public int get_data_header_size() { return SIZEOF_INT //bitSetSize
        + SIZEOF_INT //expectedNumberOfFilterElements
        + SIZEOF_INT; //numberOfAddedElements
    }

    public int get_size() { return get_data_header_size() + (bitSetSize / 8) + (bitSetSize % 8 == 0 ? 0 : 1); }

    public void read_data_header ( byte[] buffer ) {
		DataInputStream str = new DataInputStream(new ByteArrayInputStream(buffer));
    	try {
    		bitSetSize = str.readInt();
    		expectedNumberOfFilterElements = str.readInt();
    		numberOfAddedElements = str.readInt();
    	} catch ( Exception e ) { e.printStackTrace(); }
        k = (int) Math.round((bitSetSize / expectedNumberOfFilterElements) * Math.log(2.0));
    }

    public void write_data_header ( byte[] buffer ) {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	try {
    		DataOutputStream str = new DataOutputStream(out);
    		str.write(bitSetSize);
    		str.write(expectedNumberOfFilterElements);
    		str.write(numberOfAddedElements);
    		str.close();
    	} catch ( Exception e ) { e.printStackTrace(); }
    	byte[] bytes = out.toByteArray();
        for (int i = 0; i < buffer.length; ++i)
        buffer[i] = bytes[i];
    }

    public void write_to_buffer ( byte[] buffer ) {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream str = new DataOutputStream(out);
		write_to_buffer(str);
		try { str.close(); } catch ( Exception e ) { e.printStackTrace(); }
		byte[] bytes = out.toByteArray();
        for (int i = 0; i < buffer.length; ++i) buffer[i] = bytes[i];
    }

    public void read_from_buffer ( byte[] buffer ) {
		DataInputStream str = new DataInputStream(new ByteArrayInputStream(buffer));
		read_from_buffer(str);
    }
    
    public void read_from_buffer(DataInputStream str) {
		try {
    		bitSetSize = str.readInt();
    		expectedNumberOfFilterElements = str.readInt();
    		numberOfAddedElements = str.readInt();
            bitset = new BitSet(bitSetSize);
    		byte[] temp = new byte[(bitSetSize / 8) + (bitSetSize % 8 == 0 ? 0 : 1)];
            int length = 0;
            while ((length+=str.read(temp, length, temp.length - length)) < temp.length);
            for (int i = 0; i < temp.length; i++) for (int j = 0; j < 8; j++) if (((temp[i] >> j) & 0x01) != 0x00 ) bitset.set(i * 8 + j);
    	} catch ( Exception e ) { e.printStackTrace(); }
        k = (int) Math.round((bitSetSize / expectedNumberOfFilterElements) * Math.log(2.0));
    }

    public void write_to_buffer(DataOutputStream str) {
        byte[] temp = new byte[(bitSetSize / 8) + (bitSetSize % 8 == 0 ? 0 : 1)];
        for (int i = 0; i < temp.length; i++) temp[i] = 0x00;
        for (int i = 0; i < temp.length; i++) for (int j = 0; j < 8; j++) if (bitset.get(i * 8 + j)) temp[i] |= 1 << j;
		try {
    		str.writeInt(bitSetSize);
    		str.writeInt(expectedNumberOfFilterElements);
    		str.writeInt(numberOfAddedElements);
    		str.write(temp);
    	} catch ( Exception e ) { e.printStackTrace(); }
    }
    
    public BloomFilter( ) { this(1024 * 100000, 100000); }

    /**
     * Constructs an empty Bloom filter.
     *
     * @param bitSetSize defines how many bits should be used for the filter.
     * @param expectedNumberOfFilterElements defines the maximum number of elements the filter is expected to contain.
     */
    public BloomFilter(int bitSetSize, int expectedNumberOfFilterElements) {
        this.expectedNumberOfFilterElements = expectedNumberOfFilterElements;
        this.k = (int) Math.round((bitSetSize / expectedNumberOfFilterElements) * Math.log(2.0));
        bitset = new BitSet(bitSetSize);
        this.bitSetSize = bitSetSize;
        numberOfAddedElements = 0;
    }

    /**
     * Construct a new Bloom filter based on existing Bloom filter data.
     *
     * @param bitSetSize defines how many bits should be used for the filter.
     * @param expectedNumberOfFilterElements defines the maximum number of elements the filter is expected to contain.
     * @param actualNumberOfFilterElements specifies how many elements have been inserted into the <code>filterData</code> BitSet.
     * @param filterData a BitSet representing an existing Bloom filter.
     */
    public BloomFilter(int bitSetSize, int expectedNumberOfFilterElements, int actualNumberOfFilterElements, BitSet filterData) {
        this(bitSetSize, expectedNumberOfFilterElements);
        this.bitset = filterData;
        this.numberOfAddedElements = actualNumberOfFilterElements;
    }

    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data.
     * @param charset specifies the encoding of the input data.
     * @return digest as long.
     */
    private static long createHash(String val, Charset charset) { return createHash(val.getBytes(charset)); }

    /**
     * Generates a digest based on the contents of a String.
     *
     * @param val specifies the input data. The encoding is expected to be UTF-8.
     * @return digest as long.
     */
    private static long createHash(String val) { return createHash(val, Charset.forName("UTF-8")); }

    /**
     * Generates a digest based on the contents of an array of bytes.
     *
     * @param data specifies input data.
     * @return digest as long.
     */
    private static long createHash(byte[] data) {
        long h = 0;
        byte[] res;
        synchronized (digestFunction) { res = digestFunction.digest(data); }
        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= ((int) res[i]) & 0xFF;
        }
        return h;
    }

    /**
     * Compares the contents of two instances to see if they are equal.
     *
     * @param obj is the object to compare to.
     * @return True if the contents of the objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            System.err.println("null");
            return false;
        }
        if (getClass() != obj.getClass()) {
            System.err.println("neq");
            return false;
        }
        final BloomFilter<E> other = (BloomFilter<E>) obj;        
        if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
            System.err.println("exp");
            return false;
        }
        if (this.k != other.k) {
            System.err.println("k");
            return false;
        }
        if (this.bitSetSize != other.bitSetSize) {
            System.err.println("size");
            return false;
        }
        if (this.bitset != other.bitset && (this.bitset == null || !this.bitset.equals(other.bitset))) {
            System.err.println("bitset" + this.bitset.equals(other.bitset) +
                   "\n" + other.bitset.toString() +
                   "\n" + this.bitset.toString());
            return false;
        }
        return true;
    }

    /**
     * Calculates a hash code for this class.
     * @return hash code representing the contents of an instance of this class.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.bitset != null ? this.bitset.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }


    /**
     * Calculates the expected probability of false positives based on
     * the number of expected filter elements and the size of the Bloom filter.
     * <br /><br />
     * The value returned by this method is the <i>expected</i> rate of false
     * positives, assuming the number of inserted elements equals the number of
     * expected elements. If the number of elements in the Bloom filter is less
     * than the expected value, the true probability of false positives will be lower.
     *
     * @return expected probability of false positives.
     */
    public double expectedFalsePositiveProbability() {
        return getFalsePositiveProbability(expectedNumberOfFilterElements);
    }

    /**
     * Calculate the probability of a false positive given the specified
     * number of inserted elements.
     *
     * @param numberOfElements number of inserted elements.
     * @return probability of a false positive.
     */
    public double getFalsePositiveProbability(double numberOfElements) {
        return Math.pow((1 - Math.exp(-k * (double) numberOfElements / (double) bitSetSize)), k);
    }

    /**
     * Get the current probability of a false positive. The probability is calculated from
     * the size of the Bloom filter and the current number of elements added to it.
     *
     * @return probability of false positives.
     */
    public double getFalsePositiveProbability() {
        return getFalsePositiveProbability(numberOfAddedElements);
    }

    /**
     * Returns the value chosen for K.<br />
     * <br />
     * K is the optimal number of hash functions based on the size
     * of the Bloom filter and the expected number of inserted elements.
     *
     * @return optimal k.
     */
    public int getK() { return k; }

    /**
     * Sets all bits to false in the Bloom filter.
     */
    public void clear() {
        bitset.clear();
        numberOfAddedElements = 0;
    }

    /**
     * Adds an object to the Bloom filter. The output from the object's
     * toString() method is used as input to the hash functions.
     *
     * @param element is an element to register in the Bloom filter.
     */
    public void add(E element) {
       long hash;
       String valString = element.toString();
       for (int x = 0; x < k; x++) {
           hash = createHash(valString + Integer.toString(x));
           hash = hash % (long)bitSetSize;
           bitset.set(Math.abs((int)hash), true);
       }
       numberOfAddedElements++;
    }

    /**
     * Adds all elements from a Collection to the Bloom filter.
     * @param c Collection of elements.
     */
    public void addAll(Collection<? extends E> c) { for (E element : c) add(element); }

    /**
     * Returns true if the element could have been inserted into the Bloom filter.
     * Use getFalsePositiveProbability() to calculate the probability of this
     * being correct.
     *
     * @param element element to check.
     * @return true if the element could have been inserted into the Bloom filter.
     */
    public boolean contains(E element) {
       long hash;
       String valString = element.toString();
       for (int x = 0; x < k; x++) {
           hash = createHash(valString + Integer.toString(x));
           hash = hash % (long)bitSetSize;
           if (!bitset.get(Math.abs((int)hash))) return false;
       }
       return true;
    }

    /**
     * Returns true if all the elements of a Collection could have been inserted
     * into the Bloom filter. Use getFalsePositiveProbability() to calculate the
     * probability of this being correct.
     * @param c elements to check.
     * @return true if all the elements in c could have been inserted into the Bloom filter.
     */
    public boolean containsAll(Collection<? extends E> c) {
        for (E element : c) if (!contains(element)) return false;
        return true;
    }

    /**
     * Read a single bit from the Bloom filter.
     * @param bit the bit to read.
     * @return true if the bit is set, false if it is not.
     */
    public boolean getBit(int bit) { return bitset.get(bit); }

    /**
     * Set a single bit in the Bloom filter.
     * @param bit is the bit to set.
     * @param value If true, the bit is set. If false, the bit is cleared.
     */
    public void setBit(int bit, boolean value) { bitset.set(bit, value); }

    public void flipBit(int bit) { bitset.flip(bit); }

    /**
     * Return the bit set used to store the Bloom filter.
     * @return bit set representing the Bloom filter.
     */
    public BitSet getBitSet() { return bitset; }

    /**
     * Returns the number of bits in the Bloom filter. Use count() to retrieve
     * the number of inserted elements.
     *
     * @return the size of the bitset used by the Bloom filter.
     */
    public int bitSetSize() { return this.bitSetSize; }

    /**
     * Returns the number of elements added to the Bloom filter after it
     * was constructed or after clear() was called.
     *
     * @return number of elements added to the Bloom filter.
     */
    public int count() { return this.numberOfAddedElements; }

    /**
     * Returns the expected number of elements to be inserted into the filter.
     * This value is the same value as the one passed to the constructor.
     *
     * @return expected number of elements.
     */
    public int getExpectedNumberOfElements() { return expectedNumberOfFilterElements; }
    
}