package com.aliasi.crf;

import com.aliasi.tag.MarginalTagger;
import com.aliasi.tag.NBestTagger;
import com.aliasi.tag.ScoredTagging;
import com.aliasi.tag.Tagger;
import com.aliasi.tag.Tagging;
import com.aliasi.tag.TagLattice;
import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.features.Features;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.matrix.DenseVector;
import com.aliasi.matrix.Matrices;
import com.aliasi.matrix.Vector;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Iterators;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * The {@code HighOrderCrf<E>} class implements high-order conditional
 * random field decoding and estimation for input token sequences of
 * type {@code E}.
 *
 * <p>The cliques of the graphical model are typically called nodes
 * and edges in a high-order CRF.  A node consists of a token position,
 * the rest of the input tokens, whereas an edge adds the previous n
 * tags. A CRF feature extractor converts nodes and edges to
 * feature maps, which are then converted to vectors for use in
 * CRFs.
 */
public class HighOrderCrf<E> implements Tagger<E>, NBestTagger<E>, MarginalTagger<E>, Serializable {

    static final long serialVersionUID = -4868542587460878290L;

    private final List<String> mTagList;
    private final boolean[] mLegalTagStarts;
    private final boolean[] mLegalTagEnds;
    private final boolean[][] mLegalTagTransitions;
    private final Vector[] mCoefficients;
    private final SymbolTable mFeatureSymbolTable;
    private final HighOrderCrfFeatureExtractor<E> mFeatureExtractor;
    private final boolean mAddInterceptFeature;
    private final int mNumDimensions;


    /**
     * Construct a conditional random field from the specified tags,
     * feature vector coefficients, symbol table for feature, feature
     * extractors and flag indicating whether to add intercepts or
     * not.
     *
     * @param tags Array of output tags.
     * @param coefficients Array of coefficient vectors parallel to tags.
     * @param featureSymbolTable Symbol table for feature extraction
     * to vectors.
     * @param featureExtractor CRF feature extractor.
     * @param addInterceptFeature {@code true} if an intercept feature
     * at position 0 with value 1 is added to all feature vectors.
     * @throws IllegalArgumentException If the tag and coefficient
     * vector arrays are not non-empty and the same length, or if the
     * coefficient vectors are not all of the same number of dimensions.
     */
    public HighOrderCrf(String[] tags,
                        Vector[] coefficients,
                        SymbolTable featureSymbolTable,
                        HighOrderCrfFeatureExtractor<E> featureExtractor,
                        boolean addInterceptFeature) {
        this(tags,trueArray(tags.length),
             trueArray(tags.length),
             trueArray(tags.length,tags.length),
             coefficients,
             featureSymbolTable,
             featureExtractor,
             addInterceptFeature);
    }


    /**
     * Construct a conditional random field from the specified tags,
     * feature vector coefficients, symbol table for feature, feature
     * extractors and flag indicating whether to add intercepts or
     * not.
     *
     * @param tags Array of output tags
     * @param legalTagStarts Array of flags indicating if tag may be
     * first tag for a tagging.
     * @param legalTagEnds Array of flags indicating if tag may be
     * last tag for a tagging.
     * @param legalTagTransitions Two dimensional array of flags indicating
     * if the first tag may be followed by the second tag.
     * @param coefficients Array of coefficient vectors parallel to tags.
     * @param featureSymbolTable Symbol table for feature extraction
     * to vectors.
     * @param featureExtractor CRF feature extractor.
     * @param addInterceptFeature {@code true} if an intercept feature
     * at position 0 with value 1 is added to all feature vectors.
     * @throws IllegalArgumentException If the tag and coefficient
     * vector arrays are not non-empty and the same length, or if the
     * coefficient vectors are not all of the same number of dimensions.
     */
    public HighOrderCrf(String[] tags,
                    boolean[] legalTagStarts,
                    boolean[] legalTagEnds,
                    boolean[][] legalTagTransitions,
                    Vector[] coefficients,
                    SymbolTable featureSymbolTable,
                    HighOrderCrfFeatureExtractor<E> featureExtractor,
                    boolean addInterceptFeature) {
        if (tags.length < 1) {
            String msg = "Require at least one tag.";
            throw new IllegalArgumentException(msg);
        }
        if (tags.length != coefficients.length) {
            String msg = "Require tags and coefficients to be same length." + " Found tags.length=" + tags.length + " coefficients.length=" + coefficients.length;
            throw new IllegalArgumentException(msg);
        }
        if (tags.length != legalTagStarts.length) {
            String msg = "Tags and starts must be same length."
                + " Found tags.length=" + tags.length
                + " legalTagStarts.length=" + legalTagStarts.length;
            throw new IllegalArgumentException(msg);
        }
        if (tags.length != legalTagEnds.length) {
            String msg = "Tags and starts must be same length."
                + " Found tags.length=" + tags.length
                + " legalTagStarts.length=" + legalTagStarts.length;
            throw new IllegalArgumentException(msg);
        }
        if (tags.length != legalTagTransitions.length) {
            String msg = "Tags and transitions must be same length."
                + " Found tags.length=" + tags.length
                + " legalTagTransitions.length=" + legalTagTransitions.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < legalTagTransitions.length; ++i) {
            if (tags.length != legalTagTransitions[i].length) {
                String msg = "Tags and transition rows must be same length."
                    + " Found tags.length=" + tags.length
                    + " legalTagTransitions[" + i + "].length="
                    + legalTagTransitions[i].length;
                throw new IllegalArgumentException(msg);
            }
        }
        for (int k = 1; k < coefficients.length; ++k) {
            if (coefficients[0].numDimensions() != coefficients[k].numDimensions()) {
                String msg = "All coefficients must be same length."
                    + " Found coefficents[0].numDimensions()="
                    + coefficients[0].numDimensions()
                    + " coefficients[" + k + "].numDimensions()="
                    + coefficients[k].numDimensions();
                throw new IllegalArgumentException(msg);
            }
        }
        mTagList = Arrays.asList(tags);
        mLegalTagStarts = legalTagStarts;
        mLegalTagEnds = legalTagEnds;
        mLegalTagTransitions = legalTagTransitions;
        mCoefficients = coefficients;
        mNumDimensions = coefficients[0].numDimensions();
        mFeatureSymbolTable = featureSymbolTable;
        mFeatureExtractor = featureExtractor;
        mAddInterceptFeature = addInterceptFeature;
    }

