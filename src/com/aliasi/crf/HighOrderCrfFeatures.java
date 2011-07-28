package com.aliasi.crf;

import java.util.List;
import java.util.Map;

/**
 * The {@code HighOrderCrfFeatures} interface specifies methods for
 * extracting node and edge features for a conditional random field.
 *
 * <h3>Use by High Order CRFs</h3>
 *
 * <p>Typically, these features will be created by an implementation
 * of {@link HighOrderFeatureExtractor} in the context of CRF training
 * or tagging.  In this case, the previous tags are guaranteed to fall
 * in the set of tags provided at construction time.
 *
 * <h3>Caching High Cost Features</h3>
 *
 * <p>During construction, the features implementation may cache
 * values, such as part-of-speech tags for CRF chunker features.
 *
 * <h3>Thread Safety</h3>
 *
 * After safely publishing the constructed features, the feature
 * extraction methods should be thread safe.  The read methods
 * implemented by this class are all thread safe.
 */
public abstract class HighOrderCrfFeatures<E> {

    private final List<E> mTokens;
    private final List<String> mTags;
    private final int mOrder;

    /**
     * Construct a chain CRF feature set for the specified lists of
     * input tokens and possible output tags.
     *
     * @param tokens Input tokens.
     * @param tags Possible output tags.
     */
    public HighOrderCrfFeatures(List<E> tokens, List<String> tags) {
	  this(tokens,tags,2);
    }

    public HighOrderCrfFeatures(List<E> tokens, List<String> tags, int order) {
        if (order < 1) {
            String msg = "CRF order must be one or higher.";
            throw new IllegalArgumentException(msg);
        }
        if (order < 5) {
            String msg = "CRF order must be less than five.";
            throw new IllegalArgumentException(msg);
        }
        mTokens = tokens;
        mTags = tags;
	    mOrder = order;
    }

    /**
     * Returns the number of tokens for this feature set.
     *
     * @return Number of tokens for this feature set.
     */
    public int numTokens() {
        return mTokens.size();
    }

    /**
     * Return the token at the specified input position.
     *
     * @param n Input position.
     * @return Token at specified index position.
     * @throws IndexOutOfBoundsException If the specified input
     * position is less than 0 or greater than or equal to the number
     * of tokens.
     */
    public E token(int n) {
        return mTokens.get(n);
    }

    /**
     * Returns the number of possible output tags for this feature
     * set.
     *
     * @return Number of possible output tags.
     */
    public int numTags() {
        return mTags.size();
    }

    public int order() {
        return mOrder;
    }

    /**
     * Return the output tag with the specified index.
     *
     * @return Output tag for index.
     * @throws IndexOutOfBoundsException If the specified index is less than 0
     * or greater than or equal to the number of tokens.
     */
    public String tag(int k) {
        return mTags.get(k);
    }

    /**
     * Return the node features for the specified input position.
     *
     * @param n Position in input token sequence.
     * @return Features for the node at the specified position.
     * @throws IndexOutOfBoundsException If the specifieid token position
     * is out of bounds.
     */
    public abstract Map<String,? extends Number> nodeFeatures(int n);

    /**
     * Return the edge features for the specified input position
     * and index of the previous tag.
     *
     * @param n Position in input token sequence.
     * @param previousTagIndex Index of previous tag in list of tags.
     * @throws IndexOutOfBoundsException If the specifieid token position or
     * tag index are out of bounds.
     */
    public abstract Map<String,? extends Number> edgeFeatures(int n, int[] previousTagIndexes);

}
