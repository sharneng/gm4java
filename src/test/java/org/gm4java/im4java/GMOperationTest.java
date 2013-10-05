package org.gm4java.im4java;

import org.gm4java.im4java.GMOperation.GeometryAnnotation;
import org.gm4java.im4java.GMOperation.Gravity;
import org.gm4java.im4java.GMOperation.RotationAnnotation;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

public class GMOperationTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    GMOperation sut;

    @Before
    public void setup() {
        sut = new GMOperation();
    }

    private File randomFile() {
        return new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
    }

    private File createdFile() throws Exception {
        final File random = randomFile();
        if (!random.createNewFile()) {
            Assert.fail("Unable to create file " + random.getPath() + " for testing");
        }
        random.deleteOnExit();
        return random;
    }

    @Test
    public void source_ThrowsException_WhenFileIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.source(null, 1, 1);
    }

    @Test
    public void source_ThrowsException_WhenFileDoesNotExist() throws Exception {
        exception.expect(IOException.class);
        sut.source(randomFile(), 1, 1);
    }

    @Test
    public void source_OmitsSize_WhenWidthIsNull() throws Exception {
        sut.source(createdFile(), null, 1);
        assertThat(sut.getCmdArgs(), not(hasItem("size")));
    }

    @Test
    public void source_OmitsSize_WhenWidthIsNotPositive() throws Exception {
        sut.source(createdFile(), 0, 1);
        assertThat(sut.getCmdArgs(), not(hasItem("size")));
    }

    @Test
    public void source_OmitsSize_WhenHeightIsNull() throws Exception {
        sut.source(createdFile(), 1, null);
        assertThat(sut.getCmdArgs(), not(hasItem("size")));
    }

    @Test
    public void source_OmitsSize_WhenHeightIsNotPositive() throws Exception {
        sut.source(createdFile(), 1, 0);
        assertThat(sut.getCmdArgs(), not(hasItem("size")));
    }

    @Test
    public void scale_ThrowsException_WhenWidthIsNotPositive() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.scale(0, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio));
    }

    @Test
    public void scale_ThrowsException_WhenHeightIsNotPositive() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.scale(1, 0, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio));
    }

    @Test
    public void resize_ForceDisallowUpsample() throws Exception {
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.ForceDimensions, GeometryAnnotation.DownsizeOnly));
        assertThat(sut.getCmdArgs(), hasItem("1x1!>"));
    }

    @Test
    public void resize_RatioDisallowUpsample() throws Exception {
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio, GeometryAnnotation.DownsizeOnly));
        assertThat(sut.getCmdArgs(), hasItem("1x1>"));
    }

    @Test
    public void resize_ForceAllowUpsample() throws Exception {
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.ForceDimensions));
        assertThat(sut.getCmdArgs(), hasItem("1x1!"));
    }

    @Test
    public void resize_RatioAllowUpsample() throws Exception {
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio));
        assertThat(sut.getCmdArgs(), hasItem("1x1"));
    }

    @Test
    public void resize_ThrowsException_WhenNoResizeModeSpecified() {
        exception.expect(IllegalArgumentException.class);
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.DownsizeOnly));
    }

    @Test
    public void resize_ThrowsException_WhenDownSizeOnlyAndUpSizeOnly() {
        exception.expect(IllegalArgumentException.class);
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio, GeometryAnnotation.DownsizeOnly,
                GeometryAnnotation.UpsizeOnly));
    }

    @Test
    public void resize_ThrowsException_WhenFitAndForce() {
        exception.expect(IllegalArgumentException.class);
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio, GeometryAnnotation.ForceDimensions));
    }

    @Test
    public void resize_ThrowsException_WhenFillAndForce() {
        exception.expect(IllegalArgumentException.class);
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FillUsingAspectRatio, GeometryAnnotation.ForceDimensions));
    }

    @Test
    public void resize_ThrowsException_WhenFitAndFill() {
        exception.expect(IllegalArgumentException.class);
        sut.resize(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio, GeometryAnnotation.FillUsingAspectRatio));
    }

    @Test
    public void rotate_ThrowsException_NoAnnotation() {
        exception.expect(IllegalArgumentException.class);
        sut.rotate(0.0, null);
    }

    @Test
    public void rotate_Annotation_WidthLargerThanHeight() {
        sut.rotate(-90.0, RotationAnnotation.WidthExceedsHeightOnly);
        assertThat(sut.getCmdArgs(), hasItem("-90.0>"));
    }

    @Test
    public void gravity_SetsTheGravity() {
        sut.gravity(Gravity.NorthEast);
        assertThat(sut.getCmdArgs(), hasItem("NorthEast"));
    }

    @Test
    public void gravity_ThrowsException_WhenGravityIsNotDefined() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.gravity((Gravity) null);
    }

    @Test
    public void rotate_Annotation_HeightLargerThanWidth() {
        sut.rotate(90.0, RotationAnnotation.HeightExceedsWidthOnly);
        assertThat(sut.getCmdArgs(), hasItem("90.0<"));
    }

    @Test
    public void font_ThrowsException_WhenStyleIsNotDefined() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.font(null, 1, "white");
    }

    @Test
    public void font_ThrowsException_WhenColorIsNotDefined() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.font("arial", 1, null);
    }

    @Test
    public void font_SetsTypeSizeColor() {
        final String[] expected = new String[] { "-font", "arial", "-pointsize", "12", "-fill", "red" };
        sut.font("arial", 12, "red");
        assertThat(sut.getCmdArgs(), equalTo(Arrays.asList(expected)));
    }

    @Test
    public void drawText_ThrowsException_WhenTextIsNotDefined() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.drawText(null, 0, 0);
    }

    @Test
    public void drawText_DrawsTextAtGivenPosition() {
        final String[] expected = new String[] { "-draw", "text 12 13 'message'" };
        sut.drawText("message", 12, 13);
        assertThat(sut.getCmdArgs(), equalTo(Arrays.asList(expected)));
    }

    @Test
    public void addImage_ThrowsException_WhenFileIsNull() throws Exception {
        exception.expect(IllegalArgumentException.class);
        sut.addImage((File) null);
    }

    @Test
    public void scale_AddsScaleOptionWithSize() {
        sut.scale(1, 1, EnumSet.of(GeometryAnnotation.FitUsingAspectRatio));
        assertThat(sut.getCmdArgs(), equalTo(Arrays.asList(new String[] { "-scale", "1x1" })));
    }

    @Test
    public void resize_Serializes_WhenParametersValid() throws Exception {
        final File source = createdFile();
        final File target = randomFile();
        //@formatter:off
        final String[] expected = new String[] {
                "convert",
                "-limit", "threads", "42",
                "-size", "1600x1200", source.getPath(),
                "-shave", "1x2",
                "-resize", "640x480!>",
                "-rotate", "90.0>",
                "-crop", "320x240+10+20",
                "-colorspace", "COLORSPACE",
                "-type", "Grayscale", "-depth", "8",
                "-quality", "86.12",
                "+profile", "*",
                target.getPath()
        };

        sut.addRawArg("convert")
           .limitThreads(42)
           .source(source, 1600, 1200)
           .shave(1, 2);
        sut.resize(640, 480, EnumSet.of( GeometryAnnotation.ForceDimensions, GeometryAnnotation.DownsizeOnly))
           .rotate(90.0, RotationAnnotation.WidthExceedsHeightOnly)
           .crop(320, 240, 10, 20)
           .colorspace("COLORSPACE");
        sut.grayscale(8)
           .quality(86.12)
           .stripProfiles()
           .addImage(target);
        //@formatter:on

        assertThat(sut.getCmdArgs(), equalTo(Arrays.asList(expected)));
    }
}