    /**
     * Returns an unmodifiable view of the array of tags underlying this CRF.
     *
     * <p>The array of coefficient vectors is parallel to the array of
     * tags returned by {@link #tags()}k, so the coefficient vector
     * {@code coefficients()[n]} is for output tag {@code tags()[n]}.
     *
     * @return View of the output tags.
     */
    public List<String> tags() {
        return Collections.unmodifiableList(mTagList);
    }

    /**
     * Returns the tag for the specified tag index.  This uses the
     * underlying tags, so that {@code tag(k) == tags()[k]}.
     *
     * @param k Position of tag.
     * @return Tag for the specified position.
     * @throws ArrayIndexOutOfBoundsException If the specified index
     * is out of bounds for the tag array ({@code k < 0} or {@code k
     * >= tags().length}).
     */
    public String tag(int k) {
        return mTagList.get(k);
    }

    /**
     * Return the coefficient vectors for this CRF.
     *
     * <p>The array of coefficient vectors is parallel to the array of
     * tags returned by {@link #tags()}k, so the coefficient vector
     * {@code coefficients()[n]} is for output tag {@code tags()[n]}.
     *
     * @return The coefficient vectors.
     */
    public Vector[] coefficients() {
        Vector[] result = new Vector[mCoefficients.length];
        for (int k = 0; k < result.length; ++k) result[k] = Matrices.unmodifiableVector(mCoefficients[k]);
        return result;
    }

    /**
     * Returns an unmodifiable view of the symbol table for features for
     * this CRF.
     *
     * @return A view of the symbol table for features.
     */
    public SymbolTable featureSymbolTable() {
        return MapSymbolTable.unmodifiableView(mFeatureSymbolTable);
    }


    /**
     * Return the feature extractor for this CRF.
     *
     * @return The feature extractor.
     */
    public HighOrderCrfFeatureExtractor<E> featureExtractor() {
        return mFeatureExtractor;
    }

    /**
     * Returns {@code true} if this CRF adds an intercept feature
     * with value 1.0 at index 0 to all feature vectors.
     *
     * @return Whether this CRF adds an intercept feature.
     */
    public boolean addInterceptFeature() {
        return mAddInterceptFeature;
    }


