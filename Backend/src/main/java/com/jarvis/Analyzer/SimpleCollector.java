package com.jarvis.Analyzer;
import lombok.Getter;
import net.sourceforge.pmd.lang.document.TextFile;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.reporting.*;
import java.io.*;
import java.util.*;

@Getter
public class SimpleCollector implements Renderer {
    private final List<RuleViolation> violations = new ArrayList<>();
    @Override
    public void definePropertyDescriptor(PropertyDescriptor<?> propertyDescriptor) throws IllegalArgumentException {}
    @Override
    public String getName() { return "SimpleCollector"; }
    @Override
    public PropertyDescriptor<?> getPropertyDescriptor(String s) { return null; }
    @Override
    public List<PropertyDescriptor<?>> getPropertyDescriptors() { return List.of(); }
    @Override
    public List<PropertyDescriptor<?>> getOverriddenPropertyDescriptors() { return List.of(); }
    @Override
    public <T> T getProperty(PropertyDescriptor<T> propertyDescriptor) { return null; }
    @Override
    public boolean isPropertyOverridden(PropertyDescriptor<?> propertyDescriptor) { return false; }
    @Override
    public <T> void setProperty(PropertyDescriptor<T> propertyDescriptor, T t) {}
    @Override
    public Map<PropertyDescriptor<?>, Object> getPropertiesByPropertyDescriptor() { return Map.of(); }
    @Override
    public Map<PropertyDescriptor<?>, Object> getOverriddenPropertiesByPropertyDescriptor() { return Map.of(); }
    @Override
    public boolean hasDescriptor(PropertyDescriptor<?> propertyDescriptor) { return false; }
    @Override
    public void setName(String s) {}
    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public String defaultFileExtension() {
        return "";
    }
    @Override
    public void setDescription(String s) {}
    @Override
    public boolean isShowSuppressedViolations() {
        return false;
    }
    @Override
    public void setShowSuppressedViolations(boolean b) {}
    @Override
    public void setWriter(Writer writer) {}
    @Override
    public Writer getWriter() {
        return new StringWriter();
    }
    @Override
    public void setFileNameRenderer(FileNameRenderer fileNameRenderer) {}
    @Override
    public void start() {}
    @Override
    public void startFileAnalysis(TextFile textFile) {}
    @Override
    public void renderFileReport(Report report) {
        violations.addAll(report.getViolations());
    }
    @Override
    public void end() {}
    @Override
    public void flush() throws IOException {}
    @Override
    public void setReportFile(String s) {}
}