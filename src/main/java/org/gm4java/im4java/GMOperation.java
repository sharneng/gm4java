/*
 * Copyright (c) 2011 Original Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gm4java.im4java;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckForNull;

/**
 * 
 * This class extends the im4java version of GMOperation and provide a list of convenience methods to ease the
 * construction of the image command for GraphicsMagick.
 * 
 * @author Michael Magpayo
 * 
 */
public class GMOperation extends org.im4java.core.GMOperation {

    /** Options which modify the behavior of the 'degrees' specification for rotation-based settings. */
    public enum RotationAnnotation {
        /** Forces rotation. */
        Always(""),
        /** Rotate the image if and only if its width exceeds the height. */
        WidthExceedsHeightOnly(">"),
        /** Rotate the image if and only if its height exceeds the width. */
        HeightExceedsWidthOnly("<");

        private final String annotation;

        private RotationAnnotation(final String annotation) {
            this.annotation = annotation;
        }

        /**
         * Serializes option for GraphicsMagick command-line.
         * 
         * @return Annotation to append to geometry specification
         */
        public String asAnnotation() {
            return annotation;
        }
    }

    /**
     * Used in conjunction with other settings, generally defines how to interpret the offset for geometry-based
     * options.
     */
    public enum Gravity {
        /** Offset defined from top-left corner. */
        NorthWest,
        /** Offset defined from top edge at the midpoint. */
        North,
        /** Offset defined from top-right corner. */
        NorthEast,
        /** Offset defined from left edge at the midpoint. */
        West,
        /** Offset defined from image center point. */
        Center,
        /** Offset defined from right edge at the midpoint. */
        East,
        /** Offset defined from bottom-left corner. */
        SouthWest,
        /** Offset defined from bottom edge at the midpoint. */
        South,
        /** Offset defined from bottom-right corner. */
        SouthEast;
    }

    /** Options which modify the behavior of the 'width X height' specification for geometry-based settings. */
    public enum GeometryAnnotation {
        /** Resize the image while maintaining the aspect ratio using the geometry specification as minimum values. */
        FillUsingAspectRatio("^"),
        /** Resize the image while maintaining the aspect ratio using the geometry specification as maximum values. */
        FitUsingAspectRatio(""),
        /** Resize of the image to exactly match the geometry specification. */
        ForceDimensions("!"),
        /** Treat width and height declarations as percentage values instead of pixel lengths. */
        DimensionsAsPercentages("%"),
        /** Resize the image only if its width or height exceeds the geometry specification. */
        DownsizeOnly(">"),
        /** Resize the image only if both width and height are less than the geometry specification. */
        UpsizeOnly("<");

        private final String annotation;

        private GeometryAnnotation(final String annotation) {
            this.annotation = annotation;
        }

        /**
         * Serializes option for GraphicsMagick command-line.
         * 
         * @return Annotation to append to geometry specification
         */
        public String asAnnotation() {
            return annotation;
        }

        /**
         * Ensure that the {@code used} annotations are a valid set of annotations.
         * 
         * @param used
         *            Annotation set to scan.
         */
        static void validate(final Collection<GeometryAnnotation> used) {
            // '<' and '>' annotations
            throwMutuallyExclusiveException(used, false,
                    EnumSet.of(GeometryAnnotation.DownsizeOnly, GeometryAnnotation.UpsizeOnly));

            // '', '^' and '!' annotations
            throwMutuallyExclusiveException(used, true, EnumSet.of(GeometryAnnotation.FillUsingAspectRatio,
                    GeometryAnnotation.FitUsingAspectRatio, GeometryAnnotation.ForceDimensions));
        }

