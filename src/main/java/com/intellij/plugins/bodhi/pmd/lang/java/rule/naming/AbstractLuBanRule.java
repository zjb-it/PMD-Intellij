package com.intellij.plugins.bodhi.pmd.lang.java.rule.naming;


import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRulechainRule;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public abstract class AbstractLuBanRule extends AbstractJavaRulechainRule {

    public static final Set<String> ruleSetPath = Set.of("rulesets/java/luban-naming.xml");

    public static final ResourceBundle MESSAGES_BUNDLE;

    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle("messages.messages",
                new ResourceBundle.Control() {
                    public List<String> getFormats(String baseName) {
                        if (baseName == null)
                            throw new NullPointerException();
                        return Arrays.asList("xml");
                    }

                    public ResourceBundle newBundle(String baseName,
                                                    Locale locale,
                                                    String format,
                                                    ClassLoader loader,
                                                    boolean reload)
                            throws IllegalAccessException,
                            InstantiationException,
                            IOException {
                        if (baseName == null || locale == null
                                || format == null || loader == null)
                            throw new NullPointerException();
                        ResourceBundle bundle = null;
                        if (format.equals("xml")) {
                            String bundleName = toBundleName(baseName, locale);
                            String resourceName = toResourceName(bundleName, format);
                            InputStream stream = null;
                            if (reload) {
                                URL url = loader.getResource(resourceName);
                                if (url != null) {
                                    URLConnection connection = url.openConnection();
                                    if (connection != null) {
                                        // Disable caches to get fresh data for
                                        // reloading.
                                        connection.setUseCaches(false);
                                        stream = connection.getInputStream();
                                    }
                                }
                            } else {
                                stream = loader.getResourceAsStream(resourceName);
                            }
                            if (stream != null) {
                                BufferedInputStream bis = new BufferedInputStream(stream);
                                bundle = new XMLResourceBundle(bis);
                                bis.close();
                            }
                        }
                        return bundle;
                    }
                });

    }

    private static class XMLResourceBundle extends ResourceBundle {
        private Properties props;

        XMLResourceBundle(InputStream stream) throws IOException {
            props = new Properties();
            props.loadFromXML(stream);
        }

        @Override
        protected Object handleGetObject(String key) {
            return props.getProperty(key).trim();
        }

        @Override
        public Enumeration getKeys() {
            return props.keys();

        }
    }

    public AbstractLuBanRule(Class<? extends JavaNode> first, Class<? extends JavaNode>... visits) {
        super(first, visits);
    }

    public String getMessage() {
        String key = this.getClass().getName() + ".violation.msg";
        if (MESSAGES_BUNDLE.containsKey(key)) {
            return MESSAGES_BUNDLE.getString(key);
        }
        return "";
    }

    public static @NotNull String getMessage(AbstractRule abstractJavaRule) {
        return MESSAGES_BUNDLE.getString(abstractJavaRule.getClass().getName() + ".violation.msg");
    }

    public void addViolation(Object data, Node node, Object... args) {
        asCtx(data).addViolation(node, args);
    }

}