    // Tagger
    public Tagging<E> tag(List<E> tokens) {
        int numTokens = tokens.size();
        if (numTokens == 0) return new Tagging<E>(tokens, Collections.<String>emptyList());
        int numTags = mTagList.size();
        int numDimensions = mFeatureSymbolTable.numSymbols();
        double[][] bestScores = new double[numTokens][numTags];
        int[][] backPointers = new int[numTokens-1][numTags];
        HighOrderCrfFeatures<E> features = mFeatureExtractor.extract(tokens,mTagList);
        Vector nodeVector0 = nodeFeatures(0,features);
        int order = features.order();
        for (int k = 0; k < numTags; ++k)
            bestScores[0][k]
                = mLegalTagStarts[k]
                ? nodeVector0.dotProduct(mCoefficients[k])
                : Double.NEGATIVE_INFINITY;
        Vector[] edgeVectors = new Vector[numTags];
        for (int n = order; n < numTokens; ++n) {
            Vector nodeVector = nodeFeatures(n,features);
            for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                int kMinus[] = new int[order];
                for ( int i = kMinus1, j = 0 ; i < kMinus1 + order; i++, j++) kMinus[j] = i; 
                edgeVectors[kMinus1] = edgeFeatures(n,kMinus,features);
	    }
            for (int k = 0; k < numTags; ++k) {
                if (n == (numTokens-1) && !mLegalTagEnds[k]) {
                    bestScores[n][k] = Double.NEGATIVE_INFINITY;
                    backPointers[n-1][k] = -1;
                    continue;
                }
                double bestScore = Double.NEGATIVE_INFINITY;
                int backPtr = -1;
                double nodeScore = nodeVector.dotProduct(mCoefficients[k]);
                for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                    if (!mLegalTagTransitions[kMinus1][k]) continue;
                    double score = nodeScore + edgeVectors[kMinus1].dotProduct(mCoefficients[k]) + bestScores[n-1][kMinus1];
                    if (score > bestScore) {
                        bestScore = score;
                        backPtr = kMinus1;
                    }
                }
                bestScores[n][k] = bestScore;
                backPointers[n-1][k] = backPtr;
            }
        }
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestFinalTag = -1;
        for (int k = 0; k < numTags; ++k) {
            if (bestScores[numTokens-1][k] > bestScore) {
                bestScore = bestScores[numTokens-1][k];
                bestFinalTag = k;
            }
        }
        List<String> tags = new ArrayList<String>(numTokens);
        int bestPreviousTag = bestFinalTag;
        tags.add(mTagList.get(bestFinalTag));
        for (int n = numTokens-1; --n >= 0; ) {
            bestPreviousTag = backPointers[n][bestPreviousTag];
            tags.add(mTagList.get(bestPreviousTag));
        }
        Collections.reverse(tags);
        return new Tagging<E>(tokens,tags);
    }

    // NBestTagger
    public Iterator<ScoredTagging<E>> tagNBest(List<E> tokens, int maxResults) {
        if (tokens.size() == 0) {
            ScoredTagging<E> scoredTagging = new ScoredTagging<E>(tokens,
                                       Collections.<String>emptyList(),
                                       0.0);
            return Iterators.singleton(scoredTagging);
        }
        return new NBestIterator(tokens,false,maxResults);
    }

    public Iterator<ScoredTagging<E>> tagNBestConditional(List<E> tokens, int maxResults) {
        if (tokens.size() == 0) {
            ScoredTagging<E> scoredTagging
                = new ScoredTagging<E>(tokens,
                                       Collections.<String>emptyList(),
                                       0.0); // log prob norm
            return Iterators.singleton(scoredTagging);
        }
        return new NBestIterator(tokens,true,maxResults);
    }

    // Marginal Tagger
    public TagLattice<E> tagMarginal(List<E> tokens) {
        if (tokens.size() == 0) {
            return new ForwardBackwardTagLattice<E>(tokens,
                                                    mTagList,
                                                    EMPTY_DOUBLE_2D_ARRAY,
                                                    EMPTY_DOUBLE_2D_ARRAY,
                                                    EMPTY_DOUBLE_3D_ARRAY,
                                                    0.0);
        }
        FeatureVectors features = features(tokens);
        TagLattice<E> lattice = forwardBackward(tokens,features);
        return lattice;
    }

    /**
     * Return a string-based representation of this high-order CRF.
     * All information returned in this string representation is
     * available programatically.
     *
     * <p><b>Warning:</b> The output is very verbose, including
     * symbolic representations of all the coefficients.
     *
     * @return A string-based representation of this high-order CRF.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Feature Extractor=" + featureExtractor());
        sb.append("\n");
        sb.append("Add intercept=" + addInterceptFeature());
        sb.append("\n");
        List<String> tags = tags();
        sb.append("Tags=" + tags);
        sb.append("\n");
        Vector[] coeffs = coefficients();
        SymbolTable symTab = featureSymbolTable();
        sb.append("Coefficients=\n");
        for (int i = 0; i < coeffs.length; ++i) {
            sb.append(tags.get(i));
            sb.append("  ");
            int[] nzDims = coeffs[i].nonZeroDimensions();
            for (int k = 0; k < nzDims.length; ++k) {
                if (k > 0) sb.append(", ");
                int d = nzDims[k];
                sb.append(symTab.idToSymbol(d));
                sb.append("=");
                sb.append(coeffs[i].value(d));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // for serialization
    Object writeReplace() { return new Serializer<E>(this); }

    private Vector nodeFeatures(int position,
                                HighOrderCrfFeatures<E> features) {
        return Features.toVector(features.nodeFeatures(position),
                                 mFeatureSymbolTable,
                                 mNumDimensions,
                                 mAddInterceptFeature);
    }

    private Vector edgeFeatures(int position, int lastTagIndex[], HighOrderCrfFeatures<E> features) {
        return Features.toVector(features.edgeFeatures(position,lastTagIndex),mFeatureSymbolTable, mNumDimensions, mAddInterceptFeature);
    }

    private FeatureVectors features(List<E> tokens) {
        int numTags = mTagList.size();
        int numDimensions = mFeatureSymbolTable.numSymbols();
        if (tokens.size() == 0) return null;
        HighOrderCrfFeatures<E> features = mFeatureExtractor.extract(tokens,mTagList);
        int order = features.order();
        Vector[] nodeFeatureVectors = new Vector[tokens.size()];
        for (int n = 0; n < tokens.size(); ++n) nodeFeatureVectors[n] = nodeFeatures(n,features);
        Vector[][] edgeFeatureVectorss = new Vector[tokens.size()-1][mTagList.size()];
        for (int n = order; n < tokens.size(); ++n) {
            for (int k = 0; k < numTags; ++k) {
                int kMin[] = new int[order];
                for (int l = k , m = 0; m < order ; l++, m++) kMin[m] = l;
                edgeFeatureVectorss[n-order][k] = edgeFeatures(n,kMin,features);
            }
        }
        return new FeatureVectors(nodeFeatureVectors, edgeFeatureVectorss);
    }

    TagLattice<E> forwardBackward(List<E> tokens, FeatureVectors features) {
        int numTokens = tokens.size();
        int numTags = mTagList.size();
        double[] logPotentials0Begin = new double[numTags];
        for (int kTo = 0; kTo < numTags; ++kTo)
            logPotentials0Begin[kTo]
                = mLegalTagStarts[kTo]
                ? features.mNodeFeatureVectors[0].dotProduct(mCoefficients[kTo])
                : Double.NEGATIVE_INFINITY;
        double[][][] logPotentials = new double[numTokens-1][numTags][numTags];
        for (double[][] logPotentials2 : logPotentials)
            for (double[] logPotentials3 : logPotentials2)
                Arrays.fill(logPotentials3,Double.NEGATIVE_INFINITY);
        for (int nTo = 1; nTo < numTokens; ++nTo) {
            for (int kTo = 0; kTo < numTags; ++kTo) {
                if (nTo == (numTokens - 1) && !mLegalTagEnds[kTo])
                    continue;
                double nodePotentialKTo
                    = features.mNodeFeatureVectors[nTo].dotProduct(mCoefficients[kTo]);
                for (int kFrom = 0; kFrom < numTags; ++kFrom)
                    if (mLegalTagTransitions[kFrom][kTo])
                        logPotentials[nTo-1][kFrom][kTo]
                            = features.mEdgeFeatureVectorss[nTo-1][kFrom].dotProduct(mCoefficients[kTo])
                            + nodePotentialKTo;
            }
        }
        double[] buf = new double[numTags];
        double[][] logForwards = new double[numTokens][numTags];
        for (int kTo = 0; kTo < numTags; ++kTo)
            logForwards[0][kTo] = logPotentials0Begin[kTo]; // should just copy array
        for (int nTo = 1; nTo < numTokens; ++nTo) {
            for (int kTo = 0; kTo < numTags; ++kTo) {
                for (int kFrom = 0; kFrom < numTags; ++kFrom) {
                    buf[kFrom] = logForwards[nTo-1][kFrom]
                        + logPotentials[nTo-1][kFrom][kTo];
                }
                logForwards[nTo][kTo] = com.aliasi.util.Math.logSumOfExponentials(buf);
            }
        }
        double[][] logBackwards = new double[numTokens][numTags];
        for (int nFrom = numTokens-1; --nFrom >= 0; ) {
            for (int kFrom = 0; kFrom < numTags; ++kFrom) {
                for (int kTo = 0; kTo < numTags; ++kTo) {
                    buf[kTo] = logBackwards[nFrom+1][kTo]
                        + logPotentials[nFrom][kFrom][kTo];
                }
                logBackwards[nFrom][kFrom] = com.aliasi.util.Math.logSumOfExponentials(buf);
            }
        }
        double logZ = com.aliasi.util.Math.logSumOfExponentials(logForwards[numTokens-1]);
        return new ForwardBackwardTagLattice<E>(tokens,
                                                mTagList,
                                                logForwards,
                                                logBackwards,
                                                logPotentials,
                                                logZ,
                                                false);
    }

    class NBestIterator extends Iterators.Buffered<ScoredTagging<E>> {
        final List<E> mTokens;
        final double mLogZ;
        final double[][][] mTransitionScores;
        final double[][] mViterbiScores;
        final int[][] mBackPointers;
        final BoundedPriorityQueue<NBestState> mPriorityQueue;
        NBestIterator(List<E> tokens, boolean normToConditional, int maxResults) {
            mPriorityQueue = new BoundedPriorityQueue<NBestState>(ScoredObject.<NBestState>comparator(), maxResults);
            mTokens = tokens;
            int numTokens = tokens.size();
            int numTags = mTagList.size();
            mTransitionScores = new double[numTokens-1][numTags][numTags];
            for (double[][] xss : mTransitionScores)
                for (double[] xs : xss) Arrays.fill(xs,Double.NEGATIVE_INFINITY);
            mViterbiScores = new double[numTokens][numTags];
            for (double[] xs : mViterbiScores) Arrays.fill(xs,Double.NEGATIVE_INFINITY);
            mBackPointers = new int[numTokens-1][numTags];
            for (int[] ptrs : mBackPointers) Arrays.fill(ptrs,-1);
            Vector[] edgeVectors = new Vector[numTags];
            HighOrderCrfFeatures<E> features = mFeatureExtractor.extract(tokens,mTagList);
            int order = features.order();
            for (int n = order; n < numTokens; ++n) {
                Vector nodeVector = nodeFeatures(n,features);
                for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {                    
                    int kMinus[] = new int[order];
                    for ( int i = kMinus1, j = 0 ; i < kMinus1 + order; i++, j++) kMinus[j] = i;
                    if (n == order && !mLegalTagStarts[kMinus1]) continue; //TODO : check this
                    edgeVectors[kMinus1] = edgeFeatures(n,kMinus,features);
                }
                for (int k = 0; k < numTags; ++k) {
                    if (n == (numTokens-1) && !mLegalTagEnds[k])
                        continue;
                    double nodeScore = nodeVector.dotProduct(mCoefficients[k]);
                    for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                        if (!mLegalTagTransitions[kMinus1][k])
                            continue;
                        if (n == 1 && !mLegalTagStarts[kMinus1])
                            continue;
                        mTransitionScores[n-1][kMinus1][k]
                            = nodeScore
                            + edgeVectors[kMinus1].dotProduct(mCoefficients[k]);
                    }
                }
            }
            Vector nodeVector0 = nodeFeatures(0,features);
            for (int k = 0; k < numTags; ++k) {
                if (!mLegalTagStarts[k])
                    continue;
                mViterbiScores[0][k]
                    = nodeVector0.dotProduct(mCoefficients[k]);
            }
            for (int n = 1; n < numTokens; ++n) {
                for (int k = 0; k < numTags; ++k) {
                    if (n == (numTokens-1) && !mLegalTagEnds[k]) continue;
                    double bestScore = Double.NEGATIVE_INFINITY;
                    int backPtr = -1;
                    for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                        if (!mLegalTagTransitions[kMinus1][k]) continue;
                        double score = mViterbiScores[n-1][kMinus1] + mTransitionScores[n-1][kMinus1][k];
                        if (score > bestScore) {
                            bestScore = score;
                            backPtr = kMinus1;
                        }
                    }
                    mViterbiScores[n][k] = bestScore;
                    mBackPointers[n-1][k] = backPtr;
                }
            }
            mLogZ = normToConditional ? logZ() : 0.0;
            for (int k = 0; k < numTags; ++k) {
                offer(mViterbiScores[numTokens-1][k],null,numTokens-1,k);
            }
        }
        double logZ() {
            double[] forwards = mViterbiScores[0].clone();
            int numTags = forwards.length;
            double[] previousForwards = new double[numTags];
            double[] exps = new double[numTags];
            for (int n = 0; n < mTransitionScores.length; ++n) {
                double[] temp = previousForwards;
                previousForwards = forwards;
                forwards = temp;
                for (int k = 0; k < numTags; ++k) {
                    for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                        exps[kMinus1] = previousForwards[kMinus1] + mTransitionScores[n][kMinus1][k];
                    }
                    forwards[k] = com.aliasi.util.Math.logSumOfExponentials(exps);
                }
            }
            double logZ = com.aliasi.util.Math.logSumOfExponentials(forwards);
            return logZ;
        }
        void offer(double score, ForwardPointer pointer, int n, int k) {
            if (score == Double.NEGATIVE_INFINITY) return;
            if (pointer != null && pointer.mScore == Double.NEGATIVE_INFINITY) return;
            NBestState state = new NBestState(score,pointer,n,k);
            mPriorityQueue.offer(state);
        }
        public ScoredTagging<E> bufferNext() {
            NBestState resultState = mPriorityQueue.poll();
            if (resultState == null) return null;
            int n = resultState.mN-1;
            int k = resultState.mK;
            ForwardPointer fwdPointer = resultState.mForwardPointer;
            while (n >= 0) {
                addAlternatives(n,k,fwdPointer);
                int kMinus1 = mBackPointers[n][k];
                double fwdScore = mTransitionScores[n][kMinus1][k];
                if (fwdPointer != null) fwdScore += fwdPointer.mScore;
                fwdPointer = new ForwardPointer(k,fwdPointer,fwdScore);
                k = kMinus1;
                --n;
            }
            ScoredTagging<E> scoredTagging = toScoredTagging(resultState);
            return scoredTagging;
        }
        void addAlternatives(int n, int k, ForwardPointer fwdPointer) {
            int numTags = mTagList.size();
            for (int kMinus1 = 0; kMinus1 < numTags; ++kMinus1) {
                if (kMinus1 == mBackPointers[n][k]) continue;
                double score = mViterbiScores[n][kMinus1];
                double fwdScore = mTransitionScores[n][kMinus1][k];
                if (fwdPointer != null) fwdScore += fwdPointer.mScore;
                ForwardPointer pointer = new ForwardPointer(k,fwdPointer,fwdScore);
                offer(score,pointer,n,kMinus1);
            }
        }

        public ScoredTagging<E> toScoredTagging(NBestState state) {
            List<String> tags = new ArrayList<String>(mTokens.size());
            int k = state.mK;
            tags.add(mTagList.get(k));
            for (int n = state.mN; n > 0; --n) {
                k = mBackPointers[n-1][k];
                tags.add(mTagList.get(k));
            }
            Collections.reverse(tags);
            for (ForwardPointer pointer = state.mForwardPointer;
                 pointer != null;
                 pointer = pointer.mPointer) {
                tags.add(mTagList.get(pointer.mK));
            }
            return new ScoredTagging<E>(mTokens,tags,state.score() -mLogZ);
        }
    }

    static final String INTERCEPT_FEATURE_NAME = "*&^INTERCEPT%$^&**";
    static final double[][] EMPTY_DOUBLE_2D_ARRAY = new double[0][];
    static final double[][][] EMPTY_DOUBLE_3D_ARRAY = new double[0][][];

    static boolean[] legalStarts(int[][] tagIdss, int numTags) {
        boolean[] legalStarts = new boolean[numTags];
        for (int[] tagIds : tagIdss)
            if (tagIds.length > 0) legalStarts[tagIds[0]] = true;
        return legalStarts;
    }

    static boolean[] legalEnds(int[][] tagIdss, int numTags) {
        boolean[] legalEnds = new boolean[numTags];
        for (int[] tagIds : tagIdss)
            if (tagIds.length > 0) legalEnds[tagIds[tagIds.length-1]] = true;
        return legalEnds;
    }

    static boolean[][] legalTransitions(int[][] tagIdss, int numTags) {
        boolean[][] legalTransitions = new boolean[numTags][numTags];
        for (int[] tagIds : tagIdss) {
            for (int i = 1; i < tagIds.length; ++i) legalTransitions[tagIds[i-1]][tagIds[i]] = true;
        }
        return legalTransitions;
    }

    static boolean[] trueArray(int m) {
        boolean[] result = new boolean[m];
        Arrays.fill(result,true);
        return result;
    }

    static boolean[][] trueArray(int m, int n) {
        boolean[][] result = new boolean[m][n];
        for (boolean[] row : result) Arrays.fill(row,true);
        return result;
    }

    static class Serializer<F> extends AbstractExternalizable {
        static final long serialVersionUID = -4140295941325870709L;
        final HighOrderCrf<F> mCrf;
        public Serializer(HighOrderCrf<F> crf) {
            mCrf = crf;
        }
        public Serializer() {
            this(null);
        }
        public void writeExternal(ObjectOutput out)
            throws IOException {
            int numTags = mCrf.mTagList.size();
            out.writeInt(numTags);
            for (String tag : mCrf.mTagList) out.writeUTF(tag);
            for (int i = 0; i < numTags; ++i) out.writeBoolean(mCrf.mLegalTagStarts[i]);
            for (int i = 0; i < numTags; ++i) out.writeBoolean(mCrf.mLegalTagEnds[i]);
            for (int i = 0; i < numTags; ++i)
                for (int j = 0; j < numTags; ++j)
                    out.writeBoolean(mCrf.mLegalTagTransitions[i][j]);
            for (Vector v : mCrf.mCoefficients)
                out.writeObject(v);
            out.writeObject(mCrf.mFeatureSymbolTable);
            out.writeObject(mCrf.mFeatureExtractor);
            out.writeBoolean(mCrf.mAddInterceptFeature);
        }
        public Object read(ObjectInput in) throws ClassNotFoundException, IOException {
            int numTags = in.readInt();
            String[] tags = new String[numTags];
            for (int i = 0; i < tags.length; ++i)
                tags[i] = in.readUTF();
            boolean[] legalTagStarts = new boolean[numTags];
            for (int i = 0; i < numTags; ++i)
                legalTagStarts[i] = in.readBoolean();
            boolean[] legalTagEnds = new boolean[numTags];
            for (int i = 0; i < numTags; ++i)
                legalTagEnds[i] = in.readBoolean();
            boolean[][] legalTagTransitions = new boolean[numTags][numTags];
            for (int i = 0; i < numTags; ++i)
                for (int j = 0; j < numTags; ++j)
                    legalTagTransitions[i][j] = in.readBoolean();
            Vector[] coefficients = new Vector[numTags];
            for (int i = 0; i < tags.length; ++i) coefficients[i] = (Vector) in.readObject();
            SymbolTable featureSymbolTable = (SymbolTable) in.readObject();
            @SuppressWarnings("unchecked")
            HighOrderCrfFeatureExtractor<F> featureExtractor = (HighOrderCrfFeatureExtractor<F>) in.readObject();
            boolean addInterceptFeature = in.readBoolean();
            return new HighOrderCrf<F>(tags,
                                       legalTagStarts,
                                       legalTagEnds,
                                       legalTagTransitions,
                                       coefficients,
                                       featureSymbolTable,
                                       featureExtractor,
                                       addInterceptFeature);
        }
    }

    static class ForwardPointer {
        final int mK;
        final ForwardPointer mPointer;
        final double mScore;
        ForwardPointer(int k, ForwardPointer pointer, double score) {
            mK = k;
            mPointer = pointer;
            mScore = score;
        }
    }

    static class NBestState implements Scored {
        final double mScore;
        final ForwardPointer mForwardPointer;
        final int mN;
        final int mK;
        NBestState(double score,
                   ForwardPointer forwardPointer,
                   int n,
                   int k) {
            mScore = score;
            mForwardPointer = forwardPointer;
            mN = n;
            mK = k;
        }
        public double score() {
            return (mForwardPointer != null)
                ?  mScore + mForwardPointer.mScore
                : mScore;
        }
    }


    /**
     * Return the CRF estimated using stochastic gradient descent with
     * the specified prior from the specified corpus of taggings of
     * type {@code F} pruned to the specified minimum feature count,
     * using the specified feature extractor, automatically adding an
     * intercept feature if the flag is {@code true}, allow unseen tag
     * transitions as specified, using the specified training
     * parameters for annealing, measuring convergence, and reporting
     * the incremental results to the specified reporter.
     *
     * <p>Reporting at the info level provides parameter and epoch
     * level.  At the debug level, it reports epoch-by-epoch
     * likelihoods.
     *
     * @param corpus Corpus from which to estimate.
     * @param featureExtractor Feature extractor for the CRF.
     * @param addInterceptFeature Set to {@code true} if an intercept
     * feature with index 0 is automatically added to all feature
     * vectors with value 1.0.
     * @param minFeatureCount Minimum number of instances of a feature
     * to keep it.
     * @param cacheFeatureVectors Flag indicating whether or not to
     * keep the computed feature vectors in memory.
     * @param allowUnseenTransitions Flag indicating whether to allow
     * tags to start a tagging, end a tagging, or follow another tag
     * if there was not an example of that in the corpus.
     * @param prior Prior for coefficients to use during estimation.
     * @param annealingSchedule Schedule for annealing the learning
     * rate during gradient descent.
     * @param minImprovement Minimum relative improvement objective
     * (log likelihood plus log prior) computed as a 10-epoch rolling
     * average to signal convergence.
     * @param minEpochs Minimum number of epochs for which to run
     * gradient descent estimation.
     * @param maxEpochs Maximum number of epochs for which to run
     * gradient descent estimation.
     * @param reporter Reporter to which results are written, or
     * {@code null} for no reporting of intermediate results.
     */
    public static <F> HighOrderCrf<F>
        estimate(Corpus<ObjectHandler<Tagging<F>>> corpus,
                 HighOrderCrfFeatureExtractor<F> featureExtractor,
                 boolean addInterceptFeature,
                 int minFeatureCount,
                 boolean cacheFeatureVectors,
                 boolean allowUnseenTransitions,
                 RegressionPrior prior,
                 int priorBlockSize,
                 AnnealingSchedule annealingSchedule,
                 double minImprovement,
                 int minEpochs,
                 int maxEpochs,
                 Reporter reporter)
        throws IOException {
        if (reporter == null) reporter = Reporters.silent();
        reporter.info("HighOrderCrf.estimate Parameters");
        reporter.info("featureExtractor=" + featureExtractor);
        reporter.info("addInterceptFeature=" + addInterceptFeature);
        reporter.info("minFeatureCount=" + minFeatureCount);
        reporter.info("cacheFeatureVectors=" + cacheFeatureVectors);
        reporter.info("allowUnseenTransitions=" + allowUnseenTransitions);
        reporter.info("prior=" + prior);
        reporter.info("annealingSchedule=" + annealingSchedule);
        reporter.info("minImprovement=" + minImprovement);
        reporter.info("minEpochs=" + minEpochs);
        reporter.info("maxEpochs=" + maxEpochs);
        reporter.info("priorBlockSize=" + priorBlockSize);
        reporter.info("Computing corpus tokens and features");
        List<List<F>> tokenss = corpusTokens(corpus);
        String[][] tagss = corpusTags(corpus);
        int numTrainingInstances = tagss.length;
        int longestInput = longestInput(tagss);
        long numTrainingTokens = 0L;
        for (String[] tags : tagss) numTrainingTokens += tags.length;
        int[][] tagIdss = new int[tagss.length][];
        MapSymbolTable tagSymbolTable = tagSymbolTable(tagss,tagIdss);
        MapSymbolTable featureSymbolTable
            = featureSymbolTable(tagss,tokenss,
                                 addInterceptFeature,
                                 featureExtractor,
                                 minFeatureCount);
        int numTags = tagSymbolTable.numSymbols();
        String[] allTags = new String[numTags];
        for (int n = 0; n < numTags; ++n) allTags[n] = tagSymbolTable.idToSymbol(n);
        boolean[] legalTagStarts
            = allowUnseenTransitions
            ? trueArray(numTags)
            : legalStarts(tagIdss,numTags);
        boolean[] legalTagEnds
            = allowUnseenTransitions
            ? trueArray(numTags)
            : legalEnds(tagIdss,numTags);
        boolean[][] legalTagTransitions
            = allowUnseenTransitions
            ? trueArray(numTags,numTags)
            : legalTransitions(tagIdss,numTags);
        int numDimensions = featureSymbolTable.numSymbols();
        DenseVector[] weightVectors = new DenseVector[numTags];
        for (int i = 0; i < weightVectors.length; ++i) weightVectors[i] = new DenseVector(numDimensions);
        reporter.info("Corpus Statistics");
        reporter.info("Num Training Instances=" + numTrainingInstances);
        reporter.info("Num Training Tokens=" + numTrainingTokens);
        reporter.info("Num Dimensions After Pruning=" + numDimensions);
        reporter.info("Tags=" + tagSymbolTable);
        HighOrderCrf<F> crf = new HighOrderCrf<F>(allTags,
                                          legalTagStarts,
                                          legalTagEnds,
                                          legalTagTransitions,
                                          weightVectors,
                                          featureSymbolTable,
                                          featureExtractor,
                                          addInterceptFeature);
        FeatureVectors[] featureVectorsCache
            = cacheFeatureVectors 
            ? new FeatureVectors[numTrainingInstances]
            : null;
        if (cacheFeatureVectors) {
            reporter.info("Caching Feature Vectors");
            for (int j = 0; j < numTrainingInstances; ++j)
                featureVectorsCache[j] = crf.features(tokenss.get(j));
        }        
        double lastLog2LikelihoodAndPrior = -(Double.MAX_VALUE / 2.0);
        double rollingAverageRelativeDiff = 1.0; // arbitrary starting point
        double bestLog2LikelihoodAndPrior = Double.NEGATIVE_INFINITY;
        long cumFeatureExtractionMs = 0L;
        long cumForwardBackwardMs = 0L;
        long cumUpdateMs = 0L;
        long cumLossMs = 0L;
        long cumPriorUpdateMs = 0L;
        for (int epoch = 0; epoch < maxEpochs; ++epoch) {
            int instancesSinceLastPriorUpdate = 0;
            double learningRate = annealingSchedule.learningRate(epoch);
            double learningRatePerTrainingInstance = learningRate / numTrainingInstances;
            for (int j = 0; j < numTrainingInstances; ++j) {
                int[] tagIds = tagIdss[j];
                List<F> tokens = tokenss.get(j);
                int numTokens = tokens.size();
                if (numTokens < 1) continue;
                long startMs = System.currentTimeMillis();
                FeatureVectors features
                    = cacheFeatureVectors
                    ? featureVectorsCache[j]
                    : crf.features(tokens);
                long featsMs = System.currentTimeMillis();
                cumFeatureExtractionMs += (featsMs - startMs);
                TagLattice<F> lattice = crf.forwardBackward(tokens,features);
                long fwdBkMs = System.currentTimeMillis();
                cumForwardBackwardMs += (fwdBkMs - featsMs);
                for (int nTo = 0; nTo < numTokens; ++nTo) {
                    weightVectors[tagIds[nTo]].increment(learningRate,
                                                         features.mNodeFeatureVectors[nTo]);
                }
                for (int nTo = 1; nTo < numTokens; ++nTo) {
                    weightVectors[tagIds[nTo]].increment(learningRate,
                                                         features.mEdgeFeatureVectorss[nTo-1][tagIds[nTo-1]]);
                }
                for (int nTo = 0; nTo < numTokens; ++nTo) {
                    for (int kTo = 0; kTo < numTags; ++kTo) {
                        double logP = lattice.logProbability(nTo,kTo);
                        if (logP < -400.0) continue; // will underflow to 0.0 or close enough
                        double p = Math.exp(logP);
                        weightVectors[kTo].increment(-p*learningRate,
                                                     features.mNodeFeatureVectors[nTo]);
                    }
                }
                for (int nTo = 1; nTo < numTokens; ++nTo) {
                    for (int kFrom = 0; kFrom < numTags; ++kFrom) {
                        for (int kTo = 0; kTo < numTags; ++kTo) {
                            double logP = lattice.logProbability(nTo,kFrom,kTo);
                            if (logP < -400) continue;
                            double p = Math.exp(logP);
                            weightVectors[kTo].increment(-p*learningRate,
                                                         features.mEdgeFeatureVectorss[nTo-1][kFrom]);
                        }
                    }
                }
                long updateMs = System.currentTimeMillis();
                cumUpdateMs += (updateMs-fwdBkMs);
                if ((++instancesSinceLastPriorUpdate) == priorBlockSize) {
                    adjustWeightsWithPrior(weightVectors,prior,
                                           instancesSinceLastPriorUpdate * learningRatePerTrainingInstance);
                    instancesSinceLastPriorUpdate = 0;
                }
                long priorMs = System.currentTimeMillis();
                cumPriorUpdateMs += (priorMs - updateMs);
            }
            long finalPriorStartMs = System.currentTimeMillis();
            adjustWeightsWithPrior(weightVectors,prior,
                                   instancesSinceLastPriorUpdate * learningRatePerTrainingInstance);
            long finalPriorEndMs = System.currentTimeMillis();
            cumPriorUpdateMs += (finalPriorEndMs - finalPriorStartMs);            
            long lossStartMs = System.currentTimeMillis();
            double log2Likelihood = 0.0;
            for (int j = 0; j < numTrainingInstances; ++j) {
                if (tokenss.get(j).size() < 1) continue;
                FeatureVectors features 
                    = cacheFeatureVectors
                    ? featureVectorsCache[j]
                    : crf.features(tokenss.get(j));
                TagLattice<F> lattice = crf.forwardBackward(tokenss.get(j),features);
                log2Likelihood += lattice.logProbability(0,tagIdss[j]);
            }
            double log2Prior = prior == null ? 0.0 : prior.log2Prior(weightVectors);
            double log2LikelihoodAndPrior = log2Likelihood + log2Prior;
            double relativeDiff = com.aliasi.util.Math.relativeAbsoluteDifference(lastLog2LikelihoodAndPrior,log2LikelihoodAndPrior);
            rollingAverageRelativeDiff = (9.0 * rollingAverageRelativeDiff + relativeDiff)/10.0;
            lastLog2LikelihoodAndPrior = log2LikelihoodAndPrior;
            if (log2LikelihoodAndPrior > bestLog2LikelihoodAndPrior)
                bestLog2LikelihoodAndPrior = log2LikelihoodAndPrior;
            long lossMs = System.currentTimeMillis();
            cumLossMs += (lossMs - lossStartMs);
            if (reporter.isDebugEnabled()) {
                Formatter formatter = null;
                try {
                    formatter = new Formatter(Locale.ENGLISH);
                    formatter.format("epoch=%5d lr=%11.9f ll=%11.4f lp=%11.4f llp=%11.4f llp*=%11.4f",
                                     epoch,
                                     learningRate,
                                     log2Likelihood,
                                     log2Prior,
                                     log2LikelihoodAndPrior,
                                     bestLog2LikelihoodAndPrior);
                    reporter.debug(formatter.toString());
                } catch (IllegalFormatException e) {
                    reporter.warn("Illegal format in Logistic Regression");
                } finally {
                    if (formatter != null)
                        formatter.close();
                }
            }
            if (rollingAverageRelativeDiff < minImprovement) {
                reporter.info("Converged with rollingAverageRelativeDiff="
                              + rollingAverageRelativeDiff);
                break; // goes to "return regression;"
            }

        }
        reporter.info("Feat Extraction Time=" + Strings.msToString(cumFeatureExtractionMs));
        reporter.info("Forward Backward Time=" + Strings.msToString(cumForwardBackwardMs));
        reporter.info("Update Time=" + Strings.msToString(cumUpdateMs));
        reporter.info("Prior Update Time=" + Strings.msToString(cumPriorUpdateMs));
        reporter.info("Loss Time=" + Strings.msToString(cumLossMs));
        return crf;
    }

    // cut and pasted from stats.LogisticRegression.adjustWeightsWithPriorDense()
    static void adjustWeightsWithPrior(DenseVector[] weightVectors,
                                       RegressionPrior prior,
                                       double learningRateDividedByNumTrainingInstances) {
        if (prior.isUniform()) return;
        for (DenseVector weightVectorsK : weightVectors) {
            for (int dim = 0; dim < weightVectorsK.numDimensions(); ++dim) {
                double weightVectorsKDim = weightVectorsK.value(dim);
                double priorMode = prior.mode(dim);
                if (weightVectorsKDim == priorMode) continue;
                double priorGradient = prior.gradient(weightVectorsKDim,dim);
                double delta = priorGradient * learningRateDividedByNumTrainingInstances;
                double newVal = weightVectorsKDim > priorMode
                    ? Math.max(priorMode, weightVectorsKDim - delta)
                    : Math.min(priorMode, weightVectorsKDim - delta);
                weightVectorsK.setValue(dim, newVal);
            }
        }
    }

    static MapSymbolTable tagSymbolTable(String[][] tagss, int[][] tagIdss) {
        MapSymbolTable tagSymbolTable = new MapSymbolTable();
        for (int j = 0; j < tagss.length; ++j) {
            tagIdss[j] = new int[tagss[j].length];
            for (int n = 0; n < tagIdss[j].length; ++n) {
                tagIdss[j][n] = tagSymbolTable.getOrAddSymbol(tagss[j][n]);
            }
        }
        return tagSymbolTable;
    }

    static <F> MapSymbolTable featureSymbolTable(String[][] tagss,
                                                 List<List<F>> tokenss,
                                                 boolean addInterceptFeature,
                                                 HighOrderCrfFeatureExtractor<F> featureExtractor,
                                                 int minFeatureCount) {
        ObjectToCounterMap<String> featureCounter = new ObjectToCounterMap<String>();
        for (int j = 0; j < tagss.length; ++j) {
            String[] tags = tagss[j];
            List<String> tagList = Arrays.asList(tags);
            List<F> tokens = tokenss.get(j);
            HighOrderCrfFeatures<F> features = featureExtractor.extract(tokens,tagList);
            int order = features.order();
            for (int n = 0; n < tags.length; ++n) {
                for (String feature : features.nodeFeatures(n).keySet())
                    featureCounter.increment(feature);
            }
            for (int k = order; k < tags.length; ++k) {
                int kMinus[] = new int[order];
                for ( int i = k - order,  l = 0; l < order ; i++, l++ ) kMinus[l] = i;
                for (String feature : features.edgeFeatures(k,kMinus).keySet()) {
                    featureCounter.increment(feature);
                }
            }
        }
        featureCounter.prune(minFeatureCount);
        MapSymbolTable featureSymbolTable = new MapSymbolTable();
        if (addInterceptFeature) featureSymbolTable.getOrAddSymbol(INTERCEPT_FEATURE_NAME);
        for (String feature : featureCounter.keySet()) featureSymbolTable.getOrAddSymbol(feature);
        return featureSymbolTable;
    }

    static <F> List<List<F>> corpusTokens(Corpus<ObjectHandler<Tagging<F>>> corpus) throws IOException {
        final List<List<F>> corpusTokenList = new ArrayList<List<F>>();
        corpus.visitTrain(new ObjectHandler<Tagging<F>>() {
                              public void handle(Tagging<F> tagging) {
                                  corpusTokenList.add(tagging.tokens());
                              }
                          });
        return corpusTokenList;
    }

    static <F> String[][] corpusTags(Corpus<ObjectHandler<Tagging<F>>> corpus) throws IOException {
        final List<String[]> corpusTagList = new ArrayList<String[]>(1024);
        corpus.visitTrain(new ObjectHandler<Tagging<F>>() {
                              public void handle(Tagging<F> tagging) {
                                  corpusTagList.add(tagging.tags().toArray(Strings.EMPTY_STRING_ARRAY));
                              }
                          });
        return corpusTagList.toArray(Strings.EMPTY_STRING_2D_ARRAY);
    }

    static DenseVector[] copy(DenseVector[] xs) {
        DenseVector[] result = new DenseVector[xs.length];
        for (int k = 0; k < xs.length; ++k) result[k] = new DenseVector(xs[k]);
        return result;
    }

    static int longestInput(String[][] tagss) {
        int longest = 0;
        for (String[] tags : tagss) if (tags.length > longest) longest = tags.length;
        return longest;
    }

    static class FeatureVectors {
        final Vector[] mNodeFeatureVectors;
        final Vector[][] mEdgeFeatureVectorss;
        FeatureVectors(Vector[] nodeFeatureVectors, Vector[][] edgeFeatureVectorss) {
            mNodeFeatureVectors = nodeFeatureVectors;
            mEdgeFeatureVectorss = edgeFeatureVectorss;
        }
    }

}
