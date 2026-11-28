#ifdef _FLW_OIT
#define TRANSPARENCY_WAVELET_RANK 3
#define TRANSPARENCY_WAVELET_COEFFICIENT_COUNT 16

// -------------------------------------------------------------------------
// WRITING
// -------------------------------------------------------------------------

void add_to_index(inout vec4[4] coefficients, int index, float addend) {
    coefficients[index >> 2][index & 3] = addend;
}

void add_absorbance(inout vec4[4] coefficients, float signal, float depth) {
    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    int index = clamp(int(floor(depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    index += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;
        int new_index = (index - 1) >> 1;
        float k = float((new_index + 1) & ((1 << power) - 1));

        int wavelet_sign = ((index & 1) << 1) - 1;
        float wavelet_phase = ((index + 1) & 1) * exp2(-power);
        float addend = fma(fma(-exp2(-power), k, depth), wavelet_sign, wavelet_phase) * exp2(power * 0.5) * signal;
        add_to_index(coefficients, new_index, addend);

        index = new_index;
    }

    float addend = fma(signal, -depth, signal);
    add_to_index(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1, addend);
}

void add_transmittance(inout vec4[4] coefficients, float transmittance, float depth) {
    float absorbance = -log(max(transmittance, 0.00001));// transforming the signal from multiplicative transmittance to additive absorbance
    add_absorbance(coefficients, absorbance, depth);
}

// -------------------------------------------------------------------------
// READING
// -------------------------------------------------------------------------

// TODO: maybe we could reduce the number of texel fetches below?
float get_coefficients(in sampler2DArray coefficients, int index) {
    return texelFetch(coefficients, ivec3(gl_FragCoord.xy, index >> 2), 0)[index & 3];
}

/// Compute the total absorbance, as if at infinite depth.
float total_absorbance(in sampler2DArray coefficients) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
        return 0;
    }

    int index_b = TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;
    }

    return b;
}

/// Compute the absorbance at a given normalized depth.
float absorbance(in sampler2DArray coefficients, float depth) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
        return 0;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index_b = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    bool sample_a = index_b >= 1;
    int index_a = sample_a ? (index_b - 1) : index_b;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;
    index_a += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;
    float a = sample_a ? scale_coefficient : 0;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);
        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a) {
            int new_index_a = (index_a - 1) >> 1;
            int wavelet_sign_a = ((index_a & 1) << 1) - 1;
            float coeff_a = (new_index_a == new_index_b) ? coeff_b : get_coefficients(coefficients, new_index_a);
            a -= exp2(float(power) * 0.5) * coeff_a * wavelet_sign_a;
            index_a = new_index_a;
        }
    }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);

    return mix(a, b, t);
}