        /**
         * Throws {@link IllegalArgumentException} if more than one option from {@code mutuallyExclusive} annotations
         * exist in the {@code used} set.
         */
        private static void throwMutuallyExclusiveException(final Collection<GeometryAnnotation> used,
                final boolean shouldHaveAtLeastOne, final Collection<GeometryAnnotation> mutuallyExclusive) {
            final EnumSet<GeometryAnnotation> matched = EnumSet.copyOf(used);
            matched.retainAll(mutuallyExclusive);
            if (matched.size() > 1) {
                throw new IllegalArgumentException("Geometry annotations " + join(matched, "and")
                        + "are mutually exclusive");
            }
            if (shouldHaveAtLeastOne && (matched.size() != 1)) {
                throw new IllegalArgumentException("One of the following geometry annotations must be used: "
                        + join(mutuallyExclusive, "or"));
            }
        }

        private static String join(Collection<?> set, String andOr) {
            StringBuffer buf = new StringBuffer();
            Iterator<?> i = set.iterator();
            for (int n = set.size() - 2; n > 0; n--) {
                buf.append(i.next()).append(", ");
            }
            buf.append(i.next()).append(' ').append(andOr).append(' ').append(i.next());
            return buf.toString();
        }
    }

    /**
     * Limits the number of threads used by the GraphicsMagick process during execution. Note that no validation is
     * made, so ensure that the value is a non-positive integer presumably less than the maximum CPU cores on the host.
     * 
     * @param threadsPerProcess
     *            Number of threads to use per process.
     * @return Builder object for chained options setup.
     */
    public GMOperation limitThreads(final int threadsPerProcess) {
        final List<String> args = getCmdArgs();
        args.add("-limit");
        args.add("threads");
        args.add(Integer.toString(threadsPerProcess));
        return this;
    }

    /**
     * Resize source to desired target dimensions, using default resizing filter algorithm.
     * 
     * @param width
     *            Length in pixels of the target image's width.
     * @param height
     *            Length in pixels of the target image's height.
     * @param annotations
     *            Geometry annotations to define how the {@code width} and {@code height} options are to be interpreted.
     * @return Builder object for chained options setup.
     */
    public GMOperation resize(final int width, final int height, final Collection<GeometryAnnotation> annotations) {
        final List<String> args = getCmdArgs();
        args.add("-resize");
        args.add(resample(width, height, annotations));
        return this;
    }

    /**
     * Scale source to desired target dimensions, using simplified resizing algorithm with pixel averaging.
     * 
     * @param width
     *            Length in pixels of the target image's width.
     * @param height
     *            Length in pixels of the target image's height.
     * @param annotations
     *            Geometry annotations to define how the {@code width} and {@code height} options are to be interpreted.
     * @return Builder object for chained options setup.
     */
    public GMOperation scale(final int width, final int height, final Collection<GeometryAnnotation> annotations) {
        final List<String> args = getCmdArgs();
        args.add("-scale");
        args.add(resample(width, height, annotations));
        return this;
    }

