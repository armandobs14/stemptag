package com.aliasi.crf;

import java.util.List;

/**
 * The {@code ChainCrfFeatureExtractor} interface specifies a method
 * for conditional random fields to extract the necessary node and
 * edge features for estimation and tagging.
 */
public interface HighOrderCrfFeatureExtractor<E> {

    /**
     * Return the chain CRF features for the specified list of input
     * tokens and specified list of possible tags.
     *
     * @param tokens List of token objects.
     * @param tags List of possible output tags.
     * @return The features for the specified tokens and tags.
     */
    public HighOrderCrfFeatures<E> extract(List<E> tokens, List<String> tags);

}