/// Compute the absorbance at a given normalized depth,
/// correcting for self-occlusion by undoing the previously recorded absorbance event.
float signal_corrected_absorbance(in sampler2DArray coefficients, float depth, float signal) {
    float scale_coefficient = get_coefficients(coefficients, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    if (scale_coefficient == 0) {
        return 0;
    }

    depth *= float(TRANSPARENCY_WAVELET_COEFFICIENT_COUNT-1) / TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;

    float scale_coefficient_addend = fma(signal, -depth, signal);
    scale_coefficient -= scale_coefficient_addend;

    float coefficient_depth = depth * TRANSPARENCY_WAVELET_COEFFICIENT_COUNT;
    int index_b = clamp(int(floor(coefficient_depth)), 0, TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1);
    bool sample_a = index_b >= 1;
    int index_a = sample_a ? (index_b - 1) : index_b;

    index_b += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;
    index_a += TRANSPARENCY_WAVELET_COEFFICIENT_COUNT - 1;

    float b = scale_coefficient;
    float a = sample_a ? scale_coefficient : 0;

    for (int i = 0; i < (TRANSPARENCY_WAVELET_RANK+1); ++i) {
        int power = TRANSPARENCY_WAVELET_RANK - i;

        int new_index_b = (index_b - 1) >> 1;
        int wavelet_sign_b = ((index_b & 1) << 1) - 1;
        float coeff_b = get_coefficients(coefficients, new_index_b);

        float wavelet_phase_b = ((index_b + 1) & 1) * exp2(-power);
        float k = float((new_index_b + 1) & ((1 << power) - 1));
        float addend = fma(fma(-exp2(-power), k, depth), wavelet_sign_b, wavelet_phase_b) * exp2(power * 0.5) * signal;
        coeff_b -= addend;

        b -= exp2(float(power) * 0.5) * coeff_b * wavelet_sign_b;
        index_b = new_index_b;

        if (sample_a) {
            int new_index_a = (index_a - 1) >> 1;
            int wavelet_sign_a = ((index_a & 1) << 1) - 1;
            float coeff_a = (new_index_a == new_index_b) ? coeff_b : get_coefficients(coefficients, new_index_a);// No addend here on purpose, the original signal didn't contribute to this coefficient
            a -= exp2(float(power) * 0.5) * coeff_a * wavelet_sign_a;
            index_a = new_index_a;
        }
    }

    float t = coefficient_depth >= TRANSPARENCY_WAVELET_COEFFICIENT_COUNT ? 1.0 : fract(coefficient_depth);

    return mix(a, b, t);
}

// Helpers below to deal directly in transmittance.

#define ABSORBANCE_TO_TRANSMITTANCE(a) clamp(exp(-(a)), 0., 1.)

float total_transmittance(in sampler2DArray coefficients) {
    return ABSORBANCE_TO_TRANSMITTANCE(total_absorbance(coefficients));
}

float transmittance(in sampler2DArray coefficients, float depth) {
    return ABSORBANCE_TO_TRANSMITTANCE(absorbance(coefficients, depth));
}

float signal_corrected_transmittance(in sampler2DArray coefficients, float depth, float signal) {
    return ABSORBANCE_TO_TRANSMITTANCE(signal_corrected_absorbance(coefficients, depth, signal));
}
#elif defined(OIT)
const float OIT_FULLY_OPAQUE_ALPHA = 0.99;
const float OIT_FULLY_OPAQUE_TOTAL_TRANSMITTANCE = 0.02;
const int OIT_NUMBER_OF_DEPTH_BINS = OIT_COEFF_COUNT;
float deviceToLinearDepth(float deviceDepth) {
    #ifndef RENDERPEARL_DEPTH_IS_ZERO_TO_ONE
    deviceDepth = (deviceDepth - 0.5) * 2.0;
    #endif
    return flw_projection[3][2] / (deviceDepth + flw_projection[2][2]);
}
float toAbsorbance(float transmittance) {
    return clamp(-log(max(transmittance, 0.0001)), 0, 4.0);
}
float toTransmittance(float absorbance) {
    return clamp(exp(-absorbance), 0.0001, 1.0);
}
#if defined(OIT_TRANSMITTANCE) || defined(OIT_ACCUMULATE)
uniform sampler2D _flw_depthRange;
const float OIT_HIGH_PRECISION_DEPTH_THRESHOLD = 10.0;
const int OIT_LOW_PRECISION_DEPTH_BINS = 2;
float normalizeDepth(float fragmentDeviceDepth) {
    vec4 depthBoundsSample = texelFetch(_flw_depthRange, ivec2(gl_FragCoord.xy), 0);
    float closestBoundLinearDepthNegated = depthBoundsSample.r;
    float closestBoundLinearDepth = -closestBoundLinearDepthNegated;
    float furthestBoundLinearDepth = depthBoundsSample.g;

    float fragmentLinearDepth = deviceToLinearDepth(fragmentDeviceDepth);
    float range = max(furthestBoundLinearDepth - closestBoundLinearDepth, 0.0001);
    float depthWithinBounds = clamp(fragmentLinearDepth - closestBoundLinearDepth, 0.0, range);

    const float depthFractionWithHighPrecision = float(OIT_NUMBER_OF_DEPTH_BINS - OIT_LOW_PRECISION_DEPTH_BINS) / float(OIT_NUMBER_OF_DEPTH_BINS);
    float depthWithHighPrecision = depthFractionWithHighPrecision * range;

    if (depthWithHighPrecision <= OIT_HIGH_PRECISION_DEPTH_THRESHOLD) {
        return depthWithinBounds / range;
    } else if (depthWithinBounds <= OIT_HIGH_PRECISION_DEPTH_THRESHOLD) {
        return (depthWithinBounds / OIT_HIGH_PRECISION_DEPTH_THRESHOLD) * depthFractionWithHighPrecision;
    } else {
        return depthFractionWithHighPrecision + ((depthWithinBounds - OIT_HIGH_PRECISION_DEPTH_THRESHOLD) / (range - OIT_HIGH_PRECISION_DEPTH_THRESHOLD)) * (1.0 - depthFractionWithHighPrecision);
    }
}
#endif
#if defined(OIT_DEPTH_BOUNDS)
layout(location = 0) out vec4 _flw_depthRange_out;
void calculateDepthBounds(float fragmentDeviceDepth, float alpha) {
    float fragmentLinearDepth = deviceToLinearDepth(fragmentDeviceDepth);
    float opaqueFragmentDeviceDepth = alpha > OIT_FULLY_OPAQUE_ALPHA ? fragmentDeviceDepth : 0.0;
    _flw_depthRange_out = vec4(-fragmentLinearDepth, fragmentLinearDepth, fragmentDeviceDepth, opaqueFragmentDeviceDepth);
}
#elif defined(OIT_TRANSMITTANCE)
layout(location = 0) out vec4 coeff[OIT_COEFF_ATTACHMENT_COUNT];
void addTransmittance(float transmittance) {
    float coefficients[OIT_COEFF_COUNT];
    for (int i = 0; i < OIT_COEFF_COUNT; i++) {
        coefficients[i] = 0;
    }

    float absorbance = toAbsorbance(transmittance);

    float originalDepth = normalizeDepth(gl_FragCoord.z);
    float depth = originalDepth * float(OIT_NUMBER_OF_DEPTH_BINS - 1) / OIT_NUMBER_OF_DEPTH_BINS;

    float averageAbsorbance = absorbance * (1 - depth);
    coefficients[0] = averageAbsorbance;

    int depthBinIndex = int(floor(originalDepth * (OIT_NUMBER_OF_DEPTH_BINS - 1)));
    // The tree starts at the coefficient 1
    int treeIndex = depthBinIndex + OIT_COEFF_COUNT;

    for (int waveletLevel = 0; waveletLevel <= OIT_WAVELET_RANK; waveletLevel++) {
        int power = OIT_WAVELET_RANK - waveletLevel;
        int parentIndex = treeIndex >> 1;
        float indexAtParentLevel = float(parentIndex & ((1 << power) - 1));

        int isRightHalf = treeIndex & 1;
        int waveletSign = 1 - (isRightHalf << 1);
        float waveletWidth = exp2(-power);
        float waveletHeightScale = exp2(power * 0.5);
        float depthOffsetRelativeToWavelet = depth - waveletWidth * indexAtParentLevel;
        float distanceFromWaveletEdge = isRightHalf * waveletWidth + waveletSign * depthOffsetRelativeToWavelet;
        float differenceCoefficient = distanceFromWaveletEdge * waveletHeightScale * absorbance;
        coefficients[parentIndex] = differenceCoefficient;

        treeIndex = parentIndex;
    }

    for (int attachmentIndex = 0; attachmentIndex < OIT_COEFF_ATTACHMENT_COUNT; attachmentIndex++) {
        for (int i = 0; i < 4; i++) {
            coeff[attachmentIndex][i] = coefficients[attachmentIndex * 4 + i];
        }
    }
    #ifdef RENDERPEARL_EXPLICIT_DEPTH_INVARIANCE
    gl_FragDepth = gl_FragCoord.z;
    #endif
}
#elif defined(OIT_ACCUMULATE)
uniform sampler2D _flw_coeffs0;
#if OIT_COEFF_COUNT > 4
uniform sampler2D _flw_coeffs1;
#endif
#if OIT_COEFF_COUNT > 8
uniform sampler2D _flw_coeffs2;
uniform sampler2D _flw_coeffs3;
#endif
layout (location = 0) out vec4 _flw_accumulate;
float sampleAbsorbance(float coefficients[OIT_COEFF_COUNT], float originalDepth, float currentAbsorbance) {
    float averageAbsorbance = coefficients[0];
    if (averageAbsorbance == 0) {
        return 0.0;
    }

    float depthMeasuredInBins = originalDepth * (OIT_NUMBER_OF_DEPTH_BINS - 1);

    float depth = depthMeasuredInBins / OIT_NUMBER_OF_DEPTH_BINS;

    float currentAverageAbsorbanceContribution = currentAbsorbance * (1 - depth);
    averageAbsorbance -= currentAverageAbsorbanceContribution;

    int treeIndexB = clamp(int(floor(depthMeasuredInBins)), 0, OIT_NUMBER_OF_DEPTH_BINS - 1);
    bool shouldSampleA = treeIndexB >= 1;
    int treeIndexA = shouldSampleA ? (treeIndexB - 1) : treeIndexB;

    treeIndexB += OIT_COEFF_COUNT;
    treeIndexA += OIT_COEFF_COUNT;

    float sampleB = averageAbsorbance;
    float sampleA = shouldSampleA ? averageAbsorbance : 0;

    for (int waveletLevel = 0; waveletLevel <= OIT_WAVELET_RANK; waveletLevel++) {
        int power = OIT_WAVELET_RANK - waveletLevel;
        float waveletWidth = exp2(-power);
        float waveletHeightScale = exp2(power * 0.5);

        int parentIndexB = treeIndexB >> 1;
        int isRightHalfB = treeIndexB & 1;
        int waveletSignB = 1 - (isRightHalfB << 1);

        float indexAtParentLevel = float(parentIndexB & ((1 << power) - 1));
        float depthOffsetRelativeToWavelet = depth - waveletWidth * indexAtParentLevel;
        float currentDifferenceCoefficient = (isRightHalfB * waveletWidth + waveletSignB * depthOffsetRelativeToWavelet) * waveletHeightScale * currentAbsorbance;

        float differenceCoefficientB = coefficients[parentIndexB];
        differenceCoefficientB -= currentDifferenceCoefficient;

        sampleB -= waveletHeightScale * differenceCoefficientB * waveletSignB;
        treeIndexB = parentIndexB;

        if (shouldSampleA) {
            int parentIndexA = treeIndexA >> 1;
            float differenceCoefficientA = (parentIndexA == parentIndexB) ? differenceCoefficientB : coefficients[parentIndexA];
            int isRightHalfA = treeIndexA & 1;
            int waveletSignA = 1 - (isRightHalfA << 1);
            sampleA -= waveletHeightScale * differenceCoefficientA * waveletSignA;
            treeIndexA = parentIndexA;
        }
    }

    float lerpAlpha = depthMeasuredInBins >= OIT_NUMBER_OF_DEPTH_BINS ? 1.0 : fract(depthMeasuredInBins);

    return mix(sampleA, sampleB, lerpAlpha);
}
float sampleTransmittance(ivec2 pos, float depth, float currentTransmittance) {
    float coefficients[OIT_COEFF_COUNT];
    const int targetCount = OIT_COEFF_COUNT / 4;
    vec4 coeffSamples[targetCount];
    coeffSamples[0] = texelFetch(_flw_coeffs0, pos, 0);
    #if OIT_COEFF_COUNT > 4
    coeffSamples[1] = texelFetch(_flw_coeffs1, pos, 0);
    #endif
    #if OIT_COEFF_COUNT > 8
    coeffSamples[2] = texelFetch(_flw_coeffs2, pos, 0);
    coeffSamples[3] = texelFetch(_flw_coeffs3, pos, 0);
    #endif
    for (int i = 0; i < targetCount; i++) {
        for (int j = 0; j < 4; j++) {
            coefficients[i * 4 + j] = coeffSamples[i][j];
        }
    }
    return toTransmittance(sampleAbsorbance(coefficients, depth, toAbsorbance(currentTransmittance)));
}
vec4 sampleColorForAccumulation(vec4 color) {
    #ifdef OIT_ADDITIVE
    float transmittance = 1.0;
    float accumAlpha = 0.0;
    #else
    float transmittance = 1.0 - color.a;
    float accumAlpha = color.a;
    #endif
    #ifdef RENDERPEARL_EXPLICIT_DEPTH_INVARIANCE
    gl_FragDepth = gl_FragCoord.z;
    #endif
    float sampledTransmittance = sampleTransmittance(ivec2(gl_FragCoord.xy), normalizeDepth(gl_FragCoord.z), transmittance);
    return vec4(color.rgb * color.a, accumAlpha) * sampledTransmittance;
}
#endif
#ifdef OIT_ALPHA_ONLY
void executeAlphaOnlyPhase(float deviceDepth, float alpha) {
    #ifdef OIT_DEPTH_BOUNDS
    if (alpha < 0.01) {
        discard;
    }
    calculateDepthBounds(deviceDepth, alpha);
    #elif defined(OIT_ADDITIVE)
    discard;
    #elif defined(OIT_TRANSMITTANCE)
    addTransmittance(1.0 - alpha);
    #endif
}
#endif
#endif