    /**
     * Rotates the image, with empty triangles back-filled using default background color.
     * 
     * @param degrees
     *            The angle by which the image should be rotated. Positive numbers indicate clockwise direction;
     *            negative numbers indicate counterclockwise direction.
     * @param annotation
     *            Rotate annotation to define when rotation should happen.
     * @return Builder object for chained options setup.
     */
    public GMOperation rotate(final double degrees, final RotationAnnotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Rotation annotation must be defined");
        }
        final List<String> args = getCmdArgs();
        args.add("-rotate");
        args.add(String.format(Locale.ENGLISH,"%.1f%s", degrees, annotation.asAnnotation()));
        return this;
    }

    /**
     * Defines the gravity for geometry-based operations. See documentation for more details, as this option works in
     * conjunction with various options in different ways.
     * 
     * @param value
     *            Gravity value to use, defining the coordinate system to use.
     * @return Builder object for chained options setup.
     */
    public GMOperation gravity(final Gravity value) {
        if (value == null) {
            throw new IllegalArgumentException("Gravity value must be defined");
        }
        gravity(value.toString());
        return this;
    }

    /**
     * Convert the image to grayscale (i.e. replace color encoding with smaller gray encoding).
     * 
     * @param depth
     *            the depth of the grayscale
     * @return Builder object for chained options setup.
     */
    public GMOperation grayscale(final int depth) {
        type("Grayscale");
        depth(depth);
        return this;
    }

    /**
     * Strips out ICC profiles.
     * 
     * @return Builder object for chained options setup.
     */
    public GMOperation stripProfiles() {
        final List<String> args = getCmdArgs();
        args.add("+profile");
        args.add("*");
        return this;
    }

    /**
     * Defines font for text overlay.
     * 
     * @param style
     *            Text font style (system-dependent based on installed fonts).
     * @param size
     *            Text point size.
     * @param color
     *            Text color, either named or {@code #RGB} variant.
     * @return Builder object for chained options setup.
     */
    public GMOperation font(final String style, final int size, final String color) {
        if (isBlank(style)) {
            throw new IllegalArgumentException("Text font style must be defined");
        }
        if (isBlank(color)) {
            throw new IllegalArgumentException("Text font color must be defined");
        }
        font(style);
        pointsize(size);
        fill(color);
        return this;
    }

    /**
     * Draws text overlay on image with the upper-right corner defined by the {@code offsetX} and {@code offsetY}
     * parameters. Note that {@link#gravity(Gravity)} will affect how the offset values are interpreted.
     * 
     * @param text
     *            Text to write.
     * @param offsetX
     *            Offset value in the horizontal direction.
     * @param offsetY
     *            Offset value in the vertical direction.
     * @return Builder object for chained options setup.
     */
    public GMOperation drawText(final String text, final int offsetX, final int offsetY) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("Text string must be defined");
        }
        draw(String.format(Locale.ENGLISH,"text %d %d '%s'", offsetX, offsetY, text));
        return this;
    }

    /**
     * Specifies the source image to convert.
     * 
     * @param file
     *            Source image file to convert.
     * @param width
     *            Length in pixels of the source image's width; if {@code null} or negative, the in-memory size
     *            definition is omitted. Optional.
     * @param height
     *            Length in pixels of the source image's height; if {@code null} or negative, the in-memory size
     *            definition is omitted. Optional.
     * @return Builder object for chained options setup.
     * @throws IOException
     *             when the source file does not exist.
     */
    public GMOperation source(final File file, @CheckForNull final Integer width, @CheckForNull final Integer height)
            throws IOException {
        if (file != null && !file.exists()) {
            throw new IOException("Source file '" + file + "' does not exist");
        }
        if ((width != null) && (height != null) && (width > 0) && (height > 0)) {
            size(width, height);
        }
        return addImage(file);
    }

    /**
     * Add image to operation.
     * 
     * @param file
     *            image file to be added.
     * @return Builder object for chained options setup.
     */
    public GMOperation addImage(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must be defined");
        }
        getCmdArgs().add(file.getPath());
        return this;
    }

    /**
     * Add raw text to the list of arguments.
     * 
     * @param arg
     *            argument to add
     * @return Builder object for chained options setup.
     */
    public GMOperation addRawArg(String arg) {
        getCmdArgs().add(arg);
        return this;
    }

    /**
     * Add option -quality to the GraphicsMagick commandline (see the documentation of GraphicsMagick for details).
     * 
     * @param quality
     *            the quality of the image from 0 - 100
     * @return Builder object for chained options setup
     */
    public GMOperation quality(double quality) {
        final List<String> args = getCmdArgs();
        args.add("-quality");
        args.add(Double.toString(quality));
        return this;
    }

    private static String resample(final int width, final int height, final Collection<GeometryAnnotation> annotations) {
        if ((width < 1) || (height < 1)) {
            throw new IllegalArgumentException("Target height and width both should be greater than zero");
        }
        if (annotations == null) {
            throw new IllegalArgumentException("Geometry annotation(s) must be defined");
        }
        GeometryAnnotation.validate(annotations);
        StringBuilder buf = new StringBuilder();
        buf.append(width).append('x').append(height);
        for (final GeometryAnnotation entry : annotations) {
            buf.append(entry.asAnnotation());
        }
        return buf.toString();
    }

    private static boolean isBlank(String s) {
        return (s == null) || s.length() == 0;
    }

}
