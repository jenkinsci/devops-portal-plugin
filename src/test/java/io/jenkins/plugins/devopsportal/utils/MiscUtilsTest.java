package io.jenkins.plugins.devopsportal.utils;

import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class MiscUtilsTest {

    @Test
    public void testReadableFileSize() {
        assertEquals("0 kB", MiscUtils.readableFileSize(-1));
        assertEquals("0 kB", MiscUtils.readableFileSize(0));
        assertEquals("256 B", MiscUtils.readableFileSize(256));
        assertThat(MiscUtils.readableFileSize(1023), anyOf(equalTo("1,023 B"), equalTo("1 023 B")));
        assertEquals("1 kB", MiscUtils.readableFileSize(1024));
        assertEquals("128 kB", MiscUtils.readableFileSize(1024 * 128));
        assertEquals("1 MB", MiscUtils.readableFileSize(1024 * 1024));
        assertThat(MiscUtils.readableFileSize(1024 * 1024 * 1011), anyOf(equalTo("1,011 MB"), equalTo("1 011 MB")));
        assertEquals("1 GB", MiscUtils.readableFileSize(1024 * 1024 * 1057));
        assertEquals("57 GB", MiscUtils.readableFileSize(1024L * 1024 * 1024 * 57));
        assertEquals("1 TB", MiscUtils.readableFileSize(1024L * 1024 * 1024 * 1024));
        assertEquals("613 TB", MiscUtils.readableFileSize(1024L * 1024 * 1024 * 1024 * 613));
        assertThat(MiscUtils.readableFileSize(1024L * 1024 * 1024 * 1024 * 9781), anyOf(equalTo("9,781 TB"), equalTo("9 781 TB")));
    }

    @Test
    public void testSplit() {
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split(null,","),
                is(List.of())
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split("",","),
                is(List.of())
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split("A,B,C",","),
                is(Arrays.asList("A", "B", "C"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split("B,A,C",","),
                is(Arrays.asList("B", "A", "C"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split("B,A,C,",","),
                is(Arrays.asList("B", "A", "C"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split(",B,A,C",","),
                is(Arrays.asList("B", "A", "C"))
        );
        org.hamcrest.MatcherAssert.assertThat(
                MiscUtils.split("B,,A,C",","),
                is(Arrays.asList("B", "A", "C"))
        );
    }

    @Test
    public void testCheckNotEmptyOk() {
        MiscUtils.checkNotEmpty("a", "b", "c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotEmptyKo1() {
        MiscUtils.checkNotEmpty("a", "b", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotEmptyKo2() {
        MiscUtils.checkNotEmpty("a", "b", "");
    }

    @Test
    public void testIsValidURL() {
        assertTrue(MiscUtils.isValidURL("http://www.sample.io"));
        assertTrue(MiscUtils.isValidURL("http://www.sample.io/api/"));
        assertTrue(MiscUtils.isValidURL("http://www.sample.io/index.html"));
        assertTrue(MiscUtils.isValidURL("https://www.sample.io/secured.html"));
        assertFalse(MiscUtils.isValidURL("ftp://www.sample.io/~bob/"));
        assertTrue(MiscUtils.isValidURL("http://www.sample.io/index.html#hash"));
        assertTrue(MiscUtils.isValidURL("http://www.sample.io/index.html?query=param"));
        assertTrue(MiscUtils.isValidURL("http://www.sample.io//double"));
        assertFalse(MiscUtils.isValidURL("http//invalid/"));
        assertFalse(MiscUtils.isValidURL("www.invalid.org"));
        assertFalse(MiscUtils.isValidURL(""));
        assertFalse(MiscUtils.isValidURL(null));
    }

    @Test
    public void testGetStringOrEmptyMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "AAAA");
        map.put("b", "BBBB");
        map.put("d", "DDDD");
        assertEquals("AAAA", MiscUtils.getStringOrEmpty(map, "a"));
        assertEquals("", MiscUtils.getStringOrEmpty(map, "A"));
        assertEquals("", MiscUtils.getStringOrEmpty(null, "key"));
        assertEquals("", MiscUtils.getStringOrEmpty(map, null));
        assertEquals("", MiscUtils.getStringOrEmpty(map, "e"));
    }

    @Test
    public void testGetStringOrEmptyList() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("type", "a");
        map1.put("value", "AAAA");
        map1.put("foo", "bar");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("type", "b");
        map2.put("value", "BBBB");
        map2.put("foo", "bar");
        List<Map<String, Object>> list = Arrays.asList(map1, map2);
        assertEquals("", MiscUtils.getStringOrEmpty(null, "a", "b", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, null, "a", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "unknown", null, "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "unknown", "a", null));
        assertEquals("AAAA", MiscUtils.getStringOrEmpty(list, "type", "a", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "unknown", "a", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "type", "unknown", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "type", "a", "unknown"));
        assertEquals("BBBB", MiscUtils.getStringOrEmpty(list, "type", "b", "value"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "type", "c", "value"));
        assertEquals("bar", MiscUtils.getStringOrEmpty(list, "type", "b", "foo"));
        assertEquals("", MiscUtils.getStringOrEmpty(list, "type", "b", ""));
    }

    @Test
    public void testGetIntOrZeroMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "AAAA");
        map.put("b", "125");
        map.put("c", "-15");
        assertEquals(0, MiscUtils.getIntOrZero(map, "a"));
        assertEquals(125, MiscUtils.getIntOrZero(map, "b"));
        assertEquals(-15, MiscUtils.getIntOrZero(map, "c"));
        assertEquals(0, MiscUtils.getIntOrZero(map, null));
        assertEquals(0, MiscUtils.getIntOrZero(null, "d"));
        assertEquals(0, MiscUtils.getIntOrZero(map, "e"));
    }

    @Test
    public void getGetFloatOrZero() {
        assertEquals(0, MiscUtils.getFloatOrZero(null), 0);
        assertEquals(0, MiscUtils.getFloatOrZero(""), 0);
        assertEquals(0, MiscUtils.getFloatOrZero("invalid"), 0);
        assertEquals(0, MiscUtils.getFloatOrZero("0"), 0);
        assertEquals(0, MiscUtils.getFloatOrZero("0.0"), 0);
        assertEquals(0.1, MiscUtils.getFloatOrZero("0.1"), 0.01);
        assertEquals(25.62015, MiscUtils.getFloatOrZero("25.62015"), 0.00001);
        assertEquals(3.569, MiscUtils.getFloatOrZero("3.569f"), 0.001);
        assertEquals(-5.002, MiscUtils.getFloatOrZero("-5.002F"), 0.001);
        assertEquals(13.57, MiscUtils.getFloatOrZero("13.57D"), 0.01);
        assertEquals(0, MiscUtils.getFloatOrZero("100,01"), 0.01);
    }

    @Test
    public void testGetIntOrZero() {
        assertEquals(0, MiscUtils.getIntOrZero(null));
        assertEquals(0, MiscUtils.getIntOrZero(""));
        assertEquals(0, MiscUtils.getIntOrZero("invalid"));
        assertEquals(256, MiscUtils.getIntOrZero("256"));
        assertEquals(-512, MiscUtils.getIntOrZero("-512"));
        assertEquals(0, MiscUtils.getIntOrZero("1024f"));
    }

    @Test
    public void testCheckFilePathIllegalAccess() {
        File file = MiscUtils.checkFilePathIllegalAccess(null, null);
        assertNull(file);
        file = MiscUtils.checkFilePathIllegalAccess("", "");
        assertNull(file);
        file = MiscUtils.checkFilePathIllegalAccess(".", "test");
        assertNull(file);
        file = MiscUtils.checkFilePathIllegalAccess(".", "pom.xml");
        assertNotNull(file);
        file = MiscUtils.checkFilePathIllegalAccess(".", "../pom.xml");
        assertNull(file);
    }

}
